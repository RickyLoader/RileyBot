package LOL.Blitz;


import Command.Structure.CachedImage;

import java.awt.image.BufferedImage;

/**
 * Champion data
 */
public class Champion {
    private final String name, id;
    private final Ability[] abilities;
    private final CachedImage championImage;

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
        this.championImage = new CachedImage("src/main/resources/LOL/Champions/Thumbnails/" + key + ".png");
        this.abilities = abilities;
    }

    /**
     * Get the champion image
     *
     * @return Champion image
     */
    public BufferedImage getChampionImage() {
        return championImage.getImage();
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