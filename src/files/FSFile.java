import filexcp.*;

/**
 * Objeto que regista, na generalidade dos casos, um ficheiro do sistema.
 * 
 * @param hashlist
 *      A lista de todas as hashcodes registadas até agora no programa (para evitar repetições).
 * 
 * @param hash
 *      A hashcode do ficheiro.
 * 
 * @param name
 *      O nome e a extensão do ficheiro.
 * 
 * @param size
 *      O tamanho do ficheiro em bytes.
 */
public abstract class FSFile {

    private static final String emptyhash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private String hash;
    private String name;
    private long size;
    
    /* Constructors */
    public FSFile() {
        this.hash = emptyhash;
        this.name = "";
        this.size = 0;
    }

    public FSFile (String hash, String name, String extension, long size) throws FileException {
        this.setHash(hash);
        this.setName(name);
        this.setSize(size);
    }

    public FSFile (FSFile file) {
        this.hash = file.hash;
        this.name = file.name;
        this.size = file.size;
    }

    /* Setters */
    private void setHash(String hash) throws EmptyObjectHashException {
        if (hash.equals(emptyhash))
            throw new EmptyObjectHashException(hash);

        this.hash = hash;
    }

    public void setName(String name) throws EmptyFilenameException {
        if (name.equals(""))
            throw new EmptyFilenameException(name);

        this.name = name;
    }

    public void setSize(long size) throws InvalidFileSizeException {
        if (size <= 0)
            throw new InvalidFileSizeException("" + size);

        this.size = size;
    }

    /* Getters */
    public String getHash(){
        return this.hash;
    }

    public String getName(){
        return this.name;
    }

    public long getSize(){
        return this.size;
    }

    /* Auxiliar */
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if ((o == null) || (this.getClass() != o.getClass()))
            return false;

        FSFile file = (FSFile) o;
        return this.hash.equals(file.hash) && this.name.equals(file.name) && this.size == file.size;
    }

    public abstract Object clone();

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(FSFile)hash:").append(this.hash).append(";");
        builder.append("name:").append(this.name).append(";");
        builder.append("size:").append(this.size).append(";");

        return builder.toString();
    }
}