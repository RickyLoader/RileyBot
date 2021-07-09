package Network;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class NetworkRequest {
    private final OkHttpClient client;
    private final NetworkResponse timeout;
    private final String url;
    private final boolean local;

    /**
     * Initialise an OkHTTP client with a url
     *
     * @param url   URL to query
     * @param local Use local api
     */
    public NetworkRequest(String url, boolean local) {
        this(url, local, true);
    }

    /**
     * Initialise an OkHTTP client with a url
     *
     * @param url      URL to query
     * @param local    Use local api
     * @param redirect Follow redirects
     */
    public NetworkRequest(String url, boolean local, boolean redirect) {
        this.client = new OkHttpClient.Builder()
                .followRedirects(redirect)
                .followSslRedirects(redirect)
                .build();
        this.timeout = new NetworkResponse(null, NetworkResponse.TIMEOUT_CODE, null);
        this.url = url;
        this.local = local;
    }

    /**
     * Get the request builder
     *
     * @return Request builder
     * @throws MalformedURLException On bad URL
     */
    private Request.Builder getRequestBuilder() throws MalformedURLException {
        return new Request.Builder().url(
                new URL(local ? "http://" + Secret.LOCAL_IP + Secret.LOCAL_API_PATH + url : url)
        );
    }

    /**
     * Get the request URL
     *
     * @return Request URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Make a GET request
     *
     * @return Response from request
     */
    public NetworkResponse get() {
        try {
            Response response = client.newCall(
                    getRequestBuilder().addHeader("accept", "application/json").build()
            ).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            return timeout;
        }
    }

    /**
     * Make a DELETE request
     *
     * @return Response from request
     */
    public NetworkResponse delete() {
        try {
            Response response = client.newCall(
                    getRequestBuilder().delete().addHeader("accept", "application/json").build()
            ).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            return timeout;
        }
    }

    /**
     * Add headers to the builder
     *
     * @param builder Request builder to add headers to
     * @param headers Map of header name -> value to add to the request builder
     */
    private void parseHeaders(Request.Builder builder, HashMap<String, String> headers) {
        for(String header : headers.keySet()) {
            builder.addHeader(header, headers.get(header));
        }
    }

    /**
     * Make a GET request
     *
     * @param headers Map of header name -> value to add to send with the request
     * @return Response from request
     */
    public NetworkResponse get(HashMap<String, String> headers) {
        try {
            headers.put("accept", "application/json");
            Request.Builder builder = getRequestBuilder();
            parseHeaders(builder, headers);
            Response response = client.newCall(builder.build()).execute();
            return handleResponse(response);
        }
        catch(Exception e) {
            e.printStackTrace();
            return timeout;
        }
    }

    /**
     * Make a POST request
     *
     * @param body    body to send
     * @param headers Map of header name -> value to add to send with the request
     * @param async   Async request
     * @return Response from request
     */
    public NetworkResponse post(RequestBody body, @Nullable HashMap<String, String> headers, boolean async) {
        try {
            Request.Builder builder = getRequestBuilder();
            if(headers != null) {
                parseHeaders(builder, headers);
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
            return timeout;
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
     * @return Response
     */
    public NetworkResponse post(String body) {
        return post(body, false);
    }

    /**
     * Make a POST request without a body
     *
     * @return Response
     */
    public NetworkResponse post() {
        return post("");
    }

    /**
     * Make a form POST request
     *
     * @param body Form to send
     * @return Response
     */
    public NetworkResponse post(RequestBody body) {
        return post(body, null, false);
    }

    /**
     * Return the message of a response
     *
     * @param response Response from network call
     * @return Response
     */
    private NetworkResponse handleResponse(Response response) {
        try {
            if(response == null || response.body() == null) {
                return timeout;
            }
            NetworkResponse networkResponse = new NetworkResponse(
                    response.body().string(),
                    response.code(),
                    response.headers()
            );
            response.close();
            return networkResponse;
        }
        catch(Exception e) {
            e.printStackTrace();
            return timeout;
        }
    }
}