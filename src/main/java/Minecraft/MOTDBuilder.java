package Minecraft;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static Command.Structure.ImageBuilder.copyImage;

/**
 * Build an MOTD image (server browser image) for a Minecraft server
 */
public class MOTDBuilder {
    private static MOTDBuilder instance = null;
    private static final String
            MODIFIER = "§", // Prefix before a MOTD text modifier
            RESET = "§r", // Reset colour text modifier
            MODIFIER_REGEX = MODIFIER + "[a-z0-9]"; // Regex to match a modifier
    private static final int
            BORDER = 4, // Border around background image
            LINE_SPACING = 20, // Lines are drawn at every 20 pixels after the border
            SERVER_ICON_SIZE = 64; // Server icons are 64x64
    private static final Color GREY = Color.decode("#AAAAAA");
    public static final int MAX_LINES = 2;
    private final BufferedImage background, offlineIcon, onlineIcon;
    private final HashMap<String, Color> colours;

    /**
     * Initialise the required images & templates for building an MOTD image
     */
    private MOTDBuilder() {
        ResourceHandler resourceHandler = new ResourceHandler();
        String path = ResourceHandler.MINECRAFT_BASE_PATH;
        this.background = resourceHandler.getImageResource(path + "motd_template.png");
        this.offlineIcon = resourceHandler.getImageResource(path + "offline_icon.png");
        this.onlineIcon = resourceHandler.getImageResource(path + "online_icon.png");
        this.colours = buildColourMap();
    }

    /**
     * Build a map of text modifier -> colour.
     * E.g "§c" -> red
     *
     * @return Map of text modifier -> colour
     * @see <a href="https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes">For colour codes</a>
     */
    private HashMap<String, Color> buildColourMap() {
        HashMap<String, Color> colours = new HashMap<>();
        colours.put(MODIFIER + "0", Color.BLACK);
        colours.put(MODIFIER + "1", Color.decode("#0000AA")); // Dark blue
        colours.put(MODIFIER + "2", Color.decode("#00AA00")); // Dark green
        colours.put(MODIFIER + "3", Color.decode("#00AAAA")); // Dark aqua
        colours.put(MODIFIER + "4", Color.decode("#AA0000")); // Dark red
        colours.put(MODIFIER + "5", Color.decode("#AA00AA")); // Dark purple
        colours.put(MODIFIER + "6", Color.decode("#FFAA00")); // Gold
        colours.put(MODIFIER + "7", GREY);
        colours.put(MODIFIER + "8", Color.decode("#555555")); // Dark grey
        colours.put(MODIFIER + "9", Color.decode("#5555FF")); // Blue
        colours.put(MODIFIER + "a", Color.decode("#55FF55")); // Green
        colours.put(MODIFIER + "b", Color.decode("#55FFFF")); // Aqua
        colours.put(MODIFIER + "c", Color.decode("#FF5555")); // Red
        colours.put(MODIFIER + "d", Color.decode("#FF55FF")); // Light purple
        colours.put(MODIFIER + "e", Color.decode("#FFFF55")); // Yellow
        colours.put(MODIFIER + "f", Color.WHITE);
        colours.put(MODIFIER + "g", Color.decode("#DDD605")); // Gold
        return colours;
    }

    /**
     * Get an instance of the MOTDBuilder class
     *
     * @return Instance
     */
    public static MOTDBuilder getInstance() {
        if(instance == null) {
            instance = new MOTDBuilder();
        }
        return instance;
    }

    /**
     * Build an MOTD server browser image for the given Minecraft server.
     *
     * @param server Server to build image for
     * @return Server browser image
     */
    public BufferedImage buildImage(MinecraftServer server) {
        BufferedImage background = copyImage(this.background);
        Graphics g = background.getGraphics();
        g.setFont(FontManager.MINECRAFT_FONT.deriveFont(16f));

        // Draw server icon
        BufferedImage icon = EmbedHelper.downloadImage(server.getIconUrl());

        // May fail to download, all server icons are 64x64
        if(icon != null) {
            g.drawImage(icon, BORDER, BORDER, null);
        }

        final int textX = BORDER + SERVER_ICON_SIZE + BORDER;
        final int titleRowY = BORDER + LINE_SPACING;

        // Draw server name + version e.g "[version] name"
        final String serverName = "[" + server.getVersion() + "] " + server.getMapName();
        drawFormattedText(g, serverName, textX, titleRowY, Color.WHITE);

        g.setColor(GREY);

        // Draw connection icon
        final BufferedImage connectionIcon = server.isOnline() ? onlineIcon : offlineIcon;
        final int connectionX = background.getWidth() - BORDER - connectionIcon.getWidth();
        g.drawImage(connectionIcon, connectionX, titleRowY - connectionIcon.getHeight(), null);

        // Draw connected players
        final String players = server.getCurrentPlayerCountString() + "/" + server.getMaxPlayersString();
        g.drawString(players, connectionX - BORDER - g.getFontMetrics().stringWidth(players), titleRowY);
        int motdY = titleRowY + LINE_SPACING;

        // Draw message of the day if the server data is available
        if(server.hasData()) {
            final String[] motd = server.getMotd();

            // Cap MOTD lines at the max lines
            for(int i = 0; i < Math.min(motd.length, MAX_LINES); i++) {
                drawFormattedText(g, motd[i], textX, motdY, GREY);
                motdY += LINE_SPACING;
            }
        }
        // Draw connection error message in place of MOTD when server data is unavailable
        else {
            g.setColor(Color.RED);
            g.drawString("Can't reach server", textX, motdY);
        }

        g.dispose();
        return background;
    }

    /**
     * Draw text on to the given graphics instance.
     * Strip and use any colour modifiers in the text.
     *
     * @param g             Graphics instance to draw on
     * @param text          Text to draw
     * @param x             X co-ordinate for text
     * @param y             Y co-ordinate for text
     * @param defaultColour Default colour to use
     */
    private void drawFormattedText(Graphics g, String text, int x, int y, Color defaultColour) {
        final String splitRegex = "(?<=" + MODIFIER_REGEX + ")|(?=" + MODIFIER_REGEX + ")";

        // "§1text1 §2text2" -> [§1, text1, §2, text2]
        String[] textParts = text.split(splitRegex);
        g.setColor(defaultColour);

        for(String part : textParts) {
            if(part.matches(MODIFIER_REGEX)) {
                Color colour = part.matches(RESET) ? defaultColour : colours.get(part);

                // May be a non colour related modifier
                if(colour != null) {
                    g.setColor(colour);
                }
                continue;
            }
            g.drawString(part, x, y);
            x += g.getFontMetrics().stringWidth(part);
        }
    }
}
