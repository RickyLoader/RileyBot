package Riot.LOL.Blitz;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * Champion data
 */
public class Champion {
    private final String name, id;
    private final Ability[] abilities;
    private final BufferedImage championImage;
    public static final String BASE_PATH = ResourceHandler.LEAGUE_BASE_PATH + "Champions/";

    /**
     * Create a champion
     *
     * @param name      Champion name - Aurelion Sol
     * @param id        Champion id - 136
     * @param key       Champion name key - AurelionSol
     * @param abilities Champion abilities
     */
    public Champion(String name, String id, String key, Ability[] abilities) {
        this.name = name;
        this.id = id;

        String thumbnailPath = BASE_PATH + "Thumbnails/";
        ResourceHandler resourceHandler = new ResourceHandler();
        BufferedImage image = resourceHandler.getImageResource(thumbnailPath + key + ".png");
        this.championImage = (image == null) ? resourceHandler.getImageResource(thumbnailPath + "Default.png") : image;
        this.abilities = abilities;
    }

    /**
     * Get the champion image
     *
     * @return Champion image
     */
    public BufferedImage getChampionImage() {
        return championImage;
    }

    /**
     * Get the champion id
     *
     * @return Champion id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the ability by index
     * 0 = Q
     * 1 = W
     * 2 = E
     * 3 = R
     *
     * @param index Index of ability
     * @return Champion ability
     */
    public Ability getAbility(int index) {
        return abilities[index];
    }

    /**
     * Get the champion name
     *
     * @return Champion name
     */
    public String getName() {
        return name;
    }
}