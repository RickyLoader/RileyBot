package Network;

import Command.Structure.EmbedHelper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class ImgurManager {
    public static final String BASE_URL = "https://i.imgur.com/";

    /**
     * Strip the alpha channel from the image before saving as JPG
     *
     * @param image Image to have alpha channel stripped
     * @return JPG ready image
     */
    public static BufferedImage stripAlpha(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copy.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Convert a buffered image to base64 encoded JPEG
     *
     * @param image BufferedImage to be encoded
     * @return String containing base64 encoded JPEG
     */
    private static String toBase64JPEG(BufferedImage image) {
        try {
            if(image.getType() != BufferedImage.TYPE_INT_RGB) {
                image = stripAlpha(image);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", out);
            return Base64.encodeBase64String(out.toByteArray());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Convert a buffered image to base64 encoded PNG
     *
     * @param image BufferedImage to be encoded
     * @return String containing base64 encoded PNG
     */
    private static String toBase64PNG(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Base64.encodeBase64String(out.toByteArray());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Upload an image to Imgur by a URL to the image
     *
     * @param image String URL to image to be uploaded
     * @return Link to image or null
     */
    public static String uploadImage(String image) {
        try {
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", image)
                    .addFormDataPart("type", "URL")
                    .build();

            String url = "https://api.imgur.com/3/image";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "CLIENT-ID " + Secret.IMGUR_CLIENT_ID);
            JSONObject response = new JSONObject(
                    new NetworkRequest(url, false).post(body, headers, false).body
            );
            return response.getJSONObject("data").getString("link");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Pass the image to the local API & save it on the server.
     *
     * @param image Image to upload
     * @param png   Upload as PNG instead of JPG
     * @return URL to uploaded image or null
     */
    @Nullable
    public static String localUpload(BufferedImage image, boolean png) {
        String base64 = png ? toBase64PNG(image) : toBase64JPEG(image);
        if(base64 == null) {
            return null;
        }
        JSONObject body = new JSONObject().put("image", base64);
        NetworkResponse response = new NetworkRequest("images/upload", true).post(body.toString());
        if(response.code != 200) {
            return null;
        }
        final String key = "relative_url";
        JSONObject responseBody = new JSONObject(response.body);
        return responseBody.has(key) ? Secret.LOCAL_DOMAIN + responseBody.getString(key) : null;
    }

    /**
     * Upload the given image to imgbb
     *
     * @param image Image to upload
     * @return URL to uploaded image or null
     */
    public static String alternativeUpload(BufferedImage image) {
        String base64 = toBase64PNG(image);
        if(base64 == null) {
            return null;
        }

        String url = "https://api.imgbb.com/1/upload?key=" + Secret.IMGBB_KEY;
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", base64)
                .build();

        NetworkResponse response = new NetworkRequest(url, false).post(body);
        return new JSONObject(response.body).getJSONObject("data").getString("url");
    }

    /**
     * Upload a given BufferedImage to Imgur and return the link to the image
     *
     * @param image Buffered image to be uploaded
     * @param png   Upload as PNG instead of JPG
     * @return Link to image or null
     */
    public static String uploadImage(BufferedImage image, boolean png) {
        try {
            String base64 = png ? toBase64PNG(image) : toBase64JPEG(image);
            if(base64 == null) {
                return null;
            }

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", base64)
                    .addFormDataPart("type", "base64")
                    .build();

            String url = "https://api.imgur.com/3/image";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "CLIENT-ID " + Secret.IMGUR_CLIENT_ID);

            String response = new NetworkRequest(url, false).post(body, headers, false).body;
            return new JSONObject(response).getJSONObject("data").getString("link");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Upload a video from the given URL to Imgur and return the link
     *
     * @param video Video URL
     * @return Link to video or null
     */
    public static String uploadVideo(String video) {
        try {
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("video", Base64.encodeBase64String(EmbedHelper.downloadVideo(video)))
                    .addFormDataPart("type", "base64")
                    .build();

            String url = "https://api.imgur.com/3/upload";

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "CLIENT-ID " + Secret.IMGUR_CLIENT_ID);

            String response = new NetworkRequest(url, false).post(body, headers, false).body;
            return BASE_URL + new JSONObject(response).getJSONObject("data").getString("id");
        }
        catch(Exception e) {
            return null;
        }
    }
}
