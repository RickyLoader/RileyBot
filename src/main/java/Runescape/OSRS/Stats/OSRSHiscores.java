package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Bot.FontManager;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import Command.Structure.PieChart;
import Network.NetworkRequest;
import Runescape.Boss;
import Runescape.Hiscores;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.PlayerStats;
import Runescape.Skill;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Runescape.OSRS.League.LeagueTier.*;
import static Runescape.Skill.SKILL_NAME.*;

/**
 * Build an image displaying a player's OSRS stats
 */
public class OSRSHiscores extends Hiscores {
    private final String[] bossNames;
    private final boolean league, virtual, xp;
    public final static String leagueThumbnail = "https://i.imgur.com/xksIl6S.png";
    private final Font trackerFont;
    private final SimpleDateFormat
            parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'"),
            displayFormat = new SimpleDateFormat("dd/MM/yyyy");


    /**
     * Create the OSRS Hiscores instance
     *
     * @param channel     Channel to send message to
     * @param emoteHelper Emote helper
     * @param league      Use league hiscores or normal
     * @param virtual     Calculate virtual levels or display hiscores provided levels
     * @param xp          Get the XP tracker info for the player
     */
    public OSRSHiscores(MessageChannel channel, EmoteHelper emoteHelper, boolean league, boolean virtual, boolean xp) {
        super(channel, emoteHelper, "/Runescape/OSRS/", FontManager.OSRS_FONT);
        this.bossNames = Boss.getBossNames();
        this.league = league;
        this.virtual = virtual;
        this.xp = xp;
        this.trackerFont = FontManager.WISE_OLD_MAN_FONT;
    }

    @Override
    public ArrayList<String> getLoadingCriteria() {
        ArrayList<String> criteria = new ArrayList<>();
        if(league) {
            criteria.add("Player has League stats...");
            criteria.add("Player has stored Relic/Region data...");
            criteria.add("Calculating League Tier...");
        }
        else {
            criteria.add("Player exists...");
            criteria.add("Checking account type...");
            criteria.add("Fetching achievements...");
        }
        if(xp) {
            criteria.add("Checking XP tracker...");
        }
        return criteria;
    }

