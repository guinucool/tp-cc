import java.net.UnknownHostException;

import file.DirectoryException;
import menu.Console;
import model.Client;
import model.ClientException;
import tools.DNS;

public class FS_Node {
    
    public static void main(String[] args) {

        try {
            if (args.length == 3) {
                Client.startClient(args[0], args[1], Integer.parseInt(args[2]));
                System.out.println("FS Track protocol connection established with server " + DNS.getName(args[1]) + " port " + args[2] + ".");
            }

            else if (args.length == 2) {
                Client.startClient(args[0], args[1]);
                System.out.println("FS Track protocol connection established with server " + DNS.getName(args[1]) + " port 9090.");
            }

            else {
                System.out.println("Invalid parameters given!");
                System.exit(1);
            }

            System.out.println("FS Transfer protocol connection established at " + Client.getInstance().getNode().getAddress() + " port " + Client.getInstance().getNode().getPort() + ".");

            Console cli = new Console();
            cli.run();

            System.exit(0);

        } catch (DirectoryException e) {
            System.out.println("Invalid path given!");
        } catch (ClientException | UnknownHostException e) {
            System.out.println("Failed to estabilish connection to the given ip! Maybe the given ip is wrong?");
        } catch (NumberFormatException e) {
            System.out.println("The given port is invalid! Please try again!");
        }
    }
}
