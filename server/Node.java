/* Pacote de objetos do servidor (Tracker) */
package server;

import java.net.Socket;

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
    
    private int id;
    private String ip;
    private Socket socket;
    
    
}
