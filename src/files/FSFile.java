import java.util.*;

import filexcp.*;

/**
 * Objeto que regista, na generalidade dos casos, um ficheiro do sistema.
 * 
 * @param hashlist
 *      A lista de todas as hashcodes registadas até agora no programa (para evitar repetições)
 * 
 * @param hash
 *      A hashcode do ficheiro
 * 
 * @param name
 *      O nome do ficheiro
 * 
 * @param extension
 *      A extensão do ficheiro
 * 
 * @param size
 *      O tamanho do ficheiro em bytes
 */
public abstract class FSFile {

    private static final String emptyhash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static List<String> hashlist = new ArrayList<>();

    private String hash;
    private String name;
    private String extension;
    private long size;
    
    /* Constructors */
    public FSFile() {
        this.hash = emptyhash;
        this.name = "";
        this.extension = "";
        this.size = 0;
    }

    public FSFile (String hash, String name, String extension, long size) throws FileException {
        this.setHash(hash);
        this.setName(name);
        this.setExtension(extension);
        this.setSize(size);
    }

    public FSFile (FSFile file) {
        this.hash = file.hash;
        this.name = file.name;
        this.extension = file.extension;
        this.size = file.size;
    }

    /* Setters */
    private void setHash(String hash) throws HashcodeRegisteredException, EmptyObjectHashException {

        /* Verifica se o ficheiro já não existe no sistema */
        if (hashlist.contains(hash))
            throw new HashcodeRegisteredException(hash);

        /* Verifica se é um ficheiro não vazio */
        if (hash.equals(emptyhash))
            throw new EmptyObjectHashException(hash);

        this.hash = hash;
    }

    public void setName(String name) throws EmptyFilenameException {
        if (name.equals(""))
            throw new EmptyFilenameException(name);

        this.name = name;
    }

    public void setExtension(String extension) throws EmptyFileExtensionException {
        if (extension.equals(""))
            throw new EmptyFileExtensionException(extension);

        this.extension = extension;
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

    public String getExtension(){
        return this.extension;
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
        return this.hash.equals(file.hash) && this.name.equals(file.name) && this.extension.equals(file.extension) && this.size == file.size;
    }

    public abstract Object clone();

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(FSFile)Hash:").append(this.hash);
        builder.append("Name:").append(this.name);
        builder.append("Extension:").append(this.extension);
        builder.append("Size:").append(this.size);
        builder.append(";");

        return builder.toString();
    }
}