import java.util.*;

import exception.*;
import socket.*;

public class ClientC
{
    private Scanner sc;
	private List<ConsoleItem> items;
	private boolean exit;

	private ClientSocketRunner runner;
    
    public ClientC() {
		this.sc = new Scanner(System.in);
		this.items = new ArrayList<>();
		this.exit = false;
    }

	private String readCommand() {
		System.out.print("> ");
		return sc.nextLine();
	}

    private void printHeader() {
        System.out.println("+-----------------------+");
        System.out.println("|    Client--Console    |");
        System.out.println("+-----------------------+");
		System.out.println("");
    }

	private void printInfo() {
		for (ConsoleItem item : this.items) {
			System.out.println(item.toInfo());
		}
	}

	public void addItem(String name, String desc) {
		this.items.add(new ConsoleItem(name, desc));
	}

	private void runCommand(String cmd) throws InvalidArgumentException {
		String[] args = cmd.split(" ");

		if (args.length < 1)
			throw new InvalidArgumentException(cmd);

		switch (args[0]) {
			case "quit":
				runner.
				this.exit = true;
				break;
		
			default:
				throw new InvalidArgumentException(cmd);
		}
	}

    public void start() {
		this.printHeader();
		this.printInfo();

		while (!exit) {
			String cmd = this.readCommand();
			try {
				this.runCommand(cmd);
			} catch (InvalidArgumentException e) {
				System.out.println("Invalid argument was submitted (" + e.getMessage() + ")");
			}
		}
    }
}
