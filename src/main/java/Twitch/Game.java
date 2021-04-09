package Twitch;

/**
 * Twitch.tv game
 */
public class Game {
    private final String name, id, thumbnail, url;

    /**
     * Create a game
     *
     * @param name      Name
     * @param id        Unique id
     * @param thumbnail Thumbnail URL
     */
    public Game(String name, String id, String thumbnail) {
        this.name = name;
        this.id = id;
        this.thumbnail = thumbnail.replace("-{width}x{height}", "")
                .replace("/./", "/");
        this.url = TwitchTV.TWITCH_URL + "directory/game/" + name.replace(" ", "%20");
    }

    /**
     * Get the URL to the game category on Twitch
     *
     * @return URL to game category
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the thumbnail URL of the game art
     *
     * @return Game art thumbnail URL
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Get the unique ID of the game
     *
     * @return Unique game ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the name of the game
     *
     * @return Game name
     */
    public String getName() {
        return name;
    }
}
