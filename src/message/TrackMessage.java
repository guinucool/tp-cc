package message;

import java.io.*;
import java.util.*;

/**
 * Objeto que descreve uma mensagem que pode ser usada para comunicação entre cliente e servidor.
 * 
 * @param type
 *      O tipo da mensagem (request ou response).
 * 
 * @param code
 *      O código da mensagem.
 * 
 * @param arguments
 *      Os argumentos da mensagem.
 * 
 * Nota: Existem vários códigos de mensagem como:
 *      - 0 (Sucesso)
 */
public class TrackMessage {
    
    public enum Type {
        REQUEST,
        RESPONSE
    }

    private Type type;
    private int code;
    private List<String> arguments;

    /* Constructors */
    public TrackMessage() {
        this.type = Type.RESPONSE;
        this.code = 0;
        this.arguments = new ArrayList<>();
    }

    public TrackMessage(Type type, int code, List<String> arguments) {
        this.type = type;
        this.code = code;
        this.arguments = new ArrayList<>(arguments);
    }

    public TrackMessage(boolean type, int code, List<String> arguments) {
        this.setType(type);
        this.code = code;
        this.arguments = new ArrayList<>(arguments);
    }

    public TrackMessage(TrackMessage msg) {
        this.type = msg.type;
        this.code = msg.code;
        this.arguments = msg.getArguments();
    }

    /* Setters */
    public void setType(Type type) {
        this.type = type;
    }

    public void setType(boolean type) {
        if (type) this.type = Type.REQUEST;
        else this.type = Type.RESPONSE;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = new ArrayList<>(arguments);
    }

    /* Getters */
    public Type getType() {
        return this.type;
    }

    public boolean getBoolType() {
        return this.type == Type.REQUEST;
    }

    public int getCode() {
        return this.code;
    }

    public List<String> getArguments() {
        return new ArrayList<>(this.arguments);
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        TrackMessage msg = (TrackMessage) o;
        return this.type == msg.type && this.code == msg.code && this.arguments.equals(msg.arguments);
    }

    public Object clone() {
        return new TrackMessage(this);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(TrackMessage)type:").append(this.type).append(";");
        builder.append("code:").append(this.code).append(";");
        builder.append("arguments:").append(this.arguments).append(";");

        return builder.toString();
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(this.getBoolType());
        out.writeInt(this.code);
        out.writeInt(this.arguments.size());
        
        for (String argument : this.arguments)
            out.writeUTF(argument);
    }

    public static TrackMessage deserialize(DataInputStream in) throws IOException {
        boolean type = in.readBoolean();
        int code = in.readInt();
        int argsize = in.readInt();
        List<String> arguments = new ArrayList<>();

        for (int i = 0; i < argsize; i++)
            arguments.add(in.readUTF());

        return new TrackMessage(type, code, arguments);
    }
}
