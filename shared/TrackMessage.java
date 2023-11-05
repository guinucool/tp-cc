/* Pacote de objetos partilhados entre o servidor e cliente */
package shared;

import java.io.*;

/**
 * @class TrackMessage
 * 
 * Estrutura básica de mensagem usada para comunicar de um lado para o outro.
 * 
 * @param argument Argumento da mensagem a ser passada.
 */
public abstract class TrackMessage implements Serializable {

    private String argument;

    public TrackMessage() {
        this.argument = "";
    }

    public TrackMessage(String arg) {
        this.argument = arg;
    }

    public void setArgument(String arg) {
        this.argument = arg;
    }

    public String getArgument() {
        return this.argument;
    }

    public String[] getArguments() {
        return this.argument.split(":");
    }
}