    @Override
    public String getLoadingTitle(String name) {
        return "OSRS " + (league ? "League" : "Hiscores") + " lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail() {
        return league ? leagueThumbnail : EmbedHelper.OSRS_LOGO;
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

    @Override
    public String getURL(String type, String name) {
        return "https://secure.runescape.com/m=hiscore_oldschool" + type + "/index_lite.ws?player=" + EmbedHelper.urlEncode(name);
    }

    @Override
    public String getDefaultURL(String name) {
        return league ? getLeagueAccount(name) : getNormalAccount(name);
    }

    @Override
    public String getNotFoundMessage(String name) {
        return league ? "isn't on the league hiscores" : "doesn't exist cunt";
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
     * Fetch the player stats
     *
     * @param name Player name
     * @return Player stats object
     */
    private PlayerStats fetchStats(String name) {
        String url = league ? getLeagueAccount(name) : getNormalAccount(name);
        String[] normal = hiscoresRequest(url);

        if(normal == null) {
            return null;
        }

        loading.completeStage();

        List<Boss> bossKills = parseBossKills(normal);
        String[] clues = parseClueScrolls(normal);

        OSRSPlayerStats normalAccount = new OSRSPlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                bossKills,
                league ? PlayerStats.ACCOUNT.LEAGUE : PlayerStats.ACCOUNT.NORMAL
        );

        if(league) {
            normalAccount.setLeaguePoints(Integer.parseInt(normal[73]), Long.parseLong(normal[72]));
            String leagueJSON = DiscordUser.getOSRSLeagueData(name);
            if(leagueJSON == null) {
                loading.failStage("Unable to connect to the API");
                return normalAccount;
            }
            JSONObject leagueData = new JSONObject(leagueJSON);
            normalAccount.setRegions(Region.parseRegions(leagueData.getJSONArray("regions")));
            normalAccount.setRelicTiers(RelicTier.parseRelics(leagueData.getJSONArray("relics")));
            if(normalAccount.hasLeagueUnlockData()) {
                loading.completeStage();
            }
            else {
                loading.failStage("Try 'trailblazer' command to store unlocks");
            }
            return normalAccount;
        }

        loading.updateStage("Player exists, checking ironman hiscores");
        String ironURL = getIronmanAccount(name);
        String[] iron = hiscoresRequest(ironURL);

        if(iron == null) {
            loading.completeStage("Player is a normal account!");
            return normalAccount;
        }

        OSRSPlayerStats ironAccount = new OSRSPlayerStats(
                name,
                ironURL,
                parseSkills(iron),
                clues,
                bossKills,
                PlayerStats.ACCOUNT.IRON
        );

        if(normalAccount.getTotalXP() > ironAccount.getTotalXP()) {
            loading.completeStage("Player is a de-ironed normal account!");
            return normalAccount;
        }

        loading.updateStage("Player is an Ironman, checking Hardcore Ironman hiscores");

        String hardcoreURL = getHardcoreAccount(name);
        String[] hardcore = hiscoresRequest(hardcoreURL);

        if(hardcore != null) {
            OSRSPlayerStats hardcoreAccount = new OSRSPlayerStats(
                    name,
                    hardcoreURL,
                    parseSkills(hardcore),
                    clues,
                    bossKills,
                    PlayerStats.ACCOUNT.HARDCORE
            );

            if(ironAccount.getTotalXP() > hardcoreAccount.getTotalXP()) {
                loading.completeStage("Player was a Hardcore Ironman and died! What a loser!");
                return ironAccount;
            }

            loading.completeStage("Player is a Hardcore Ironman!");
            return hardcoreAccount;
        }

        loading.updateStage("Player is not hardcore, checking Ultimate Ironman hiscores");

        String ultimateURL = getUltimateAccount(name);
        String[] ultimate = hiscoresRequest(ultimateURL);

        if(ultimate != null) {
            OSRSPlayerStats ultimateAccount = new OSRSPlayerStats(
                    name,
                    ultimateURL,
                    parseSkills(ultimate),
                    clues,
                    bossKills,
                    PlayerStats.ACCOUNT.ULTIMATE
            );

            if(ironAccount.getTotalXP() > ultimateAccount.getTotalXP()) {
                loading.completeStage("Player is an Ironman who chickened out of Ultimate Ironman!");
                return ironAccount;
            }

            loading.completeStage("Player is an Ultimate Ironman!");
            return ultimateAccount;
        }
        loading.completeStage("Player is an Ironman!");
        return ironAccount;
    }

    @Override
    public PlayerStats fetchPlayerData(String name) {
        PlayerStats playerStats = fetchStats(name);
        OSRSPlayerStats stats = (OSRSPlayerStats) playerStats;

        if(playerStats == null) {
            return null;
        }

        if(league) {
            LeagueTier leagueTier = stats.getLeagueTier();
            leagueTier.setTier(calculateTier(leagueTier.getRank()));
            loading.completeStage("Player is " + leagueTier.getTierName() + "!");
        }

        addPlayerAchievements(stats);

        if(xp) {
            getTrackerData(stats);
        }
        return playerStats;
    }

    /**
     * Fetch and add recent achievements for the player
     *
     * @param stats Player stats
     */
    private void addPlayerAchievements(OSRSPlayerStats stats) {
        try {
            String json = fetchRecentPlayerAchievementData(stats.getName(), league);
            if(json == null) {
                loading.failStage("Achievement tracker didn't respond, unlucky cunt");
                return;
            }

            JSONArray achievements = new JSONArray(json);
            String dateKey = "createdAt";
            String baseLevelRegex = "Base \\d+ Stats";

            for(int i = 0; i < achievements.length(); i++) {
                JSONObject achievement = achievements.getJSONObject(i);
                long progress = achievement.getLong("currentValue");

                // Ignore achievements with no current progress
                if(progress <= 0) {
                    continue;
                }
                String dateString = achievement.isNull(dateKey) ? null : achievement.getString(dateKey);
                String name = achievement.getString("name");

                stats.addAchievement(
                        new Achievement(
                                name,
                                name.matches(baseLevelRegex)
                                        ? "lowest xp"
                                        : achievement.getString("measure"),
                                achievement.getString("metric"),
                                progress,
                                achievement.getLong("threshold"),
                                dateString == null ? new Date(0) : parseFormat.parse(dateString)
                        )
                );
            }
            loading.completeStage(stats.getAchievementSummary());
        }
        catch(Exception e) {
            loading.failStage("Failed to parse achievement data!");
        }
    }

    /**
     * Update player tracking data/Begin tracking a player
     *
     * @param name   Player name
     * @param league Player is a league account
     * @param wait   Wait for response
     */
    private void updatePlayerTracking(String name, boolean league, boolean wait) {
        new NetworkRequest(
                "https://" + getTrackerDomain(league) + "/api/players/track",
                false
        ).post(
                new JSONObject().put("username", name).toString(),
                !wait
        );
    }

    /**
     * Get the domain to use for the tracker data
     *
     * @param league League XP tracker
     * @return domain to use for tracker data
     */
    private String getTrackerDomain(boolean league) {
        String domain = "wiseoldman.net";
        return league ? "trailblazer." + domain : domain;
    }

    /**
     * Fetch XP tracking data for the given player
     *
     * @param name   Player name
     * @param league Player is a league account
     * @return XP tracking data
     */
    private String fetchPlayerTrackingData(String name, boolean league) {
        return new NetworkRequest(
                "https://" + getTrackerDomain(league) + "/api/players/username/" + name + "/gained",
                false
        ).get().body;
    }

    /**
     * Fetch recent achievement data for the player
     *
     * @param name   Player name
     * @param league Player is a league account
     * @return Achievement data
     */
    private String fetchRecentPlayerAchievementData(String name, boolean league) {
        return new NetworkRequest(
                "https://" + getTrackerDomain(league) + "/api/players/username/" + name + "/achievements/progress",
                false
        ).get().body;
    }

    /**
     * Get the weekly tracker data for the given player.
     * If the player is not currently tracked begin tracking
     *
     * @param playerStats Player stats
     */
    private void getTrackerData(OSRSPlayerStats playerStats) {
        try {
            loading.updateStage("Checking player is tracked...");
            boolean league = playerStats.isLeague();
            String name = playerStats.getName();
            String json = fetchPlayerTrackingData(name, league); // Check if player exists
            if(json == null) {
                loading.failStage("XP tracker didn't respond, unlucky cunt");
                return;
            }
            if(json.equals("err")) {
                updatePlayerTracking(name, league, false); // Tracking a new player can take 20+ seconds, don't wait
                loading.completeStage("Player not tracked - They will be *soon*™");
                return;
            }

            loading.updateStage("Player is tracked, refreshing tracker...");
            updatePlayerTracking(name, league, true);
            loading.updateStage("Player is tracked, getting stats...");
            json = fetchPlayerTrackingData(name, league);

            JSONObject week = new JSONObject(json).getJSONObject("week");
            JSONObject stats = week.getJSONObject("data");

            for(String key : stats.keySet()) {
                JSONObject entry = stats.getJSONObject(key);
                if(!entry.has("experience")) {
                    continue;
                }
                Skill.SKILL_NAME skillName = Skill.SKILL_NAME.valueOf(key.toUpperCase());

                JSONObject experienceData = entry.getJSONObject("experience");
                playerStats.addGainedXP(skillName, experienceData.getLong("gained"));
            }
            playerStats.setTrackerPeriod(
                    parseFormat.parse(week.getString("startsAt")),
                    parseFormat.parse(week.getString("endsAt"))
            );
            loading.completeStage(playerStats.hasWeeklyGains() ? "Weekly XP obtained" : "No XP gained this week");
        }
        catch(Exception e) {
            loading.failStage("Failed to parse Weekly XP");
        }
    }

    /**
     * Calculate a player's league tier manually from the given rank
     *
     * @param rank Player league point rank
     * @return League tier
     */
    private LEAGUE_TIER calculateTier(long rank) {
        LEAGUE_TIER tier = LEAGUE_TIER.UNQUALIFIED;
        try {
            String json = new NetworkRequest("https://trailblazer.wiseoldman.net/api/league/tiers", false).get().body;
            if(json == null) {
                throw new Exception();
            }
            JSONArray tiers = new JSONArray(json);
            for(int i = 0; i < tiers.length(); i++) {
                JSONObject tierInfo = tiers.getJSONObject(i);
                if(rank > tierInfo.getLong("threshold")) {
                    break;
                }
                tier = LEAGUE_TIER.valueOf(tierInfo.getString("name").toUpperCase());
            }
            return tier;
        }
        catch(Exception e) {
            return tier;
        }
    }

    /**
     * Add boss kills to the base image or a red X if the
     * player has none
     *
     * @param baseImage Base player image
     * @param bosses    List of bosses
     */
    private void addBossKills(BufferedImage baseImage, List<Boss> bosses) {
        Graphics g = baseImage.getGraphics();
        g.setColor(Color.YELLOW);

        if(bosses.size() > 0) {
            int max = Math.min(5, bosses.size());

            // All images have 220px height, and the top name banner + bottom border has 260px total height, clue section has height of 425
            int padding = ((baseImage.getHeight() - 260 - 425) - (5 * 220)) / 6;

            // Height of top name banner
            int y = 230 + padding;

            int bossCentre = (int) (baseImage.getWidth() * 0.625); // mid point of boss image section
            g.setFont(getGameFont().deriveFont(70f));
            FontMetrics fm = g.getFontMetrics();

            for(int i = 0; i < max; i++) {
                Boss boss = bosses.get(i);
                BufferedImage bossImage = boss.getImage();
                g.drawImage(bossImage, bossCentre - (bossImage.getWidth() / 2), y, null);

                String kills = boss.formatKills();
                int killWidth = fm.stringWidth(kills);
                g.drawString(
                        kills,
                        (int) ((baseImage.getWidth() * 0.875) - killWidth / 2),
                        (y + (bossImage.getHeight() / 2) + (fm.getHeight() / 2))
                );
                y += 220 + padding;
            }
        }
        else {
            BufferedImage noBoss = getResourceHandler().getImageResource(
                    getResourcePath() + "Templates/no_boss.png"
            );
            g.drawImage(
                    noBoss,
                    (int) ((baseImage.getWidth() * 0.75)) - (noBoss.getWidth() / 2),
                    200 + (((baseImage.getHeight() - 200 - 425) / 2) - (noBoss.getHeight() / 2)),
                    null
            );
        }
        g.dispose();
    }

    /**
     * Build an image displaying player skills
     *
     * @param stats Player stats
     * @return Image displaying player skills
     */
    private BufferedImage buildSkillsImage(PlayerStats stats) {
        BufferedImage image = getResourceHandler().getImageResource(
                getResourcePath() + "Templates/stats_template.png"
        );

        Graphics g = image.getGraphics();
        g.setFont(getGameFont().deriveFont(65f));

        // First skill location
        int x = 200, ogX = x;
        int y = 315;
        Skill[] skills = stats.getSkills();

        for(int i = 0; i < skills.length - 1; i++) {
            Skill skill = skills[i];
            int displayLevel = virtual ? skill.getVirtualLevel() : skill.getLevel();
            boolean master = displayLevel > 99;
            String level = String.valueOf(displayLevel);

            g.setColor(Color.BLACK); // shadow

            g.drawString(level, master ? x - 10 : x + 5, y + 5); // top
            g.drawString(level, master ? x + 60 : x + 65, y + 65); // bottom

            g.setColor(Color.YELLOW); // skill

            g.drawString(level, master ? x - 15 : x, y); // top
            g.drawString(level, master ? x + 55 : x + 60, y + 60); // bottom

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

        g.setColor(Color.YELLOW);
        String level = String.valueOf(virtual ? stats.getVirtualTotalLevel() : stats.getTotalLevel());
        g.drawString(
                level,
                825 - (g.getFontMetrics().stringWidth(level) / 2),
                y + 65
        );
        g.dispose();
        return image;
    }

    @Override
    public BufferedImage buildHiscoresImage(PlayerStats playerStats) {
        OSRSPlayerStats stats = (OSRSPlayerStats) playerStats;

        BufferedImage baseImage = buildSkillsImage(playerStats);
        addBossKills(baseImage, stats.getBossKills());
        addClues(baseImage, playerStats.getClues());
        addNameSection(baseImage, stats);

        if(stats.isLeague()) {
            baseImage = addLeagueInfo(baseImage, stats);
        }

        if(xp && stats.hasWeeklyGains()) {
            baseImage = addXPTracker(baseImage, stats);
        }

        if(!league && stats.hasAchievements()) {
            baseImage = addAchievementInfo(baseImage, stats);
        }
        return baseImage;
    }

    /**
     * Add player achievement info. Display most recently completed & closest to be completed achievements.
     *
     * @param baseImage Base player image
     * @param stats     Player stats
     * @return Base image with achievements appended
     */
    private BufferedImage addAchievementInfo(BufferedImage baseImage, OSRSPlayerStats stats) {

        // Sort by most recent completion first
        BufferedImage completedAchievements = buildAchievementsSection(
                stats.getCompletedAchievements(),
                (o1, o2) -> o2.getCompletionDate().compareTo(o1.getCompletionDate()),
                "Recent Achievements"
        );

        // Sort by closest to completion first
        BufferedImage inProgressAchievements = buildAchievementsSection(
                stats.getInProgressAchievements(),
                (o1, o2) -> Double.compare(o2.getProgressPercent(), o1.getProgressPercent()),
                "Upcoming Achievements"
        );

        BufferedImage achievementsImage = new BufferedImage(
                baseImage.getWidth(),
                baseImage.getHeight() + completedAchievements.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = achievementsImage.getGraphics();
        g.drawImage(baseImage, 0, 0, null);

        int x = 0;
        if(!stats.getCompletedAchievements().isEmpty()) {
            g.drawImage(completedAchievements, x, baseImage.getHeight(), null);
            x += completedAchievements.getWidth();
        }
        if(!stats.getInProgressAchievements().isEmpty()) {
            g.drawImage(inProgressAchievements, x, baseImage.getHeight(), null);
        }
        g.dispose();
        return achievementsImage;
    }

    /**
     * Build an image displaying the top 5 from the given list of achievements
     *
     * @param achievements Achievements to display
     * @param comparator   Comparator for sorting achievements (the first 5 will be displayed after sorting)
     * @param title        Title to display
     * @return Image displaying achievements
     */
    private BufferedImage buildAchievementsSection(ArrayList<Achievement> achievements, Comparator<Achievement> comparator, String title) {
        BufferedImage achievementsContainer = getResourceHandler()
                .getImageResource(getResourcePath() + "Templates/achievement_container.png");
        achievements.sort(comparator);
        Graphics g = achievementsContainer.getGraphics();

        int y = 225;
        int border = 25;
        int max = 5;
        int titleSectionHeight = (y - 2 * border);

        g.setFont(getGameFont().deriveFont(80f));
        g.setColor(Color.YELLOW);

        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                title,
                (achievementsContainer.getWidth() / 2) - (fm.stringWidth(title) / 2),
                ((titleSectionHeight / 2) + (fm.getMaxAscent() / 2)) + border
        );

        int rowCount = Math.min(achievements.size(), max);

        // Always calculate for max rows
        int rowHeight = (achievementsContainer.getHeight() - y - border) / max;
        int rowWidth = achievementsContainer.getWidth() - (2 * border);

        Color dark = new Color(EmbedHelper.ROW_DARK);
        Color light = new Color(EmbedHelper.ROW_LIGHT);

        for(int i = 0; i < rowCount; i++) {
            Achievement achievement = achievements.get(i);
            boolean even = i % 2 == 0;
            g.drawImage(
                    buildAchievementRow(
                            achievement,
                            even ? dark : light,
                            even ? light : dark,
                            rowWidth,
                            rowHeight
                    ),
                    border,
                    y,
                    null
            );
            y += rowHeight;
        }

        g.dispose();
        return achievementsContainer;
    }

    /**
     * Build an achievement row displaying the given achievement details.
     *
     * @param achievement Achievement to display
     * @param rowColour   Background colour to use behind row
     * @param chartColour Background colour to use behind chart
     * @param width       Width of row
     * @param height      Height of row
     * @return Achievement row image
     */
    private BufferedImage buildAchievementRow(Achievement achievement, Color rowColour, Color chartColour, int width, int height) {
        BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = row.getGraphics();
        g.setFont(trackerFont.deriveFont(35f));

        int chartSectionWidth = width / 3;
        int midY = height / 2;
        int padding = 10;

        g.setColor(rowColour);
        g.fillRect(0, 0, width - chartSectionWidth, height);
        g.setColor(chartColour);
        g.fillRect(width - chartSectionWidth, 0, width, height);

        BufferedImage progressImage = getAchievementChart(achievement, (height / 2) - padding);

        g.drawImage(
                progressImage,
                (width - chartSectionWidth / 2) - (progressImage.getWidth() / 2),
                midY - (progressImage.getHeight() / 2),
                null
        );


        g.setColor(achievement.isCompleted() ? Color.GREEN : Color.WHITE);
        FontMetrics fm = g.getFontMetrics();

        int midLeftX = (width - chartSectionWidth) / 2;

        DecimalFormat df = new DecimalFormat("#,###");
        ArrayList<String> summary = new ArrayList<>();
        summary.add(achievement.getName());
        summary.add(
                df.format(achievement.getProgress())
                        + "/"
                        + df.format(achievement.getThreshold())
                        + " " + achievement.getMeasure()
        );

        if(achievement.isCompleted()) {
            String date = achievement.hasCompletionDate()
                    ? displayFormat.format(achievement.getCompletionDate())
                    : "Unknown";
            summary.add("Date: " + date);
        }

        int lineHeight = fm.getMaxAscent();
        int gap = 20;
        int textHeight = (lineHeight * summary.size()) + (gap * (summary.size() - 1)); // total size occupied by text

        // text draws from bottom up, add line height so first line is drawn with the top at textHeight
        int textY = midY - (textHeight / 2) + lineHeight;

        for(String line : summary) {
            g.drawString(line, midLeftX - (fm.stringWidth(line) / 2), textY);
            textY += lineHeight + gap;
        }
        g.dispose();
        return row;
    }

    /**
     * Get a pie chart image for the given achievement
     *
     * @param achievement Achievement to build chart image for
     * @param radius      Radius to use
     * @return Achievement pie chart
     */
    private BufferedImage getAchievementChart(Achievement achievement, int radius) {
        return new PieChart(
                new PieChart.Section[]{
                        new PieChart.Section(
                                "Complete",
                                achievement.getProgress(),
                                Color.GREEN
                        ),
                        new PieChart.Section(
                                "Incomplete",
                                achievement.getRemaining(),
                                Color.RED
                        )
                },
                getGameFont(),
                false,
                radius
        ).getChart();
    }

    /**
     * Add the league points, tier, and relic/region data if available
     *
     * @param baseImage Base player image
     * @param stats     Player stats
     * @return Base image with league data appended
     */
    private BufferedImage addLeagueInfo(BufferedImage baseImage, OSRSPlayerStats stats) {
        Graphics g = baseImage.getGraphics();
        g.setFont(getGameFont().deriveFont(65f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        BufferedImage leaguePointImage = getResourceHandler().getImageResource(
                PlayerStats.getAccountTypeImagePath(stats.getAccountType())
        );

        int x = 50;
        int y = 35;

        g.drawImage(leaguePointImage, x, y, null);

        LeagueTier leagueTier = stats.getLeagueTier();

        String points = new DecimalFormat("#,### points").format(leagueTier.getPoints());
        int titleX = x + leaguePointImage.getWidth() + 25;

        g.drawString(
                points,
                titleX,
                y + (leaguePointImage.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );

        if(leagueTier.hasTier()) {
            BufferedImage tierIcon = getResourceHandler().getImageResource(leagueTier.getTierImagePath());
            y += leaguePointImage.getHeight() + 3;
            g.drawImage(tierIcon, x, y, null);

            g.drawString(
                    leagueTier.getTierName(),
                    titleX,
                    y + (tierIcon.getHeight() / 2) + (fm.getMaxAscent() / 2)
            );
        }
        g.dispose();

        if(stats.hasLeagueUnlockData()) {
            return buildLeagueImage(baseImage, stats);
        }
        return baseImage;
    }

    /**
     * Add the name and account type to the base image
     *
     * @param baseImage Base player image
     * @param stats     Player stats
     */
    private void addNameSection(BufferedImage baseImage, OSRSPlayerStats stats) {
        Graphics g = baseImage.getGraphics();
        g.setFont(getGameFont().deriveFont(140f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        String name = stats.getName();

        int centerVertical = 115;
        int nameWidth = fm.stringWidth(name);

        int x = (baseImage.getWidth() / 2) - (nameWidth / 2);

        g.drawString(name.toUpperCase(), x, centerVertical + (fm.getMaxAscent() / 2));

        g.setFont(getGameFont().deriveFont(75f));
        fm = g.getFontMetrics();

        PlayerStats.ACCOUNT accountType = stats.getAccountType();

        if(accountType != PlayerStats.ACCOUNT.NORMAL && !stats.isLeague()) {
            BufferedImage accountImage = getResourceHandler().getImageResource(
                    PlayerStats.getAccountTypeImagePath(stats.getAccountType())
            );
            g.drawImage(
                    accountImage,
                    x - (int) (accountImage.getWidth() * 1.5),
                    centerVertical - (accountImage.getHeight() / 2),
                    null
            );
        }

        String rank = "Rank: " + stats.getFormattedRank();
        BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
        int rankX = baseImage.getWidth() - fm.stringWidth(rank) - 50;
        g.drawString(rank, rankX, centerVertical + (fm.getMaxAscent() / 2));

        g.drawImage(
                rankImage,
                rankX - rankImage.getWidth() - 15,
                centerVertical - (rankImage.getHeight() / 2),
                null
        );
        g.dispose();
    }

    /**
     * Add clue scroll completions to the base image
     *
     * @param baseImage Base player image
     * @param clues     List of clue completions
     */
    private void addClues(BufferedImage baseImage, String[] clues) {
        Graphics g = baseImage.getGraphics();
        g.setFont(getGameFont().deriveFont(65f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        int x = 170;
        int y = 1960;
        for(String quantity : clues) {
            int quantityWidth = fm.stringWidth(quantity) / 2;
            g.drawString(quantity, x - quantityWidth, y);
            x += 340;
        }
        g.dispose();
    }

    /**
     * Append XP tracker info to the player image
     *
     * @param baseImage Base player image
     * @param stats     Player stats
     * @return Base image with XP tracker appended
     */
    private BufferedImage addXPTracker(BufferedImage baseImage, OSRSPlayerStats stats) {
        BufferedImage trackerSection = buildTrackerSection(stats);
        BufferedImage trackerImage = new BufferedImage(
                baseImage.getWidth() + trackerSection.getWidth(),
                baseImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = trackerImage.getGraphics();
        g.drawImage(baseImage, 0, 0, null);
        g.drawImage(trackerSection, baseImage.getWidth(), 0, null);
        g.dispose();
        return trackerImage;
    }

    /**
     * Build an image displaying the player's weekly gained XP
     *
     * @param stats Player stats
     * @return Image displaying player's weekly gained XP
     */
    private BufferedImage buildTrackerSection(OSRSPlayerStats stats) {
        BufferedImage image = getResourceHandler()
                .getImageResource(
                        getResourcePath() + "Templates/xp_tracker_container.png"
                );

        Graphics g = image.getGraphics();
        g.setFont(trackerFont.deriveFont(40f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        String trackerPeriod = displayFormat.format(stats.getTrackerStartDate())
                + " - "
                + displayFormat.format(stats.getTrackerEndDate());

        g.drawString(
                trackerPeriod,
                660 - (fm.stringWidth(trackerPeriod) / 2),
                166
        );

        DecimalFormat df = new DecimalFormat("#,### XP");
        int iconX = 64, skillX = 150, expX = 500, gap = 68, y = 362;

        for(Skill skill : stats.getSkills()) {
            BufferedImage icon = getResourceHandler().getImageResource(skill.getImagePath());
            int sectionMid = y - (gap / 2);
            g.drawImage(icon, iconX, sectionMid - (icon.getHeight() / 2), null);

            int textY = sectionMid + (fm.getMaxAscent() / 2);

            String name = StringUtils.capitalize(skill.getName().name().toLowerCase());
            String xp = df.format(skill.getGainedXP());
            if(skill.hasGainedXP()) {
                g.setColor(Color.GREEN);
                xp = "+" + xp;
            }
            else {
                g.setColor(Color.WHITE);
            }
            g.drawString(name, skillX, textY);
            g.drawString(xp, expX, textY);
            y += gap;
        }
        g.dispose();
        return image;
    }

    /**
     * Append league relics and region unlocks to the player image
     *
     * @param baseImage Base Player image
     * @param stats     Player stats
     * @return Base image with league relics/regions appended
     */
    private BufferedImage buildLeagueImage(BufferedImage baseImage, OSRSPlayerStats stats) {
        BufferedImage regionImage = buildRegionSection(stats.getRegions());
        BufferedImage relicImage = buildRelicSection(stats.getRelicTiers());

        BufferedImage leagueImage = new BufferedImage(
                baseImage.getWidth(),
                baseImage.getHeight() + regionImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = leagueImage.getGraphics();
        g.drawImage(baseImage, 0, 0, null);
        g.drawImage(relicImage, 0, baseImage.getHeight(), null);
        g.drawImage(regionImage, relicImage.getWidth(), baseImage.getHeight(), null);
        g.dispose();
        return leagueImage;
    }

    /**
     * Create an image displaying the unlocked/locked relic tiers
     * of a league player
     *
     * @param relicTiers List of unlocked relics
     * @return Image displaying player relics
     */
    private BufferedImage buildRelicSection(ArrayList<RelicTier> relicTiers) {
        BufferedImage relicContainer = getResourceHandler().getImageResource(
                getResourcePath() + "Templates/relic_container.png"
        );
        BufferedImage lockedRelic = getResourceHandler().getImageResource(Relic.res + Relic.lockedRelic);
        Graphics g = relicContainer.getGraphics();
        g.setFont(getGameFont().deriveFont(40f));
        FontMetrics fm = g.getFontMetrics();

        int x = 170, ogX = 170, y = 170;

        // Always display 6 relics - Anything less than max is considered locked
        for(int i = 0; i < RelicTier.MAX_RELICS; i++) {
            BufferedImage relicImage;
            if(i >= relicTiers.size()) {
                relicImage = lockedRelic;
            }
            else {
                Relic relic = relicTiers.get(i).getRelicByIndex(0);
                relicImage = getResourceHandler().getImageResource(relic.getImagePath());
                String name = relic.getName();
                g.drawString(
                        name,
                        x - (fm.stringWidth(name) / 2),
                        y + (relicImage.getHeight() / 2) + 15 + fm.getMaxAscent()
                );

            }
            g.drawImage(
                    relicImage,
                    x - (relicImage.getWidth() / 2),
                    y - (relicImage.getHeight() / 2),
                    null
            );

            if((i + 1) % 3 == 0) {
                x = ogX;
                y += 312;
            }
            else {
                x += 340;
            }
        }
        g.dispose();
        return relicContainer;
    }

    /**
     * Create an image displaying the unlocked/locked regions
     * of a league player
     *
     * @param regions List of unlocked regions
     * @return Image displaying player regions
     */
    private BufferedImage buildRegionSection(ArrayList<Region> regions) {
        BufferedImage map = getResourceHandler().getImageResource(Region.res + Region.baseMap);
        BufferedImage mapContainer = getResourceHandler().getImageResource(
                getResourcePath() + "Templates/map_container.png"
        );
        Graphics g = map.getGraphics();
        for(Region region : regions) {
            g.drawImage(
                    getResourceHandler().getImageResource(region.getImagePath()),
                    0,
                    0,
                    null
            );
        }
        g = mapContainer.getGraphics();
        g.drawImage(
                map,
                (mapContainer.getWidth() / 2) - (map.getWidth() / 2),
                (mapContainer.getHeight() / 2) - (map.getHeight() / 2),
                null
        );
        g.dispose();
        return mapContainer;
    }

    /**
     * Parse and sort skill data from the API CSV to the in-game display order
     *
     * @param csv CSV from API
     * @return Sorted CSV
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
                new Skill(OVERALL, 0, csv)
        };
    }

    /**
     * Build a sorted list of boss kill counts
     *
     * @param csv csv from API
     * @return Sorted list of boss kill counts
     */
    private List<Boss> parseBossKills(String[] csv) {
        List<String> stats = Arrays.asList(csv).subList(96, csv.length);
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
}
