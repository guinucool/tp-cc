package runner.datagram;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import message.Communicable;
import message.CommunicableException;
import message.Message;
import message.Communicable.Operation;
import message.datagram.Datagram;
import runner.Runner;
import runner.RunnerException;

/** 
 * Objeto que define um runner de udp fiável.
 */
public class ReliableRunner extends Runner {

    public static final int SEND_TIMEOUT = 10;          /* Timeout de reenvio de uma mensagem */
    public static final int RECEIVE_TIMEOUT = 100;      /* Timeout de receção de uma mensagem */

    private DatagramRunner runner;                      /* Runner básico de datagramas */
    private TimeoutList sent;                           /* Lista de pacotes enviados mas à espera de confirmação */
    private TimeoutList received;                       /* Lista de pacotes recebidos recentemente */
    private TimeoutList fragments;                      /* Lista de fragmentos */

    private Lock reinforcedSend = new ReentrantLock();                      /* Bloqueio de reforço de envio */
    private Condition sendCondition = this.reinforcedSend.newCondition();   /* Condição do bloqueio do envio */

    /* Cosntrutor vazio */
    public ReliableRunner() throws RunnerException {

        /* Criação das listas de timeout */
        this.sent = new TimeoutList(SEND_TIMEOUT);
        this.received = new TimeoutList(RECEIVE_TIMEOUT);
        this.fragments = new TimeoutList(RECEIVE_TIMEOUT);

        /* Cria o runner base */
        this.runner = new DatagramRunner();
    }

    /* Cosntrutor parametrizado */
    public ReliableRunner(int port) throws RunnerException {

        /* Criação das listas de timeout */
        this.sent = new TimeoutList(SEND_TIMEOUT);
        this.received = new TimeoutList(RECEIVE_TIMEOUT);
        this.fragments = new TimeoutList(RECEIVE_TIMEOUT);

        /* Cria o runner base */
        this.runner = new DatagramRunner(port);
    }

    /* Endereço de alojamento local */
    public String getAddress() {
        return this.runner.getAddress();
    }

    /* Porta de alojamento local */
    public int getPort() {
        return this.runner.getPort();
    }

    /**
     * Envio de uma mensagem para um node
     * 
     * @throws RunnerException no caso de o socket de envio tiver sido desligado.
     * @throws CommunicableException no caso de a criação do pacote para envio for
     * impossível.
     */
    public void send(Communicable msg) throws RunnerException {

        /* Verifica se a mensagem pretendida é um frame */
        if (!(msg instanceof Datagram))
            throw new RunnerException("message-invalid");

        /* Verifica se há necessidade de fragmentar a mensagem */
        if (msg.getPayloadSize() > Datagram.MAX_PAYLOAD)
            this.sendFragments((Datagram) msg);

        /* Caso contrário envia-a */
        else {

            /* Bloqueia o envio */
            this.reinforcedSend.lock();

            /* Verifica se é possível passar por cima da condição de envio */
            while (this.sent.getSize() > 100) {
                try {
                    this.sendCondition.await();        
                } catch (InterruptedException e) {
                    /* Não é preciso tratar a exceção */
                }
            }

            /* Adiciona o pacote à lista de enviados */
            if (msg.getOperation() == Operation.QUERY || ((Datagram) msg).needsAck())
                this.sent.add((Datagram) msg);

            /* Envia a mensagem */
            this.runner.send(msg);

            /* Desbloqueia o envio */
            this.reinforcedSend.unlock();
        }
    }

    /* Receção de uma mensagem */
    public Message receive() {

        /* Tenta devolver um fragmento completo */
        Datagram received = this.fragments.getComplete();

        /* Escuta por uma mensagem ou espera por um timeout */
        try {

            /* Caso não haja fragmentos completos, escuta por mensagem */
            if (received == null)
                received = (Datagram) this.runner.receive();   
            
        } catch (SocketTimeoutException e) {

            /* Caso aconteça um timeout */
            this.timeout();

            /* Volta a ouvir */
            return this.receive();
        }

        /* Caso o serviço tenha fechado */
        if (received == null)
            return null;

        /* Envia o ack do pacote recebido, caso seja necessário */
        this.sendAck(received);

        /* Verifica se o pacote é duplicado */
        if (this.received.contains(received) || this.sent.containsResponses(received))
            return this.receive();
        
        /* Caso contrário */
        else {

            /* Verifica se o recebido pode ser usado como ack de alguma mensagem */
            if (received.getOperation() != Operation.QUERY) {
                this.sent.ack(received);

                /* Adiciona o recebido à lista de recebidos */
                this.received.add(received);

                /* Sinaliza a condição de envio já que pode ter sido recebido um ack */
                this.reinforcedSend.lock();
                this.sendCondition.signalAll();
                this.reinforcedSend.unlock();
            }

            /* Verifica se o recebido não é apenas um ack */
            if (received.isAck())
                return this.receive();

            /* Verifica se o recebido não é um fragmento */
            if (!received.isFragmented())
                return received;

            /* Adiciona-o à lista de fragmentos caso contrário */
            else {
                this.fragments.addFragment(received);
                return this.receive();
            }
        }
    }

    /* Fecho do socket no caso de o mesmo estar aberto */
    public void close() {
        this.runner.close();
    }

    /* Verifica se o socket associado ao runner está fechado */
    public boolean isClosed() {
        return this.runner.isClosed();
    }

    /* Conversão do datagram runner para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Reliable)").append(super.toString());
        builder.append("runner:").append(this.runner.toString()).append(";");
        builder.append("sent:").append(this.sent.toString()).append(";");
        builder.append("received:").append(this.received.toString()).append(";");
        builder.append("fragments:").append(this.fragments.toString()).append(";");

        return builder.toString();
    }

    /* Provoca um tick no relógio das várias listas de timeout */
    private void timeout() {
        this.sent.timeout(this.runner);
        this.received.timeout();
        this.fragments.timeout();
    }

    /* Envia o ack de um pacote que precisa de ack */
    private void sendAck(Datagram packet) {
        
        /* Tenta criar um ack do pacote e enviar de volta para o destino */
        try {
            Datagram ack = packet.ack();
            this.runner.send(ack);
        } catch (CommunicableException | RunnerException e) {
            /* Não é preciso tratar as exceções, elas são tratadas por funções superiores */
        }
    }

    /* Cria os vários fragmentos para serem enviados */
    private void sendFragments(Datagram packet) throws RunnerException {

        /* Fragmenta o pacote em vários */
        List<Datagram> fragments = packet.fragment();

        /* Manda os vários fragmentos para o destinatário */
        for (Datagram fragment : fragments)
            this.send(fragment);
    }
}
