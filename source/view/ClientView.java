package view;

import java.util.List;

import file.RegisterFile;
import model.Client;
import model.Node;

public class ClientView {
    
    private static ClientView singleton;

    private Client client;

    private ClientView() {
        this.client = Client.getInstance();
    }

    public static ClientView getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new ClientView();

        /* Devolve a instância global */
        return singleton;
    }

    /* Verifica se existe o cliente está aberto */
    public boolean isClosed() {
        return !(this.client.getStatus());
    }

    /* Verifica o estado dos ficheiros do cliente */
    public void printFileState() {

        /* Lista de ficheiros no cliente */
        List<RegisterFile> files = this.client.getFiles();

        /* Variável que verifica se todos os ficheiros foram enviados */
        boolean sent = true;

        /* Verificação por todos os ficheiros */
        for (RegisterFile file : files) {

            /* Em caso de repetição de filename */
            if (file.getStatus() == 2)
                System.out.println("Please change the name of " + file.getName() + "! There is already a file with the same name on tracker!");

            /* Em caso de falha de envio */
            if (file.getStatus() == 1)
                System.out.println("The file " + file.getName() + "could not be sent... Please try again!");

            /* Muda o estado global */
            if (file.getStatus() != 0)
                sent = false;
        }

        /* Caso tudo tenha sido bem sucedido */
        if (sent)
            System.out.println("All files were sucessfully sent to tracker!");
    }

    /* Imprime todos os ficheiros num node */
    public void printFiles() {

        /* Lista de ficheiros no cliente */
        List<RegisterFile> files = this.client.getFiles();

        /* Imprime a informação para cada ficheiro */
        for (RegisterFile file : files)
            System.out.println("File - Hash: " + file.getHash() + " | Name: " + file.getName() + " | Size: " + file.getSize() + " | Status : " + file.getStatus());

        /* Impressão no caso de não haver ficheiros */
        if (files.size() == 0)
            System.out.println("There are no files associated with this node!");
    }

    /* Imprime o caminho em que o node corre */
    public void printPath() {
        System.out.println("The node is running on: " + this.client.getPath() + "!");
    }

    /* Imprime o endereço em que o node está alojado */
    public void printAddress() {

        /* Vai buscar o node que define a conexão do cliente */
        Node node = this.client.getNode();

        /* Imprime as informações */
        System.out.println("The node is running on: " + node.getAddress() + ":" + node.getPort() + "!");
    }
}
