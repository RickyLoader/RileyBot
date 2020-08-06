package OSRS.Stats;

import Command.Structure.EmbedLoadingMessage;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Network.ImgurManager;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Build an image displaying a player's OSRS stats
 */
public class Hiscores {
    private final String[] bossNames;
    private final String resources = "src/main/resources/OSRS/";
    private final MessageChannel channel;
    private ImageLoadingMessage loading;
    private boolean timeout = false;

    /**
     * Initialise the Hiscores with a channel to send the image to
     *
     * @param channel Channel to send image to
     */
    public Hiscores(MessageChannel channel) {
        registerFont();
        bossNames = getBossNames();
        this.channel = channel;
    }

    /**
     * Look a player up on the OSRS hiscores and return an image displaying their skills
     *
     * @param name Player name
     */
    public void lookupPlayer(String name) {
        this.loading = new ImageLoadingMessage(
                channel,
                "OSRS Hiscores lookup: " + name.toUpperCase(),
                "Give me a second, their website is slow as fuck.",
                "https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png",
                new String[]{
                        "Player exists...",
                        "Checking account type...",
                        "Building image...",
                        "Uploading image..."
                }
        );
        loading.showLoading();
        String[] data = fetchPlayerData(name);

        if(data == null) {
            if(timeout) {
                loading.failLoading("I wasn't able to connect to the Hiscores");
                return;
            }
            loading.failLoading("That player doesn't exist cunt");
            return;
        }

        List<Boss> bossKills = getBossKills(data);

        if(bossKills.size() == 0 && name.toLowerCase().equals("hectiserect")) {
            bossKills.add(new Boss("Butler", 500000));
            bossKills.add(new Boss("Black dude", 12));
        }

        if(name.toLowerCase().equals("heineken_3")) {
            bossKills.add(new Boss("Harambe", 1));
        }

        String[] clues = getClueScrolls(data);
        String[] stats = orderSkills(data);
        BufferedImage playerImage = buildImage(name, data[0], stats, bossKills, clues);
        loading.completeStage();
        String url = ImgurManager.uploadImage(playerImage);
        loading.completeStage();
        loading.completeLoading(url);
    }

    /**
     * Parse the player's clue scroll data from the Hiscores CSV
     *
     * @param data CSV data from API
     * @return Clue scroll data
     */
    private String[] getClueScrolls(String[] data) {
        data = Arrays.copyOfRange(data, 80, 93);
        String[] clues = new String[6];
        int j = 0;
        for(int i = 1; i < data.length; i += 2) {
            int quantity = Integer.parseInt(data[i]);
            clues[j] = "x" + ((quantity == -1) ? "0" : data[i]);
            j++;
        }
        return clues;
    }

    /**
     * Fetch the CSV from the hiscores API
     *
     * @param name Player name
     * @return CSV data from API
     */
    private String[] fetchPlayerData(String name) {
        String[] normal = hiscoresRequest(name, "");

        if(normal == null) {
            return null;
        }

        loading.completeStage();

        normal[0] = null;
        loading.updateStage("Player exists, checking ironman hiscores");
        String[] iron = hiscoresRequest(name, "_ironman");

        if(iron == null) {
            loading.completeStage("Player is a normal account!");
            return normal;
        }

        iron[0] = "iron";

        long ironXP = Long.parseLong(iron[2]);
        long normXP = Long.parseLong(normal[2]);

        if(normXP > ironXP) {
            loading.completeStage("Player is a de-ironed normal account!");
            return normal;
        }

        loading.updateStage("Player is an Ironman, checking Hardcore Ironman hiscores");
        String[] hardcore = hiscoresRequest(name, "_hardcore_ironman");

        if(hardcore != null) {
            hardcore[0] = "hardcore";
            long hcXP = Long.parseLong(hardcore[2]);

            if(ironXP > hcXP) {
                loading.completeStage("Player was a Hardcore Ironman and died! What a loser!");
                return iron;
            }

            loading.completeStage("Player is a Hardcore Ironman!");
            return hardcore;
        }

        loading.updateStage("Player is not hardcore, checking Ultimate Ironman hiscores");
        String[] ultimate = hiscoresRequest(name, "_ultimate");

        if(ultimate != null) {
            ultimate[0] = "ultimate";
            long ultXP = Long.parseLong(ultimate[2]);

            if(ironXP > ultXP) {
                loading.completeStage("Player is an Ironman who chickened out of Ultimate Ironman!");
                return iron;
            }

            loading.completeStage("Player is an Ultimate Ironman!");
            return ultimate;
        }
        loading.completeStage("Player is an Ironman!");
        return iron;
    }

    /**
     * Make a request to the OSRS hiscores API
     *
     * @param name Name to request from hiscores
     * @return Response from API
     */
    private String[] hiscoresRequest(String name, String type) {
        String baseURL = "https://secure.runescape.com/m=hiscore_oldschool" + type;
        String suffix = "/index_lite.ws?player=" + name;
        String response = new NetworkRequest(baseURL + suffix, false).get();
        if(response == null) {
            timeout = true;
            return null;
        }
        if(response.equals("err")) {
            return null;
        }
        return response.replace("\n", ",").split(",");
    }

