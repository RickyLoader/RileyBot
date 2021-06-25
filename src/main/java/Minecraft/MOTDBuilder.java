package Minecraft;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;

import java.awt.*;
import java.awt.image.BufferedImage;

import static Command.Structure.ImageBuilder.copyImage;

/**
 * Build an MOTD image (server browser image) for a Minecraft server
 */
public class MOTDBuilder {
    private static MOTDBuilder instance = null;
    private static final int
            BORDER = 4, // Border around background image
            LINE_SPACING = 20, // Lines are drawn at every 20 pixels after the border
            SERVER_ICON_SIZE = 64; // Server icons are 64x64

    public static final int MAX_LINES = 2;
    private final BufferedImage background, offlineIcon, onlineIcon;

    /**
     * Initialise the required images & templates for building an MOTD image
     */
    private MOTDBuilder() {
        ResourceHandler resourceHandler = new ResourceHandler();
        String path = ResourceHandler.MINECRAFT_BASE_PATH;
        this.background = resourceHandler.getImageResource(path + "motd_template.png");
        this.offlineIcon = resourceHandler.getImageResource(path + "offline_icon.png");
        this.onlineIcon = resourceHandler.getImageResource(path + "online_icon.png");
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

        g.setColor(Color.WHITE);

        // Draw server name + version e.g "[version] name
        final String serverName = "[" + server.getVersion() + "] " + server.getMapName();
        g.drawString(serverName, textX, titleRowY);

        g.setColor(Color.decode("#b3adad")); // Grey

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
                String line = motd[i];
                g.drawString(line, textX, motdY);
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
}
