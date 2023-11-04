package shared;

public class TrackRequest {

    public enum Command {
        REGS,
        GET
    }

    private Command cmd;
    private String args;

    public TrackRequest(Command cmd, String args) {
        this.cmd = cmd;
        this.args = args;
    }

    public Command getCommand() {
        return this.cmd;
    }

    public String getArguments() {
        return this.args;
    }   

    public Command setCommand(Command cmd) {
        return this.cmd = cmd;
    }

    public String setArguments(String args) {
        return this.args = args;
    }  
}
