package model.directory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import model.file.FileException;
import model.file.NodeFile;

/**
 * Objeto que define uma diretoria usada por um cliente para armazenar ficheiros.
 * 
 * @param directory
 *      Diretoria onde os ficheiros se encontram.
 * 
 * @param files
 *      Map de ficheiros presentes na diretoria.
 */
public class Directory {

    private File directory;
    private Map<String,NodeFile> files;

    /* Constructor */
    public Directory() {
        this.directory = new File(".");
        this.files = new HashMap<String,NodeFile>();
        this.readFiles();
    }

    public Directory(String path) throws PathException {
        this.setPath(path);
        this.files = new HashMap<String,NodeFile>();
        this.readFiles();
    }

    /* Setters */
    private void setPath(String path) throws PathException {

        File dir = new File(path);

        if(!dir.isDirectory())
            throw new PathException("path-invalid");

        this.directory = dir;
    }

    /* Getters */
    public String getPath() {
        return this.directory.getAbsolutePath();
    }

    public List<NodeFile> getFiles(boolean notSent) {

        List<NodeFile> filelist = new ArrayList<>();

        for (NodeFile file : this.files.values())
            if (!notSent || !file.isSent())
                filelist.add((NodeFile) file.clone());

        return filelist;
    }

    /* Auxiliar */
    public String toString() {

        StringBuilder builder = new StringBuilder();
    
        builder.append("(Directory)path:").append(this.getPath()).append(";");
        builder.append("files:").append(this.files).append(";");
    
        return builder.toString();
    }

    public void readFiles() {

        File[] contents = this.directory.listFiles();

        for (File file : contents) {
            if (!file.isDirectory()) {
                try {
                    NodeFile nfile = new NodeFile(file);

                    if (!this.files.containsKey(nfile.getHash()))
                        this.files.put(nfile.getHash(), nfile);
                } catch (FileException e) {
                    /* Não é preciso tratar a exceção */
                }
            }
        }
    }

    public void sendFile(String hash) throws PathException {
        if (!this.files.containsKey(hash))
            throw new PathException("file-undefined");

        this.files.get(hash).send();
    }
}
