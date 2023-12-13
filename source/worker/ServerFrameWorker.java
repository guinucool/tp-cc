package worker;

import java.util.ArrayList;
import java.util.List;

import controller.TrackerController;
import message.Message;
import message.frame.Frame;
import model.TrackerException.UsedFilenameException;
import runner.Runner;

/**
 * Objeto que define o interpretador de mensagens do tracker.
 */
public class ServerFrameWorker implements MessageWorker {
    
    private Runner runner;          /* Runner que é usado para a comunicação destas mensagens */
    private Frame frame;            /* Mensagem a ser intrepretada */

    /* Construtor parametrizado */
    public ServerFrameWorker(Runner runner) throws WorkerException {
        if (runner.isClosed())
            throw new WorkerException("runner-disconnected");

        this.runner = runner;
    }

    /* Construtor de intrepretador para threads */
    private ServerFrameWorker(ServerFrameWorker worker) {
        this.runner = worker.runner;
    }

    /* Cria um interpretador de mensagem para ser corrido numa thread */
    public MessageWorker operateMessage(Message msg) throws WorkerException {
        
        /* Verifica se a mensagem fornecida é nula */
        if(msg == null)
            throw new WorkerException("message-null");
        
        /* Verifica se a mensagem fornecida é do tipo correto */
        if (!(msg instanceof Frame))
            throw new WorkerException("message-invalid");

        /* Cria um novo worker para operar a mensagem */
        ServerFrameWorker worker = new ServerFrameWorker(this);
        worker.frame = (Frame) msg;

        return worker;
    }

    /* Processo que intrepreta e executa uma mensagem */
    public void run() {

        /* Vai buscar o controlador do tracker */
        TrackerController controller = TrackerController.getInstance();

        /* Procura por erros na interpretação */
        try {
            
            /* Handler de registos de nodes */
            if (this.frame.getFlag() == 100 && this.frame.getNrArguments() == 1) {
                
                /* Regista o node no tracker */
                controller.registerNode(this.frame.getPayload(), this.runner.getId());

                /* Envia a resposta de novo para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 0));

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de registos de nodes */
            if (this.frame.getFlag() == 200) {

                /* Pega em todos os argumentos da mensagem */
                List<byte[]> payload = this.frame.getPayloadList();

                /* Cria uma lista que vai armazenar todos os futuros argumentos */
                List<byte[]> arguments = new ArrayList<>();

                /* Apaga todos os ficheiros existentes do node associado a este runner */
                controller.deleteNodeFiles(this.runner.getId());

                /* Percorre o payload por completo */
                for (byte[] data : payload) {

                    try {

                        /* Regista o ficheiro no tracker */
                        controller.registerFile(data, 0);

                    } catch (UsedFilenameException e) {

                        /* Caso haja um nome repetido adiciona-o aos argumentos */
                        arguments.add(e.getMessage().getBytes());
                    }
                }

                /* Envia a resposta de novo para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 201, arguments));

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de desconexão */
            if (this.frame.getIdentifier() == 101 && this.frame.getNrArguments() == 0) {

                /* Envia a resposta de confirmação para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 0));

                /* Fecha o runner após o envio */
                this.runner.close();

                /* Impede a interpretação de continuar */
                return;
            }

        } catch (Exception e) {
            /* Não é preciso tratar a exceção */
        }

        /* Caso a mensagem recebida não seja reconhecida */
        System.out.println("Unstable connection detected on runner " + this.runner.getId() + "! Closing connection...");
        this.runner.close();
    }

    /* Processo a executar no caso do fecho do runner */
    public void close(boolean stable) {
        
        /* Notifica o servidor caso um cliente tenha perdido conexão */
        if (!stable)
            System.out.println("Lost connection on runner " + this.runner.getId() + "!");

        /* Vai buscar o controlador do tracker */
        TrackerController controller = TrackerController.getInstance();

        /* Remove o node e tudo associado do tracker */
        controller.disconnectNode(0);
    }
}
