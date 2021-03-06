package Runescape.Stats;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.ImageLoadingMessage;
import Command.Structure.PieChart;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Runescape.*;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Command.Structure.PieChart.*;
import static Runescape.Hiscores.LOADING_UPDATE_TYPE.*;
import static Runescape.Skill.SKILL_NAME.*;
import static Runescape.Stats.Clan.*;

/**
 * Build an image displaying a player's RS3 stats
 */
public class RS3Hiscores extends Hiscores<HiscoresArgs, RS3PlayerStats> {
    private final Color orange, yellow, red, blue;
    private final String BASE_URL = "https://secure.runescape.com/";
    private final BufferedImage skillsSection, clanSection, clueSection, titleSection, questSection;

    /**
     * Create the RS3 Hiscores instance
     *
     * @param emoteHelper Emote helper
     * @param helpMessage Help message to display in loading message
     */
    public RS3Hiscores(EmoteHelper emoteHelper, String helpMessage) {
        super(emoteHelper, ResourceHandler.RS3_BASE_PATH + "Templates/", FontManager.RS3_FONT, helpMessage);
        this.orange = new Color(EmbedHelper.RUNESCAPE_ORANGE);
        this.yellow = new Color(EmbedHelper.RUNESCAPE_YELLOW);
        this.blue = new Color(EmbedHelper.RUNESCAPE_BLUE);
        this.red = new Color(EmbedHelper.RUNESCAPE_RED);

        ResourceHandler handler = getResourceHandler();
        String templates = getResourcePath();
        this.skillsSection = handler.getImageResource(templates + "skills_section.png");
        this.clanSection = handler.getImageResource(templates + "clan_section.png");
        this.clueSection = handler.getImageResource(templates + "clue_section.png");
        this.titleSection = handler.getImageResource(templates + "title_section.png");
        this.questSection = handler.getImageResource(templates + "quest_section.png");
    }

    @Override
    public String getURL(String type, String name) {
        return BASE_URL + "m=hiscore" + type + "/index_lite.ws?player=" + EmbedHelper.urlEncode(name);
    }

    @Override
    public String getDefaultURL(String name, HiscoresArgs args) {
        return getNormalAccount(name);
    }

    @Override
    public String getLoadingTitle(String name, HiscoresArgs args) {
        return "RS3 Hiscores lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail(HiscoresArgs args) {
        return "https://vignette.wikia.nocookie.net/runescape2/images/a/a7/RuneScape_Companion_logo.png";
    }

    @Override
    public ArrayList<String> getLoadingCriteria(HiscoresArgs args) {
        ArrayList<String> loadingCriteria = new ArrayList<>();
        loadingCriteria.add("Player exists...");
        loadingCriteria.add("Checking RuneMetrics...");
        loadingCriteria.add("Checking clan...");
        loadingCriteria.add("Checking account type...");
        return loadingCriteria;
    }

