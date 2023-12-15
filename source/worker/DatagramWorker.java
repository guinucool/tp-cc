package worker;

import controller.RunnerClientController;
import file.DirectoryException;
import file.FileException;
import file.block.BlockException;
import message.CommunicableException;
import message.Message;
import message.Communicable.Operation;
import message.datagram.Datagram;
import runner.Runner;
import runner.RunnerException;
import tools.RequestException;
import view.RunnerClientView;

/**
 * Objeto que define o interpretador de mensagens do tracker.
 */
public class DatagramWorker implements MessageWorker {
    
    private Runner runner;          /* Runner que é usado para a comunicação destas mensagens */
    private Datagram datagram;      /* Mensagem a ser intrepretada */

    /* Construtor parametrizado */
    public DatagramWorker(Runner runner) throws WorkerException {
        if (runner.isClosed())
            throw new WorkerException("runner-disconnected");

        this.runner = runner;
    }

    /* Construtor de intrepretador para threads */
    private DatagramWorker(DatagramWorker worker) {
        this.runner = worker.runner;
    }

    /* Cria um interpretador de mensagem para ser corrido numa thread */
    public MessageWorker operateMessage(Message msg) {
        
        /* Verifica se a mensagem fornecida é nula */
        if(msg == null)
            throw new RuntimeException("message-null");
        
        /* Verifica se a mensagem fornecida é do tipo correto */
        if (!(msg instanceof Datagram))
            throw new RuntimeException("message-invalid");

        /* Cria um novo worker para operar a mensagem */
        DatagramWorker worker = new DatagramWorker(this);
        worker.datagram = (Datagram) msg;

        return worker;
    }

    /* Processo que intrepreta e executa uma mensagem */
    public void run() {

        /* Vai buscar o controller do cliente */
        RunnerClientController controller = RunnerClientController.getInstance();

        /* Vai buscar o view do cliente */
        RunnerClientView view = RunnerClientView.getInstance();

        try {

            /* Caso seja um pedido */
            if (this.datagram.getOperation() == Operation.QUERY) {

                /* Tratamento dos pedidos de blocos */
                if (this.datagram.getFlag() == 100) {
                    
                    /* Obtém o payload da mensagem */
                    byte[] payload = this.datagram.getPayload();

                    try {

                        /* Encontra o bloco no cliente */
                        byte[] data = view.getFileBlock(payload);

                        /* Envia o bloco pretendido */
                        this.runner.send(new Datagram(this.datagram.getIdentifier(), (short) 200, data, datagram.getNode()));

                    } catch (DirectoryException | BlockException e) {
                        /* Não é preciso tratar a exceção */
                    }

                    return;
                }
            }

            /* Caso seja uma resposta */
            if (this.datagram.getOperation() == Operation.RESPONSE) {

                /* Tratamento de blocos recebidos */
                if (this.datagram.getFlag() == 200 && view.hasRequest(this.datagram.getIdentifier())) {
                    
                    /* Obtém o payload da mensagem */
                    byte[] payload = this.datagram.getPayload();

                    try {

                        /* Recebe o bloco no cliente */
                        controller.receiveFileBlock(payload, this.datagram.getNode());

                        /* Resolve o pedido no sistema */
                        controller.solveRequest(this.datagram.getIdentifier());

                    } catch (DirectoryException | BlockException | FileException e) {
                        /* Não é preciso tratar a exceção */
                    }

                    return;
                }
            }

        } catch (RunnerException e) {
            return;
        } catch (RequestException | CommunicableException e) {
            this.runner.close();
            return;
        }
    }

    /* Processo a executar no caso do fecho do runner */
    public void close(boolean stable) {

        /* Vai buscar o controlador do cliente */
        RunnerClientController controller = RunnerClientController.getInstance();

        /* Desliga o cliente */
        controller.close();
    }
}
