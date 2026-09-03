package tools;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Objeto que define os vários métodos que dão handle ao dns e encontram
 * nomes e endereços.
 */
public class DNS {

    private static final String localzone = ".cc2324.local";            /* Define o nome da zone local */
    
    /* Encontra o InetAddress do host pretendido */
    private static InetAddress getInetAddress(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    /* Converte o nome, caso seja necessário */
    private static String convertName(String host) {

        /* Considera o host como nome final */
        String name = host;

        /* Converte o nome para a zone local */
        if (!name.contains("."))
            name += localzone;

        /* Devolve nome convertido */
        return name;
    }

    /* Verifica se um endereço é local */
    private static boolean isLocal(String host) {
        return host.contains(localzone);
    }

    /* Simplifica um nome, caso seja necessário */
    private static String simpleName(String host) {

        /* Verifica se o endereço é local */
        if (isLocal(host))
            return host.split("\\.")[0];

        /* Devolve o endereço caso não sejam necessárias alterações */
        return host;
    }

    /* Verifica se o nome fornecido é um nome */
    public static boolean isName(String host) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    
        return !host.matches(PATTERN);
    }

    /* Encontra o endereço de um host */
    public static String getAddress(String host) throws UnknownHostException {
        return getInetAddress(convertName(host)).getHostAddress();
    }

    /* Encontra o nome de um host */
    public static String getName(String host) throws UnknownHostException {

        /* Procura pelo nome */
        String name = simpleName(getInetAddress(convertName(host)).getCanonicalHostName());

        /* Verifica se o encontrado é um nome */
        if (!isName(name))
            name = getName(host);

        return name;
    }
}
