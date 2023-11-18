package socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import message.TrackMessage;

/**
 * Objeto que segura um socket tcp e executa operações bem definidas no mesmo.
 * 
 * @param socket
 *      O socket que deve ser segurado e operado.
 * 
 * @param reader
 *      Stream de leitura do socket.
 * 
 * @param writer
 *      Stream de escrita do socket.
 * 
 * @param lock
 *      Lock que bloqueia durante as operações de fecho, leitura e envio.
 */
public abstract class SocketRunner {
    
    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;

    /* Locks */
    private Lock lock = new ReentrantLock();

    /* Constructors */
    public SocketRunner(Socket socket) throws RunnerException {
        this.setSocket(socket);
    }

    /* Setters */
    private void setSocket(Socket socket) throws RunnerException {
        if (socket.isClosed())
            throw new RunnerException("socket-close");

        this.socket = socket;
    }

    protected void setReader() throws RunnerException {
        try {
            this.reader = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        } catch (IOException e) {
            this.close();
            throw new RunnerException("socket-connection");
        }
    }

    protected void setWriter() throws RunnerException {
        try {    
            this.writer = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            this.close();
            throw new RunnerException("socket-connection");
        }
    }

    /**
     * @brief Define a ordem de abertura correta para o streams de leitura e escrita.
     */
    public abstract void setStream() throws RunnerException;

    /* Getters */
    public String getSourceIP() {
        return this.socket.getLocalAddress().getHostAddress();
    }

    public String getDestIP() {
        return this.socket.getRemoteSocketAddress().toString().substring(1).split(":")[0];
    }

    public int getSourcePort() {
        return this.socket.getLocalPort();
    }

    public int getDestPort() {
        return this.socket.getPort();
    }

    /* Auxiliar */
    public String toString() {
        
        StringBuilder builder = new StringBuilder();

        builder.append("(SocketRunner)socket").append(this.socket.toString()).append(";");
        builder.append("reader:").append(this.reader.toString()).append(";");
        builder.append("writer:").append(this.writer.toString()).append(";");
        builder.append("lock:").append(this.lock.toString()).append(";");

        return builder.toString();
    }

    public TrackMessage listenMessage() throws RunnerException {

        try {

            /* Bloqueia a espera de uma nova mensagem */
            this.reader.readBoolean();

            /* Bloqueia e lê a mensagem */
            this.lock.lock();
            try {
                return TrackMessage.deserialize(this.reader);
            } finally {
                this.lock.unlock();
            }
        } catch (IOException e) {
            this.close();
            throw new RunnerException("socket-connection");
        }
    }

    public void sendMessage(TrackMessage msg) throws RunnerException {

        /* Bloqueia */
        this.lock.lock();

        /* Envia o inicio de uma nova mensagem */
        try {
            this.writer.writeBoolean(true);
            msg.serialize(this.writer);
        } catch(IOException e) {
            this.close();
            throw new RunnerException("socket-connection");
        } finally {
            this.lock.unlock();
        }
    }

    public void close() {

        /* Bloqueia */
        this.lock.lock();

        /* Fecha o socket */
        try {
            if (!this.socket.isClosed()) this.socket.close();
        } catch (IOException e) {
            /* Não é necessário tratar esta exceção */
        } finally {
            this.lock.unlock();
        }
    }

    /* Checkers */
    public boolean isClosed() {
        
        this.lock.lock();

        try {
            return this.socket.isClosed();
        } finally {
            this.lock.unlock();
        }
    }
}
