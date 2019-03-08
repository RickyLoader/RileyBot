package Bot;

import okhttp3.*;

import java.net.URL;

public class ApiRequest{

    public static String executeQuery(String endPoint, String request, String query, boolean api){
        String result = null;
        try{
            String baseURL = "";
            if(api){
                baseURL+="http://192.168.1.69/DiscordBotAPI/public/index.php/api/";
            }
            URL url = new URL(baseURL + endPoint);
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(url);
            RequestBody body;

            switch(request){
                case "GET":
                    break;
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
            Response response = client.newCall(builder.build()).execute();
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
            e.printStackTrace();
        }
        return result;
    }
}