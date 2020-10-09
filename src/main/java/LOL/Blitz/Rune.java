package LOL.Blitz;


import Command.Structure.CachedImage;

import java.awt.image.BufferedImage;

/**
 * Summoner rune
 */
public class Rune {
    private final int id;
    private final String name;
    private final boolean keyRune;
    private final CachedImage image;

    /**
     * Create a rune
     *
     * @param id      Rune id
     * @param name    Rune name
     * @param image   Rune image name - /perk-images/Styles/Sorcery/ManaflowBand/ManaflowBand.png
     * @param keyRune Rune is a key rune
     */
    public Rune(int id, String name, String image, boolean keyRune) {
        String filename = image.contains("/") ? (image.substring(image.lastIndexOf("/") + 1)) : image;
        this.image = new CachedImage("src/main/resources/LOL/Summoner/Runes/" + filename);
        this.id = id;
        this.name = name;
        this.keyRune = keyRune;
    }

    /**
     * Rune is a key stone rune
     *
     * @return Rune is a key stone rune
     */
    public boolean isKeyRune() {
        return keyRune;
    }

    /**
     * Get the rune image
     *
     * @return Rune image
     */
    public BufferedImage getImage() {
        return image.getImage();
    }

    /**
     * Get rune name
     *
     * @return Rune name
     */
    public String getName() {
        return name;
    }

    /**
     * Get rune id
     *
     * @return Rune id
     */
    public int getId() {
        return id;
    }
}