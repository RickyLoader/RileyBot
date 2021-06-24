package Network;

import okhttp3.Headers;

/**
 * Hold a network response code & body
 */
public class NetworkResponse {
    public static final int TIMEOUT_CODE = -1;
    public final String body;
    public final int code;
    public final Headers headers;

    public NetworkResponse(String body, int code, Headers headers) {
        this.body = body;
        this.code = code;
        this.headers = headers;
    }
}