package runner.datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import message.Communicable;
import message.CommunicableException;
import message.Message;
import message.datagram.Datagram;
import model.NodeException;
import runner.Runner;
import runner.RunnerException;
import tools.Network;

public class BasicDatagramRunner extends Runner {

    public static final int WAIT_TIMEOUT = 5000;                    /* Timeout por falta de receção de mensagens em milisegundos */
    
    private DatagramSocket socket;                                  /* Socket que aloja a comunicação UDP */

    private Lock send = new ReentrantLock();                        /* Bloqueia a operação de envio */
    private Lock receive = new ReentrantLock();                     /* Bloqueia a operação de receção */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();    /* Lock de escrita e leitura de informação */
    private Lock write = this.rwlock.writeLock();                   /* Lock de receção de informação */
    private Lock read = this.rwlock.readLock();                     /* Lock de envio de informação */


    /* Construtor vazio */
    public BasicDatagramRunner() throws RunnerException {
        this.setSocket(9090);
    }

    /* Construtor parametrizado */
    public BasicDatagramRunner(int port) throws RunnerException {
        this.setSocket(port);
    }

    /* Define o socket e seu timeout */
    private void setSocket(int port) throws RunnerException {

        /* Tenta abrir o socket */
        try {
            this.socket = new DatagramSocket(port);
            this.socket.setSoTimeout(WAIT_TIMEOUT);
        } catch (IOException e) {
            this.close();
            throw new RunnerException("socket-refused");
        }
    }

    /* Endereço de alojamento local */
    public String getAddress() {

        /* Bloqueia a operação de escrita */
        this.read.lock();
        
        /* Devolve o endereço em que o serviço udp está alojado */
        try {
            return Network.getLocalAddress();   
        } finally {
            this.read.unlock();
        }
    }

    /* Porta de alojamento local */
    public int getPort() {
        
        /* Bloqueia a operação de escrita */
        this.read.lock();
        
        /* Devolve a porta em que o serviço udp está alojado */
        try {
            return this.socket.getLocalPort();
        } finally {
            this.read.unlock();
        }
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

        /* Pacote que irá conter a mensagem recebida */
        byte[] data = new byte[Datagram.MAX_PACKET];
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
            System.out.println("timeout!");
            return this.receive();
        } catch (IOException e) {
            return null;
        }

        /* Tenta devolver a mensagem recebida convertida */
        try {

            /* Converte a mensagem e devolve em caso de sucesso */
            return Datagram.fromCommunicable(packet);
            
        } catch (CommunicableException | NodeException e) {

            /* Ignora o pacote em caso de falhanço */
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
}
