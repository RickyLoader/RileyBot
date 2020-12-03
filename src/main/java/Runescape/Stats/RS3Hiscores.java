package Runescape.Stats;

import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Runescape.Hiscores;
import Runescape.PlayerStats;
import Runescape.Skill;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static Runescape.Skill.SKILL_NAME.*;

/**
 * Build an image displaying a player's RS3 stats
 */
public class RS3Hiscores extends Hiscores {
    private final Color orange, yellow, red, blue;
    private final ResourceHandler handler;
    private final String[] bgImages;
    private final boolean virtual;

    /**
     * Create the RS3 Hiscores instance
     *
     * @param channel     Channel to send message to
     * @param emoteHelper Emote helper
     * @param virtual     Calculate virtual levels or display hiscores provided levels
     */
    public RS3Hiscores(MessageChannel channel, EmoteHelper emoteHelper, boolean virtual) {
        super(channel, emoteHelper, "/Runescape/RS3/", "rs3.ttf");
        this.orange = new Color(EmbedHelper.RUNESCAPE_ORANGE);
        this.yellow = new Color(EmbedHelper.RUNESCAPE_YELLOW);
        this.blue = new Color(EmbedHelper.RUNESCAPE_BLUE);
        this.red = new Color(EmbedHelper.RUNESCAPE_RED);
        this.handler = new ResourceHandler();
        this.bgImages = new String[]{
                "1.png",
                "2.png",
                "3.png",
                "4.png",
                "5.png"
        };
        this.virtual = virtual;
    }

    @Override
    public String getURL(String type, String name) {
        return "https://secure.runescape.com/m=hiscore" + type + "/index_lite.ws?player=" + encodeName(name);
    }

    @Override
    public String getDefaultURL(String name) {
        return getNormalAccount(name);
    }

    @Override
    public String getLoadingTitle(String name) {
        return "RS3 Hiscores lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail() {
        return "https://vignette.wikia.nocookie.net/runescape2/images/a/a7/RuneScape_Companion_logo.png";
    }

    @Override
    public ArrayList<String> getLoadingCriteria() {
        ArrayList<String> loadingCriteria = new ArrayList<>();
        loadingCriteria.add("Player exists...");
        loadingCriteria.add("Checking RuneMetrics...");
        loadingCriteria.add("Checking account type...");
        return loadingCriteria;
    }

