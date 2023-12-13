package controller;

import file.FileException;
import file.TrackFile;
import file.block.BlockException;
import model.Node;
import model.NodeException;
import model.Tracker;
import model.TrackerException;

/**
 * Objeto que define o controlador de tracker.
 */
public class TrackerController {
    
    private static TrackerController singleton;             /* Instância única do tracker controller */

    private Tracker tracker;            /* Tracker cujo tracker controller opera sobre */

    /* Construtor vazio */
    private TrackerController() {
        this.tracker = Tracker.getInstance();
    }

    /* Devolve a instância única do tracker controller */
    public static TrackerController getInstance() {

        /* Verifica se a instância de tracker controller já existe */
        if (singleton == null)
            singleton = new TrackerController();

        /* Devolve o singleton */
        return singleton;
    }

    /* Regista um node no tracker */
    public void registerNode(byte[] data, int id) throws NodeException, TrackerException {

        /* Cria um novo node com a informação recebida */
        Node node = new Node(data);

        /* Regista este novo node */
        this.tracker.addNode(id, node);
    }

    /* Regista um ficheiro no tracker */
    public void registerFile(byte[] data, int id) throws FileException, TrackerException, BlockException {

        /* Cria um novo ficheiro com a informação recebida */
        TrackFile file = new TrackFile(data);

        /* Insere o ficheiro e associa o node */
        this.tracker.addFile(id, file);
    }

    /* Destroi um node de todos os ficheiros onde está presente */
    public void deleteNodeFiles(int id) {
        this.tracker.removeNodeFromFiles(id);
    }

    /* Dessassocia um node ao tracker */
    public void disconnectNode(int id) {
        this.tracker.removeNodeFromFiles(id);
        this.tracker.removeNode(id);
    }
}
