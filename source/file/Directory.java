package file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import file.block.Block;
import file.block.ResponseBlock;
import message.CommunicableException;
import message.frame.FrameRequest;
import model.Node;
import runner.datagram.ReliableRunner;
import runner.frame.FrameRunner;
import tools.RequestException;
import tools.RequestManager;

/**
 * Objeto que define uma diretoria na visão de um node.
 */
public class Directory {
    
    private File directory;                     /* Diretoria pretendida pronta para leitura */
    private Map<String,RegisterFile> files;     /* Ficheiros lidos da diretoria */
    private RequestFile request;                /* Ficheiro atual para download */

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
                    /* Ignora o ficheiro inválido */
                }
            }
        }
    }

    /* Define o ficheiro para download */
    public void setRequest(RequestFile file) {
        this.request = (RequestFile) file.clone();
    }

    /* Recebe informação sobre o bloco e coloca-a no pedido */
    public void setCurrent(Block block) throws FileException, DirectoryException {

        /* Verifica se já foi definido o pedido de ficheiro */
        if (this.request == null)
            throw new DirectoryException("request-null");

        /* Incorpora a informação do bloco no ficheiro em pedido */
        this.request.receive(block);
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

    /* Devolve um bloco pertencente a um ficheiro */
    public byte[] getBlock(String filename, long offset, int size) throws DirectoryException {

        /* Verifica se o ficheiro está disponível para download */
        if ((!this.files.containsKey(filename) && this.request == null) || (!this.files.containsKey(filename) && this.request != null && !this.request.getName().equals(filename)))
            throw new DirectoryException("filename-invalid");

        /* Cria o leitor do ficheiro */
        File file = new File(this.getPath() + "/" + filename);

        /* Verifica se o bloco pedido existe no ficheiro */
        if (file.length() - offset - size < 0)
            throw new DirectoryException("block-undefined");

        try {

            /* Abre as streams e lê a informação pretendida de lá */
            FileInputStream reader = new FileInputStream(file);
            BufferedInputStream input = new BufferedInputStream(reader);

            byte[] data = new byte[size];

            input.skip(offset);
            input.read(data);

            /* Fecha a stream buffer */
            input.close();

            /* Devolve a informação lida */
            return data;

        } catch (IOException e) {
            throw new DirectoryException("filename-undefined");
        }
    }

    /* Devolve o ficheiro em pedido do momento */
    public RequestFile getRequest() throws DirectoryException {

        /* Verifica se já foi definido o pedido de ficheiro */
        if (this.request == null)
            throw new DirectoryException("request-null");

        /* Devolve o pedido */
        return (RequestFile) this.request.clone();
    }

    /* Envia os vários ficheiros no node através do runner para o tracker */
    public void sendFiles(RequestManager manager, FrameRunner runner) throws DirectoryException, CommunicableException, RequestException {

        /* Atualiza a diretoria */
        this.setFiles();

        /* Verifica se existem ficheiros */
        if (this.files.size() == 0)
            throw new DirectoryException("directory-empty");

        List<byte[]> payload = new ArrayList<>();

        for (RegisterFile file : this.files.values()) {
            payload.add(file.getBytes());
            file.setStatus(0);
        }

        FrameRequest request = new FrameRequest((short) 200, payload);
        manager.sendSequentialRequest(runner, request);
    }

    /* Atualiza o estado de um ficheiro em específico para nome repetido */
    public void updateFileStatus(String filename) throws DirectoryException {

        /* Verifica se o ficheiro pretendido existe */
        if (!this.files.containsKey(filename))
            throw new DirectoryException("filename-undefined");

        /* Atualiza o estado */
        this.files.get(filename).setStatus(2);
    }

    /* Pede um ficheiro para download */
    public void requestFile(ReliableRunner udp, FrameRunner tcp, Condition wait, RequestManager manager) throws FileException, RequestException, CommunicableException, DirectoryException {

        /* Verifica se já foi definido o pedido de ficheiro */
        if (this.request == null)
            throw new RuntimeException("request-null");

        /* Cria o ficheiro */
        File file = new File(this.getPath() + "/" + this.request.getName());

        try {

            /* Verifica se o ficheiro já existe */
            if (!file.createNewFile())
                throw new DirectoryException("file-exists");

        } catch (IOException e) {
            throw new RuntimeException("file-uncreatable");
        }

        try {

            /* Pede o ficheiro */
            this.request.request(udp, tcp, wait, manager);

            /* Atualiza a lista de ficheiros */
            RegisterFile rfile = new RegisterFile(this.request, file);
            this.files.put(rfile.getName(), rfile);

            /* Apaga o pedido */
            this.request = null;

        } catch (RequestException | CommunicableException | FileException e) {

            /* Apaga o ficheiro em caso de erro */
            file.delete();
            throw e;
        }
    }

    /* Recebe partes de um ficheiro em download */
    public void receiveFile(ResponseBlock block, Node node, FrameRunner tcp, RequestManager manager) throws DirectoryException, FileException, RequestException, CommunicableException {

        /* Verifica se já foi definido o pedido de ficheiro */
        if (this.request == null)
            throw new DirectoryException("request-null");

        /* Incorpora o bloco no ficheiro em pedido */
        this.request.receive(block, node, tcp, manager, this.getPath());
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

        builder.append("request:").append(this.request.toString()).append(";");

        return builder.toString();
    }
}
