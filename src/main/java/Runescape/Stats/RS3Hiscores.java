package Runescape.Stats;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.PieChart;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Runescape.Hiscores;
import Runescape.PlayerStats;
import Runescape.Skill;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Command.Structure.PieChart.*;
import static Runescape.Skill.SKILL_NAME.*;
import static Runescape.Stats.Clan.*;

/**
 * Build an image displaying a player's RS3 stats
 */
public class RS3Hiscores extends Hiscores {
    private final Color orange, yellow, red, blue;
    private final ResourceHandler handler;
    private final boolean virtual;
    private final String BASE_URL = "https://secure.runescape.com/";

    /**
     * Create the RS3 Hiscores instance
     *
     * @param channel     Channel to send message to
     * @param emoteHelper Emote helper
     * @param virtual     Calculate virtual levels or display hiscores provided levels
     */
    public RS3Hiscores(MessageChannel channel, EmoteHelper emoteHelper, boolean virtual) {
        super(channel, emoteHelper, "/Runescape/RS3/", FontManager.RS3_FONT);
        this.orange = new Color(EmbedHelper.RUNESCAPE_ORANGE);
        this.yellow = new Color(EmbedHelper.RUNESCAPE_YELLOW);
        this.blue = new Color(EmbedHelper.RUNESCAPE_BLUE);
        this.red = new Color(EmbedHelper.RUNESCAPE_RED);
        this.handler = new ResourceHandler();
        this.virtual = virtual;
    }

    @Override
    public String getURL(String type, String name) {
        return BASE_URL + "m=hiscore" + type + "/index_lite.ws?player=" + EmbedHelper.urlEncode(name);
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
        loadingCriteria.add("Checking clan...");
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

        if(stats.isClanMember()) {
            BufferedImage clanSection = buildClanSection(stats.getClan(), stats.getName());
            BufferedImage expandedImage = new BufferedImage(
                    playerImage.getWidth(),
                    playerImage.getHeight() + clanSection.getHeight(),
                    playerImage.getType()
            );
            g = expandedImage.getGraphics();
            g.drawImage(playerImage, 0, 0, null);
            g.drawImage(
                    clanSection,
                    overhang,
                    titleSection.getHeight() + skillSection.getHeight(),
                    null
            );
            playerImage = expandedImage;
        }
        g.dispose();
        return playerImage;
    }

