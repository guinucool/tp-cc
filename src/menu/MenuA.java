package menu;

import java.util.*;

public class MenuA {
    
    private Map<String,Option> commands;
    private List<String> arguments;

    /* Constructors */
    public Menu() {
        this.commands = new HashMap<>();

        Option cmd = new Option();
        this.commands.put(cmd.getCommand(), cmd);
    }

    /* Getters */
    public List<String> getArguments() {
        return new ArrayList<>(this.arguments);
    }

    /* Setters */
    public void addOption(String command, int arguments) {
        this.commands.put(command, new Option(command, arguments));
    }

    /* Auxiliar */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Menu)commands:").append(this.commands).append(";");
        builder.append("arguments:").append(this.arguments).append(";");

        return builder.toString();
    }

    public void display() {

        System.out.println("*** HELP ***");
        
        for (Option command : this.commands.values())
            System.out.println(command.display());

        System.out.println("************");
    }

    public void readCommand() {

        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        String argument = scanner.nextLine();

        List <String> args = Arrays.asList(argument.split(" "));

        if (!this.commands.containsKey(args.get(0))) {
            System.out.println("Invalid command! Type HELP to get more information...");
            this.readCommand();
            return;
        }

        if (!this.commands.get(args.get(0)).validCommand(args)) {
            System.out.println("Invalid arguments! Type HELP to get more information...");
            this.readCommand();
            return;
        }

        this.arguments = args;
    }
}
