package file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import message.CommunicableException;
import message.frame.FrameRequest;
import runner.frame.FrameRunner;
import tools.RequestException;
import tools.RequestManager;

/**
 * Objeto que define uma diretoria na visão de um node.
 */
public class Directory {
    
    private File directory;                     /* Diretoria pretendida pronta para leitura */
    private Map<String,RegisterFile> files;     /* Ficheiros lidos da diretoria */

    /* Construtor vazio */
    public Directory() {
        this.directory = new File(".");
        this.setFiles();
    }

    /* Construtor parametrizado */
    public Directory(String path) throws DirectoryException {
        this.setPath(path);
        this.setFiles();
    }

    /* Define o caminho para diretoria pretendida */
    private void setPath(String path) throws DirectoryException {

        /* Cria um leitor para a futura diretoria */
        File dir = new File(path);

        /* Verifica se o caminho fornecido é o de uma diretoria */
        if (!dir.isDirectory())
            throw new DirectoryException("path-invalid");

        this.directory = dir;
    }

    /* Lê os vários ficheiros existentes na diretoria */
    private void setFiles() {

        /* Cria o mapa que vai alojar os ficheiros */
        this.files = new HashMap<>();

        /* Lê todos os ficheiros presentes na pasta */
        File[] contents = this.directory.listFiles();

        for (File file : contents) {
            if (!file.isDirectory()) {
                try {
                    RegisterFile rfile = new RegisterFile(file);
                    this.files.put(rfile.getName(), rfile);
                } catch (FileException e) {
                    /* Não é preciso tratar a exceção */
                }
            }
        }
    }

    /* Devolve a diretoria em que o node está correr */
    public String getPath() {
        return this.directory.getAbsolutePath();
    }

    /* Devolve um ficheiro em específico da diretoria */
    public RegisterFile getFile(String filename) throws DirectoryException {
        
        /* Verifica se o ficheiro existe na diretoria */
        if (!this.files.containsKey(filename))
            throw new DirectoryException("filename-undefined");

        return (RegisterFile) this.files.get(filename).clone();
    }

    /* Devolve os vários ficheiros existentes na diretoria */
    public List<RegisterFile> getFiles() {

        /* Cria a lista que vai originar o resultado */
        List<RegisterFile> files = new ArrayList<>();

        for (RegisterFile file : this.files.values())
            files.add((RegisterFile) file.clone());

        return files;
    }

    /* Envia os vários ficheiros no node através do runner para o tracker */
    public void sendFiles(RequestManager manager, FrameRunner runner) throws DirectoryException, FileException, CommunicableException, RequestException {

        /* Atualiza a diretoria */
        this.setFiles();

        /* Verifica se existem ficheiros */
        if (this.files.size() == 0)
            throw new DirectoryException("directory-empty");

        List<byte[]> payload = new ArrayList<>();

        for (RegisterFile file : this.files.values())
            payload.add(file.getBytes());

        FrameRequest request = new FrameRequest((short) 200, payload);
        manager.sendSequentialRequest(runner, request);
    }

    /* Atualiza o estado de um ficheiro em específico */
    public void updateFileStatus(String filename, int status) throws DirectoryException {

        /* Verifica se o ficheiro pretendido existe */
        if (!this.files.containsKey(filename))
            throw new DirectoryException("filename-undefined");

        /* Atualiza o estado */
        this.files.get(filename).setStatus(status);
    }

    /* Converte a diretoria em formato string */
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("(Directory)");
        builder.append("path:").append(this.getPath()).append(";");
        builder.append("files:");

        for (Map.Entry<String, RegisterFile> register : this.files.entrySet()) {
            builder.append(" key:").append(register.getKey());
            builder.append(" value:").append(register.getValue());
        }

        builder.append(";");

        return builder.toString();
    }
}