    @Override
    public BufferedImage buildHiscoresImage(PlayerStats playerStats) {
        RS3PlayerStats stats = (RS3PlayerStats) playerStats;

        int overhang = 65; // title section left overhang

        BufferedImage skillSection = buildSkillSection(stats);
        BufferedImage clueSection = buildClueSection(playerStats.getClues());
        BufferedImage titleSection = buildTitleSection(stats);

        BufferedImage playerImage = new BufferedImage(
                titleSection.getWidth(),
                skillSection.getHeight() + titleSection.getHeight() + overhang,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = playerImage.getGraphics();
        g.drawImage(titleSection, 0, 0, null);
        g.drawImage(skillSection, overhang, titleSection.getHeight(), null);
        g.drawImage(clueSection, overhang + skillSection.getWidth(), titleSection.getHeight(), null);

        if(stats.hasRuneMetrics()) {
            g.drawImage(
                    buildQuestSection(stats.getRuneMetrics()),
                    overhang + skillSection.getWidth(),
                    titleSection.getHeight() + clueSection.getHeight(),
                    null
            );
        }

        BufferedImage background = handler.getImageResource(
                getResourcePath() + "Templates/Background/" + bgImages[new Random().nextInt(bgImages.length)]
        );

        if(background == null) {
            return playerImage;
        }
        g = background.getGraphics();
        g.drawImage(
                playerImage,
                (background.getWidth() / 2) - (playerImage.getWidth() / 2),
                (background.getHeight() / 2) - (playerImage.getHeight() / 2),
                null
        );
        g.dispose();
        return background;
    }

    /**
     * Build the clue section of the image
     *
     * @param clues Clue data
     * @return Clue scroll image section
     */
    private BufferedImage buildClueSection(String[] clues) {
        BufferedImage clueSection = null;
        try{
            clueSection = handler.getImageResource(getResourcePath() + "Templates/clue_section.png");
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
        catch(Exception e){
            e.printStackTrace();
        }
        return clueSection;
    }

    /**
     * Build the skill section of the image
     *
     * @param playerStats Player stats
     * @return Skill image section
     */
    private BufferedImage buildSkillSection(RS3PlayerStats playerStats) {
        BufferedImage skillSection = null;
        try{
            setGameFont(new Font("TrajanPro-Regular", Font.PLAIN, 55));
            skillSection = getResourceHandler().getImageResource(
                    getResourcePath() + "Templates/skills_section.png"
            );
            Graphics g = skillSection.getGraphics();
            g.setFont(getGameFont());

            // First skill location
            int x = 170, ogX = x;
            int y = 72;

            int totalY = 1560;

            g.setColor(yellow);
            Skill[] skills = playerStats.getSkills();
            for(int i = 0; i < skills.length - 1; i++) {
                Skill skill = skills[i];
                int displayLevel = virtual ? skill.getVirtualLevel() : skill.getLevel();
                boolean master = displayLevel > 99;
                String level = String.valueOf(displayLevel);

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
            g.drawString(
                    String.valueOf(virtual ? playerStats.getVirtualTotalLevel() : playerStats.getTotalLevel()),
                    240,
                    totalY
            );
            g.drawString(String.valueOf(playerStats.getCombatLevel()), 730, totalY);
            g.dispose();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return skillSection;
    }

    /**
     * Build the title section of the image
     *
     * @param playerStats Player stats
     * @return Title section of image
     */
    private BufferedImage buildTitleSection(RS3PlayerStats playerStats) {
        BufferedImage titleSection = null;
        try{
            String name = playerStats.getName();
            titleSection = handler.getImageResource(getResourcePath() + "Templates/title_section.png");
            BufferedImage avatar = ImageIO.read(
                    new URL(
                            "http://services.runescape.com/m=avatar-rs/" + encodeName(name) + "/chat.gif"
                    )
            );
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

            if(playerStats.hasAccountTypeImage()) {
                BufferedImage typeImage = handler.getImageResource(
                        PlayerStats.getAccountTypeImagePath(playerStats.getAccountType())
                );
                g.drawImage(typeImage, 511 - (typeImage.getWidth() / 2), 423 - typeImage.getHeight(), null);

                if(playerStats.hasHCIMStatus() && playerStats.getHcimStatus().isDead()) {
                    HCIMStatus hcimStatus = playerStats.getHcimStatus();

                    if(playerStats.isHardcore()) {
                        g.setColor(red);
                        ((Graphics2D) g).setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g.drawLine(x - (width / 2), y, x + (width / 2), y);
                    }

                    BufferedImage deathSection = getResourceHandler().getImageResource(
                            getResourcePath() + "Templates/death_section.png"
                    );

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
                    g.drawImage(
                            deathSection,
                            titleSection.getWidth() - 65 - deathSection.getWidth(),
                            titleSection.getHeight() - deathSection.getHeight(),
                            null
                    );
                    return titleSection;
                }
            }
            g.dispose();
        }
        catch(Exception e){
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
    private BufferedImage buildQuestSection(RuneMetrics runeMetrics) {
        BufferedImage questSection = null;
        try{
            double completed = runeMetrics.getQuestsCompleted();
            double notStarted = runeMetrics.getQuestsNotStarted();
            double started = runeMetrics.getQuestsStarted();
            double total = runeMetrics.getTotalQuests();

            questSection = handler.getImageResource(getResourcePath() + "Templates/quest_section.png");
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
        catch(Exception e){
            e.printStackTrace();
        }
        return questSection;
    }

    /**
     * Parse and sort skill data from the API CSV to the in-game display order
     *
     * @param csv CSV from API
     * @return Skills in game order
     */
    private Skill[] parseSkills(String[] csv) {
        return new Skill[]{
                new Skill(ATTACK, 3, csv),
                new Skill(HITPOINTS, 12, csv),
                new Skill(MINING, 45, csv),
                new Skill(STRENGTH, 9, csv),
                new Skill(AGILITY, 51, csv),
                new Skill(SMITHING, 42, csv),
                new Skill(DEFENCE, 6, csv),
                new Skill(HERBLORE, 48, csv),
                new Skill(FISHING, 33, csv),
                new Skill(RANGED, 15, csv),
                new Skill(THIEVING, 54, csv),
                new Skill(COOKING, 24, csv),
                new Skill(PRAYER, 18, csv),
                new Skill(CRAFTING, 39, csv),
                new Skill(FIREMAKING, 36, csv),
                new Skill(MAGIC, 21, csv),
                new Skill(FLETCHING, 30, csv),
                new Skill(WOODCUTTING, 27, csv),
                new Skill(RUNECRAFTING, 63, csv),
                new Skill(SLAYER, 57, csv),
                new Skill(FARMING, 60, csv),
                new Skill(CONSTRUCTION, 69, csv),
                new Skill(HUNTER, 66, csv),
                new Skill(SUMMONING, 72, csv),
                new Skill(DUNGEONEERING, 75, csv),
                new Skill(DIVINATION, 78, csv),
                new Skill(INVENTION, 81, csv),
                new Skill(ARCHAEOLOGY, 84, csv),
                new Skill(OVERALL, 0, csv),
        };
    }

    /**
     * Parse and format the player's clue scroll data from the hiscores CSV
     *
     * @param data CSV data from API
     * @return Clue scroll data
     */
    private String[] parseClueScrolls(String[] data) {
        data = Arrays.copyOfRange(data, 137, data.length - 1);
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
     * @return Player RuneMetrics stats
     */
    private RuneMetrics getRuneMetrics(String name) {
        String url = "https://apps.runescape.com/runemetrics/profile/profile?user=" + encodeName(name);
        JSONObject profile = new JSONObject(
                new NetworkRequest(url, false).get()
        );
        String message = "Player's " + EmbedHelper.embedURL("RuneMetrics", url) + " is";
        if(profile.has("error")) {
            loading.failStage(message + " private (Can't display Quest data)");
            return null;
        }
        loading.completeStage(message + " public!");
        return new RuneMetrics(
                profile.getInt("questsstarted"),
                profile.getInt("questsnotstarted"),
                profile.getInt("questscomplete")
        );
    }


    @Override
    public PlayerStats fetchPlayerData(String name) {
        String url = getNormalAccount(name);
        String[] normal = hiscoresRequest(url);

        if(normal == null) {
            return null;
        }

        loading.completeStage();

        RuneMetrics runeMetrics = getRuneMetrics(name);
        String[] clues = parseClueScrolls(normal);

        RS3PlayerStats normalAccount = new RS3PlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                runeMetrics,
                PlayerStats.ACCOUNT.NORMAL
        );

        loading.updateStage("Player exists, checking ironman hiscores");
        String[] iron = hiscoresRequest(getIronmanAccount(name));

        if(iron == null) {
            loading.completeStage("Player is a normal account!");
            return normalAccount;
        }

        RS3PlayerStats ironAccount = new RS3PlayerStats(
                name,
                url,
                parseSkills(iron),
                clues,
                runeMetrics,
                PlayerStats.ACCOUNT.IRON
        );

        if(normalAccount.getTotalXP() > ironAccount.getTotalXP()) {
            loading.completeStage("Player is a de-ironed normal account!");
            return normalAccount;
        }

        loading.updateStage("Player is an Ironman, checking Hardcore Ironman hiscores");
        String[] hardcore = hiscoresRequest(getHardcoreAccount(name));

        if(hardcore != null) {
            RS3PlayerStats hardcoreAccount = new RS3PlayerStats(
                    name,
                    url,
                    parseSkills(hardcore),
                    clues,
                    runeMetrics,
                    PlayerStats.ACCOUNT.HARDCORE
            );

            HCIMStatus hcimStatus = new HCIMStatus(name);
            hardcoreAccount.setHcimStatus(hcimStatus);

            if(hcimStatus.isDead()) {
                String status = "Player was a Hardcore Ironman and died.";
                if(ironAccount.getTotalXP() > hardcoreAccount.getTotalXP()) {
                    loading.completeStage(status + " They were saved by a "
                            + EmbedHelper.embedURL(
                            "Jar of divine light",
                            "https://runescape.fandom.com/wiki/Jar_of_divine_light") + "!"
                    );
                    ironAccount.setHcimStatus(hcimStatus);
                    return ironAccount;
                }
                else {
                    loading.completeStage(status + " What a loser!");
                    return hardcoreAccount;
                }
            }
            loading.completeStage("Player is a Hardcore Ironman!");
            return hardcoreAccount;
        }

        loading.completeStage("Player is an Ironman!");
        return ironAccount;
    }

    @Override
    public String getNotFoundMessage(String name) {
        return "doesn't exist cunt";
    }
}
