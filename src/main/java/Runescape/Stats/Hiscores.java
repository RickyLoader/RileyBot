package Runescape.Stats;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import Network.ImgurManager;
import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * Build an image displaying a player's RS3 stats
 */
public class Hiscores extends ImageBuilder {
    private ImageLoadingMessage loading;
    private boolean timeout = false;
    private final Color orange, yellow, red, blue;
    private HCIMStatus hcimStatus;

    public Hiscores(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName) {
        super(channel, emoteHelper, resourcePath, fontName);
        this.orange = new Color(EmbedHelper.getRunescapeOrange());
        this.yellow = new Color(EmbedHelper.getRunescapeYellow());
        this.blue = new Color(EmbedHelper.getRunescapeBlue());
        this.red = new Color(EmbedHelper.getRunescapeRed());
    }

    /**
     * URL encode the player's name
     *
     * @param name Player name
     * @return URL encoded name
     */
    private String encodeName(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            return name;
        }
    }

    /**
     * Get the URL for looking a player up on the RS3 hiscores
     *
     * @param type Account type
     * @param name Player name
     * @return Hiscores url
     */
    private String getURL(String type, String name) {
        return "https://secure.runescape.com/m=hiscore" + type + "/index_lite.ws?player=" + name;
    }

    /**
     * Get the URL for looking a player up on the RS3 hiscores
     *
     * @param name Player name
     * @return Normal hiscores URL
     */
    private String getNormalAccount(String name) {
        return getURL("", name);
    }

    /**
     * Get the URL for looking an ironman up on the RS3 hiscores
     *
     * @param name Player name
     * @return Ironman hiscores URL
     */
    private String getIronmanAccount(String name) {
        return getURL("_ironman", name);

    }

    /**
     * Get the URL for looking a hardcore ironman up on the RS3 hiscores
     *
     * @param name Player name
     * @return Hardcore ironman hiscores URL
     */
    private String getHardcoreAccount(String name) {
        return getURL("_hardcore_ironman", name);
    }

    /**
     * Look a player up on the RS3 hiscores and return an image displaying their skills
     *
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     * @param args        none
     */
    @Override
    public void buildImage(String nameQuery, String helpMessage, String... args) {
        String encodedName = encodeName(nameQuery);
        String defaultURL = getNormalAccount(encodedName);
        this.loading = new ImageLoadingMessage(
                getChannel(),
                getEmoteHelper(),
                "RS3 Hiscores lookup: " + nameQuery.toUpperCase(),
                "Give me a second, their website can be slow as fuck.",
                "https://vignette.wikia.nocookie.net/runescape2/images/a/a7/RuneScape_Companion_logo.png",
                helpMessage,
                new String[]{
                        "Player exists...",
                        "Checking account type...",
                        "Checking RuneMetrics...",
                        "Building image...",
                        "Uploading image...",
                }
        );
        loading.showLoading();

        String[] data = fetchPlayerData(encodedName);
        if(data == null) {
            if(timeout) {
                loading.failLoading("I wasn't able to connect to the " + EmbedHelper.embedURL("Hiscores", defaultURL));
                return;
            }
            loading.failLoading("That player " + EmbedHelper.embedURL("doesn't exist", defaultURL) + " cunt");
            return;
        }

        String[] clues = getClueScrolls(Arrays.copyOfRange(data, 137, data.length - 1));
        String[] stats = orderSkills(data);
        JSONObject runeMetrics = getRuneMetrics(encodedName);
        BufferedImage playerImage = buildImage(nameQuery, data[0], stats, clues, runeMetrics);
        loading.completeStage();
        String url = ImgurManager.uploadImage(playerImage);
        loading.completeStage();
        loading.completeLoading(url, EmbedHelper.embedURL("View raw data", data[data.length - 1]));
    }

    /**
     * Build an image based on the player's stats, boss kills, and clue scrolls
     *
     * @param name        Player name
     * @param type        Player account type
     * @param skills      Player stats
     * @param clues       Player clue scroll completions
     * @param runeMetrics Player RuneMetrics data
     * @return Image showing player stats
     */
    private BufferedImage buildImage(String name, String type, String[] skills, String[] clues, JSONObject runeMetrics) {
        int overhang = 65; // title section left overhang

        BufferedImage skillSection = buildSkillSection(skills);
        BufferedImage clueSection = buildClueSection(clues);
        BufferedImage titleSection = buildTitleSection(name, type);

        BufferedImage playerImage = new BufferedImage(
                titleSection.getWidth(),
                skillSection.getHeight() + titleSection.getHeight() + overhang,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = playerImage.getGraphics();
        g.drawImage(titleSection, 0, 0, null);
        g.drawImage(skillSection, overhang, titleSection.getHeight(), null);
        g.drawImage(clueSection, overhang + skillSection.getWidth(), titleSection.getHeight(), null);
        if(runeMetrics != null) {
            g.drawImage(buildQuestSection(runeMetrics), overhang + skillSection.getWidth(), titleSection.getHeight() + clueSection.getHeight(), null);
        }
        BufferedImage background = getBackgroundImage();
        if(background == null) {
            return playerImage;
        }
        g = background.getGraphics();
        g.drawImage(playerImage, (background.getWidth() / 2) - (playerImage.getWidth() / 2), (background.getHeight() / 2) - (playerImage.getHeight() / 2), null);
        g.dispose();
        return background;
    }

    /**
     * Get a random background image to use
     *
     * @return Background image
     */
    private BufferedImage getBackgroundImage() {
        try {
            File[] dir = new File(getResourcePath() + "Templates/Background").listFiles();
            return ImageIO.read(dir[new Random().nextInt(dir.length)]);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Build the clue section of the image
     *
     * @param clues Clue data
     * @return Clue scroll image section
     */
    private BufferedImage buildClueSection(String[] clues) {
        BufferedImage clueSection = null;
        try {
            clueSection = ImageIO.read(new File(getResourcePath() + "Templates/clue_section.png"));
            Graphics g = clueSection.getGraphics();
            g.setFont(getGameFont().deriveFont(40f));
            g.setColor(orange);
            int x = 330;
            int y = 174;
            FontMetrics fm = g.getFontMetrics();
            for(String quantity : clues) {
                g.drawString(quantity, x, y - (fm.getHeight() / 2) + fm.getAscent());
                y += 140;
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return clueSection;
    }

    /**
     * Build the skill section of the image
     *
     * @param skills Skill data
     * @return Skill image section
     */
    private BufferedImage buildSkillSection(String[] skills) {
        BufferedImage skillSection = null;
        try {
            setGameFont(new Font("TrajanPro-Regular", Font.PLAIN, 55));
            skillSection = ImageIO.read(new File(getResourcePath() + "Templates/skills_section.png"));
            Graphics g = skillSection.getGraphics();
            g.setFont(getGameFont());

            // First skill location
            int x = 170, ogX = x;
            int y = 72;

            int totalY = 1560;

            g.setColor(yellow);

            for(int i = 0; i < skills.length - 2; i++) {
                String level = skills[i];
                boolean master = level.length() > 2;
                g.drawString(level, master ? x - 15 : x, y); // top
                g.drawString(level, master ? x + 60 : x + 78, y + 64); // bottom

                // Currently 3rd column, reset back to first column and go down a row
                if((i + 1) % 3 == 0) {
                    x = ogX;
                    y += 142;
                }
                // Move to next column
                else {
                    x += 330;
                }
            }

            g.setColor(Color.WHITE);
            g.drawString(skills[skills.length - 2], 240, totalY);
            g.drawString(skills[skills.length - 1], 730, totalY);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return skillSection;
    }

    /**
     * Build the title section of the image
     *
     * @param name        Player name
     * @param accountType Player account type
     * @return Title section of image
     */
    private BufferedImage buildTitleSection(String name, String accountType) {
        BufferedImage titleSection = null;
        try {
            titleSection = ImageIO.read(new File(getResourcePath() + "Templates/title_section.png"));
            BufferedImage avatar = ImageIO.read(new URL("http://services.runescape.com/m=avatar-rs/" + encodeName(name) + "/chat.gif"));
            BufferedImage scaledAvatar = new BufferedImage(287, 287, BufferedImage.TYPE_INT_ARGB);
            Graphics g = scaledAvatar.createGraphics();
            g.drawImage(avatar, 0, 0, 287, 287, null);
            g = titleSection.getGraphics();
            g.drawImage(scaledAvatar, 110, 124, null);

            g.setFont(getGameFont().deriveFont(75f));
            g.setColor(orange);
            FontMetrics fm = g.getFontMetrics();

            int x = 1000;
            int y = 375;
            int width = fm.stringWidth(name.toUpperCase());
            g.drawString(name.toUpperCase(), x - (width / 2), y - (fm.getHeight() / 2) + fm.getAscent());

            if(accountType != null) {
                BufferedImage typeImage = ImageIO.read(new File(getResourcePath() + "Accounts/" + accountType + ".png"));
                g.drawImage(typeImage, 511 - (typeImage.getWidth() / 2), 423 - typeImage.getHeight(), null);
                if(hcimStatus != null && hcimStatus.isDead()) {
                    if(accountType.equals("hardcore")) {
                        g.setColor(red);
                        ((Graphics2D) g).setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g.drawLine(x - (width / 2), y, x + (width / 2), y);
                    }

                    BufferedImage deathSection = ImageIO.read(new File(getResourcePath() + "Templates/death_section.png"));
                    g = deathSection.getGraphics();
                    g.setFont(getGameFont().deriveFont(20f));
                    fm = g.getFontMetrics();
                    g.setColor(Color.WHITE);
                    x = (deathSection.getWidth() / 2);
                    y = 45 + fm.getHeight();

                    g.drawString(hcimStatus.getDate(), x - (fm.stringWidth(hcimStatus.getDate()) / 2), y);
                    y += fm.getHeight();


                    String[] cause = (hcimStatus.getLocation() + " " + hcimStatus.getCause()).split(" ");
                    String curr = "";

                    for(String word : cause) {
                        String attempt = curr + " " + word;
                        if(fm.stringWidth(attempt) > deathSection.getWidth()) {
                            g.drawString(curr, x - (fm.stringWidth(curr) / 2), y);
                            y += fm.getHeight();
                            curr = word;
                            continue;
                        }
                        curr = attempt;
                    }
                    g.drawString(curr, x - (fm.stringWidth(curr) / 2), y);
                    g = titleSection.getGraphics();
                    g.drawImage(deathSection, titleSection.getWidth() - 65 - deathSection.getWidth(), titleSection.getHeight() - deathSection.getHeight(), null);
                    return titleSection;
                }
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return titleSection;
    }

    /**
     * Build quest image section
     *
     * @param runeMetrics Player RuneMetrics data
     * @return Quest section displaying player quest stats
     */
    private BufferedImage buildQuestSection(JSONObject runeMetrics) {
        BufferedImage questSection = null;
        try {
            double completed = runeMetrics.getDouble("questscomplete");
            double notStarted = runeMetrics.getDouble("questsnotstarted");
            double started = runeMetrics.getDouble("questsstarted");
            double total = completed + notStarted + started;
            questSection = ImageIO.read(new File(getResourcePath() + "Templates/quest_section.png"));
            Graphics2D g = (Graphics2D) questSection.getGraphics();
            g.setStroke(new BasicStroke(80f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int radius = 150;
            int diameter = radius * 2;
            int x = (questSection.getWidth() / 2) - radius;
            int y = (questSection.getHeight() / 4);
            double angle = 360;

            int notStartedAngle = (int) (angle * (notStarted / total));
            int startedAngle = (int) (angle * (started / total));
            int completeAngle = (int) (angle - (startedAngle + notStartedAngle));
            g.setColor(red);
            g.drawArc(x, y, diameter, diameter, 90, notStartedAngle);
            g.setColor(blue);
            g.drawArc(x, y, diameter, diameter, notStartedAngle + 90, startedAngle);
            g.setColor(orange);
            g.drawArc(x, y, diameter, diameter, notStartedAngle + startedAngle + 90, completeAngle);
            g.setFont(getGameFont().deriveFont(40f));
            g.setColor(orange);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(String.valueOf((int) completed), 385, 619 - (fm.getHeight() / 2) + fm.getAscent());
            g.drawString(String.valueOf((int) started), 385, 693 - (fm.getHeight() / 2) + fm.getAscent());
            g.drawString(String.valueOf((int) notStarted), 385, 767 - (fm.getHeight() / 2) + fm.getAscent());

            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return questSection;
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
     * Order the skills skill data in to the in game order
     *
     * @param skills CSV from API
     * @return Skills in game order
     */
    private String[] orderSkills(String[] skills) {
        return new String[]{
                skills[4],     // ATTACK
                skills[13].equals("1") ? "10" : skills[13],    // HITPOINTS
                skills[46],    // MINING

                skills[10],    // STRENGTH
                skills[52],    // AGILITY
                skills[43],    // SMITHING

                skills[7],     // DEFENCE
                skills[49],    // HERBLORE
                skills[34],    // FISHING

                skills[16],    // RANGED
                skills[55],    // THIEVING
                skills[25],    // COOKING

                skills[19],    // PRAYER
                skills[40],    // CRAFTING
                skills[37],    // FIREMAKING

                skills[22],    // MAGIC
                skills[31],    // FLETCHING
                skills[28],    // WOODCUTTING

                skills[64],    // RUNECRAFT
                skills[58],    // SLAYER
                skills[61],    // FARMING

                skills[70],    // CONSTRUCTION
                skills[67],    // HUNTER
                skills[73],    // SUMMONING
                skills[76],    // DUNGEONEERING
                skills[79],    // DIVINATION
                skills[82].equals("0") ? "1" : skills[82],    // INVENTION
                skills[85],    // ARCHAEOLOGY
                skills[1].equals("0") ? "---" : skills[1],     // TOTAL
                String.valueOf(calculateCombatLevel(
                        Integer.parseInt(skills[4]),
                        Integer.parseInt(skills[10]),
                        Integer.parseInt(skills[22]),
                        Integer.parseInt(skills[16]),
                        Integer.parseInt(skills[7]),
                        Integer.parseInt(skills[13]),
                        Integer.parseInt(skills[19]),
                        Integer.parseInt(skills[73])
                ))
        };
    }

    /**
     * Calculate the player's combat level
     *
     * @param attack    Attack level
     * @param defence   Defence level
     * @param hitpoints Hitpoints level
     * @param magic     Magic level
     * @param prayer    Prayer level
     * @param ranged    Ranged level
     * @param strength  Strength level
     * @param summoning Summoning level
     * @return Combat level
     */
    private int calculateCombatLevel(int attack, int strength, int magic, int ranged, int defence, int hitpoints, int prayer, int summoning) {
        if(hitpoints == 0) {
            hitpoints = 10;
        }
        int multiplier = Math.max((attack + strength), Math.max((2 * magic), (2 * ranged)));
        return (int) (((13d / 10d) * multiplier + defence + hitpoints + (prayer / 2d) + (summoning / 2d)) / 4);
    }

    /**
     * Format the skills clue scroll data to xN or x0
     *
     * @param data skills from API truncated to include only clue scrolls
     * @return Formatted clue scroll data
     */
    private String[] getClueScrolls(String[] data) {
        String[] clues = new String[5];
        int j = 0;
        DecimalFormat format = new DecimalFormat("x#,###");
        for(int i = 1; i < data.length; i += 2) {
            int quantity = Integer.parseInt(data[i]);
            quantity = quantity == -1 ? 0 : quantity;
            clues[j] = format.format(quantity);
            j++;
        }
        return clues;
    }

    /**
     * Get the player's RuneMetrics stats if available
     *
     * @param name URL encoded player name
     * @return RuneMetrics stats
     */
    private JSONObject getRuneMetrics(String name) {
        String url = "https://apps.runescape.com/runemetrics/profile/profile?user=" + name;
        JSONObject profile = new JSONObject(
                new NetworkRequest(url, false).get()
        );
        String message = "Player's " + EmbedHelper.embedURL("RuneMetrics", url) + " is";
        if(profile.has("error")) {
            loading.failStage(message + " private (Can't display Quest data)");
            return null;
        }
        loading.completeStage(message + " public!");
        return profile;
    }

    /**
     * Fetch the CSV from the hiscores API
     *
     * @param name Player name
     * @return CSV data from API
     */
    private String[] fetchPlayerData(String name) {
        String[] normal = hiscoresRequest(getNormalAccount(name));

        if(normal == null) {
            return null;
        }

        loading.completeStage();

        normal[0] = null;
        loading.updateStage("Player exists, checking ironman hiscores");
        String[] iron = hiscoresRequest(getIronmanAccount(name));

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
        String[] hardcore = hiscoresRequest(getHardcoreAccount(name));

        if(hardcore != null) {
            hardcore[0] = "hardcore";
            long hcXP = Long.parseLong(hardcore[2]);
            this.hcimStatus = new HCIMStatus(name);
            if(hcimStatus.isDead()) {
                String status = "Player was a Hardcore Ironman and died.";
                if(ironXP > hcXP) {
                    loading.completeStage(status + " They were saved by a "
                            + EmbedHelper.embedURL("Jar of divine light", "https://runescape.fandom.com/wiki/Jar_of_divine_light") + "!");
                    return iron;
                }
                else {
                    loading.completeStage(status + " What a loser!");
                    return hardcore;
                }
            }
            loading.completeStage("Player is a Hardcore Ironman!");
            return hardcore;
        }

        loading.completeStage("Player is an Ironman!");
        return iron;
    }

    /**
     * Check if a Hardcore account is alive or not
     */
    private static class HCIMStatus {
        private boolean dead;
        private String cause, location, date;

        public HCIMStatus(String name) {
            fetchStatus(name);
        }

        /**
         * Get death status
         *
         * @return Death status
         */
        public boolean isDead() {
            return dead;
        }

        /**
         * Get date of death
         *
         * @return Date of death
         */
        public String getDate() {
            return date;
        }

        /**
         * Get location of death
         *
         * @return Location of death
         */
        public String getLocation() {
            return location;
        }

        /**
         * Get cause for death
         *
         * @return Death cause
         */
        public String getCause() {
            return cause;
        }

        /**
         * Check HCIM status by parsing hiscores for death icon
         *
         * @param name Player name
         */
        private void fetchStatus(String name) {
            String url = "https://secure.runescape.com/m=hiscore_hardcore_ironman/ranking?user=" + name;
            try {
                Document d = Jsoup.connect(url).get();
                Element column = d.selectFirst(".hover .col2 .death-icon .death-icon__details");
                this.dead = column != null;
                if(dead) {
                    Elements values = column.children();
                    this.date = values.get(1).text();
                    this.location = values.get(2).text();
                    this.cause = values.get(3).text();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
