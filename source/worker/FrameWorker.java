package worker;

import java.util.ArrayList;
import java.util.List;

import controller.RunnerClientController;
import controller.TrackerController;
import message.CommunicableException;
import message.Message;
import message.Communicable.Operation;
import message.frame.Frame;
import model.TrackerException;
import model.TrackerException.UsedFilenameException;
import runner.Runner;
import tools.RequestException;
import view.RunnerClientView;
import view.TrackerView;

/**
 * Objeto que define o interpretador de mensagens do tracker.
 */
public class FrameWorker implements MessageWorker {
    
    private Runner runner;          /* Runner que é usado para a comunicação destas mensagens */
    private Frame frame;            /* Mensagem a ser intrepretada */
    private boolean server;         /* Indicador de intrepretador de server ou cliente */

    /* Construtor parametrizado */
    public FrameWorker(Runner runner, boolean server) throws WorkerException {
        if (runner.isClosed())
            throw new WorkerException("runner-disconnected");

        this.runner = runner;
        this.server = server;
    }

    /* Construtor de intrepretador para threads */
    private FrameWorker(FrameWorker worker) {
        this.runner = worker.runner;
        this.server = worker.server;
    }

    /* Cria um interpretador de mensagem para ser corrido numa thread */
    public MessageWorker operateMessage(Message msg) {
        
        /* Verifica se a mensagem fornecida é nula */
        if(msg == null)
            throw new RuntimeException("message-null");
        
        /* Verifica se a mensagem fornecida é do tipo correto */
        if (!(msg instanceof Frame))
            throw new RuntimeException("message-invalid");

        /* Cria um novo worker para operar a mensagem */
        FrameWorker worker = new FrameWorker(this);
        worker.frame = (Frame) msg;

        return worker;
    }

    /* Processo que intrepreta e executa uma mensagem */
    public void run() {

        /* Intrepretação a correr no caso de ser servidor */
        if (this.server)
            this.runServer();

        /* Caso contrário */
        else
            this.runClient();
    }

