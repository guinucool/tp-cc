/* Pacote de objetos do servidor (Tracker) */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Message;

/**
 * @class Node
 * 
 * Representação de um node na visão do servidor (Tracker).
 * 
 * @param id Identificador gerado para o node.
 * @param ip IP do node.
 * @param udp Porta UDP associada ao node.
 * @param socket Socket usado para comunicar com o node.
 */
public class Node {
    
    private static int globalId = 1;

    private int id;
    private String ip;
    private Socket socket;
    
    public Node(Socket socket) {
        this.id = globalId++;
        this.socket = socket;
    }

    public void listenSocket() throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(this.socket.getInputStream());
        ObjectOutputStream os = new ObjectOutputStream(this.socket.getOutputStream());

        while (true) {
            Message input = (Message) is.readObject();

            switch (input.getType()) {

                case CONNECTION:
                    // Message check
                    break;
            
                default:
                    break;
            }
        }
    }
}
