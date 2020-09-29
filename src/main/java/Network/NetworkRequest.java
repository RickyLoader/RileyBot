package Network;

import okhttp3.*;

import java.net.URL;
import java.util.HashMap;

public class NetworkRequest {
    private final OkHttpClient client;
    private Request.Builder builder;

    /**
     * Initialise an OkHTTP client with a url
     *
     * @param url   URL to query
     * @param local Boolean use local api
     */
    public NetworkRequest(String url, boolean local) {
        client = new OkHttpClient();
        try {
            builder = new Request.Builder().url(new URL(local ? NetworkInfo.getAddress() + "/DiscordBotAPI/api/" + url : url));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Make a GET request
     *
     * @return Response from request
     */
    public String get() {
        try {
            Response response = client.newCall(builder.addHeader("accept", "application/json").build()).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Add headers to the builder
     */
    private void parseHeaders(HashMap<String, String> headers) {
        for(String header : headers.keySet()) {
            builder.addHeader(header, headers.get(header));
        }
    }

    /**
     * Make a GET request
     *
     * @return Response from request
     */
    public String get(HashMap<String, String> headers) {
        try {
            headers.put("accept", "application/json");
            parseHeaders(headers);
            Response response = client.newCall(builder.build()).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Make a POST request
     *
     * @param body    body to send
     * @param headers Headers if required
     * @return Response from request
     */
    public String post(RequestBody body, HashMap<String, String> headers) {
        try {
            if(headers != null) {
                parseHeaders(headers);
            }
            Response response = client.newCall(builder.post(body).build()).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Make a JSON POST request
     *
     * @param body body to send
     * @return Response from request
     */
    public String post(String body) {
        return post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body), null);
    }

    /**
     * Return the message of a response
     *
     * @param response Response from network call
     * @return String response
     */
    private String handleResponse(Response response) {
        String data = null;
        try {
            if(response.body() == null) {
                return null;
            }
            if(response.code() == 200 || response.code() == 201) {
                data = response.body().string();
            }
            else if(response.code() == 404 || response.code() == 501 || response.code() == 500) {
                data = "err";
            }
            else {
                System.out.println(response.body().string());
                System.out.println(response.code());
            }
            response.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }
}