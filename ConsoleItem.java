public class ConsoleItem {

    private static int globalId = 1;

    private int id;
    private String name;
    private String desc;

    public ConsoleItem(String name, String desc) {
	    this.id = globalId++;
	    this.name = name;
	    this.desc = desc;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
	    return this.name;
    }

    public String getDesc() {
    	return this.desc;
    }

    public String toInfo() {
	    return (id + " -> " + name + " - " + desc);
    }
}
