package Bot;

/**
 * DiscordImage.java Object for holding information about an image. Used in LINK and RANDOM type commands.
 *
 * @author Ricky Loader
 * @version 5000.0
 */

public class DiscordImage{

    // URL to the image
    private String image;

    public DiscordImage(String image){
        this.image = image;
    }

    public String getImage(){
        return image;
    }
}
