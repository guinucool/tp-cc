package worker;

import message.Message;

/**
 * Interface que define um operador de mensagem em geral.
 */
public interface MessageWorker extends Runnable {

    /* Criação de um operador para a mensagem fornecida */
    public MessageWorker operateMessage(Message msg);

    /* Ação de execução no caso do fecho do runner */
    public void close(boolean stable);
}
