package Bot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Reading resources
 */
public class ResourceHandler {
    public static String
            RUNESCAPE_BASE_PATH = "/Runescape/",
            OSRS_BASE_PATH = RUNESCAPE_BASE_PATH + "OSRS/",
            RS3_BASE_PATH = RUNESCAPE_BASE_PATH + "RS3/";

    /**
     * Get an input stream from a given file path
     *
     * @param path Path to file relative to resource folder e.g - /Commands/meme_command.json
     * @return Input stream of file
     */
    public InputStream getResourceFileAsStream(String path) {
        InputStream stream = this.getClass().getResourceAsStream(path);
        if(stream == null) {
            System.out.println("File not found: " + path);
            return null;
        }
        return stream;
    }

    /**
     * Get an image resource
     *
     * @param path Path to file relative to resource image e.g - /COD/CW/Templates/Countdown/1.png
     * @return Input stream of file
     */
    public BufferedImage getImageResource(String path) {
        try {
            System.out.println("Reading image: " + path);
            return ImageIO.read(getResourceFileAsStream(path));
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the String contents of a given resource file
     *
     * @param path Path to file relative to resource folder e.g - /Commands/meme_command.json
     * @return String File contents
     */
    public String getResourceFileAsString(String path) {
        InputStream stream = getResourceFileAsStream(path);
        if(stream == null) {
            return null;
        }
        return getStreamAsString(stream);
    }

    /**
     * Get an InputStream as a String
     *
     * @param is Input Stream of file
     * @return File contents String
     */
    public String getStreamAsString(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            is.close();
            reader.close();
            return result;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
