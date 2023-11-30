package tools;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Objeto que armazena e define vários métodos relacionados com a
 * rede.
 */
public class Network {
    
    /* Procura pelo endereço local da máquina em que é executado */
    public static String getLocalAddress() {

        /* Placeholder para o futuro endereço */
        String address = "0.0.0.0";

        /* Procura pelo endereço local da máquina */
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();

            while( networkInterfaceEnumeration.hasMoreElements()) {
                for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                    if ( interfaceAddress.getAddress().isSiteLocalAddress())
                        address = interfaceAddress.getAddress().getHostAddress();
            }
        } catch (SocketException e) {
            address = "localhost";
        }

        return address;
    }
}
