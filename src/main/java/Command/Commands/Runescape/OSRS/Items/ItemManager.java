package Command.Commands.Runescape.OSRS.Items;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Hold information on OSRS items
 */
public class ItemManager {

    private final HashMap<String, Item> items;
    private final String attackBonus, defenceBonus, otherBonus, slash, stab, crush, magic, ranged, strength, rangedStrength, magicStrength, prayer;

    public ItemManager(EmoteHelper emoteHelper) {
        this.items = new HashMap<>();
        this.attackBonus = EmoteHelper.formatEmote(emoteHelper.getAttack());
        this.defenceBonus = EmoteHelper.formatEmote(emoteHelper.getDefence());
        this.otherBonus = EmoteHelper.formatEmote(emoteHelper.getOtherBonus());
        this.slash = EmoteHelper.formatEmote(emoteHelper.getSlash());
        this.stab = EmoteHelper.formatEmote(emoteHelper.getStab());
        this.crush = EmoteHelper.formatEmote(emoteHelper.getCrush());
        this.magic = EmoteHelper.formatEmote(emoteHelper.getMagic());
        this.ranged = EmoteHelper.formatEmote(emoteHelper.getRanged());
        this.strength = EmoteHelper.formatEmote(emoteHelper.getStrength());
        this.rangedStrength = EmoteHelper.formatEmote(emoteHelper.getRangedStrength());
        this.magicStrength = EmoteHelper.formatEmote(emoteHelper.getMagicDamage());
        this.prayer = EmoteHelper.formatEmote(emoteHelper.getPrayer());
    }

    /**
     * Search for an item
     *
     * @param name Item name
     * @return Item or null
     */
    public Item searchItem(String name) {
        if(items.containsKey(name)) {
            return items.get(name);
        }
        Item item = fetchItem(name);
        if(item != null) {
            items.put(name, item);
        }
        return item;
    }

    /**
     * Build a message embed displaying the item
     *
     * @param channel Channel to send message to
     * @param i       Item to show
     * @param footer  Footer to display
     */
    public void sendItemEmbed(MessageChannel channel, Item i, String footer) {
        String osrsIcon = "https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(EmbedHelper.getGreen());
        builder.setThumbnail(osrsIcon);
        builder.setTitle(i.getName());
        builder.setDescription(i.getDescription());
        builder.setImage("attachment://item.png");
        builder.setFooter("Try: " + footer, osrsIcon);
        if(i.isEquipable()) {
            addStandardBonusFields(builder, attackBonus + " Attack bonuses", i.getAttackBonus());
            addStandardBonusFields(builder, defenceBonus + " Defence bonuses", i.getDefenceBonus());
            addOtherBonusFields(builder, i.getOtherBonus());

        }
        channel.sendMessage(builder.build()).addFile(toByeArray(i.getImage()), "item.png").queue();
    }

    /**
     * Add fields to display an equipable items attack/defence bonuses
     *
     * @param builder EmbedBuilder
     * @param title   Title to display above bonuses
     * @param bonus   Bonuses
     */
    private void addStandardBonusFields(EmbedBuilder builder, String title, StandardBonus bonus) {
        builder.addField(title, EmbedHelper.getBlankChar(), false);
        builder.addField(stab, bonus.getStab(), true);
        builder.addField(slash, bonus.getSlash(), true);
        builder.addField(crush, bonus.getCrush(), true);
        builder.addField(magic, bonus.getMagic(), true);
        builder.addField(ranged, bonus.getRanged(), true);
        builder.addBlankField(true);
    }

    /**
     * Add fields to display an equipable items other bonuses
     *
     * @param builder EmbedBuilder
     * @param bonus   Bonuses
     */
    private void addOtherBonusFields(EmbedBuilder builder, OtherBonus bonus) {
        builder.addField(otherBonus + " Other bonuses", EmbedHelper.getBlankChar(), false);
        builder.addField(strength, bonus.getStrength(), true);
        builder.addField(rangedStrength, bonus.getRanged(), true);
        builder.addField(magicStrength, bonus.getMagic(), true);
        builder.addField(prayer, bonus.getPrayer(), true);
        builder.addBlankField(true);
        builder.addBlankField(true);
    }

