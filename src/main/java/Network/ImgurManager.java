package Network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class ImgurManager {
    /**
     * Strip the alpha channel from the image before saving as JPG
     *
     * @param image Image to have alpha channel stripped
     * @return JPG ready image
     */
    private static BufferedImage stripAlpha(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = copy.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Convert a buffered image to base64 encoded png
     *
     * @param image BufferedImage to be encoded
     * @return String containing base64 encoded png
     */
    private static String toBase64(BufferedImage image) {
        try {
            if(image.getType() != BufferedImage.TYPE_INT_RGB) {
                image = stripAlpha(image);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(stripAlpha(image), "jpg", out);
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
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", image)
                .addFormDataPart("type", "URL")
                .build();

        String url = "https://api.imgur.com/3/image";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "CLIENT-ID " + Secret.getImgurClientID());

        String response = new NetworkRequest(url, false).post(body, headers);
        return new JSONObject(response).getJSONObject("data").getString("link");
    }

    /**
     * Upload a given BufferedImage to Imgur and return the link to the image
     *
     * @param image Buffered image to be uploaded
     * @return Link to image or null
     */
    public static String uploadImage(BufferedImage image) {
        try {
            String base64 = toBase64(image);
            if(base64 == null) {
                return null;
            }

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", base64)
                    .addFormDataPart("type", "base64")
                    .build();

            String url = "https://api.imgur.com/3/image";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "CLIENT-ID " + Secret.getImgurClientID());

            String response = new NetworkRequest(url, false).post(body, headers);
            return new JSONObject(response).getJSONObject("data").getString("link");
        }
        catch(Exception e) {
            return null;
        }
    }
}
