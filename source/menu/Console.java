package menu;

import java.util.List;

import model.ClientException;
import controller.ConsoleClientController;
import file.DirectoryException;
import file.FileException;
import message.CommunicableException;
import view.ConsoleClientView;

public class Console {

    private ConsoleClientController control;
    private ConsoleClientView view;
    private Menu menu;
    private boolean quit;
    
    /* Constructor vazio */
    public Console() {

        this.control = ConsoleClientController.getInstance();
        this.view = ConsoleClientView.getInstance();
        this.quit = false;

        this.menu = new Menu();
        this.menu.addOption("PATH", 0);
        this.menu.addOption("ADDRESS", 0);
        this.menu.addOption("FILES", 0);
        this.menu.addOption("UPDATE", 0);
        this.menu.addOption("AVAILABLE", 0);
        this.menu.addOption("REQUEST", 1);
        this.menu.addOption("HELP", 0);
    }

    /* Corre a consola de comandos */
    public void run() {

        /* Corre a consola enquanto houver ligação */
        while (!this.view.isClosed()) {
            this.menu.readCommand();
            this.readArguments(this.menu.getArguments());   
        }

        /* Caso o cliente se tenha desligado sem razão */
        if (!quit)
            System.out.println("Connection was lost unexpectadly!");
    }

    /* Lê os argumentos obtidos na leitura de comandos */
    private void readArguments(List<String> args) {

        /* Procura pelo comando nos argumentos */
        String cmd = args.get(0);

        /* Tenta intrepretar o comando, tendo em conta as possíveis exceções */
        try {

            /* Handler do comando "QUIT" */
            if (cmd.equals("QUIT")) {
                this.control.disconnect();
                this.quit = true;
                System.out.println("Disconnecting...");
            }

            /* Handler do comando "UPDATE" */
            if (cmd.equals("UPDATE")) {

                /* Processo de envio e verificação de envio */
                try {
                    this.control.sendFiles();
                    this.view.printFileState();
                } catch (DirectoryException e) {
                    System.out.println("There are no files available to be sent!");
                }
            }

            /* Handler do comando "AVAILABLE" */
            if (cmd.equals("AVAILABLE")) {

                /* Processo de pedido e receção da lista de ficheiros */
                this.control.requestFiles();
                this.view.printAvailable();
            }

            /* Handler do comando "REQUEST" */
            if (cmd.equals("REQUEST")) {

                /* Processo de envio e verificação de envio */
                try {
                    this.control.requestFile(args.get(1));
                    System.out.println("File downloaded sucessfully!");
                } catch (CommunicableException e) {
                    System.out.println("The requested file does not exist!");
                } catch (FileException e) {

                    if (e.getMessage().equals("file-corrupted"))
                        System.out.println("The requested file got corrupted! Please try again later!");
                    else
                        System.out.println("The requested file has insufficient nodes to be downloaded!");

                } catch (DirectoryException e) {
                    System.out.println("The requested file already exists in the node!");
                } 
            }

            /* Handler do comando "FILES" */
            if (cmd.equals("FILES")) {
                this.view.printFiles();
            }

            /* Handler do comando "PATH" */
            if (cmd.equals("PATH")) {
                this.view.printPath();
            }

            /* Handler do comando "HELP" */
            if (cmd.equals("HELP")) {
                this.menu.display();
            }

            /* Handler do comando "ADDRESS" */
            if (cmd.equals("ADDRESS")) {    
                this.view.printAddress();
            }
            
        } catch (ClientException e) {
            System.out.println("Disconnected from unsafe connection...");
        }
    }
}
