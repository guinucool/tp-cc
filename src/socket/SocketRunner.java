package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;

import message.*;

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

        try {
            this.setStream();
        } catch (IOException e) {
            this.close();
            throw new RunnerException("socket lost connection");
        }
    }

    /* Setters */
    private void setSocket(Socket socket) throws RunnerException {
        if (socket.isClosed())
            throw new RunnerException("one-side closed");

        this.socket = socket;
    }

    protected void setReader() throws IOException {
        this.reader = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
    }

    protected void setWriter() throws IOException {
        this.writer = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * @brief Define a ordem de abertura correta para o streams de leitura e escrita.
     */
    protected abstract void setStream() throws IOException;

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
            throw new RunnerException("socket lost connection");
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
            throw new RunnerException("socket lost connection");
        } finally {
            this.lock.unlock();
        }
    }

    public void close() {

        /* Bloqueia */
        this.lock.lock();

        /* Fecha o socket */
        try {
            this.socket.close();
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
