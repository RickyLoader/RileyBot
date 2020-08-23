package Network;

import java.net.InetAddress;

public class NetworkInfo {
    public static String getAddress() {
        try {
            //return "http://" + InetAddress.getLocalHost().getHostAddress();
            return "http://" + "192.168.1.19";
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPublicAddress() {
        try {
            return "http://" + new NetworkRequest("https://api.ipify.org", false).get();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
