package worker;

import message.Message;
import message.frame.Frame;
import runner.Runner;

public class FrameWorker implements MessageWorker {
    
    private Runner runner;
    private Frame frame;

    public FrameWorker(Runner runner) throws WorkerException {
        if (runner.isClosed())
            throw new WorkerException("runner-disconnected");

        this.runner = runner;
    }

    private FrameWorker(FrameWorker worker) {
        this.runner = worker.runner;
    }

    public MessageWorker operateMessage(Message msg) throws WorkerException {
        
        /* Verifica se a mensagem fornecida é nula */
        if(msg == null)
            throw new WorkerException("message-null");
        
        /* Verifica se a mensagem fornecida é do tipo correto */
        if (!(msg instanceof Frame))
            throw new WorkerException("message-invalid");

        /* Cria um novo worker para operar a mensagem */
        FrameWorker worker = new FrameWorker(this);
        worker.frame = (Frame) msg;

        return worker;
    }

    public void run() {
        System.out.println(this.frame.toString());
    }

    public void close(boolean stable) {
        System.out.println(stable);
    }
}
