package LOL.Blitz;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * League of Legends item
 */
public class Item {
    private final String name;
    private final BufferedImage itemImage;

    /**
     * Create an item
     *
     * @param name Item name - Boots of speed
     * @param id   Item id - 3044
     */
    public Item(String name, String id) {
        this.itemImage = new ResourceHandler().getImageResource("/LOL/Items/" + id + ".png");
        this.name = name;
    }

    /**
     * Get the item image
     *
     * @return Item image
     */
    public BufferedImage getItemImage() {
        return itemImage;
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