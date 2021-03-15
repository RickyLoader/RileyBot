package Runescape.OSRS.GE;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * OSRS tradeable item
 */
public class Item {
    private final int id, highAlch, lowAlch, buyLimit;
    private final String name, examine;
    private final boolean members;
    private final ItemImage itemImage;

    /**
     * Create a tradeable OSRS item
     *
     * @param id        Unique id of item
     * @param name      Item name
     * @param examine   Examine text
     * @param members   Member's item
     * @param highAlch  High alchemy price
     * @param lowAlch   Low alchemy price
     * @param buyLimit  Grand Exchange buy limit
     * @param itemImage Item image
     */
    public Item(int id, String name, String examine, boolean members, int highAlch, int lowAlch, int buyLimit, ItemImage itemImage) {
        this.id = id;
        this.name = name;
        this.examine = examine;
        this.members = members;
        this.highAlch = highAlch;
        this.lowAlch = lowAlch;
        this.buyLimit = buyLimit;
        this.itemImage = itemImage;
    }

    /**
     * Get the item image
     *
     * @return Item image
     */
    public ItemImage getItemImage() {
        return itemImage;
    }

    /**
     * Check if the item is a member's item
     *
     * @return Item is member's item
     */
    public boolean isMembers() {
        return members;
    }

    /**
     * Get the Grand Exchange buy limit
     *
     * @return Grand Exchange buy limit
     */
    public int getBuyLimit() {
        return buyLimit;
    }

    /**
     * Get the high alchemy price
     *
     * @return High alchemy price
     */
    public int getHighAlch() {
        return highAlch;
    }

    /**
     * Get the low alchemy price
     *
     * @return Low alchemy price
     */
    public int getLowAlch() {
        return lowAlch;
    }

    /**
     * Get the examine text
     *
     * @return Examine text
     */
    public String getExamine() {
        return examine;
    }

    /**
     * Get the name of the item
     *
     * @return Item name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique id of the item
     *
     * @return Unique id
     */
    public int getId() {
        return id;
    }

    /**
     * Check if the item has a buy limit
     *
     * @return Item has a buy limit
     */
    public boolean hasBuyLimit() {
        return buyLimit != -1;
    }

    /**
     * Check if the item is high alchable
     *
     * @return Item is high alchable
     */
    public boolean isHighAlchable() {
        return highAlch != -1;
    }

    /**
     * Check if the item is low alchable
     *
     * @return Item is low alchable
     */
    public boolean isLowAlchable() {
        return lowAlch != -1;
    }

    /**
     * Inventory image/High detail user-submitted image
     */
    public static class ItemImage {
        private final String inventoryImageUrl, highDetailImageUrl;

        /**
         * Create the item image
         *
         * @param filename Item filename
         * @param id       Item id
         */
        public ItemImage(String filename, int id) {
            this.inventoryImageUrl = generateImageUrl(filename, id);
            this.highDetailImageUrl = generateImageUrl(filename.replace(".png", "_detail.png"), id);
        }

        /**
         * Generate the item image URL from the filename.
         *
         * @param filename Filename to generate image URL for
         * @param id       item id
         * @return Image URL
         * @see <a href="https://bit.ly/2OoJVY7">How wiki generates image URL</a>
         */
        private String generateImageUrl(String filename, int id) {
            try {
                filename = filename.replace(" ", "_");
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(filename.getBytes(StandardCharsets.UTF_8));
                String hash = Hex.encodeHexString(md5.digest()).toLowerCase();
                String baseUrl = "https://oldschool.runescape.wiki/images/";
                filename = filename.replace("(", "%28")
                        .replace(")", "%29")
                        .replace("'", "%27");
                return baseUrl + hash.charAt(0) + "/" + hash.substring(0, 2) + "/" + filename;
            }
            catch(NoSuchAlgorithmException e) {
                return "https://static.runelite.net/cache/item/icon/" + id + ".png";
            }
        }

        /**
         * Get the URL to the high detail image of the item
         *
         * @return High detail image
         */
        public String getHighDetailImageUrl() {
            return highDetailImageUrl;
        }

        /**
         * Get the URL to the inventory image of the item
         *
         * @return Inventory image
         */
        public String getInventoryImageUrl() {
            return inventoryImageUrl;
        }
    }
}
