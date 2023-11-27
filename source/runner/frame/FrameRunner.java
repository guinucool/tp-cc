package runner.frame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    /**
     * Envio de uma mensagem para o socket ao qual está ligado
     * 
     * @throws RunnerException no caso de a ligação entre sockets tiver sido perdida.
     * @throws MessageException no caso de a criação da mensagem binária para envio for
     * impossível.
     */
    public void send(Message msg) throws RunnerException, MessageException {

        /* Verifica se a mensagem pretendida é um frame */
        if (!(msg instanceof Frame))
            throw new RunnerException("message-invalid");

        /* Conversão da mensagem para frame binário */
        Frame frame = (Frame) msg;
        byte[] packet = frame.toByte();

        /* Envio da mensagem binária */
        this.send.lock();
        try {
            this.writer.writeInt(packet.length);
            this.writer.write(packet);
        } catch (IOException e) {
            this.close();
            throw new RunnerException("runner-disconnected");
        } finally {
            this.send.unlock();
        }
    }

    /**
     * Receção de uma mensagem cuja origem é o socket ao qual está ligado
     * 
     * @throws RunnerException no caso de a ligação entre sockets tiver sido perdida.
     * @throws MessageException no caso de a criação da mensagem binária para envio for
     * impossível.
     */
    public Message receive() throws RunnerException, MessageException {

        /* Variável que irá conter a mensagem recebida */
        byte[] data;

        /* Receção de uma mensagem binária */
        this.receive.lock();
        try {
            int size = this.reader.readInt();

            data = new byte[size];
            this.reader.read(data);
        } catch (Exception e) {
            this.close();
            throw new RunnerException("runner-disconnected");
        } finally {
            this.receive.unlock();
        }

        /* Devolve a mensagem recebida */
        return new Frame(data);
    }

    /* Fecho do socket no caso de o mesmo estar aberto */
    public void close() {

        /* Bloqueia o envio de futuras mensagens */
        this.send.lock();
        this.write.lock();

        /* Fecha o socket (caso ele esteja aberto) */
        try {
            if (!this.socket.isClosed())
                this.socket.close();
        } catch (IOException e) {
            /* Não é preciso tratar esta exceção */
        } finally {
            this.write.unlock();
            this.send.unlock();
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
}
