package Network;

import java.net.InetAddress;

public class NetworkInfo {
    public static String getAddress() {
        try {
            return "http://" + InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
