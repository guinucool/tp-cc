package view;

import file.DirectoryException;
import file.block.BlockException;
import file.block.ResponseBlock;
import file.block.TransferBlock;
import model.Client;

public class RunnerClientView {
    
    private static RunnerClientView singleton;

    private Client client;

    private RunnerClientView() {
        this.client = Client.getInstance();
    }

    public static RunnerClientView getInstance() {

        /* Cria uma nova instância global deste controlador, caso não exista */
        if (singleton == null)
            singleton = new RunnerClientView();

        /* Devolve a instância global */
        return singleton;
    }

    /* Verifica se existe um pedido para o identificador fornecido */
    public boolean hasRequest(short identifier) {
        return this.client.wasRequested(identifier);
    }

    /* Vê a informação relativo a um ficheiro pedido */
    public byte[] getFileBlock(byte[] data) throws DirectoryException, BlockException {

        /* Cria o bloco de transferência */
        TransferBlock block = new TransferBlock(data);

        /* Obtém a informação do bloco */
        byte[] blockdata = this.client.getBlock(block.getFilename(), block.getOffset(), block.getSize());

        /* Cria bloco de resposta */
        ResponseBlock res = new ResponseBlock(block, blockdata);
        return res.getBytes();
    }
}