    @Override
    public BufferedImage buildHiscoresImage(RS3PlayerStats playerStats, HiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        int overhang = 65; // title section left overhang

        BufferedImage skillSection = buildSkillSection(playerStats, args);
        BufferedImage clueSection = buildClueSection(playerStats.getClues());
        BufferedImage titleSection = buildTitleSection(playerStats);

        BufferedImage playerImage = new BufferedImage(
                titleSection.getWidth(),
                skillSection.getHeight() + titleSection.getHeight() + overhang,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = playerImage.getGraphics();
        g.drawImage(titleSection, 0, 0, null);
        g.drawImage(skillSection, overhang, titleSection.getHeight(), null);
        g.drawImage(clueSection, overhang + skillSection.getWidth(), titleSection.getHeight(), null);

        if(playerStats.hasRuneMetrics()) {
            g.drawImage(
                    buildQuestSection(playerStats.getRuneMetrics()),
                    overhang + skillSection.getWidth(),
                    titleSection.getHeight() + clueSection.getHeight(),
                    null
            );
        }

        if(playerStats.isClanMember()) {
            BufferedImage clanSection = buildClanSection(playerStats.getClan(), playerStats.getName());
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
        final BufferedImage clanSection = copyImage(this.clanSection);
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
        return clanSection;
    }

    /**
     * Build the clue section of the image
     *
     * @param clues Clue data
     * @return Clue scroll image section
     */
    private BufferedImage buildClueSection(Clue[] clues) {
        final BufferedImage clueSection = copyImage(this.clueSection);
        Graphics g = clueSection.getGraphics();
        g.setFont(getGameFont().deriveFont(40f));
        g.setColor(orange);
        int x = 330;
        int y = 174;
        FontMetrics fm = g.getFontMetrics();
        for(Clue clue : clues) {
            g.drawString(clue.getFormattedCompletions(), x, y - (fm.getHeight() / 2) + fm.getAscent());
            y += 140;
        }
        g.dispose();
        return clueSection;
    }

    /**
     * Build the skill section of the image
     *
     * @param playerStats Player stats
     * @param args        Hiscores arguments
     * @return Skill image section
     */
    private BufferedImage buildSkillSection(RS3PlayerStats playerStats, HiscoresArgs args) {
        final BufferedImage skillsSection = copyImage(this.skillsSection);
        Graphics g = skillsSection.getGraphics();
        g.setFont(getGameFont().deriveFont(55f));

        // First skill location
        int x = 170, ogX = x;
        int y = 72;

        int totalY = 1560;

        g.setColor(yellow);
        Skill[] skills = playerStats.getSkills();
        for(int i = 0; i < skills.length - 1; i++) {
            Skill skill = skills[i];
            int displayLevel = args.displayVirtualLevels() ? skill.getVirtualLevel() : skill.getLevel();
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
                String.valueOf(
                        args.displayVirtualLevels()
                                ? playerStats.getVirtualTotalLevel()
                                : playerStats.getTotalLevel()
                ),
                240,
                totalY
        );
        g.drawString(String.valueOf(playerStats.getCombatLevel()), 730, totalY);
        g.dispose();
        return skillsSection;
    }

    /**
     * Build the title section of the image
     *
     * @param playerStats Player stats
     * @return Title section of image
     */
    private BufferedImage buildTitleSection(RS3PlayerStats playerStats) {
        final BufferedImage titleSection = copyImage(this.titleSection);
        String name = playerStats.getName();
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
            BufferedImage typeImage = getResourceHandler().getImageResource(
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
        return titleSection;
    }

    /**
     * Build quest image section
     *
     * @param runeMetrics Player RuneMetrics data
     * @return Quest section displaying player quest stats
     */
    private BufferedImage buildQuestSection(RuneMetrics runeMetrics) {
        final BufferedImage questSection = copyImage(this.questSection);
        Section[] sections = new Section[]{
                new Section("Not Started", runeMetrics.getQuestsNotStarted(), red),
                new Section("In Progress", runeMetrics.getQuestsStarted(), blue),
                new Section("Completed", runeMetrics.getQuestsCompleted(), orange)
        };
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
    private Clue[] parseClueScrolls(String[] data) {
        data = Arrays.copyOfRange(data, 137, data.length - 2);
        Clue.TYPE[] clueTypes = new Clue.TYPE[]{
                Clue.TYPE.EASY,
                Clue.TYPE.MEDIUM,
                Clue.TYPE.HARD,
                Clue.TYPE.ELITE,
                Clue.TYPE.MASTER
        };
        Clue[] clues = new Clue[clueTypes.length];
        int j = 0;
        for(int i = 0; i < data.length; i += 2) {
            clues[j] = new Clue(
                    clueTypes[j],
                    Integer.parseInt(data[i]),
                    Integer.parseInt(data[i + 1])
            );
            j++;
        }
        return clues;
    }

    /**
     * Get the player's RuneMetrics stats if available
     *
     * @param name           Player name
     * @param loadingMessage Optional loading message
     * @return Player RuneMetrics stats or null
     */
    private RuneMetrics getRuneMetrics(String name, ImageLoadingMessage... loadingMessage) {
        String url = "https://apps.runescape.com/runemetrics/profile/profile?user=" + EmbedHelper.urlEncode(name);
        JSONObject profile = new JSONObject(
                new NetworkRequest(url, false).get().body
        );
        String message = "Player's " + EmbedHelper.embedURL("RuneMetrics", url) + " is";
        if(profile.has("error")) {
            updateLoadingMessage(FAIL, message + " private (Can't display Quest data)", loadingMessage);
            return null;
        }
        updateLoadingMessage(COMPLETE, message + " public!", loadingMessage);
        return new RuneMetrics(
                profile.getInt("questsstarted"),
                profile.getInt("questsnotstarted"),
                profile.getInt("questscomplete")
        );
    }

    /**
     * Get the clan that the given player is a member of (if they are in a clan)
     *
     * @param name           Player name
     * @param loadingMessage Optional loading message
     * @return Player clan or null
     */
    private Clan getClan(String name, ImageLoadingMessage... loadingMessage) {
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
                updateLoadingMessage(FAIL, "Player is not part of a " + embedUrl, loadingMessage);
                return null;
            }
            String clanName = playerClanDetails.getString("clan");
            String clanNameEncode = EmbedHelper.urlEncode(clanName);
            updateLoadingMessage(UPDATE, "Player is in the **" + clanName + "** clan, fetching details...", loadingMessage);
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
            updateLoadingMessage(
                    COMPLETE,
                    "Player is **"
                            + playerRole.getPrefix()
                            + " "
                            + playerRole.getName()
                            + "** of the **" + clanName + "** " + embedUrl + "!",
                    loadingMessage
            );
            return clan;
        }
        catch(Exception e) {
            e.printStackTrace();
            updateLoadingMessage(FAIL, "Unable to determine if player is part of a " + embedUrl, loadingMessage);
            return null;
        }
    }

    @Override
    protected RS3PlayerStats fetchPlayerData(String name, HiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        String url = getNormalAccount(name);
        String[] normal = hiscoresRequest(url);

        if(normal == null) {
            return null;
        }

        completeLoadingMessageStage(loadingMessage);

        RuneMetrics runeMetrics = getRuneMetrics(name, loadingMessage);
        Clan clan = getClan(name, loadingMessage);
        Clue[] clues = parseClueScrolls(normal);

        RS3PlayerStats normalAccount = new RS3PlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                runeMetrics,
                PlayerStats.ACCOUNT.NORMAL,
                clan
        );

        updateLoadingMessage(UPDATE, "Player exists, checking ironman hiscores", loadingMessage);
        String[] iron = hiscoresRequest(getIronmanAccount(name));

        if(iron == null) {
            updateLoadingMessage(COMPLETE, "Player is a normal account!", loadingMessage);
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
            updateLoadingMessage(COMPLETE, "Player is a de-ironed normal account!", loadingMessage);
            return normalAccount;
        }

        updateLoadingMessage(UPDATE, "Player is an Ironman, checking Hardcore Ironman hiscores", loadingMessage);
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
                    updateLoadingMessage(
                            COMPLETE,
                            status + " They were saved by a "
                                    + EmbedHelper.embedURL(
                                    "Jar of divine light",
                                    "https://runescape.fandom.com/wiki/Jar_of_divine_light") + "!",
                            loadingMessage
                    );
                    ironAccount.setHcimStatus(hcimStatus);
                    return ironAccount;
                }
                else {
                    updateLoadingMessage(COMPLETE, status + " What a loser!", loadingMessage);
                    return hardcoreAccount;
                }
            }
            updateLoadingMessage(COMPLETE, "Player is a Hardcore Ironman!", loadingMessage);
            return hardcoreAccount;
        }

        updateLoadingMessage(COMPLETE, "Player is an Ironman!", loadingMessage);
        return ironAccount;
    }

    @Override
    public String getNotFoundMessage(String name, HiscoresArgs args) {
        return "doesn't exist cunt";
    }
}
