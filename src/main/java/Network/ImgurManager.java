package Network;

import Command.Structure.EmbedHelper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ImgurManager {
    private static final String
            BASE_URL = "https://i.imgur.com/",
            BASE_API_URL = "https://api.imgur.com/3/",
            IMGUR_DOMAIN_REGEX = "https?://(i.)?imgur.com/",
            LINK_KEY = "link",
            DATA_KEY = "data";

    /**
     * Strip the alpha channel from the image before saving as JPG
     *
     * @param image Image to have alpha channel stripped
     * @return JPG ready image
     */
    public static BufferedImage stripAlpha(BufferedImage image) {
        BufferedImage copy = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
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

            JSONObject response = new JSONObject(
                    new NetworkRequest(BASE_API_URL + "image", false)
                            .post(body, getHeaders(), false).body
            );
            return response.getJSONObject(DATA_KEY).getString(LINK_KEY);
        }
        catch(Exception e) {
            return null;
        }
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
        return new JSONObject(response.body).getJSONObject(DATA_KEY).getString("url");
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

            String response = new NetworkRequest(BASE_API_URL + "image", false)
                    .post(body, getHeaders(), false).body;
            return new JSONObject(response).getJSONObject(DATA_KEY).getString(LINK_KEY);
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

            String response = new NetworkRequest(BASE_API_URL + "upload", false)
                    .post(body, getHeaders(), false).body;

            return BASE_URL + new JSONObject(response).getJSONObject(DATA_KEY).getString("id");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the authentication headers required to make requests to the API
     *
     * @return Map of authentication headers
     */
    private static HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "CLIENT-ID " + Secret.IMGUR_CLIENT_ID);
        return headers;
    }

    /**
     * Get a list of image URLs from the given URL to an Imgur album
     *
     * @param albumUrl URL to Imgur album
     * @return List of image URLs in the album or null (if an error occurs)
     */
    @Nullable
    public static ArrayList<String> getAlbumImagesByUrl(String albumUrl) {
        if(!isAlbumUrl(albumUrl)) {
            return null;
        }
        try {
            ArrayList<String> images = new ArrayList<>();

            // Remove trailing slash
            if(albumUrl.endsWith("/")) {
                albumUrl = albumUrl.substring(0, albumUrl.length() - 1);
            }
            String[] urlArgs = albumUrl.split("/");
            final String hash = urlArgs[urlArgs.length - 1];

            NetworkResponse response = new NetworkRequest(BASE_API_URL + "album/" + hash, false)
                    .get(getHeaders());

            JSONArray imageList = new JSONObject(response.body).getJSONObject(DATA_KEY).getJSONArray("images");

            for(int i = 0; i < imageList.length(); i++) {
                images.add(imageList.getJSONObject(i).getString(LINK_KEY));
            }
            return images;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Check if the given URL is an Imgur album URL
     *
     * @param url URL to check
     * @return URL is an album URL
     */
    public static boolean isAlbumUrl(String url) {
        return url.matches(BASE_API_URL + "a/.+/?");
    }

    /**
     * Check if the given URL is an Imgur image/gif URL
     *
     * @param url URL to check
     * @return URL is an image/gif URL
     */
    public static boolean isImageUrl(String url) {
        return url.matches(IMGUR_DOMAIN_REGEX + ".+.(gifv|png|jpeg|jpg)/?");
    }
}
