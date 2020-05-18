package OSRS.Stats;

import Network.ApiRequest;
import com.objectplanet.image.PngEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class Hiscores {
    private String[] bossNames;
    private String resources = "src/main/resources/OSRS/";

    public Hiscores() {
        registerFont();
        bossNames = getBossNames();
    }

    /**
     * Look a player up on the OSRS hiscores and return an embedded message displaying their skills
     *
     * @param name Player name
     * @return Image displaying player skills
     */
    public File lookupPlayer(String name) {
        long start = System.currentTimeMillis();
        String data = fetchPlayerData(name);
        System.out.println("\nLookup: " + (System.currentTimeMillis() - start) + "ms\n");
        if(data == null) {
            return null;
        }
        List<Boss> bossKills = getBossKills(data.replace("\n", ",").split(","));
        String[] stats = orderSkills(data.split(","));
        return buildImage(name, stats, bossKills);
    }

    /**
     * Test reading from text file to see if delay comes from server or local side
     *
     * @return Image displaying player skills
     */
    public File localLookup() {
        String data = "";
        String curr;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(resources + "stats.txt"));
            while((curr = reader.readLine()) != null) {
                data += curr;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return buildImage("test", orderSkills(data.split(",")), null);
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
    private File buildImage(String name, String[] skills, List<Boss> bosses) {
        File playerStats = null;
        int scale = 5; //204*275
        int fontSize = 13 * scale;
        try {
            String imagePath = (resources + "Templates/stats_template.png");

            BufferedImage image = ImageIO.read(new File(imagePath));
            Graphics g = image.getGraphics();
            Font runeFont = new Font("RuneScape Chat '07", Font.PLAIN, fontSize);
            g.setFont(runeFont);

            // First skill location
            int x = 40 * scale;
            int y = 63 * scale;


            for(int i = 0; i < skills.length; i++) {
                String level = skills[i];

                // total level
                if(i == skills.length - 1) {
                    g.setColor(Color.YELLOW);
                    g.drawString(level, x - (14 * scale), y + (12 * scale));
                    continue;
                }

                g.setColor(Color.BLACK); // shadow

                g.drawString(level, x + (1 * scale), y + (1 * scale)); // top
                g.drawString(level, x + (12 + 1) * scale, y + (12 + 1) * scale); // bottom

                g.setColor(Color.YELLOW); // skill

                g.drawString(level, x, y); // top
                g.drawString(level, x + (12 * scale), y + (12 * scale)); // bottom

                // Currently 3rd column, reset back to first column and go down a row
                if((i + 1) % 3 == 0) {
                    x = (40 * scale);
                    y = (y + (32 * scale));
                }
                // Move to next column
                else {
                    x = x + (63 * scale);
                }
            }

            if(bosses.size() > 0) {
                int max = Math.min(5, bosses.size());
                int padding = ((image.getHeight() - 260) - 5 * 220) / 5;
                y = padding + 200;
                int bossCentre = (int) (image.getWidth() * 0.625); // mid point of boss image section
                for(int i = 0; i < max; i++) {
                    Boss boss = bosses.get(i);
                    BufferedImage bossImage = ImageIO.read(boss.getImage());
                    g.drawImage(bossImage, bossCentre - (bossImage.getWidth() / 2), y, null);

                    g.setColor(Color.YELLOW);
                    g.setFont(runeFont.deriveFont(runeFont.getSize() * 1.2F));
                    String kills = boss.formatKills();
                    int killWidth = g.getFontMetrics().stringWidth(kills);
                    g.drawString(kills, (int) ((image.getWidth() * 0.875) - killWidth / 2), (y + (bossImage.getHeight() / 2) + (g.getFont().getSize() / 2)));
                    y += 220 + padding;
                }
            }
            else {
                BufferedImage noBoss = ImageIO.read(new File(resources + "no_boss.png"));
                g.drawImage(noBoss, (int) ((image.getWidth() * 0.75)) - (noBoss.getWidth() / 2), 200 + (((image.getHeight() - 200) / 2) - (noBoss.getHeight() / 2)), null);
            }

            g.setFont(runeFont.deriveFont(runeFont.getSize() * 3F));
            int nameWidth = g.getFontMetrics().stringWidth(name);
            x = (image.getWidth() / 2) - (nameWidth / 2);
            y = 100 + (g.getFont().getSize() / 2);
            g.drawString(name.toUpperCase(), x, y);

            g.dispose();
            playerStats = saveImage(image, name);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return playerStats;
    }

    /**
     * Saves the created image to disk
     *
     * @param image Image to be saved
     * @return Image file
     */
    private File saveImage(BufferedImage image, String name) {
        File file = null;
        try {
            file = new File(resources + name +".png");
            new PngEncoder().encode(image, new FileOutputStream(file));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Register the OSRS font with the graphics environment
     */
    private void registerFont() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(resources + "osrs.ttf")));
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

    /**
     * Build a sorted list of boss kill counts
     *
     * @param csv csv from API
     * @return Sorted list of boss kill counts
     */
    private List<Boss> getBossKills(String[] csv) {

        // Truncate csv to begin at index of first boss rank
        List<String> stats = Arrays.asList(csv).subList(94, csv.length);
        List<Boss> bosses = new ArrayList<>();

        int i = 1;
        for(String boss : bossNames) {
            int kills = Integer.parseInt(stats.get(i));
            if(kills > -1) {
                bosses.add(new Boss(boss, kills));
            }
            i += 2;
        }
        Collections.sort(bosses);
        return bosses;
    }

    /**
     * Hold information about a boss, sortable by kill count
     */
    private class Boss implements Comparable<Boss> {
        private String name, filename;
        private int kills;

        Boss(String name, int kills) {
            this.name = name;
            this.kills = kills;
            this.filename = name + ".png";
        }

        int getKills() {
            return kills;
        }

        /**
         * Get the qualifier for the boss, default is kills but games etc is an option too where applicable
         *
         * @return Formatted string containing kill count and qualifier (kills, games, etc)
         */
        String formatKills() {
            String type;
            switch(name) {
                case "Barrows Chests":
                case "Chambers of Xeric":
                case "Chambers of Xeric Challenge Mode":
                case "Theatre of Blood":
                case "The Gauntlet":
                case "The Corrupted Gauntlet":
                    type = "Clears";
                    break;
                case "TzKal-Zuk":
                case "TzTok-Jad":
                    type = "Caves";
                    break;
                case "Wintertodt":
                case "Zalcano":
                    type = "Games";
                    break;
                default:
                    type = "Kills";
            }

            // Comma for large numbers
            return NumberFormat.getNumberInstance().format(kills) + " " + type;
        }

        public String getName() {
            return name;
        }

        public File getImage() {
            return new File(resources + "Bosses/" + filename);
        }

        @Override
        public int compareTo(Hiscores.Boss o) {
            return o.getKills() - kills;
        }
    }

    /**
     * Get a list of boss names
     *
     * @return List of boss names
     */
    private String[] getBossNames() {
        return new String[]{
                "Abyssal Sire",
                "Alchemical Hydra",
                "Barrows Chests",
                "Bryophyta",
                "Callisto",
                "Cerberus",
                "Chambers of Xeric",
                "Chambers of Xeric Challenge Mode",
                "Chaos Elemental",
                "Chaos Fanatic",
                "Commander Zilyana",
                "Corporeal Beast",
                "Crazy Archaeologist",
                "Dagannoth Prime",
                "Dagannoth Rex",
                "Dagannoth Supreme",
                "Deranged Archaeologist",
                "General Graardor",
                "Giant Mole",
                "Grotesque Guardians",
                "Hespori",
                "Kalphite Queen",
                "King Black Dragon",
                "Kraken",
                "Kree'Arra",
                "K'ril Tsutsaroth",
                "Mimic",
                "Nightmare",
                "Obor",
                "Sarachnis",
                "Scorpia",
                "Skotizo",
                "The Gauntlet",
                "The Corrupted Gauntlet",
                "Theatre of Blood",
                "Thermonuclear Smoke Devil",
                "TzKal-Zuk",
                "TzTok-Jad",
                "Venenatis",
                "Vet'ion",
                "Vorkath",
                "Wintertodt",
                "Zalcano",
                "Zulrah"
        };
    }
}
