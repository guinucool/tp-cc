package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Objeto que representa um node ligado ao servidor na vista do tracker.
 */
public class Node {
    
    private String address;          /* Endereço associado ao node representado */
    private int port;           /* Porta de alojamento udp associada ao node */

    /* Construtor parametrizado */
    public Node(String address, int port) throws NodeException {
        this.setAddress(address);
        this.setPort(port);
    }

    /* Construtor de cópia */
    public Node(Node node) {
        this.address = node.address;
        this.port = node.port;
    }

    /* Construtor binário */
    public Node(byte[] data) throws NodeException {

        ByteArrayInputStream barray = new ByteArrayInputStream(data);
        DataInputStream stream = new DataInputStream(barray);

        /* Conversão binária para node */
        try {
            this.setAddress(stream.readUTF());
            this.setPort(stream.readInt());

        } catch (IOException e) {
            throw new NodeException("node-invalid");
        }
    }

    /* Associação de um endereço à representação do node */
    private void setAddress(String address) throws NodeException {
        if (address.equals(""))
            throw new NodeException("address-invalid");

        this.address = address;
    }

    /* Associação de uma porta à representação do node */
    public void setPort(int port) throws NodeException {
        if (port < 0 || port > 65535)
            throw new NodeException("port-invalid");

        this.port = port;
    }

    /* Endereço usado pelo node para alojar o serviço de ficheiros */
    public String getAddress() {
        return this.address;
    }

    /* Porta usada pelo node para alojar o serviço de ficheiros */
    public int getPort() {
        return this.port;
    }

    /* Transforma o node em formato binário */
    public byte[] getBytes() throws NodeException {

        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(barray);
        
        try {
            stream.writeUTF(this.address);
            stream.writeInt(this.port);

            stream.flush();

            return barray.toByteArray();

        } catch (IOException e) {
            throw new NodeException("node-outofmemory");
        }
    }

    /* Verifica se este node é igual a um objeto */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        Node node = (Node) o;
        return this.address.equals(node.address) && this.port == node.port;
    }

    /* Clona este node para um node idêntico */
    public Object clone() {
        return new Node(this);
    }

    /* Converte o node para o formato de String */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Node)address:").append(this.address).append(";");
        builder.append("port:").append(this.port).append(";");

        return builder.toString();
    }
}
