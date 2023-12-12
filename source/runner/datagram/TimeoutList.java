package runner.datagram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import message.CommunicableException;
import message.Requestable;
import message.Communicable.Operation;
import message.datagram.Datagram;
import message.datagram.DatagramRequest;
import runner.RunnerException;
import tools.Timeout;

/**
 * Objeto que descreve uma lista de timeouts e os pacotes que estes armazenam.
 */
public class TimeoutList {
    
    private List<Timeout<Datagram>> list;                           /* Lista de timeouts de datagramas */
    private int minTimeout;                                         /* O tempo minímo de timeout desta lista */
    private int currentTimeout;                                     /* O tempo de timeout desta lista */

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();    /* Bloqueio de leitura e escrita */
    private Lock read = this.rwlock.readLock();                     /* Bloqueio de escrita */
    private Lock write = this.rwlock.writeLock();                   /* Bloqueio de escrita e leitura */

    /* Construtor parametrizado */
    public TimeoutList(int minTimeout) {

        /* Verifica se o timeout fornecido é válido */
        if (minTimeout < 0)
            throw new RuntimeException("timeout-invalid");

        /* Associa as propriedades */
        this.list = new ArrayList<>();
        this.minTimeout = minTimeout;
        this.currentTimeout = minTimeout;
    }

    /* Adiciona um pacote à lista de timeouts */
    private void put(Datagram packet) {

        /* Adiciona o elemento à lista */
        if (!this.has(packet))
            this.list.add(new Timeout<Datagram>(packet));

        /* Atualiza o timeout da lista */
        this.currentTimeout = this.minTimeout + this.list.size();
    }

    /* Adiciona um pacote à lista de timeouts */
    public void add(Datagram packet) {
        
        /* Bloqueia a operação de escrita e leitura */
        this.write.lock();

        /* Adiciona o elemento à lista */
        this.put(packet);

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Adiciona um fragmento à lista, onde pode ser considerado como placeholder ou junto a um placeholder */
    public void addFragment(Datagram fragment) {

        /* Decide se vai adicionar um novo elemento à lista */
        boolean joined = false;

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Verifica se parte do fragmento já existe */
        for (Timeout<Datagram> timeout : this.list) {
            try {
                timeout.getObject().join(fragment);
                joined = true;
            } catch (CommunicableException e) {
                /* Não é preciso tratar a exceção */
            }
        }

        /* Caso o fragmento ainda não existisse adiciona à lista */
        if (!joined) {
            try {
                this.put(fragment.placeholder());   
            } catch (CommunicableException e) {
                /* Não é preciso tratar a exceção */
            }
        }

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Verifica se um pacote existe na lista de timeouts */
    private boolean has(Datagram packet) {

        /* Procura pela existência do pacote desejado */
        for (Timeout<Datagram> timeout : this.list)
            if (timeout.getObject().equals(packet))
                return true;

        /* Caso não encontra o pacote desejado */
        return false;
    }

    /* Verifica se um pacote existe na lista de timeouts */
    public boolean contains(Datagram packet) {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {
            /* Verifica se existe o pacote */
            return this.has(packet);  
        } finally {
            this.read.unlock();
        }
    }

    /* Verifica se existem fragmentos de um pacote na lista de timeouts */
    public boolean containsFragments(Datagram packet) throws CommunicableException {

        /* Cria a lista de fragmentos pretendida */
        List<Datagram> fragments = packet.fragment();

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {
            /* Procura pelos fragmentos do pacote */
            for (Datagram fragment : fragments)
                if (this.has(fragment))
                    return true;

            /* Caso não encontre */
            return false;

        } finally {
            this.read.unlock();
        }
    }

    /* Verifica se existem resposta a um pacote existe na lista de timeouts */
    public boolean containsResponses(Datagram packet) {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {
            
            /* Procura pela existência de respostas desejadas */
            for (Timeout<Datagram> timeout : this.list)
                if (timeout.getObject().isResponse(packet))
                    return true;

            /* Caso não encontra o pacote desejado */
            return false;
             
        } finally {
            this.read.unlock();
        }
    }

    /* Remove uma pacote, caso exista da lista de timeouts */
    public void remove(Datagram packet) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Remove o pacote fornecido da lista de timeouts */
        for (Timeout<Datagram> timeout : new ArrayList<>(this.list))
            if (timeout.getObject().equals(packet))
                this.list.remove(timeout);

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Provoca um tick no relógio de todos os timeouts da lista */
    public void timeout() {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Provoca um tick em todos os timeouts da lista */
        for (Timeout<Datagram> timeout : new ArrayList<>(this.list)) {
            timeout.tick();

            /* Caso o timeout tenha expirado */
            if (timeout.hasExpired(this.currentTimeout))
                this.list.remove(timeout);
        }

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Provoca um tick no relógio de todos os timeouts da lista, reenviado as mensagens que não tenham expirado */
    public void timeout(DatagramRunner runner) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();
        
        /* Provoca um tick em todos os timeouts da lista */
        for (Timeout<Datagram> timeout : new ArrayList<>(this.list)) {
            timeout.tick();

            /* Recupera a mensagem do timeout */
            Datagram packet = timeout.getObject();

            /* Verifica se o timeout expirou */
            if (timeout.hasExpired(this.currentTimeout)) {

                /* Remove o timeout da lista */
                this.list.remove(timeout);

                /* Caso seja uma mensagem de pedido */
                if (packet instanceof Requestable)
                    ((DatagramRequest) packet).fail(new CommunicableException("destination-unreachable"));
                    
            } else {

                try {

                    /* Tenta reenviar o pacote */
                    runner.send(packet);

                } catch (CommunicableException e) {

                    /* Invalida este tick */
                    timeout.untick();

                } catch (RunnerException e) {
                    /* Já vai ser tratada após o timeout */
                }
            }
        }

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Passa por todos os pacotes e verifica se o ack fornecido lhes pertence */
    public void ack(Datagram ack) {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        /* Passa por todos os elementos da lista */
        for (Timeout<Datagram> timeout : new ArrayList<>(this.list)) {

            /* Recupera a mensagem do timeout */
            Datagram packet = timeout.getObject();

            /* Verifica se o ack fornecido é ack do pacote */
            if (packet.isAck(ack)) {

                /* Reseta o timeout */
                timeout.reset();

                /* Verifica se é um ack completo ou apenas de um fragmento */
                if (packet.getOperation() != Operation.QUERY || !ack.isFragmented())
                    this.list.remove(timeout);
            }
        }

        /* Desbloqueia o bloqueio anterior */
        this.write.unlock();
    }

    /* Devolve fragmentos completos, caso existam */
    public Datagram getComplete() {

        /* Bloqueia as operações de escrita e leitura */
        this.write.lock();

        try {

            /* Procura por fragmentos completos */
            for (Timeout<Datagram> timeout : new ArrayList<>(this.list)) {

                /* Recupera o pacote do timeout */
                Datagram packet = timeout.getObject();

                /* Devolve um pacote que não esteja fragmentado */
                if (!packet.isFragmented()) {
                    this.list.remove(timeout);
                    return packet;
                }
            }

            /* Caso não haja fragmentos completos */
            return null;

        } finally {
            this.write.unlock();
        }
    }

    /* Devolve o tamanho da lista de timeouts */
    public int getSize() {

        /* Bloqueia a operação de escrita */
        this.read.lock();

        try {

            /* Devolve o tamanho */ 
            return this.list.size();
            
        } finally {
            this.read.unlock();
        }
    }
}
