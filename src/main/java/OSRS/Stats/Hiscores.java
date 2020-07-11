package OSRS.Stats;

import Network.ApiRequest;
import com.objectplanet.image.PngEncoder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class Hiscores {
    private final String[] bossNames;
    private final String resources = "src/main/resources/OSRS/";
    private String playerName;
    private String accountType;
    private String checkBox = "\uD83D\uDDF9";
    private String neutral = "☐";
    private long id, startTime, lookupTime, imageTime, accountTypeTime;
    private MessageChannel channel;
    private boolean playerExists, imageBuilt, done, fail, accountTypeFound, timeOut;

    public Hiscores(MessageChannel channel) {
        registerFont();
        bossNames = getBossNames();
        this.channel = channel;
        this.startTime = System.currentTimeMillis();
    }

    private void showLoading() {
        channel.sendMessage(createLoadingMessage()).queue(message -> id = message.getIdLong());
    }

    private MessageEmbed createLoadingMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("OSRS Hiscores lookup: " + playerName.toUpperCase());
        builder.setDescription("Give me a second, their website is slow as fuck.");
        builder.setThumbnail("https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png");
        builder.setColor(65280);
        String waiting = neutral + " ---";
        String playerExistsValue = waiting;
        String accountTypeValue = waiting;
        String buildingImageValue = waiting;

        if(playerExists) {
            playerExistsValue = checkBox + formatTime(lookupTime - startTime);
        }

        builder.addField("Player exists...", playerExistsValue, false);

        if(accountTypeFound) {
            accountTypeValue = checkBox + " " + accountType + formatTime(accountTypeTime - lookupTime);
        }
        else if(accountType != null) {
            accountTypeValue = neutral + " " + accountType;
        }
        builder.addField("Checking account type...", accountTypeValue, false);

        if(imageBuilt) {
            buildingImageValue = checkBox + formatTime(imageTime - accountTypeTime);
        }

        builder.addField("Building image...", buildingImageValue, false);

        if(done) {
            builder.addField("Done!", checkBox + formatTime(imageTime - startTime), false);
        }
        else if(fail) {
            String value = "That player doesn't exist cunt.";
            if(timeOut) {
                value = "I couldn't reach the hiscores cunt.";
            }
            builder.addField("FAILED!", value + formatTime(System.currentTimeMillis() - startTime), false);

        }
        return builder.build();
    }

    private void updateLoadingMessage() {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(createLoadingMessage()).queue());
    }

    private String formatTime(long time) {
        String seconds = String.format("%.2f", (double) time / 1000);
        return " (" + seconds + " seconds" + ")";
    }

    private void failLoading() {
        this.neutral = "☒";
        this.fail = true;
        this.accountType = null;
    }

    private void showPlayerExists(boolean playerExists) {
        this.playerExists = playerExists;
        lookupTime = System.currentTimeMillis();
        if(!playerExists) {
            failLoading();
        }
        updateLoadingMessage();
    }

    private void showAccountTypeFound(String accountType) {
        this.accountType = accountType;
        accountTypeTime = System.currentTimeMillis();
        accountTypeFound = true;
        updateLoadingMessage();
    }

    private void showImageBuilt() {
        this.imageBuilt = true;
        imageTime = System.currentTimeMillis();
        updateLoadingMessage();
    }

    private void showCheckingAccountType(String accountType) {
        this.accountType = accountType;
        updateLoadingMessage();
    }

    /**
     * Look a player up on the OSRS hiscores and return an image displaying their skills
     *
     * @param name Player name
     * @return Image displaying player skills
     */
    public boolean lookupPlayer(String name) {
        this.playerName = name;

        showLoading();

        String[] data = fetchPlayerData(name);

        if(data == null) {
            return false;
        }

        List<Boss> bossKills = getBossKills(data);

        if(bossKills.size() == 0 && name.toLowerCase().equals("hectiserect")) {
            bossKills.add(new Boss("Butler", 500000));
            bossKills.add(new Boss("Black dude", 12));
        }

        if(name.toLowerCase().equals("heineken_3")) {
            bossKills.add(new Boss("Harambe", 1));
        }

        String[] stats = orderSkills(data);
        File playerImage = buildImage(name, data[0], stats, bossKills);
        showImageBuilt();
        channel.sendFile(playerImage).queue(message -> {
            playerImage.delete();
            this.done = true;
            updateLoadingMessage();
        });
        return true;
    }

    /**
     * Fetch the CSV from the hiscores API
     *
     * @param name Player name
     * @return CSV data from API
     */
    private String[] fetchPlayerData(String name) {
        String[] normal = hiscoresRequest(name, "");

        if(timeOut) {
            failLoading();
            updateLoadingMessage();
            return null;
        }

        // Player doesn't exist
        if(normal == null) {
            showPlayerExists(false);
            return null;
        }

        showPlayerExists(true);

        normal[0] = null;
        showCheckingAccountType("Player exists, checking ironman hiscores");
        String[] iron = hiscoresRequest(name, "_ironman");

        // Player is not an ironman
        if(iron == null) {
            showAccountTypeFound("Player is a normal account!");
            return normal;
        }

        iron[0] = "iron";

        long ironXP = Long.parseLong(iron[2]);
        long normXP = Long.parseLong(normal[2]);

        // Player was some kind of ironman and de-ironed
        if(normXP > ironXP) {
            showAccountTypeFound("Player is a de-ironed normal account!");
            return normal;
        }

        // Player currently is some kind of ironman
        showCheckingAccountType("Player is an Ironman, checking Hardcore Ironman hiscores");
        String[] hardcore = hiscoresRequest(name, "_hardcore_ironman");

        // Player was at some point a hardcore ironman
        if(hardcore != null) {
            hardcore[0] = "hardcore";
            long hcXP = Long.parseLong(hardcore[2]);

            // Player died as a hardcore ironman and is now a normal ironman
            if(ironXP > hcXP) {
                showAccountTypeFound("Player was a Hardcore Ironman and died! What a loser!");
                return iron;
            }

            // Player is still a hardcore ironman
            showAccountTypeFound("Player is a Hardcore Ironman!");
            return hardcore;
        }

        showCheckingAccountType("Player is not hardcore, checking Ultimate Ironman hiscores");
        String[] ultimate = hiscoresRequest(name, "_ultimate");

        // Player was at some point an ultimate ironman
        if(ultimate != null) {
            ultimate[0] = "ultimate";
            long ultXP = Long.parseLong(ultimate[2]);

            // Player downgraded to a normal ironman
            if(ironXP > ultXP) {
                showAccountTypeFound("Player is an Ironman who chickened out of Ultimate Ironman!");
                return iron;
            }

            // Player is still an ultimate ironman
            showAccountTypeFound("Player is an Ultimate Ironman!");
            return ultimate;
        }
        showAccountTypeFound("Player is an Ironman!");
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
        String response = ApiRequest.executeQuery(baseURL + suffix, "GET", null, false);
        if(response == null) {
            timeOut = true;
            return null;
        }
        if(response.equals("err")) {
            return null;
        }
        return response.replace("\n", ",").split(",");
    }

    /**
     * Build an image displaying player skills
     *
     * @param skills Skills to display on message
     * @return Embedded message displaying player skills
     */
    private File buildImage(String name, String type, String[] skills, List<Boss> bosses) {
        File playerStats = null;
        int fontSize = 65;
        try {
            String imagePath = (resources + "Templates/stats_template.png");

            BufferedImage image = ImageIO.read(new File(imagePath));
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

            if(bosses.size() > 0) {
                int max = Math.min(5, bosses.size());

                // All images have 220px height, and the top name banner + bottom border has 260px total height
                int padding = ((image.getHeight() - 260) - (5 * 220)) / 6;

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
                g.drawImage(noBoss, (int) ((image.getWidth() * 0.75)) - (noBoss.getWidth() / 2), 200 + (((image.getHeight() - 200) / 2) - (noBoss.getHeight() / 2)), null);
            }

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
            file = new File(resources + name + ".png");
            FileOutputStream output = new FileOutputStream(file);
            new PngEncoder().encode(image, output);
            output.close();
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
                csv[1],     // TOTAL
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
