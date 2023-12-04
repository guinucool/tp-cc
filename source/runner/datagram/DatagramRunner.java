package runner.datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import message.Communicable;
import message.CommunicableException;
import message.Message;
import message.Requestable;
import message.datagram.Datagram;
import message.datagram.DatagramRequest;
import model.NodeException;
import runner.Runner;
import runner.RunnerException;
import tools.Network;
import tools.Timeout;

public class DatagramRunner extends Runner {

    public static final int SEND_TIMEOUT = 10;                      /* Timeout de reenvio de uma mensagem */
    public static final int RECEIVE_TIMEOUT = 100;                  /* Timeout de receção de uma mensagem */
    public static final int WAIT_TIMEOUT = 5000;                    /* Timeout por falta de receção de mensagens */     

    private DatagramSocket socket;                                  /* Socket que aloja a comunicação UDP */
    private Map<Short,Timeout<DatagramRequest>> requests;           /* Mapa de pedidos feitos por responder */

    private List<Timeout<Datagram>> sentFragments;                  /* Lista de fragmentos á espera de um ack de confirmação */
    private List<Timeout<Datagram>> receivedFragments;              /* Lista de fragmentos á espera de serem completos */

    private List<Timeout<Datagram>> received;                       /* Lista de datagramas recebidos para evitar duplicações */

    private Lock send = new ReentrantLock();                        /* Bloqueia a operação de envio */
    private Lock receive = new ReentrantLock();                     /* Bloqueia a operação de receção */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();    /* Bloqueio de leitura e escrita */
    private Lock read = this.rwlock.readLock();                     /* Bloqueio de escrita */
    private Lock write = this.rwlock.writeLock();                   /* Bloqueio de escrita e leitura */

    /* Cosntrutor parametrizado */
    public DatagramRunner(int port) throws RunnerException {

        /* Criação das listas de controlo de fiabilidade */
        this.requests = new HashMap<>();
        this.sentFragments = new ArrayList<>();
        this.receivedFragments = new ArrayList<>();
        this.received = new ArrayList<>();

        /* Tenta abrir o socket e definir o timeout */
        try {
            this.socket = new DatagramSocket(port);   
            this.socket.setSoTimeout(WAIT_TIMEOUT);
        } catch (IOException e) {
            this.socket.close();
            throw new RunnerException("socket-unavailable");
        }
    }

    /* Endereço de alojamento local */
    public String getAddress() {
        return Network.getLocalAddress();
    }

    /* Porta de alojamento local */
    public int getPort() {
        return this.socket.getLocalPort();
    }

