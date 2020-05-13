package OSRS.Stats;

import Network.ApiRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class Hiscores {

    public Hiscores() {
        registerFont();
    }

    /**
     * Look a player up on the OSRS hiscores and return an embedded message displaying their skills
     *
     * @param name Player name
     * @return Embedded message displaying skills
     */
    public File lookupPlayer(String name) {
        String data = fetchPlayerData(name);
        if(data == null) {
            return null;
        }
        String[] cunt = data.split(",");
        for(int i = 0;i<cunt.length;i++){
            System.out.println(i+"-----"+cunt[i]);
        }
        return buildSkillImage(name, orderSkills(data.split(",")));
    }

    /**
     * Fetch the CSV from the hiscores API
     *
     * @param name Player name
     * @return CSV data from API
     */
    private String fetchPlayerData(String name) {
        String url = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + name;
        return ApiRequest.executeQuery(url, "GET", null, false);
    }

    /**
     * Build an image displaying player skills
     *
     * @param skills Skills to display on message
     * @return Embedded message displaying player skills
     */
    private File buildSkillImage(String name, String[] skills) {
        File playerStats = null;
        try {
            BufferedImage image = ImageIO.read(new URL("https://i.imgur.com/FzKqSZC.png"));
            Graphics g = image.getGraphics();
            g.setFont(new Font("RuneScape Chat '07", Font.PLAIN, 13));

            // First skill location
            int x = 40;
            int y = 23;

            for(int i = 0; i < skills.length; i++) {
                String level = skills[i];

                // total level
                if(i == skills.length - 1) {
                    g.setColor(Color.YELLOW);
                    g.drawString(level, x - 14, y + 12);
                    continue;
                }

                g.setColor(Color.BLACK); // shadow

                g.drawString(level, x + 1, y + 1); // top
                g.drawString(level, x + 12 + 1, y + 12 + 1); // bottom

                g.setColor(Color.YELLOW); // skill

                g.drawString(level, x, y); // top
                g.drawString(level, x + 12, y + 12); // bottom

                // Currently 3rd column, reset back to first column and go down a row
                if((i + 1) % 3 == 0) {
                    x = 40;
                    y = (y + 32);
                }
                // Move to next column
                else {
                    x = (x + 63);
                }
            }

            g.dispose();
            playerStats = new File("src/main/resources/playerStats.png");
            ImageIO.write(image, "png", playerStats);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return playerStats;
    }

    /**
     * Register the OSRS font with the graphics environment
     */
    private void registerFont() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/osrs.ttf")));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sort the CSV from the API in to the in-game display order
     *
     * @param csv Unsorted CSV from API
     * @return Sorted CSV
     */
    private String[] orderSkills(String[] csv) {
        return new String[]{
                csv[3],
                csv[9],
                csv[31],
                csv[7],
                csv[35],
                csv[29],
                csv[5],
                csv[33],
                csv[23],
                csv[11],
                csv[37],
                csv[17],
                csv[13],
                csv[27],
                csv[25],
                csv[15],
                csv[21],
                csv[19],
                csv[43],
                csv[39],
                csv[41],
                csv[47],
                csv[45],
                csv[1]
        };
    }
}
