import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ClientCli
{
    private Scanner scanner;
    private List<CliItem> items;
    private boolean exit;
    
    public ClientCli()
    {
	this.scanner = new Scanner(System.in);
	this.items = new ArrayList<>();
	this.exit = false;
    }

    private void printLn(int lines)
    {
	for(int i = 0; i < lines; i++)
	    System.out.println("");
    }

    private void printHeader() {
        System.out.println("+-----------------------+");
        System.out.println("|      Client Cli       |");
        System.out.println("+-----------------------+");
    }
    
    private void errorMsg(Exception e) {
	switch (e.getClass().getSimpleName())
	    {
	    case "ArrayIndexOutOfBoundsException":
		System.out.println("Erro - argumentos em falta;");
		break;
	    default:
		System.out.println("Erro desconhecido");
	    }
    }

    private void cmdHELP() {
	for (CliItem ci : this.items)
	    System.out.println(ci.toInfo() + ";");
    }

    private void cmdGET(String arg1) {
	System.out.println("get "+ arg1);
	// williamAfton.springLockFailure();
	// williamAfton.scream("I always come back!!");
    }

    private void cmdUPDATE() {
	System.out.println("update");
    }

    private void cmdQUIT() {
	exit = true;}

    public String readLine()
    {
        return scanner.nextLine().replaceAll("\n", "");
    }

    private boolean parseCmd()
    {
	String line = readLine();
	String segs[] = line.split(" ");
	switch (line)
	    {
	    case "UPDATE":
		cmdUPDATE();
		return true;
	    case "HELP":
		cmdHELP();
		return true;
	    case "QUIT":
		cmdQUIT();
		return true;
	    }
	switch (segs[0])
	    {
	    case "GET":
		try {cmdGET(segs[1]);}
		catch (ArrayIndexOutOfBoundsException e)
		    {errorMsg(e);}
		return true;
	    }
	return false;
    }

    private void insertItem(int i, String n, String desc)
    {
	CliItem ci = new CliItem(i, n, desc);
	this.items.add(i - 1, ci);
    }

    public void start()
    {
	insertItem(1, "GET", "efetuar pedido de download de um ficheiro");
	insertItem(2, "UPDATE", "efetuar nova leitura da pasta partilhada para consequente upload de alterações/adições de ficheiros");
	insertItem(3, "HELP", "imprimir esta lista");
	insertItem(4, "QUIT", "sair do programa");
	printHeader();
	printLn(2);
	while(!exit)
	    {
		parseCmd();
	    }
    }

    public static void main(String args[])
    {
	ClientCli cli = new ClientCli();
	cli.start();
    }
}
