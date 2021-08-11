package Riot.LOL.Blitz;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * League of Legends item
 */
public class Item {
    private final String name;
    private final BufferedImage itemImage;
    public final static String IMAGE_PATH = ResourceHandler.LEAGUE_BASE_PATH + "Items/";

    /**
     * Create an item
     *
     * @param name Item name - Boots of speed
     * @param id   Item id - 3044
     */
    public Item(String name, String id) {
        this.itemImage = new ResourceHandler().getImageResource(getImagePath(id));
        this.name = name;
    }

    /**
     * Create an item with an existing image
     *
     * @param name      Item name
     * @param itemImage Item image
     */
    public Item(String name, BufferedImage itemImage) {
        this.name = name;
        this.itemImage = itemImage;
    }

    /**
     * Get the image path for the given item id
     *
     * @param id Item id
     * @return Image path for given item id
     */
    public static String getImagePath(String id) {
        return IMAGE_PATH + id + ".png";
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