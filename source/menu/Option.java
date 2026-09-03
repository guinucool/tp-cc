package menu;

import java.util.*;

public class Option {
    
    private String command;
    private int arguments;

    /* Constructors */
    public Option() {
        this.command = "QUIT";
        this.arguments = 0;
    }

    public Option(String command, int arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public Option(Option option) {
        this.command = option.command;
        this.arguments = option.arguments;
    }

    /* Setters */
    public void setCommand(String command) {
        this.command = command;
    }

    public void setArguments(int arguments) {
        this.arguments = arguments;
    }

    /* Getters */
    public String getCommand() {
        return this.command;
    }

    public int getArguments() {
        return this.arguments;
    }

    /* Checkers */
    public boolean validCommand(List<String> arguments) {
        return arguments.get(0).equals(command) && arguments.size() == this.arguments + 1;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        Option cmd = (Option) o;
        return this.command.equals(cmd.command) && this.arguments == cmd.arguments;
    }

    public Object clone() {
        return new Option(this);
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Option)command:").append(this.command).append(";");
        builder.append("arguments:").append(this.arguments).append(";");

        return builder.toString();
    }

    public String display() {

        StringBuilder builder = new StringBuilder();

        builder.append(this.command);
        for (int i = 0; i < this.arguments; i++) {
            builder.append(" ").append("<argument>");
        }

        return builder.toString();
    }
}