    /**
     * Parse the item from the OSRS wiki
     *
     * @param name Item name
     * @return Item object
     */
    private Item fetchItem(String name) {
        long start = System.currentTimeMillis();
        String url = "https://oldschool.runescape.wiki/w/" + name.replaceAll(" ", "_");
        System.out.println(url);
        try {
            Document document = Jsoup.connect(url).get();
            if(document.select("a[title=\"Category:Items\"]").isEmpty()) {
                return null;
            }
            System.out.println("\n\nFetching wiki data: " + (System.currentTimeMillis() - start) + " ms");
            return parseItem(document, url);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse the HTML document and retrieve item information
     *
     * @param document OSRS wiki page of item
     * @param url      OSRS wiki page URL
     * @return Item object
     */
    private Item parseItem(Document document, String url) {
        long start = System.currentTimeMillis();
        Elements info = document.selectFirst(".plainlinks tbody").children();
        String examine = null, exchange = null, highAlch = null;
        boolean tradeable = false, equipable = false;

        for(Element element : info) {
            Elements e = element.children();
            switch(e.get(0).text()) {
                case "Tradeable":
                    tradeable = e.get(1).text().equals("Yes");
                    break;
                case "High alch":
                case "Alchemy":
                    highAlch = e.get(1).text();
                    break;
                case "Examine":
                    examine = e.get(1).text();
                    break;
                case "Exchange":
                    exchange = e.get(1).child(0).text();
                    break;
                case "Equipable":
                    equipable = e.get(1).text().equals("Yes");
                    break;
            }
        }

        Item item = new Item(
                document.getElementById("firstHeading").text(),
                document.getElementsByTag("p").get(0).text(),
                examine,
                url,
                highAlch,
                exchange,
                readImage(document.selectFirst(".floatleft").child(0).child(0).absUrl("src")),
                tradeable,
                equipable
        );

        if(equipable) {
            Elements bonuses = document.select(".plainlinks tbody").get(1).children();
            item.setAttackBonus(parseStandardBonus(bonuses.get(3)));
            item.setDefenceBonus(parseStandardBonus(bonuses.get(8)));
            item.setOtherBonus(parseOtherBonus(bonuses.get(13)));
            // Weapon
            if(bonuses.size() >= 17) {
                String speedImageURL = bonuses.get(17).child(0).child(0).child(0).absUrl("src");
                item.setImage(buildWeaponImage(item.getImage(), readImage(speedImageURL)));
            }
        }
        System.out.println("Parsing item: " + (System.currentTimeMillis() - start) + " ms");
        return item;
    }

    /**
     * Add the weapon speed to the weapon image
     *
     * @return Weapon image with speed
     */
    private BufferedImage buildWeaponImage(BufferedImage weapon, BufferedImage speed) {
        try {
            long start = System.currentTimeMillis();
            BufferedImage canvas = new BufferedImage(
                    Math.max(speed.getWidth(), weapon.getWidth()),
                    weapon.getHeight() + speed.getHeight() + 10,
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics g = canvas.getGraphics();
            g.drawImage(weapon, (canvas.getWidth() / 2) - (weapon.getWidth() / 2), 0, null);
            g.drawImage(speed, (canvas.getWidth() / 2) - (speed.getWidth() / 2), canvas.getHeight() - speed.getHeight(), null);
            g.dispose();
            System.out.println("Building weapon image: " + (System.currentTimeMillis() - start) + " ms");
            return canvas;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert BufferedImage to byte array
     *
     * @param image Buffered image
     * @return byte array of image
     */
    private byte[] toByeArray(BufferedImage image) {
        try {
            long start = System.currentTimeMillis();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            outputStream.close();
            System.out.println("To byte array: " + (System.currentTimeMillis() - start) + " ms");
            return outputStream.toByteArray();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read an image in as a BufferedImage
     *
     * @param url URL to image
     * @return BufferedImage
     */
    private BufferedImage readImage(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
            return ImageIO.read(connection.getInputStream());
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse the HTML element in to a standard bonus object
     *
     * @return Standard bonus object
     */
    private StandardBonus parseStandardBonus(Element e) {
        return new StandardBonus(
                e.child(0).text(),
                e.child(1).text(),
                e.child(2).text(),
                e.child(3).text(),
                e.child(4).text()
        );
    }

    /**
     * Parse the HTML element in to a other bonus object
     *
     * @return Other bonus object
     */
    private OtherBonus parseOtherBonus(Element e) {
        return new OtherBonus(
                e.child(0).text(),
                e.child(1).text(),
                e.child(2).text(),
                e.child(3).text()
        );
    }

    /**
     * Wrap an OSRS item
     */
    public static class Item {
        private final String name, description, url, highAlch, exchange, examine;
        private final boolean tradeable, equipable;
        private StandardBonus attackBonus, defenceBonus;
        private OtherBonus otherBonus;
        private BufferedImage image;

        /**
         * Create an item
         *
         * @param name        Item name
         * @param description Item description
         * @param examine     Item examine text
         * @param url         OSRS wiki URL
         * @param highAlch    High alchemy price
         * @param exchange    Grand exchange price
         * @param image       Item image URL
         * @param tradeable   Item tradeable
         * @param equipable   Item is equipable
         */
        public Item(String name, String description, String examine, String url, String highAlch, String exchange, BufferedImage image, boolean tradeable, boolean equipable) {
            this.name = name;
            this.description = description;
            this.url = url;
            this.highAlch = highAlch;
            this.exchange = exchange;
            this.image = image;
            this.examine = examine;
            this.tradeable = tradeable;
            this.equipable = equipable;
        }

        /**
         * Set the item image
         *
         * @param image Image
         */
        public void setImage(BufferedImage image) {
            this.image = image;
        }

        /**
         * Get other bonus (ranged strength, prayer...)
         *
         * @return Other bonus
         */
        public OtherBonus getOtherBonus() {
            return otherBonus;
        }

        /**
         * Get attack bonus
         *
         * @return Attack bonus
         */
        public StandardBonus getAttackBonus() {
            return attackBonus;
        }

        /**
         * Get defence bonus
         *
         * @return Defence bonus
         */
        public StandardBonus getDefenceBonus() {
            return defenceBonus;
        }

        /**
         * Set attack bonus
         *
         * @param attackBonus Attack bonus
         */
        public void setAttackBonus(StandardBonus attackBonus) {
            this.attackBonus = attackBonus;
        }

        /**
         * Set defence bonus
         *
         * @param defenceBonus Defence bonus
         */
        public void setDefenceBonus(StandardBonus defenceBonus) {
            this.defenceBonus = defenceBonus;
        }

        /**
         * Set other bonus
         *
         * @param otherBonus Other bonus
         */
        public void setOtherBonus(OtherBonus otherBonus) {
            this.otherBonus = otherBonus;
        }

        /**
         * Is item equipable
         *
         * @return Item equipable
         */
        public boolean isEquipable() {
            return equipable;
        }

        /**
         * Is item tradeable
         *
         * @return Item tradeable
         */
        public boolean isTradeable() {
            return tradeable;
        }

        /**
         * Get examine text of item
         *
         * @return Examine text
         */
        public String getExamine() {
            return examine;
        }

        /**
         * Get item image
         *
         * @return Image
         */
        public BufferedImage getImage() {
            return image;
        }

        /**
         * Get item name
         *
         * @return Item name
         */
        public String getName() {
            return name;
        }

        /**
         * Get item description
         *
         * @return Item description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get item grand exchange price
         *
         * @return Grand exchange price
         */
        public String getExchange() {
            return exchange;
        }

        /**
         * Get item OSRS wiki URL
         *
         * @return OSRS wiki URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get item high alchemy price
         *
         * @return High alch price
         */
        public String getHighAlch() {
            return highAlch;
        }

        /**
         * Return if item is tradeable
         *
         * @return Tradeable
         */
        public boolean tradeable() {
            return tradeable;
        }
    }

    /**
     * Hold Attack/Defence bonuses
     */
    public static class StandardBonus {
        private final String stab, slash, crush, magic, ranged;

        /**
         * Create an Attack/Defence bonus
         *
         * @param stab   Stab bonus
         * @param slash  Slash bonus
         * @param crush  Crush bonus
         * @param magic  Magic bonus
         * @param ranged Ranged bonus
         */
        public StandardBonus(String stab, String slash, String crush, String magic, String ranged) {
            this.stab = stab;
            this.slash = slash;
            this.crush = crush;
            this.magic = magic;
            this.ranged = ranged;
        }

        /**
         * Get the stab bonus
         *
         * @return Stab bonus
         */
        public String getStab() {
            return stab;
        }

        /**
         * Get the slash bonus
         *
         * @return Slash bonus
         */
        public String getSlash() {
            return slash;
        }

        /**
         * Get the crush bonus
         *
         * @return Crush bonus
         */
        public String getCrush() {
            return crush;
        }

        /**
         * Get the magic bonus
         *
         * @return Magic bonus
         */
        public String getMagic() {
            return magic;
        }

        /**
         * Get the ranged bonus
         *
         * @return Ranged bonus
         */
        public String getRanged() {
            return ranged;
        }
    }

    /**
     * Hold other bonus
     */
    public static class OtherBonus {
        private final String strength, ranged, magic, prayer;

        /**
         * Create an other bonus
         *
         * @param strength Strength bonus
         * @param ranged   Ranged strength bonus
         * @param magic    Magic damage bonus
         * @param prayer   Prayer bonus
         */
        public OtherBonus(String strength, String ranged, String magic, String prayer) {
            this.strength = strength;
            this.ranged = ranged;
            this.magic = magic;
            this.prayer = prayer;
        }

        /**
         * Get the prayer bonus
         *
         * @return Prayer bonus
         */
        public String getPrayer() {
            return prayer;
        }

        /**
         * Get the magic damage bonus
         *
         * @return Magic damage bonus
         */
        public String getMagic() {
            return magic;
        }

        /**
         * Get the ranged strength bonus
         *
         * @return Ranged strength bonus
         */
        public String getRanged() {
            return ranged;
        }

        /**
         * Get the strength bonus
         *
         * @return strength bonus
         */
        public String getStrength() {
            return strength;
        }
    }
}
