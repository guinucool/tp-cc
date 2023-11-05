package shared;

public class TrackRequest extends TrackMessage {

    public enum Command {
        REGS, /* Registar node */
        UPTF, /* Update da lista de ficheiros */
        UPTB, /* Update da lista de blocos */
        GET,  /* Procura de ficheiro */
        QUIT  /* Saída do servidor */
    }

    private Command command;

    public TrackRequest(Command cmd) {
        super();
        this.command = cmd;
    }

    public TrackRequest(Command cmd, String arg) {
        super(arg);
        this.command = cmd;
    }

    public Command getCommand() {
        return this.command;
    }
}
