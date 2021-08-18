package Bot;

import java.awt.*;

/**
 * Register fonts
 */
public class FontManager {
    public static Font
            MODERN_WARFARE_FONT,
            BLITZ_FONT,
            COLD_WAR_FONT,
            OSRS_FONT,
            OSRS_BANK_FONT,
            WISE_OLD_MAN_FONT,
            RS3_FONT,
            DEAL_OR_NO_DEAL_FONT,
            WHITEBOARD_FONT,
            MINECRAFT_FONT,
            LEAGUE_FONT;

    /**
     * Initialise the fonts in the graphics environment for use anywhere
     */
    public static void initialiseFonts() {
        MODERN_WARFARE_FONT = registerFont("/COD/MW/ModernWarfare.otf");
        COLD_WAR_FONT = registerFont("/COD/CW/ColdWar.ttf");
        OSRS_FONT = registerFont(ResourceHandler.OSRS_BASE_PATH + "osrs.ttf");
        OSRS_BANK_FONT = registerFont(ResourceHandler.OSRS_BASE_PATH + "osrs_bank.ttf");
        WISE_OLD_MAN_FONT = registerFont(ResourceHandler.OSRS_BASE_PATH + "wise_old_man.ttf");
        RS3_FONT = registerFont(ResourceHandler.RS3_BASE_PATH + "rs3.ttf");
        DEAL_OR_NO_DEAL_FONT = registerFont("/DOND/dond.ttf");
        BLITZ_FONT = registerFont(ResourceHandler.LEAGUE_BASE_PATH + "blitz_font.ttf");
        LEAGUE_FONT = registerFont(ResourceHandler.LEAGUE_BASE_PATH + "league_font.otf");
        MINECRAFT_FONT = registerFont(ResourceHandler.MINECRAFT_BASE_PATH + "font.ttf");
        WHITEBOARD_FONT = registerFont(ResourceHandler.COVID_BASE_PATH + "marker.ttf");
    }

    /**
     * Register the given font with the graphics environment
     *
     * @param fontPath Path to font relative to resource directory
     */
    public static Font registerFont(String fontPath) {
        System.out.println("Registering font:" + fontPath);
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font gameFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new ResourceHandler().getResourceFileAsStream(fontPath)
            );
            ge.registerFont(gameFont);
            return gameFont;
        }
        catch(Exception e) {
            return null;
        }
    }
}
