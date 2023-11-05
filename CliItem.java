public class CliItem
{
    private int ind;
    private String name;
    private String desc;

    public CliItem(int ind, String name, String desc)
    {
	this.ind = ind;
	this.name = name;
	this.desc = desc;
    }

    public int getInd(){
	return this.ind;}

    public String getName(){
	return this.name;}

    public String getDesc(){
	return this.desc;}

    public String toString(){
	return (ind + " " + name + ";");}

    public String toInfo(){
	return (ind + " " + name + " - " + desc);}
}
