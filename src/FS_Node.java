import client.Client;
import client.ClientException;
import menu.Console;
import model.directory.PathException;
import socket.RunnerException;

public class FS_Node {
    
    public static void main(String[] args) {

        try {
            if (args.length == 3) {
                Client client = new Client(args[0], args[1], Integer.parseInt(args[2]));
                System.out.println("FS Track protocol connection established with server " + client.getDestIP() + " port " + client.getDestPort() + ".");
                Console cli = new Console(client);
                cli.run();
            }

            else if (args.length == 2) {
                Client client = new Client(args[0], args[1]);
                System.out.println("FS Track protocol connection established with server " + client.getDestIP() + " port " + client.getDestPort() + ".");
                Console cli = new Console(client);
                cli.run();
            }

            else {
                System.out.println("Invalid parameters given!");
                System.exit(1);
            }

            System.exit(0);
        } catch (PathException e) {
            System.out.println("Invalid path given!");
        } catch (ClientException e) {
            System.out.println("Failed to estabilish connection to the given ip! Maybe the given ip is wrong?");
        } catch (RunnerException e) {
            System.out.println("Failed to estabilish a safe connection to the server! Maybe the given ip is wrong?");
        }
    }
}