    /**
     * Envio de uma mensagem para um node
     * 
     * @throws RunnerException no caso de o socket de envio tiver sido desligado.
     * @throws CommunicableException no caso de a criação do pacote para envio for
     * impossível.
     */
    public void send(Communicable msg) throws RunnerException, CommunicableException {

        /* Verifica se a mensagem pretendida é um frame */
        if (!(msg instanceof Datagram))
            throw new RunnerException("message-invalid");

        /* Verifica se a mensagem é um pedido */
        if (msg instanceof Requestable)
            this.addRequest((DatagramRequest) msg);

        /* Conversão da mensagem para pacote datagrama */
        DatagramPacket packet = (DatagramPacket) msg.toCommunicable();

        /* Envio do pacote */
        this.send.lock();
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            throw new RunnerException("runner-disconnected");
        } finally {
            this.send.unlock();
        }
    }

    /* Receção de uma mensagem */
    public Message receive() {

        /* Buffer binário do pacote */
        byte[] data = new byte[Datagram.MAX_PACKET];

        /* Pacote que irá conter a mensagem recebida */
        DatagramPacket packet = new DatagramPacket(data, Datagram.MAX_PACKET);

        /* Receção de um pacote datagrama */
        try {

            /* Bloqueia a receção */
            this.receive.lock();

            /* Recebe um pacote e desbloqueia a receção */
            try {
                this.socket.receive(packet);   
            } finally {
                this.receive.unlock();
            }

        } catch(SocketTimeoutException e) {
            this.timeout();
            return this.receive();
        } catch (IOException e) {
            return null;
        }

        /* Devolve a mensagem recebida convertida */
        try {
            Datagram received = Datagram.fromCommunicable(packet);

            /* Bloqueia as operações de escrita e leitura */
            this.write.lock();

            try {

                /* Verifica se o pacote recebido não é uma duplicação */
                if (!this.receivedContains(received)) {
                    this.addReceived(received);
                    return received;
                }

            } finally {
                this.write.unlock();
            }

            /* Caso seja uma duplicação escuta de novo */
            return this.receive();

        } catch (CommunicableException | NodeException e) {
            return this.receive();
        }
    }

    /* Fecho do socket no caso de o mesmo estar aberto */
    public void close() {

        /* Bloqueia a operação de envio */
        this.send.lock();
        this.write.lock();

        /* Fecha o socket (caso ele esteja aberto) */
        if (!this.socket.isClosed())
                this.socket.close();
        
        this.write.unlock();
        this.send.unlock();
    }

    /* Verifica se o socket associado ao runner está fechado */
    public boolean isClosed() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Verifica se o socket do runner está fechado */
        try {
            return this.socket.isClosed();   
        } finally {
            this.read.unlock();
        }
    }

    /* Conversão do datagram runner para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Datagram)").append(super.toString());
        builder.append("socket:").append(this.socket.getLocalAddress()).append(":").append(this.socket.getLocalPort()).append(";");

        return builder.toString();
    }

    /* Adiciona um pedido ao runner */
    private void addRequest(DatagramRequest request) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Executa a adição na fila de pedidos */
        if (!this.requests.containsKey(request.getIdentifier()))
            this.requests.put(request.getIdentifier(), new Timeout<>(request, SEND_TIMEOUT));

        this.write.unlock();
    }

    /* Adiciona uma mensagem aos envios */
    private void addSent(Datagram packet) {
        if (!this.sentContains(packet))
            this.sentFragments.add(new Timeout<>(packet, SEND_TIMEOUT));
    }

    /* Adiciona uma mensagem à lista de recebidos */
    private void addReceived(Datagram packet) {
        this.received.add(new Timeout<>(packet, RECEIVE_TIMEOUT));
    }

    private void addFragment(Datagram fragment) {

    }

    /* Verifica se um certo pacote já existe na lista de chegadas para evitar repetições */
    private boolean receivedContains(Datagram packet) {

        /* Verifica se o pacote está presente na lista de fragmentos enviados */
        for (Timeout<Datagram> received : this.received)
            if (received.getObject().equals(packet))
                return true;

        return false;
    }

    /* Verifica se um certo pacote já existe na lista de envios à espera de confirmação para evitar repetições */
    private boolean sentContains(Datagram packet) {

        /* Verifica se o pacote está presente na lista de fragmentos enviados */
        for (Timeout<Datagram> sent : this.sentFragments)
            if (sent.getObject().equals(packet))
                return true;

        return false;
    }

    /* Verifica se existem fragmentos pendentes de um pedido */
    private boolean hasFragments(Datagram packet) {

        /* Verifica se existem fragmentos por confirmar de um pacote */
        try {

            /* Lista de fragmentos do pacote */
            List<Datagram> fragments = packet.fragment();

            /* Verifica se existem fragmentos do pacote à espera de confirmação */
            for (Datagram fragment : fragments)
                if (this.sentContains(fragment))
                    return true;

            return false;

        } catch (CommunicableException e) {
            return false;
        }
    }

    private void timeoutRequest() {

        /* Provoca um tick em todos os timeouts da lista requests */
        for (Timeout<DatagramRequest> timeout : this.requests.values()) {

            /* Apenas executa um tick se não existerem fragmentos por confirmar */
            if (!this.hasFragments(timeout.getObject())) {
                timeout.tick();

                /* Pega no pedido em si */
                DatagramRequest request = timeout.getObject();

                /* Se o pedido tiver sido resolvido noutro local */
                if (request.isResolved())
                    this.requests.remove(request.getIdentifier());

                /* Se o timeout exceder o tempo de vida, morre */
                if (timeout.hasExpired() && !request.isResolved()) {
                    request.fail(new CommunicableException("destination-unreachable"));
                    this.requests.remove(request.getIdentifier());
                }

                /* Caso o timeout não tenha expirado */
                if (!timeout.hasExpired() && !request.isResolved()) {
                    try {
                        this.send(request);
                    } catch (CommunicableException e) {
                        /* Invalida este tick */
                        timeout.untick();
                    } catch (RunnerException e) {
                        /* Já vai ser tratada após o timeout */
                    }
                }
            }
        }
    }

    private void timeoutSent() {

        /* Provoca um tick em todos os timeouts da lista sent */
        for (Timeout<Datagram> timeout : this.sentFragments) {
            timeout.tick();

            /* Pega na mensagem correspondente ao timeout */
            Datagram datagram = timeout.getObject();

            /* Caso o timeout tenha expirado */
            if (timeout.hasExpired())
                this.sentFragments.remove(timeout);

            /* Caso o timeout não tenha expirado */
            else {
                try {
                    this.send(datagram);
                } catch (CommunicableException e) {
                    /* Invalida este tick */
                    timeout.untick();
                } catch (RunnerException e) {
                    /* Já vai ser tratada após o timeout */
                }
            }
        }
    }

    private void timeoutReceived() {
        
        /* Provoca um tick em todos os timeouts da lista sent */
        for (Timeout<Datagram> timeout : this.received) {
            timeout.tick();

            /* Caso o timeout tenha expirado */
            if (timeout.hasExpired())
                this.received.remove(timeout);
        }
    }

    private void timeoutFragment() {

        /* Provoca um tick em todos os timeouts da lista sent */
        for (Timeout<Datagram> timeout : this.receivedFragments) {
            timeout.tick();

            /* Caso o timeout tenha expirado */
            if (timeout.hasExpired())
                this.receivedFragments.remove(timeout);
        }
    }

    private void timeout() {
        
        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Provoca um tick de timeout em todas as listas */
        this.timeoutFragment();
        this.timeoutSent();
        this.timeoutReceived();
        this.timeoutRequest();

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }
}
