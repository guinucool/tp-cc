package runner.frame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import message.Communicable;
import message.CommunicableException;
import message.Message;
import message.MessageException;
import message.frame.Frame;

import runner.Runner;
import runner.RunnerException;

public class FrameRunner extends Runner {
    
    private Socket socket;              /* Socket associado ao runner */
    private DataInputStream reader;     /* Stream de leitura do socket associado ao runner */
    private DataOutputStream writer;    /* Stream de escrita do socket associado ao runner */

    private Lock send = new ReentrantLock();                        /* Lock de receção de informação */
    private Lock receive = new ReentrantLock();                     /* Lock de envio de informação */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();    /* Lock de escrita e leitura de informação */
    private Lock write = this.rwlock.writeLock();                   /* Lock de receção de informação */
    private Lock read = this.rwlock.readLock();                     /* Lock de envio de informação */

    /**
     * Construtor de socket
     * 
     * @throws RunnerException no caso de após a associação do socket, aquando da
     * abertura das streams, a conexão já tiver sido perdida.
     */
    public FrameRunner(Socket socket) throws RunnerException {

        /* Verifica se o socket está aberto */
        if (socket.isClosed())
            throw new RunnerException("socket-disconnected");

        /* Abre o socket para o destino */
        this.socket = socket;

        /* Abre as streams de leitura e escrita */
        try {
            this.reader = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            this.writer = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        } catch (IOException e) {
            this.close();
            throw new RunnerException("runner-disconnected");
        }
    }

    /* Endereço de alojamento local */
    public String getAddress() {
        
        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Lê o endereço local que aloja o socket */
        try {
            return this.socket.getLocalAddress().toString();
        } finally {
            this.read.unlock();
        }
    }

    /* Porta de alojamento local */
    public int getPort() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Lê a porta local que aloja o socket */
        try {
            return this.socket.getPort();
        } finally {
            this.read.unlock();
        }
    }

    /**
     * Envio de uma mensagem para o socket ao qual está ligado
     * 
     * @throws RunnerException no caso de a ligação entre sockets tiver sido perdida.
     * @throws CommunicableException no caso de a criação da mensagem binária para envio for
     * impossível.
     */
    public void send(Communicable msg) throws RunnerException, CommunicableException {

        /* Verifica se a mensagem pretendida é um frame */
        if (!(msg instanceof Frame))
            throw new RunnerException("message-invalid");

        /* Conversão da mensagem para frame binário */
        byte[] packet = (byte[]) msg.toCommunicable();

        /* Envio da mensagem binária */
        this.send.lock();
        try {
            this.writer.writeInt(packet.length);
            this.writer.write(packet);
            this.writer.flush();
        } catch (IOException e) {
            this.stop();
            throw new RunnerException("runner-disconnected");
        } finally {
            this.send.unlock();
        }
    }

    /**
     * Receção de uma mensagem cuja origem é o socket ao qual está ligado
     * 
     * @throws RunnerException no caso de a ligação entre sockets tiver sido perdida ou for instável.
     */
    public Message receive() throws RunnerException {

        /* Variável que irá conter a mensagem recebida */
        byte[] data;

        /* Receção de uma mensagem binária */
        this.receive.lock();
        try {
            int size = this.reader.readInt();

            data = new byte[size];
            this.reader.read(data);
        } catch (SocketException e) {
            return null;
        } catch (IOException e) {
            this.close();
            throw new RunnerException("runner-disconnected");
        } finally {
            this.receive.unlock();
        }

        /* Devolve a mensagem recebida */
        try {
            return new Frame(data);
        } catch (MessageException e) {
            this.close();
            throw new RunnerException("runner-unstable");
        }
    }

    /* Fecho do socket no caso de o mesmo estar aberto */
    public void close() {

        /* Bloqueia o envio de futuras mensagens */
        this.send.lock();

        /* Fecha o socket (caso ele esteja aberto) */
        this.stop();
        this.send.unlock();
    }

    /* Fecho do socket sem controlo de concorrência */
    private void stop() {

        /* Bloqueia a operação de leitura e escrita */
        this.write.lock();

        /* Fecha o socket (caso ele esteja aberto) */
        try {
            if (!this.socket.isClosed())
                this.socket.close();
        } catch (IOException e) {
            /* Não é preciso tratar esta exceção */
        } finally {
            this.write.unlock();
        }
    }

    /* Verifica se o socket associado ao runner está fechado */
    public boolean isClosed() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        /* Lê o estado do socket associado */
        try {
            return this.socket.isClosed();
        } finally {
            this.read.unlock();
        }
    }

    /* Conversão do frame runner para string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Frame)").append(super.toString());
        builder.append("socket:").append(this.socket.getLocalAddress()).append(":").append(this.socket.getLocalPort()).append(";");
        builder.append("connected:").append(this.socket.getRemoteSocketAddress()).append(";");

        return builder.toString();
    }
}