    /**
     * Build an image based on the player's stats, boss kills, and clue scrolls
     *
     * @param name   Player name
     * @param type   Player account type
     * @param skills Player stats
     * @param bosses Player boss kills
     * @param clues  Player clue scroll completions
     * @return Image showing player stats
     */
    private BufferedImage buildImage(String name, String type, String[] skills, List<Boss> bosses, String[] clues) {
        BufferedImage image = null;
        int fontSize = 65;
        try {
            String imagePath = (resources + "Templates/stats_template.png");

            image = ImageIO.read(new File(imagePath));
            Graphics g = image.getGraphics();
            Font runeFont = new Font("RuneScape Chat '07", Font.PLAIN, fontSize);
            g.setFont(runeFont);

            // First skill location
            int x = 200, ogX = 200;
            int y = 315;


            for(int i = 0; i < skills.length; i++) {
                String level = skills[i];

                // total level
                if(i == skills.length - 1) {
                    g.setColor(Color.YELLOW);
                    g.drawString(level, x - 70, y + 60);
                    continue;
                }

                g.setColor(Color.BLACK); // shadow

                g.drawString(level, x + 5, y + 5); // top
                g.drawString(level, x + 65, y + 65); // bottom

                g.setColor(Color.YELLOW); // skill

                g.drawString(level, x, y); // top
                g.drawString(level, x + 60, y + 60); // bottom

                // Currently 3rd column, reset back to first column and go down a row
                if((i + 1) % 3 == 0) {
                    x = ogX;
                    y = (y + 160);
                }
                // Move to next column
                else {
                    x = (x + 315);
                }
            }

            // Clues
            x = 170;
            y = 1960;
            g.setFont(runeFont.deriveFont(fontSize));
            for(String quantity : clues) {
                int quantityWidth = g.getFontMetrics().stringWidth(quantity) / 2;
                g.drawString(quantity, x - quantityWidth, y);
                x += 340;
            }

            if(bosses.size() > 0) {
                int max = Math.min(5, bosses.size());

                // All images have 220px height, and the top name banner + bottom border has 260px total height, clue section has height of 425
                int padding = ((image.getHeight() - 260 - 425) - (5 * 220)) / 6;

                // Height of top name banner
                y = 230 + padding;

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
                BufferedImage noBoss = ImageIO.read(new File(resources + "Templates/no_boss.png"));
                g.drawImage(noBoss, (int) ((image.getWidth() * 0.75)) - (noBoss.getWidth() / 2), 200 + (((image.getHeight() - 200 - 425) / 2) - (noBoss.getHeight() / 2)), null);
            }

            // Name stuff

            g.setFont(runeFont.deriveFont(runeFont.getSize() * 3F));
            int nameWidth = g.getFontMetrics().stringWidth(name);
            x = (image.getWidth() / 2) - (nameWidth / 2);
            y = 100 + (g.getFont().getSize() / 2);
            g.drawString(name.toUpperCase(), x, y);

            if(type != null) {
                BufferedImage accountType = ImageIO.read(new File(resources + "Accounts/" + type + ".png"));
                g.drawImage(accountType, x - (int) (accountType.getWidth() * 1.5), 115 - (accountType.getHeight() / 2), null);
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
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
        String[] skills = new String[]{
                csv[4],     // ATTACK
                csv[13].equals("1") ? "10" : csv[13],    // HITPOINTS
                csv[46],    // MINING

                csv[10],    // STRENGTH
                csv[52],    // AGILITY
                csv[43],    // SMITHING

                csv[7],     // DEFENCE
                csv[49],    // HERBLORE
                csv[34],    // FISHING

                csv[16],    // RANGED
                csv[55],    // THIEVING
                csv[25],    // COOKING

                csv[19],    // PRAYER
                csv[40],    // CRAFTING
                csv[37],    // FIREMAKING

                csv[22],    // MAGIC
                csv[31],    // FLETCHING
                csv[28],    // WOODCUTTING

                csv[64],    // RUNECRAFT
                csv[58],    // SLAYER
                csv[61],    // FARMING

                csv[70],    // CONSTRUCTION
                csv[67],    // HUNTER
                csv[1].equals("0") ? "---" : csv[1],     // TOTAL
        };
        return getTotal(skills);
    }

    /**
     * Total level appears as 0 if not in the top 2,000,000 players
     *
     * @param skills Array of skills with total level from hiscores
     * @return Array of skills with adjusted total level
     */
    private String[] getTotal(String[] skills) {
        int totalLevelIndex = skills.length - 1;
        if(!skills[totalLevelIndex].equals("0")) {
            return skills;
        }
        String total = "--";
        skills[totalLevelIndex] = total;
        return skills;
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
        private final String name, filename;
        private final int kills;

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
                case "Butler":
                    type = "Planks";
                    break;
                case "Black dude":
                    type = "Slobbers";
                    break;
                default:
                    type = (kills == 1) ? "Kill" : "Kills";
            }

            // Comma for large numbers
            return NumberFormat.getNumberInstance().format(kills) + " " + type;
        }

        File getImage() {
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
