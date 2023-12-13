package menu;

import java.util.List;

import client.Client;
import client.ClientException;
import client.ClientException.AllFilesSentException;
import client.ClientException.FilenameExistsException;

import controller.ClientControl;
import socket.RunnerException;
import view.ClientView;

public class Console {

    private ClientControl control;
    private ClientView view;
    private Menu menu;
    
    /* Constructors */
    public Console(Client client) {
        this.control = new ClientControl(client);
        this.view = new ClientView(client);

        this.menu = new Menu();
        this.menu.addOption("UPDATE", 0);
        this.menu.addOption("PATH", 0);
        this.menu.addOption("IP", 0);
        this.menu.addOption("FILES", 0);
        this.menu.addOption("HELP", 0);
    }

    /* Console */
    public void run() {
        while (!this.view.isClosed()) {
            this.menu.readCommand();
            this.readArguments(this.menu.getArguments());   
        }
    }

    private void readArguments(List<String> args) {

        String cmd = args.get(0);

        if (cmd.equals("QUIT")) {
            try {
                this.control.sendDisconnect();
                System.out.println("Disconnecting...");
            } catch (RunnerException e) {
                System.out.println("Connection was already broken...");
            } catch (ClientException e) {
                System.out.println("Disconnected from unsafe connection...");
            }
        }

        if (cmd.equals("UPDATE")) {
            try {
                this.control.updateServer();
                System.out.println("Sucessful update!");
            } catch (AllFilesSentException e) {
                System.out.println("All available files have already been sent to the server!");
            } catch (FilenameExistsException e) {
                System.out.println("The filename " + e.getMessage() + " already exists on the server, please change the name!");
            } catch (RunnerException e) {
                System.out.println("Connection has been broken...");
            } catch (ClientException e) {
                System.out.println("Breaking unsafe connection...");
            }
        }

        if (cmd.equals("IP")) {
            try {
                this.view.printIp();
            } catch (ClientException e) {
                System.out.println("Breaking unstable connection...");
            }
        }

        if (cmd.equals("FILES")) {
            this.view.printFiles();
        }

        if (cmd.equals("PATH")) {
            this.view.printPath();
        }

        if (cmd.equals("HELP")) {
            this.menu.display();
        }
    }
}
