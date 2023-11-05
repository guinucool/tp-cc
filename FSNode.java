import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import shared.TrackMessage;

public class FSNode {
    private String ipTracker;
    private int trackerPort;
    private String ipAddress;

    public FSNode(String ipTracker, int trackerPort, String ipAddress) {
        this.ipTracker = ipTracker;
        this.trackerPort = trackerPort;
        this.ipAddress = ipAddress;
    }

    public void registerNode() {
        try (Socket socket = new Socket(ipTracker, trackerPort);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {

            // Envia uma mensagem de registo para o FS_Tracker
            TrackMessage registrationMessage = new TrackMessage(TrackMessage.Type.CONNECTION, ipAddress, ipTracker);
            objectOutputStream.writeObject(registrationMessage);

            // Recebe a resposta do FS_Tracker
            TrackMessage responseMessage = (TrackMessage) objectInputStream.readObject();
            if (responseMessage.getType() == TrackMessage.Type.CONFIRMATION) {
                System.out.println("Registration completed on FS_Tracker");
            } else {
                System.out.println("Registration failed on FS_Tracker");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException{
        // Configuração do FS_Node com o endereço IP e porta do FS_Tracker
        String trackerIP = args[1];
        int trackerPort = Integer.valueOf(args[2]);

        // Configuração do endereço IP e porta do FS_Node
        String ipAddress = InetAddress.getLocalHost().getHostAddress();

        FSNode fsNode = new FSNode(trackerIP, trackerPort, ipAddress);

        // Registra o FS_Node no FS_Tracker
        fsNode.registerNode();
    }
}