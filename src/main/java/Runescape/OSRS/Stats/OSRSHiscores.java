package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import Command.Structure.PieChart;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Runescape.*;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.Skill.SKILL_NAME;
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
    private final Boss.BOSS_NAME[] bossNames;
    private final boolean league, virtual, xp;
    public static final String LEAGUE_THUMBNAIL = "https://i.imgur.com/xksIl6S.png";
    private final Font trackerFont;
    private final String displayFormatDate = "dd/MM/yyyy";
    private final int border = 25; // Horizontal & vertical borders on template images
    private final Color opaqueRed;
    private final SimpleDateFormat
            parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            displayFormat = new SimpleDateFormat(displayFormatDate);

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
        super(channel, emoteHelper, ResourceHandler.OSRS_BASE_PATH + "Templates/", FontManager.OSRS_FONT);
        this.bossNames = Boss.BOSS_NAME.getNamesInHiscoresOrder();
        this.league = league;
        this.virtual = virtual;
        this.xp = xp;
        this.trackerFont = FontManager.WISE_OLD_MAN_FONT;
        this.opaqueRed = new Color(255, 0, 0, 127); // 50% opacity
        parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        return league ? LEAGUE_THUMBNAIL : EmbedHelper.OSRS_LOGO;
    }

    /**
     * Parse and format the player's clue scroll data from the hiscores CSV
     *
     * @param data CSV data from API
     * @return Clue scroll data
     */
    private Clue[] parseClueScrolls(String[] data) {
        data = Arrays.copyOfRange(data, Clue.CLUE_START_INDEX, Clue.CLUE_END_INDEX);
        Clue.TYPE[] clueTypes = new Clue.TYPE[]{
                Clue.TYPE.ALL,
                Clue.TYPE.BEGINNER,
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
     * Parse and format the player's LMS data from the hiscores CSV
     *
     * @param data CSV data from API
     * @return Last Man Standing info
     */
    private LastManStanding parseLmsInfo(String[] data) {
        return new LastManStanding(
                Integer.parseInt(data[LastManStanding.RANK_INDEX]),
                Integer.parseInt(data[LastManStanding.POINTS_INDEX])
        );
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
        Clue[] clues = parseClueScrolls(normal);
        LastManStanding lmsInfo = parseLmsInfo(normal);

        OSRSPlayerStats normalAccount = new OSRSPlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                bossKills,
                lmsInfo,
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
                lmsInfo,
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
                    lmsInfo,
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
                    lmsInfo,
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
        else {
            addPlayerAchievements(stats);
        }

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
            loading.updateStage("Checking player is tracked...");
            NetworkResponse response = fetchRecentPlayerAchievementData(stats.getName(), league);

            if(response.code == -1) {
                loading.failStage("Achievement tracker didn't respond, unlucky cunt");
                return;
            }

            if(response.code == 404) {
                updatePlayerTracking(stats.getName(), league, false); // Tracking a new player can take 20+ seconds, don't wait
                loading.completeStage("Player not tracked - They will be *soon*™");
                return;
            }

            JSONArray achievements = new JSONArray(response.body);
            String dateKey = "createdAt";

            for(int i = 0; i < achievements.length(); i++) {
                JSONObject achievement = achievements.getJSONObject(i);
                long progress = achievement.getLong("currentValue");

                // Ignore achievements with no current progress
                if(progress <= 0) {
                    continue;
                }
                String dateString = achievement.isNull(dateKey) ? null : achievement.getString(dateKey);
                stats.addAchievement(
                        new Achievement(
                                achievement.getString("name"),
                                achievement.getString("measure"),
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
     * Get the weekly XP tracker browser URL (the URL to view the tracker directly in a browser)
     *
     * @param name   Player name
     * @param league League XP tracker
     * @return XP tracker browser URL
     */
    private String getTrackerBrowserUrl(String name, boolean league) {
        return "https://" + getTrackerDomain(league)
                + "/players/" + name.replaceAll(" ", "%20") + "/gained/skilling?period=week";
    }

    /**
     * Fetch XP tracking data for the given player
     *
     * @param name   Player name
     * @param league Player is a league account
     * @return XP tracking data
     */
    private NetworkResponse fetchPlayerTrackingData(String name, boolean league) {
        return new NetworkRequest(
                "https://" + getTrackerDomain(league) + "/api/players/username/" + name + "/gained",
                false
        ).get();
    }

    /**
     * Fetch recent achievement data for the player
     *
     * @param name   Player name
     * @param league Player is a league account
     * @return Achievement data
     */
    private NetworkResponse fetchRecentPlayerAchievementData(String name, boolean league) {
        return new NetworkRequest(
                "https://" + getTrackerDomain(league) + "/api/players/username/" + name + "/achievements/progress",
                false
        ).get();
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
            NetworkResponse response = fetchPlayerTrackingData(name, league); // Check if player exists

            if(response.code == -1) {
                loading.failStage("XP tracker didn't respond, unlucky cunt");
                return;
            }

            if(response.code == 404) {
                loading.completeStage("Player not tracked - They will be *soon*™");
                return;
            }

            loading.updateStage("Player is tracked, refreshing tracker...");
            updatePlayerTracking(name, league, true);
            loading.updateStage("Player is tracked, getting stats...");
            response = fetchPlayerTrackingData(name, league);

            JSONObject week = new JSONObject(response.body).getJSONObject("week");
            JSONObject stats = week.getJSONObject("data");

            for(String key : stats.keySet()) {
                JSONObject entry = stats.getJSONObject(key);
                if(!entry.has("experience")) {
                    continue;
                }
                SKILL_NAME skillName = SKILL_NAME.fromName(key);
                if(skillName == UNKNOWN) {
                    continue;
                }
                JSONObject experienceData = entry.getJSONObject("experience");
                playerStats.addGainedXP(skillName, experienceData.getLong("gained"));
            }
            playerStats.setTrackerPeriod(
                    parseFormat.parse(week.getString("startsAt")),
                    parseFormat.parse(week.getString("endsAt"))
            );

            String beginningAt = new SimpleDateFormat(displayFormatDate + "  HH:mm:ss")
                    .format(playerStats.getTrackerStartDate());
            String trackerUrl = getTrackerBrowserUrl(name, league);

            String details = playerStats.hasWeeklyGains()
                    ? "Weekly XP " + EmbedHelper.embedURL("obtained", trackerUrl)
                    : "No XP " + EmbedHelper.embedURL("gained", trackerUrl);

            loading.completeStage(details + " for week beginning at: " + beginningAt);
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
        final int maxBosses = 5;

        final int bossSectionWidth = 960;
        final int bossSectionX = 1044;
        final int bossSectionY = 223;

        // TODO modular
        final int bossSectionHeight = (baseImage.getHeight() - bossSectionY - border);

        // Always calculate for max bosses
        final int bossRowHeight = bossSectionHeight / maxBosses;

        if(bosses.size() > 0) {
            final int bossDisplayCount = Math.min(maxBosses, bosses.size());
            int y = bossSectionY;

            for(int i = 0; i < bossDisplayCount; i++) {
                final BufferedImage bossImage = buildBossRowImage(
                        bosses.get(i),
                        bossSectionWidth,
                        bossRowHeight
                );
                g.drawImage(bossImage, bossSectionX, y, null);
                y += bossRowHeight;
            }
        }
        else {
            // Draw an opaque red layer over boss area
            g.setColor(opaqueRed);
            g.fillRect(bossSectionX, bossSectionY, bossSectionWidth, bossSectionHeight);
        }
        g.dispose();
    }

    /**
     * Build an image displaying the given boss and the player's total kills.
     * This image has a transparent background to be drawn on top of the boss section.
     *
     * @param boss   Boss to display
     * @param width  Width of boss image to build
     * @param height Height of clue image to build
     * @return Boss image
     */
    private BufferedImage buildBossRowImage(Boss boss, int width, int height) {
        final int boxWidth = (width - border) / 2;
        final int centreVertical = height / 2;
        final int centreHorizontal = boxWidth / 2;

        BufferedImage bossBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        final BufferedImage bossImage = boss.getFullImage();
        if(bossImage == null) {
            return bossBox;
        }

        Graphics g = bossBox.getGraphics();
        // Draw boss image centered in boss box
        g.drawImage(
                bossImage,
                centreHorizontal - (bossImage.getWidth() / 2),
                centreVertical - (bossImage.getHeight() / 2),
                null
        );

        final BufferedImage textBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        g = textBox.getGraphics();
        g.setFont(getGameFont().deriveFont(70f));
        g.setColor(Color.YELLOW);

        final FontMetrics fm = g.getFontMetrics();

        // Draw above centre line
        final String kills = boss.getFormattedKills();
        g.drawString(
                kills,
                centreHorizontal - (fm.stringWidth(kills) / 2),
                centreVertical
        );

        // Rank title will be different colour so must be drawn as two separate Strings
        final String rankTitle = "Rank: ";
        final String rankValue = boss.getFormattedRank(); // 1,234 kills
        final int rankTitleWidth = fm.stringWidth(rankTitle);

        /*
         * Calculate where to begin drawing the rank title such that when both Strings are drawn beside
         * each other, the full String is centered.
         */
        final int x = centreHorizontal - ((rankTitleWidth + fm.stringWidth(rankValue)) / 2);

        // Draw rank below centre line
        final int y = centreVertical + fm.getMaxAscent();
        g.drawString(rankValue, x + rankTitleWidth, y); // Add rank title width to leave room for rank title to be drawn

        g.setColor(Color.WHITE);
        g.drawString(rankTitle, x, y);


        // Combine in to row with border-sized gap in centre for mid border
        final BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = row.getGraphics();
        g.drawImage(bossBox, 0, 0, null);
        g.drawImage(textBox, boxWidth + border, 0, null);
        g.dispose();
        return row;
    }

    /**
     * Build an image displaying player skills
     *
     * @param stats Player stats
     * @return Image displaying player skills
     */
    private BufferedImage buildSkillsImage(PlayerStats stats) {
        BufferedImage image = getResourceHandler().getImageResource(
                getResourcePath() + "stats_template.png"
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
        addNameSection(baseImage, stats);

        if(playerStats.hasClueCompletions()) {
            baseImage = addClues(baseImage, playerStats.getClues());
        }

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
                .getImageResource(getResourcePath() + "achievement_container.png");
        achievements.sort(comparator);
        Graphics g = achievementsContainer.getGraphics();

        int y = 225;
        final int max = 5;
        final int titleSectionHeight = (y - 2 * border);

        g.setFont(getGameFont().deriveFont(80f));
        g.setColor(Color.YELLOW);

        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                title,
                (achievementsContainer.getWidth() / 2) - (fm.stringWidth(title) / 2),
                ((titleSectionHeight / 2) + (fm.getMaxAscent() / 2)) + border
        );

        final int rowCount = Math.min(achievements.size(), max);

        // Always calculate for max achievements
        final int rowHeight = (achievementsContainer.getHeight() - y - border) / max;
        final int rowWidth = achievementsContainer.getWidth() - (2 * border);

        final Color dark = new Color(EmbedHelper.ROW_DARK);
        final Color light = new Color(EmbedHelper.ROW_LIGHT);

        for(int i = 0; i < rowCount; i++) {
            Achievement achievement = achievements.get(i);
            final boolean even = i % 2 == 0;
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
     * Get a pie chart image for the given achievement. If the achievement has an associated image,
     * centre it in the chart.
     *
     * @param achievement Achievement to build chart image for
     * @param radius      Radius to use
     * @return Achievement pie chart
     */
    private BufferedImage getAchievementChart(Achievement achievement, int radius) {
        BufferedImage chart = new PieChart(
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

        if(achievement.canHaveImage()) {
            Graphics g = chart.getGraphics();
            BufferedImage achievementImage = achievement.getImage();

            // Skill/boss/etc may not have an image
            if(achievementImage == null) {
                return chart;
            }
            g.drawImage(
                    achievementImage,
                    (chart.getWidth() / 2) - (achievementImage.getWidth() / 2),
                    (chart.getHeight() / 2) - (achievementImage.getHeight() / 2),
                    null
            );
            g.dispose();
        }
        return chart;
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
    private BufferedImage addClues(BufferedImage baseImage, Clue[] clues) {
        final BufferedImage clueSection = getResourceHandler().getImageResource(
                getResourcePath() + "clues_container.png"
        );

        Graphics g = clueSection.getGraphics();

        final int maxClues = clues.length - 1; // Not drawing "ALL" type clue
        final int border = 28; // TODO fix

        // Always calculate for max clues
        final int clueWidth = (clueSection.getWidth() - (2 * border)) / maxClues;
        final int clueHeight = clueSection.getHeight() - (2 * border);

        int x = border;

        // Build clue stat images with transparent backgrounds to slot in to the clue section
        for(Clue clue : clues) {
            if(clue.getType() == Clue.TYPE.ALL) {
                continue;
            }
            final BufferedImage clueImage = buildClueImage(clue, clueWidth, clueHeight);
            g.drawImage(clueImage, x, border, null);
            x += clueWidth;
        }

        final BufferedImage combined = new BufferedImage(
                baseImage.getWidth(),
                baseImage.getHeight() + clueSection.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        g = combined.getGraphics();
        g.drawImage(baseImage, 0, 0, null);
        g.drawImage(clueSection, 0, baseImage.getHeight(), null);
        g.dispose();
        return combined;
    }

    /**
     * Build an image displaying the given clue and the player's total completions of it.
     * This image has a transparent background to be drawn on top of the clue section.
     *
     * @param clue   Clue to display
     * @param width  Width of clue image to build
     * @param height Height of clue image to build
     * @return Clue image
     */
    private BufferedImage buildClueImage(Clue clue, int width, int height) {
        final BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = background.getGraphics();

        g.setFont(getGameFont().deriveFont(65f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int centreVertical = height / 2;
        final int centreHorizontal = width / 2;

        final BufferedImage clueImage = clue.getFullImage(ResourceHandler.OSRS_BASE_PATH);

        if(clueImage == null) {
            return background;
        }

        // Draw clue centered in background
        g.drawImage(
                clueImage,
                centreHorizontal - (clueImage.getWidth() / 2),
                centreVertical - (clueImage.getHeight() / 2),
                null
        );

        // Height of area above & below clue
        final int textBoxHeight = (height - clueImage.getHeight()) / 2;

        // Add this to top of a text box to centre text
        final int textY = (textBoxHeight / 2) + (fm.getMaxAscent() / 2);

        final String name = clue.getType().getName();
        g.drawString(
                name,
                centreHorizontal - (fm.stringWidth(name) / 2),
                textY // Top text box begins at 0, drawing at textY centres text in it
        );

        final String completions = clue.getFormattedCompletions();
        g.drawString(
                completions,
                centreHorizontal - (fm.stringWidth(completions) / 2),
                (height - textBoxHeight) + textY // Bottom text box begins at (height - textBoxHeight)
        );

        // Draw an opaque red layer over incomplete clues
        if(!clue.hasCompletions()) {
            g.setColor(opaqueRed);
            g.fillRect(0, 0, width, height);
        }

        g.dispose();
        return background;
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
                        getResourcePath() + "xp_tracker_container.png"
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
            BufferedImage icon = skill.getImage();
            int sectionMid = y - (gap / 2);
            if(icon != null) {
                g.drawImage(icon, iconX, sectionMid - (icon.getHeight() / 2), null);
            }

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
                getResourcePath() + "relic_container.png"
        );
        BufferedImage lockedRelic = getResourceHandler().getImageResource(Relic.RES + Relic.LOCKED_RELIC_FILENAME);
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
        BufferedImage map = getResourceHandler().getImageResource(Region.RES + Region.BASE_MAP_FILENAME);
        BufferedImage mapContainer = getResourceHandler().getImageResource(
                getResourcePath() + "map_container.png"
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
     * Build a sorted list of player boss kill data
     * Sort in descending order of kill count
     *
     * @param csv csv from API
     * @return Sorted list of player boss kill data
     */
    private List<Boss> parseBossKills(String[] csv) {
        String[] stats = Arrays.copyOfRange(csv, Boss.BOSS_START_INDEX, Boss.BOSS_END_INDEX);
        List<Boss> bosses = new ArrayList<>();

        int i = 0;
        for(Boss.BOSS_NAME bossName : bossNames) {
            int kills = Integer.parseInt(stats[i + 1]);
            if(kills > -1) {
                bosses.add(
                        new Boss(
                                bossName,
                                Integer.parseInt(stats[i]),
                                kills
                        )
                );
            }
            i += 2;
        }
        // Sort in descending order of kills
        Collections.sort(bosses);
        return bosses;
    }
}
