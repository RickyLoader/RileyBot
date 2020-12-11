package Network;

/**
 * Hold a network response code & body
 */
public class NetworkResponse {
    public final String body;
    public final int code;

    public NetworkResponse(String body, int code) {
        this.body = body;
        this.code = code;
    }
}