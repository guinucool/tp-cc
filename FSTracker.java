import java.io.*;
import java.net.*;
import java.util.*;

import server.*;

public class FSTracker {

    public static void main(String[] args) {

        /* Criação do servidor de suporte */
        Server sv = new Server();

        /* Início do servidor */
        try {
            sv.start();
            System.out.println("Server running on " + InetAddress.getLocalHost().getHostAddress() + " on port " + sv.getPort());
        } catch (IOException e) {
            System.out.println("Couldn't start server. Perhaps the port is already in use?");
            return;
        }

        /* Cria uma thread para executar os processos do servidor */
        Thread thread = new Thread(sv);
        thread.start();

        /* Espera pelo comando de término do servidor */
        Scanner sc = new Scanner(System.in);
        while (!sc.nextLine().equals("stop"));
        sc.close();

        /* Fecha o servidor e termina a thread */
        sv.stop();
        try {
            thread.join();
        } catch (InterruptedException e) {}
    }
}