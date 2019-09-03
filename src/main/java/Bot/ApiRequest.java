package Bot;

import okhttp3.*;

import java.net.URL;

/**
 * ApiRequest.java handles making HTTP requests to the bot's API.
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class ApiRequest{

    /**
     * Make a request to the API and return the JSON response.
     *
     * @param endPoint Endpoint of the API to access
     * @param request  Request type
     * @param query    JSON query to be appended to the request (may be null e.g GET)
     * @param api      Boolean for accessing the bot's API or a different API
     * @return String JSON response from the API
     */
    public static String executeQuery(String endPoint, String request, String query, boolean api){
        String result = null;
        try{

            // If accessing the bot's API, the endpoint is appended to the baseURL, otherwise the endpoint is the url
            String baseURL = "";
            if(api){
                baseURL += "http://192.168.1.73/DiscordBotAPI/api/";
            }

            // Initialise the URL, client, and Request
            URL url = new URL(baseURL + endPoint);
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(url);
            RequestBody body;

            // Add the request type and body if required
            switch(request) {
                case "ADD":
                    body = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"), query
                    );
                    builder.post(body);
                    break;
                case "UPDATE":
                    if(query == null){
                        query = "{}";
                    }
                    body = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"), query
                    );
                    builder.put(body);
                    break;
                case "DELETE":
                    builder.delete();
                    break;
            }

            // Execute the request and receive a response
            Response response = client.newCall(builder.build()).execute();

            // Successful request
            if(response.code() == 200){
                result = response.body().string();
            }
            else{
                System.out.println("FAILURE");
                System.out.println(response.body().string());
                System.out.println(url);
            }
        }
        catch(Exception e){
            return null;
        }
        return result;
    }
}