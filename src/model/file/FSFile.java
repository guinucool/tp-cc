package model.file;

/**
 * Objeto que regista, na generalidade dos casos, um ficheiro do sistema.
 * 
 * @param EMPTY_MD5
 *      O hash de um ficheiro em branco.
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

    private static final String EMPTY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    private String hash;
    private String name;
    private long size;
    
    /* Constructors */
    public FSFile() {
        this.hash = EMPTY_MD5;
        this.name = "";
        this.size = 0;
    }

    public FSFile (String hash, String name, long size) throws FileException {
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
    private void setHash(String hash) throws FileException {
        if (hash.equals(EMPTY_MD5))
            throw new FileException("hash-invalid");

        this.hash = hash;
    }

    public void setName(String name) throws FileException {
        if (name.equals(""))
            throw new FileException("filename-invalid");

        this.name = name;
    }

    public void setSize(long size) throws FileException {
        if (size <= 0)
            throw new FileException("size-invalid");

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

    public static String getEmptyHash() {
        return EMPTY_MD5;
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