    /**
     * Build the clan section of the image
     *
     * @param clan Player clan
     * @param name Player name
     * @return Clan image section
     */
    private BufferedImage buildClanSection(Clan clan, String name) {
        BufferedImage clanSection = null;
        try {
            clanSection = handler.getImageResource(getResourcePath() + "Templates/clan_section.png");
            Graphics g = clanSection.getGraphics();
            g.setFont(getGameFont().deriveFont(40f));
            g.setColor(Color.WHITE);
            g.drawImage(clan.getBanner(), clanSection.getWidth() - clan.getBanner().getWidth(), 0, null);
            g.drawString(clan.getName(), 100, 50);

            g.setColor(orange);
            ArrayList<String> owners = clan.getPlayersByRole(ROLE.OWNER);
            String owner = owners.isEmpty() ? "Unknown" : owners.get(0);

            int x = 300;
            g.setFont(getGameFont().deriveFont(25f));

            g.drawString(owner, x, 150);
            g.drawString(clan.getRoleByPlayerName(name).getName(), x, 200);
            g.drawString(String.valueOf(clan.getMemberCount()), x, 250);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return clanSection;
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
        catch(Exception e) {
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
        try {
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
            BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
            String rank = "Rank: " + playerStats.getFormattedRank();
            x = 365;
            y = 1430 - rankImage.getHeight(); // Align with bottom skill row
            g.drawImage(rankImage, x, y, null);
            g.drawString(
                    rank,
                    x + rankImage.getWidth() + 20,
                    y + (rankImage.getHeight() / 2) + (g.getFontMetrics().getMaxAscent() / 2)
            );
            g.drawString(
                    String.valueOf(virtual ? playerStats.getVirtualTotalLevel() : playerStats.getTotalLevel()),
                    240,
                    totalY
            );
            g.drawString(String.valueOf(playerStats.getCombatLevel()), 730, totalY);
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
     * @param playerStats Player stats
     * @return Title section of image
     */
    private BufferedImage buildTitleSection(RS3PlayerStats playerStats) {
        BufferedImage titleSection = null;
        try {
            String name = playerStats.getName();
            titleSection = handler.getImageResource(getResourcePath() + "Templates/title_section.png");
            BufferedImage avatar = EmbedHelper.downloadImage(
                    "http://services.runescape.com/m=avatar-rs/" + EmbedHelper.urlEncode(name) + "/chat.gif"
            );
            BufferedImage scaledAvatar = new BufferedImage(287, 287, BufferedImage.TYPE_INT_ARGB);
            Graphics g = scaledAvatar.createGraphics();
            g.drawImage(avatar, 0, 0, 287, 287, null);
            g = titleSection.getGraphics();
            g.drawImage(scaledAvatar, 110, 124, null);

            g.setFont(getGameFont().deriveFont(75f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(orange);

            int x = 445;
            int y = 415;

            if(playerStats.hasAccountTypeImage()) {
                BufferedImage typeImage = handler.getImageResource(
                        PlayerStats.getAccountTypeImagePath(playerStats.getAccountType())
                );
                g.drawImage(typeImage, x, y - typeImage.getHeight(), null);
                x += typeImage.getWidth() + 50;
            }
            g.drawString(name.toUpperCase(), x, y);

            x = 1000;
            y = 375;
            int width = fm.stringWidth(name.toUpperCase());

            if(playerStats.hasHCIMStatus() && playerStats.getHcimStatus().isDead()) {
                HCIMStatus hcimStatus = playerStats.getHcimStatus();

                if(playerStats.isHardcore()) {
                    g.setColor(red);
                    ((Graphics2D) g).setStroke(
                            new BasicStroke(
                                    10f,
                                    BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND
                            )
                    );
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
    private BufferedImage buildQuestSection(RuneMetrics runeMetrics) {
        Section[] sections = new Section[]{
                new Section("Not Started", runeMetrics.getQuestsNotStarted(), red),
                new Section("In Progress", runeMetrics.getQuestsStarted(), blue),
                new Section("Completed", runeMetrics.getQuestsCompleted(), orange)
        };
        BufferedImage questSection = handler.getImageResource(getResourcePath() + "Templates/quest_section.png");
        PieChart pieChart = new PieChart(sections, getGameFont(), true);
        Graphics g = questSection.getGraphics();
        int y = 150;
        g.drawImage(
                pieChart.getChart(),
                (questSection.getWidth() / 2) - (pieChart.getChart().getWidth() / 2),
                y,
                null
        );

        g.drawImage(
                pieChart.getKey(),
                (questSection.getWidth() / 2) - (pieChart.getKey().getWidth() / 2),
                y + pieChart.getChart().getHeight() + 20,
                null
        );
        g.dispose();
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
     * @param name Player name
     * @return Player RuneMetrics stats or null
     */
    private RuneMetrics getRuneMetrics(String name) {
        String url = "https://apps.runescape.com/runemetrics/profile/profile?user=" + EmbedHelper.urlEncode(name);
        JSONObject profile = new JSONObject(
                new NetworkRequest(url, false).get().body
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

    /**
     * Get the clan that the given player is a member of (if they are in a clan)
     *
     * @param name Player name
     * @return Player clan or null
     */
    private Clan getClan(String name) {
        String url = BASE_URL
                + "m=website-data/playerDetails.ws?names=%5B%22"
                + EmbedHelper.urlEncode(name)
                + "%22%5D&callback=jQuery000000000000000_0000000000&_=0";
        String embedUrl = EmbedHelper.embedURL("clan", url);

        try {
            String response = new NetworkRequest(url, false).get().body;
            Matcher matcher = Pattern.compile("\\{.+}").matcher(response);
            if(!matcher.find()) {
                throw new Exception();
            }
            JSONObject playerClanDetails = new JSONObject(response.substring(matcher.start(), matcher.end()));
            if(!playerClanDetails.has("clan")) {
                loading.failStage("Player is not part of a " + embedUrl);
                return null;
            }
            String clanName = playerClanDetails.getString("clan");
            String clanNameEncode = EmbedHelper.urlEncode(clanName);
            loading.updateStage("Player is in the **" + clanName + "** clan, fetching details...");
            Clan clan = new Clan(
                    clanName,
                    EmbedHelper.downloadImage(
                            BASE_URL + "m=avatar-rs/" + clanNameEncode + "/clanmotif.png"
                    )
            );

            String[] clanMembers = new NetworkRequest(
                    "http://services.runescape.com/m=clan-hiscores/members_lite.ws?clanName=" + clanNameEncode,
                    false
            )
                    .get()
                    .body
                    .replace("�", " ")
                    .split("\n");

            // First row is redundant column info
            for(int i = 1; i < clanMembers.length; i++) {
                String[] memberDetails = clanMembers[i].split(",");
                clan.addPlayer(memberDetails[0], ROLE.byName(memberDetails[1]));
            }
            ROLE playerRole = clan.getRoleByPlayerName(name);
            loading.completeStage(
                    "Player is **"
                            + playerRole.getPrefix()
                            + " "
                            + playerRole.getName()
                            + "** of the **" + clanName + "** " + embedUrl + "!"
            );
            return clan;
        }
        catch(Exception e) {
            e.printStackTrace();
            loading.failStage("Unable to determine if player is part of a " + embedUrl);
            return null;
        }
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
        Clan clan = getClan(name);
        String[] clues = parseClueScrolls(normal);

        RS3PlayerStats normalAccount = new RS3PlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                runeMetrics,
                PlayerStats.ACCOUNT.NORMAL,
                clan
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
                PlayerStats.ACCOUNT.IRON,
                clan
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
                    PlayerStats.ACCOUNT.HARDCORE,
                    clan
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
