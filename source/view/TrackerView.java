package view;

import java.nio.ByteBuffer;
import java.util.List;

import file.FileException;
import model.Tracker;
import model.TrackerException;

public class TrackerView {
    
    private static TrackerView singleton;

    private Tracker tracker;

    private TrackerView() {
        this.tracker = Tracker.getInstance();
    }

    public static TrackerView getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new TrackerView();

        /* Devolve a instância global */
        return singleton;
    }

    /* Devolve um ficheiro */
    public byte[] getFile(byte[] data) throws TrackerException {

        /* Converte o formato binário em string */
        String filename = new String(data);

        /* Devolve o ficheiro contido no tracker */
        return this.tracker.getFile(filename).getBytes();
    }

    /* Devolve um bloco de um ficheiro */
    public byte[] getBlock(List<byte[]> data) throws TrackerException, FileException {

        /* Converte o formato binário em string e inteiro */
        String filename = new String(data.get(0));
        int offset = ByteBuffer.wrap(data.get(1)).getInt();


        /* Devolve o ficheiro contido no tracker */
        return this.tracker.getBlock(filename, offset).getBytes();
    }
}
