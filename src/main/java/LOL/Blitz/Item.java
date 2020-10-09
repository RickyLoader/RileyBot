package LOL.Blitz;

import Command.Structure.CachedImage;

import java.awt.image.BufferedImage;

/**
 * League of Legends item
 */
public class Item {
    private final String name;
    private final CachedImage itemImage;

    /**
     * Create an item
     *
     * @param name Item name - Boots of speed
     * @param id   Item id - 3044
     */
    public Item(String name, String id) {
        this.itemImage = new CachedImage("src/main/resources/LOL/Items/" + id + ".png");
        this.name = name;
    }

    /**
     * Get the item image
     *
     * @return Item image
     */
    public BufferedImage getItemImage() {
        return itemImage.getImage();
    }

    /**
     * Get the item name
     *
     * @return Item name
     */
    public String getName() {
        return name;
    }
}