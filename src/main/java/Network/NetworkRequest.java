package Network;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
            builder = new Request.Builder().url(
                    new URL(local ? NetworkInfo.getAddress() + "/DiscordBotAPI/api/" + url : url)
            );
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
    public NetworkResponse get() {
        try {
            Response response = client.newCall(
                    builder.addHeader("accept", "application/json").build()
            ).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            e.printStackTrace();
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
    public NetworkResponse get(HashMap<String, String> headers) {
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
     * @param async   Async request
     * @return Response from request
     */
    public NetworkResponse post(RequestBody body, HashMap<String, String> headers, boolean async) {
        try {
            if(headers != null) {
                parseHeaders(headers);
            }
            Request request = builder.post(body).build();
            if(async) {
                asyncRequest(request, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        // Do nothing
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        response.close();
                    }
                });
                return null;
            }
            Response response = client.newCall(request).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Make an async request
     *
     * @param request  Request to be executed
     * @param callback Callback to use
     */
    public void asyncRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    /**
     * Make a JSON POST request
     *
     * @param body  Body to send
     * @param async Async request
     * @return Response from request
     */
    public NetworkResponse post(String body, boolean async) {
        return post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body), null, async);
    }

    /**
     * Make a JSON POST request
     *
     * @param body Body to send
     */
    public NetworkResponse post(String body) {
        return post(body, false);
    }

    /**
     * Return the message of a response
     *
     * @param response Response from network call
     * @return String response
     */
    private NetworkResponse handleResponse(Response response) {
        try {
            boolean timeout = response == null;
            NetworkResponse networkResponse = new NetworkResponse(
                    (timeout || response.body() == null) ? null : response.body().string(),
                    timeout ? -1 : response.code()
            );
            if(!timeout) {
                response.close();
            }
            return networkResponse;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}