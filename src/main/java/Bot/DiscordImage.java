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

    // Text inside the image
    private String desc;

    public DiscordImage(String image, String desc){
        this.image = image;
        this.desc = desc;
    }

    public String getImage(){
        return image;
    }

    public String getDesc(){
        return desc;
    }
}
