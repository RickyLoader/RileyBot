package Runescape.OSRS.Stats;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageLoadingMessage;
import Command.Structure.ImageBuilder;
import Network.NetworkRequest;
import Network.ImgurManager;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Build an image displaying a player's OSRS stats
 */
public class Hiscores extends ImageBuilder {
    private final String[] bossNames;
    private ImageLoadingMessage loading;
    private boolean timeout = false;
    private final boolean league;

    /**
     * Create the Hiscores instance
     *
     * @param channel      Channel to send message to
     * @param emoteHelper  Emote helper
     * @param resourcePath Path to resources
     * @param fontName     Font name
     * @param league       Use league hiscores or normal
     */
    public Hiscores(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName, boolean league) {
        super(channel, emoteHelper, resourcePath, fontName);
        this.bossNames = getBossNames();
        this.league = league;
    }

    /**
     * URL encode the player name
     *
     * @param name Player name
     * @return URL encoded player name
     */
    private String encodeName(String name) {
        try{
            return URLEncoder.encode(name, "UTF-8");
        }
        catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return name;
        }
    }

    /**
     * Get the loading criteria to use based on whether it is a league lookup
     *
     * @return Loading criteria
     */
    private String[] getLoadingCriteria() {
        ArrayList<String> criteria = new ArrayList<>();
        criteria.add(league ? "Player has League stats..." : "Player exists...");
        if(!league) {
            criteria.add("Checking account type...");
        }
        criteria.add("Building image...");
        criteria.add("Uploading image...");
        return criteria.toArray(new String[0]);
    }

    /**
     * Look a player up on the OSRS hiscores and return an image displaying their skills
     *
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     * @param args        none
     */
    public void buildImage(String nameQuery, String helpMessage, String... args) {
        String defaultURL = league ? getLeagueAccount(nameQuery) : getNormalAccount(nameQuery);

        this.loading = new ImageLoadingMessage(getChannel(), getEmoteHelper(), "OSRS " + (league ? "League" : "Hiscores") + " lookup: " + nameQuery.toUpperCase(), "Give me a second, their website can be slow as fuck.", league ? "https://i.imgur.com/xksIl6S.png" : "https://i.imgur.com/Hoke7jA.png", helpMessage, getLoadingCriteria());
        loading.showLoading();

        PlayerStats stats = fetchPlayerData(nameQuery);

        if(stats == null) {
            if(timeout) {
                loading.failLoading("I wasn't able to connect to the " + EmbedHelper.embedURL("hiscores", defaultURL));
                return;
            }
            loading.failLoading("That player " + EmbedHelper.embedURL(league ? "isn't on the league hiscores" : "doesn't exist", defaultURL) + " cunt");
            return;
        }

        BufferedImage playerImage = buildImage(stats);
        loading.completeStage();
        String url = ImgurManager.uploadImage(playerImage);
        loading.completeStage();
        loading.completeLoading(url, EmbedHelper.embedURL("View raw data", stats.getUrl()));
    }

    /**
     * Parse and format the player's clue scroll data from the hiscores CSV
     *
     * @param data CSV data from API
     * @return Clue scroll data
     */
    private String[] parseClueScrolls(String[] data) {
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
     * Get a hiscores request URL using the given hiscores type and player name
     *
     * @param type Hiscores type
     * @param name Player name
     * @return URL to hiscores CSV
     */
    private String getURL(String type, String name) {
        return "https://secure.runescape.com/m=hiscore_oldschool" + type + "/index_lite.ws?player=" + encodeName(name);
    }

    /**
     * Get the URL to request a normal account's stats from the hiscores.
     * Every account appears on the normal hiscores regardless of type
     *
     * @param name Player name
     * @return URL to normal account hiscores CSV
     */
    private String getNormalAccount(String name) {
        return getURL("", name);
    }

    /**
     * Get the URL to request an ironman account's stats from the hiscores.
     * Every ironman account appears on the ironman hiscores regardless of type
     *
     * @param name Player name
     * @return URL to ironman account hiscores CSV
     */
    private String getIronmanAccount(String name) {
        return getURL("_ironman", name);
    }

    /**
     * Get the URL to request a hardcore ironman's stats from the hiscores.
     * Only hardcore ironman accounts appear on this page
     *
     * @param name Player name
     * @return URL to hardcore ironman account hiscores CSV
     */
    private String getHardcoreAccount(String name) {
        return getURL("_hardcore_ironman", name);
    }

    /**
     * Get the URL to request an ultimate ironman account's stats from the hiscores.
     * Only ultimate ironman accounts appear on this page
     *
     * @param name Player name
     * @return URL to ultimate ironman account hiscores CSV
     */
    private String getUltimateAccount(String name) {
        return getURL("_ultimate", name);
    }

    /**
     * Get the URL to request an account's league stats from the hiscores.
     * Accounts only appear on this page if they have played the current season of league
     *
     * @param name Player name
     * @return URL to league account hiscores CSV
     */
    private String getLeagueAccount(String name) {
        return getURL("_seasonal", name);
    }

    /**
     * Parse the hiscores CSV to a PlayerStats instance
     *
     * @param name Player name
     * @param url  URL to hiscores CSV
     * @param csv  CSV from hiscores API
     * @param type Account type
     * @return PlayerStats object
     */
    private PlayerStats parsePlayerStats(String name, String url, String[] csv, PlayerStats.ACCOUNT type) {
        return new PlayerStats(
                name,
                url,
                Integer.parseInt(csv[0]),
                parseSkills(csv),
                parseClueScrolls(csv),
                parseBossKills(csv),
                type
        );
    }

    /**
     * Fetch the CSV from the hiscores API and wrap in a PlayerStats object
     *
     * @param name Player name
     * @return CSV data from API
     */
    private PlayerStats fetchPlayerData(String name) {
        String url = league ? getLeagueAccount(name) : getNormalAccount(name);
        String[] normal = hiscoresRequest(url);

        if(normal == null) {
            return null;
        }

        loading.completeStage();
        PlayerStats normalAccount = parsePlayerStats(name, url, normal, league ? PlayerStats.ACCOUNT.LEAGUE : PlayerStats.ACCOUNT.NORMAL);

        if(league) {
            normalAccount.setLeaguePoints(Integer.parseInt(normal[73]));
            return normalAccount;
        }

        loading.updateStage("Player exists, checking ironman hiscores");
        String ironURL = getIronmanAccount(name);
        String[] iron = hiscoresRequest(ironURL);

        if(iron == null) {
            loading.completeStage("Player is a normal account!");
            return normalAccount;
        }

        long ironXP = Long.parseLong(iron[2]);
        long normXP = Long.parseLong(normal[2]);

        if(normXP > ironXP) {
            loading.completeStage("Player is a de-ironed normal account!");
            return normalAccount;
        }

        PlayerStats ironAccount = parsePlayerStats(name, ironURL, iron, PlayerStats.ACCOUNT.IRON);
        loading.updateStage("Player is an Ironman, checking Hardcore Ironman hiscores");

        String hardcoreURL = getHardcoreAccount(name);
        String[] hardcore = hiscoresRequest(hardcoreURL);

        if(hardcore != null) {
            long hcXP = Long.parseLong(hardcore[2]);

            if(ironXP > hcXP) {
                loading.completeStage("Player was a Hardcore Ironman and died! What a loser!");
                return ironAccount;
            }

            loading.completeStage("Player is a Hardcore Ironman!");
            return parsePlayerStats(name, hardcoreURL, hardcore, PlayerStats.ACCOUNT.HARDCORE);
        }

        loading.updateStage("Player is not hardcore, checking Ultimate Ironman hiscores");

        String ultimateURL = getUltimateAccount(name);
        String[] ultimate = hiscoresRequest(ultimateURL);

        if(ultimate != null) {
            long ultXP = Long.parseLong(ultimate[2]);

            if(ironXP > ultXP) {
                loading.completeStage("Player is an Ironman who chickened out of Ultimate Ironman!");
                return ironAccount;
            }

            loading.completeStage("Player is an Ultimate Ironman!");
            return parsePlayerStats(name, ultimateURL, ultimate, PlayerStats.ACCOUNT.ULTIMATE);
        }
        loading.completeStage("Player is an Ironman!");
        return ironAccount;
    }

    /**
     * Make a request to the OSRS hiscores API
     *
     * @param url Hiscores URL to query
     * @return Response from API
     */
    private String[] hiscoresRequest(String url) {
        String response = new NetworkRequest(url, false).get();
        if(response == null) {
            timeout = true;
            return null;
        }
        if(response.equals("err")) {
            return null;
        }
        response += "," + url;
        return response.replace("\n", ",").split(",");
    }

    /**
     * Build an image based on the player's stats, boss kills, clue scrolls, and league points (where applicable)
     *
     * @param playerStats Player hiscores stats
     * @return Image showing player stats
     */
    private BufferedImage buildImage(PlayerStats playerStats) {
        BufferedImage image = null;
        int fontSize = 65;
        boolean league = playerStats.isLeague();
        try{
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/stats_template.png");
            Graphics g = image.getGraphics();
            Font runeFont = new Font("RuneScape Chat '07", Font.PLAIN, fontSize);
            g.setFont(runeFont);
            FontMetrics fm = g.getFontMetrics();

            // First skill location
            int x = 200, ogX = 200;
            int y = 315;

            String[] skills = playerStats.getSkills();
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
            String[] clues = playerStats.getClues();
            x = 170;
            y = 1960;
            g.setFont(runeFont.deriveFont(fontSize));
            for(String quantity : clues) {
                int quantityWidth = fm.stringWidth(quantity) / 2;
                g.drawString(quantity, x - quantityWidth, y);
                x += 340;
            }
            List<Boss> bosses = playerStats.getBossKills();
            if(bosses.size() > 0) {
                int max = Math.min(5, bosses.size());

                // All images have 220px height, and the top name banner + bottom border has 260px total height, clue section has height of 425
                int padding = ((image.getHeight() - 260 - 425) - (5 * 220)) / 6;

                // Height of top name banner
                y = 230 + padding;

                int bossCentre = (int) (image.getWidth() * 0.625); // mid point of boss image section

                for(int i = 0; i < max; i++) {
                    Boss boss = bosses.get(i);
                    BufferedImage bossImage = boss.getImage();
                    g.drawImage(bossImage, bossCentre - (bossImage.getWidth() / 2), y, null);

                    g.setColor(Color.YELLOW);
                    g.setFont(runeFont.deriveFont(runeFont.getSize() * 1.2F));
                    String kills = boss.formatKills();
                    int killWidth = fm.stringWidth(kills);
                    g.drawString(kills, (int) ((image.getWidth() * 0.875) - killWidth / 2), (y + (bossImage.getHeight() / 2) + (fm.getHeight() / 2)));
                    y += 220 + padding;
                }
            }
            else {
                BufferedImage noBoss = getResourceHandler().getImageResource(getResourcePath() + "Templates/no_boss.png");
                g.drawImage(noBoss, (int) ((image.getWidth() * 0.75)) - (noBoss.getWidth() / 2), 200 + (((image.getHeight() - 200 - 425) / 2) - (noBoss.getHeight() / 2)), null);
            }

            // Name, rank, and optional league points
            String name = playerStats.getName();
            g.setFont(runeFont.deriveFont(140f));
            fm = g.getFontMetrics();

            int nameSectionMid = 115;
            int nameWidth = fm.stringWidth(name);
            x = (image.getWidth() / 2) - (nameWidth / 2);
            y = nameSectionMid + (fm.getMaxAscent() / 2);
            g.drawString(name.toUpperCase(), x, y);

            g.setFont(runeFont.deriveFont(75f));
            fm = g.getFontMetrics();
            int nameSectionTextY = nameSectionMid + (fm.getMaxAscent() / 2);

            String type = playerStats.getAccountType().toString().toLowerCase();
            if(playerStats.getAccountType() != PlayerStats.ACCOUNT.NORMAL) {
                BufferedImage accountType = getResourceHandler().getImageResource(getResourcePath() + "Accounts/" + type + ".png");
                int accountWidth = accountType.getWidth();
                x = league ? 50 : (x - (int) (accountWidth * 1.5));
                g.drawImage(accountType, x, nameSectionMid - (accountType.getHeight() / 2), null);

                if(league) {
                    String points = playerStats.getLeaguePoints();
                    g.drawString(points, x + accountWidth + 15, nameSectionTextY);
                }
            }

            String rank = "Rank: " + playerStats.getRank();
            BufferedImage rankImage = getResourceHandler().getImageResource(getResourcePath() + "rank.png");
            int rankX = image.getWidth() - fm.stringWidth(rank) - 50;

            g.drawString(rank, rankX, nameSectionTextY);
            g.drawImage(rankImage, rankX - rankImage.getWidth() - 15, nameSectionMid - (rankImage.getHeight() / 2), null);
            g.dispose();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Parse and sort skill data from the API CSV to the in-game display order
     *
     * @param csv CSV from API
     * @return Sorted CSV
     */
    private String[] parseSkills(String[] csv) {
        return new String[]{csv[4],     // ATTACK
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
    }

    /**
     * Build a sorted list of boss kill counts
     *
     * @param csv csv from API
     * @return Sorted list of boss kill counts
     */
    private List<Boss> parseBossKills(String[] csv) {

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
                default:
                    type = (kills == 1) ? "Kill" : "Kills";
            }

            // Comma for large numbers
            return NumberFormat.getNumberInstance().format(kills) + " " + type;
        }

        BufferedImage getImage() {
            return getResourceHandler().getImageResource(getResourcePath() + "Bosses/" + filename);
        }

        @Override
        public int compareTo(Boss o) {
            return o.getKills() - kills;
        }
    }

    /**
     * Get a list of boss names
     *
     * @return List of boss names
     */
    private String[] getBossNames() {
        return new String[]{"Abyssal Sire", "Alchemical Hydra", "Barrows Chests", "Bryophyta", "Callisto", "Cerberus", "Chambers of Xeric", "Chambers of Xeric Challenge Mode", "Chaos Elemental", "Chaos Fanatic", "Commander Zilyana", "Corporeal Beast", "Crazy Archaeologist", "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme", "Deranged Archaeologist", "General Graardor", "Giant Mole", "Grotesque Guardians", "Hespori", "Kalphite Queen", "King Black Dragon", "Kraken", "Kree'Arra", "K'ril Tsutsaroth", "Mimic", "Nightmare", "Obor", "Sarachnis", "Scorpia", "Skotizo", "The Gauntlet", "The Corrupted Gauntlet", "Theatre of Blood", "Thermonuclear Smoke Devil", "TzKal-Zuk", "TzTok-Jad", "Venenatis", "Vet'ion", "Vorkath", "Wintertodt", "Zalcano", "Zulrah"};
    }

    /**
     * Hold player hiscores entries - skills, boss kills, clue scrolls, etc
     */
    private static class PlayerStats {
        private final String rank, name, url;
        private String leaguePoints;
        private final String[] skills, clues;
        private final List<Boss> bossKills;
        private final ACCOUNT type;
        private final DecimalFormat commaFormat = new DecimalFormat("#,###");

        enum ACCOUNT {
            NORMAL, IRON, ULTIMATE, LEAGUE, HARDCORE
        }

        /**
         * Create player stats
         *
         * @param name      Player name
         * @param url       URL to hiscores CSV
         * @param rank      Player hiscores rank (relative to account type)
         * @param skills    Array of skill data
         * @param clues     Array of clue data
         * @param bossKills List of boss kill data
         * @param type      Account type
         */
        public PlayerStats(String name, String url, int rank, String[] skills, String[] clues, List<Boss> bossKills, ACCOUNT type) {
            this.name = name;
            this.url = url;
            this.rank = commaFormat.format(rank);
            this.skills = skills;
            this.clues = clues;
            this.bossKills = bossKills;
            this.type = type;
        }

        /**
         * Get the URL to the hiscores CSV
         *
         * @return URL to hiscores CSV
         */
        public String getUrl() {
            return url;
        }

        /**
         * Check if the stats are from a league
         *
         * @return Stats are from a league
         */
        public boolean isLeague() {
            return type == ACCOUNT.LEAGUE;
        }

        /**
         * Get the player name
         *
         * @return Player name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the player league points
         *
         * @return League points
         */
        public String getLeaguePoints() {
            return leaguePoints;
        }

        /**
         * Set the player league points
         *
         * @param leaguePoints League points to set
         */
        public void setLeaguePoints(int leaguePoints) {
            this.leaguePoints = commaFormat.format(leaguePoints) + " points";
        }

        /**
         * Get the player hiscores rank relative to the account type
         *
         * @return Player hiscores rank
         */
        public String getRank() {
            return rank;
        }

        /**
         * Get a list of bosses in order of player kill count
         *
         * @return Bosses
         */
        public List<Boss> getBossKills() {
            return bossKills;
        }

        /**
         * Get a list of clue scroll completions in order of Beginner, Easy, Medium, Hard, Elite, Master
         *
         * @return Clue scroll completions
         */
        public String[] getClues() {
            return clues;
        }

        /**
         * Get the player account type
         *
         * @return Account type
         */
        public ACCOUNT getAccountType() {
            return type;
        }

        /**
         * Get a list of skill values in the in-game order
         *
         * @return Player skill values
         */
        public String[] getSkills() {
            return skills;
        }
    }
}
