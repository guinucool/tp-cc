import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import server.Server;

public class FSTracker {

    private static class ServerRunner implements Runnable {
    
        private Server sv;

        public ServerRunner(Server sv) {
            this.sv = sv;
        }

        public void run() {
            try {
                sv.listen();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {

        /* Criação do servidor de suporte */
        Server sv = new Server();

        /* Início do servidor */
        try {
            sv.start();
            System.out.println("Server running on " + InetAddress.getLocalHost().getHostAddress() + " on port " + sv.getPort());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        /* Cria uma thread para executar os processos do servidor */
        Thread svr = new Thread(new ServerRunner(sv));
        svr.start();

        /* Espera pelo comando de término do servidor */
        Scanner sc = new Scanner(System.in);
        while (!sc.nextLine().equals("stop"));
        sc.close();

        /* Fecha o servidor e termina a thread */
        try {
            sv.stop();
            svr.join();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}