    private void runServer() {

        /* Vai buscar o controlador do tracker */
        TrackerController controller = TrackerController.getInstance();

        /* Vai buscar o view do tracker */
        TrackerView view = TrackerView.getInstance();

        /* Procura por erros na interpretação */
        try {

            /* Verifica se o frame recebido é um pedido */
            if (this.frame.getOperation() != Operation.QUERY)
                throw new Exception("frame-response");
            
            /* Handler de registos de nodes */
            if (this.frame.getFlag() == 100 && this.frame.getNrArguments() == 1) {
                
                /* Regista o node no tracker */
                controller.registerNode(this.frame.getPayload(), this.runner.getId());

                /* Envia a resposta de novo para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 0));

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de registos de ficheiros */
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
                        controller.registerFile(data, this.runner.getId());

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

            /* Handler de pedidos de ficheiros */
            if (this.frame.getFlag() == 202 && this.frame.getNrArguments() == 1) {

                /* Pega em todos os argumentos da mensagem */
                byte[] payload = this.frame.getPayload();

                try {

                    /* Tenta recuperar o ficheiro do tracker */
                    byte[] argument = view.getFile(payload);

                    /* Envia a resposta de novo para o node */
                    this.runner.send(new Frame(this.frame.getIdentifier(), (short) 204, argument));
                    
                } catch (TrackerException e) {

                    /* Envia a resposta de novo para o node */
                    this.runner.send(new Frame(this.frame.getIdentifier(), (short) 204));
                }
                

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de blocos de pedidos de ficheiros */
            if (this.frame.getFlag() == 203 && this.frame.getNrArguments() == 2) {

                /* Pega em todos os argumentos da mensagem */
                List<byte[]> payload = this.frame.getPayloadList();

                /* Tenta recuperar o ficheiro do tracker */
                byte[] argument = view.getBlock(payload);

                /* Envia a resposta de novo para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 205, argument));

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de registo do node em blocos de ficheiros */
            if (this.frame.getFlag() == 300 && this.frame.getNrArguments() == 2) {

                /* Pega em todos os argumentos da mensagem */
                List<byte[]> payload = this.frame.getPayloadList();

                /* Tenta recuperar o ficheiro do tracker */
                controller.registerBlock(payload, this.runner.getId());

                /* Envia a resposta de novo para o node */
                this.runner.send(new Frame(this.frame.getIdentifier(), (short) 0));

                /* Impede a interpretação de continuar */
                return;
            }

            /* Handler de desconexão */
            if (this.frame.getFlag() == 101 && this.frame.getNrArguments() == 0) {

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

    private void runClient() {

        /* Vai buscar o controlador do cliente */
        RunnerClientController controller = RunnerClientController.getInstance();

        /* Vai buscar o view do cliente */
        RunnerClientView view = RunnerClientView.getInstance();

        try {

            /* Verifica se o frame recebido é uma resposta */
            if (this.frame.getOperation() != Operation.RESPONSE)
                throw new RequestException("frame-query");

            /* Verifica se o frame recebido tem um pedido */
            if (!view.hasRequest(this.frame.getIdentifier()))
                throw new RequestException("frame-unrequested");

            /* Handler de mensagens de confirmação */
            if (this.frame.getFlag() == 0) {

                /* Resolve o pedido em sucesso */
                controller.solveRequest(this.frame.getIdentifier());

                return;
            }

            /* Handler de mensagens de confirmação de ficheiros */
            if (this.frame.getFlag() == 201) {

                /* Retira o payload da mensagem */
                List<byte[]> payload = this.frame.getPayloadList();

                /* Atualiza o estado dos ficheiros */
                for (byte[] data : payload) {

                    /* Transforma a informação binária em string */
                    String filename = new String(data);

                    /* Atualiza o estado no cliente */
                    controller.notifyRepeteadFilename(filename);

                }

                /* Resolve o pedido em sucesso */
                controller.solveRequest(this.frame.getIdentifier());

                return;
            }

            /* Handler de mensagens com ficheiros pedidos */
            if (this.frame.getFlag() == 204 && this.frame.getNrArguments() == 1) {

                /* Retira o payload da mensagem */
                byte[] payload = this.frame.getPayload();

                /* Atualiza o ficheiro em pedido */
                controller.receiveFile(payload);

                /* Resolve o pedido em sucesso */
                controller.solveRequest(this.frame.getIdentifier());

                return;
            }

            /* Handler de mensagens com ficheiros pedidos inexistentes */
            if (this.frame.getFlag() == 204 && this.frame.getNrArguments() == 0) {

                /* Resolve o pedido em sucesso */
                controller.failRequest(this.frame.getIdentifier(), new CommunicableException("filename-inexistent"));

                return;
            }

            /* Handler de mensagens de blocos de ficheiros */
            if (this.frame.getFlag() == 205 && this.frame.getNrArguments() == 1) {

                /* Retira o payload da mensagem */
                byte[] payload = this.frame.getPayload();

                /* Atualiza a informação do bloco no ficheiro em pedido */
                controller.receiveBlock(payload);

                /* Resolve o pedido em sucesso */
                controller.solveRequest(this.frame.getIdentifier());

                return;
            }
            
        } catch (Exception e) {
            /* Não é preciso tratar a exceção */
        }

        this.runner.close();
    }

    /* Processo a executar no caso do fecho do runner */
    public void close(boolean stable) {

        /* Intrepretação a correr no caso de ser servidor */
        if (this.server)
            this.closeServer(stable);

        /* Caso contrário */
        else
            this.closeClient(stable);
    }

    private void closeServer(boolean stable) {

        /* Notifica o servidor caso um cliente tenha perdido conexão */
        if (!stable)
            System.out.println("Lost connection on runner " + this.runner.getId() + "!");

        /* Vai buscar o controlador do tracker */
        TrackerController controller = TrackerController.getInstance();

        /* Remove o node e tudo associado do tracker */
        controller.disconnectNode(this.runner.getId());
    }

    private void closeClient(boolean stable) {

        /* Vai buscar o controlador do cliente */
        RunnerClientController controller = RunnerClientController.getInstance();

        /* Desliga o cliente */
        controller.close();
    }
}
