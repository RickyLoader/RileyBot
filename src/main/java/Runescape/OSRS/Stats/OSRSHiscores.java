package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import Command.Structure.ImageLoadingMessage;
import Command.Structure.PieChart;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Runescape.*;
import Runescape.OSRS.Boss.Boss.BOSS_ID;
import Runescape.OSRS.Boss.BossManager;
import Runescape.OSRS.Boss.BossStats;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.Skill.SKILL_NAME;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Runescape.Hiscores.LOADING_UPDATE_TYPE.*;
import static Runescape.OSRS.League.LeagueTier.*;
import static Runescape.Skill.SKILL_NAME.*;

/**
 * Build an image displaying a player's OSRS stats
 */
public class OSRSHiscores extends Hiscores<OSRSHiscoresArgs, OSRSPlayerStats> {
    private final BOSS_ID[] bossIds;
    private final BossManager bossManager;
    public static final String LEAGUE_THUMBNAIL = "https://i.imgur.com/xksIl6S.png";
    public static final int MAX_BOSSES = 5;
    private final Font trackerFont;
    private final String displayFormatDate = "dd/MM/yyyy";
    private final int border = 25; // Horizontal & vertical borders on template images
    private final float titleFontSize = 65;
    private final Color redOverlay, dark, light;
    private final SimpleDateFormat
            parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            displayFormat = new SimpleDateFormat(displayFormatDate);
    private final BufferedImage
            titleContainer,
            achievementsContainer,
            achievementsTitleContainer,
            bossContainer,
            cluesContainer,
            skillsContainer,
            xpContainer,
            xpHeader,
            mapContainer,
            relicContainer,
            leagueInfoContainer;


    /**
     * Create the OSRS Hiscores instance
     *
     * @param emoteHelper Emote helper
     * @param helpMessage Help message to display in loading message
     */
    public OSRSHiscores(EmoteHelper emoteHelper, String helpMessage) {
        super(emoteHelper, ResourceHandler.OSRS_BASE_PATH + "Templates/", FontManager.OSRS_FONT, helpMessage);
        this.bossIds = BossManager.getIdsInHiscoresOrder();
        this.bossManager = BossManager.getInstance();
        this.trackerFont = FontManager.WISE_OLD_MAN_FONT;
        this.redOverlay = new Color(255, 0, 0, 127); // 50% opacity
        this.dark = new Color(EmbedHelper.ROW_DARK);
        this.light = new Color(EmbedHelper.ROW_LIGHT);
        this.parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        ResourceHandler handler = getResourceHandler();
        String templates = getResourcePath();
        this.titleContainer = handler.getImageResource(templates + "title_container.png");
        this.achievementsContainer = handler.getImageResource(templates + "achievements_container.png");
        this.achievementsTitleContainer = handler.getImageResource(templates + "achievements_title_container.png");
        this.bossContainer = handler.getImageResource(templates + "boss_container.png");
        this.cluesContainer = handler.getImageResource(templates + "clues_container.png");
        this.skillsContainer = handler.getImageResource(templates + "skills_container.png");
        this.xpContainer = handler.getImageResource(templates + "xp_tracker_container.png");
        this.xpHeader = handler.getImageResource(templates + "xp_tracker_header.png");
        this.mapContainer = handler.getImageResource(templates + "map_container.png");
        this.relicContainer = handler.getImageResource(templates + "relic_container.png");
        this.leagueInfoContainer = handler.getImageResource(templates + "league_info_container.png");
    }

    @Override
    public ArrayList<String> getLoadingCriteria(OSRSHiscoresArgs args) {
        ArrayList<String> criteria = new ArrayList<>();
        if(args.searchLeagueStats()) {
            criteria.add("Player has League stats...");
            criteria.add("Player has stored Relic/Region data...");
            criteria.add("Calculating League Tier...");
        }
        else {
            criteria.add("Player exists...");
            criteria.add("Checking account type...");
            if(shouldFetchAchievements(args)) {
                criteria.add("Fetching achievements...");
            }
        }
        if(shouldFetchXpTracker(args)) {
            criteria.add("Checking XP tracker...");
        }
        return criteria;
    }

    @Override
    public String getLoadingTitle(String name, OSRSHiscoresArgs args) {
        return "OSRS " + (args.searchLeagueStats() ? "League" : "Hiscores") + " lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail(OSRSHiscoresArgs args) {
        return args.searchLeagueStats() ? LEAGUE_THUMBNAIL : EmbedHelper.OSRS_LOGO;
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
    public String getDefaultURL(String name, OSRSHiscoresArgs args) {
        return args.searchLeagueStats() ? getLeagueAccount(name) : getNormalAccount(name);
    }

    @Override
    public String getNotFoundMessage(String name, OSRSHiscoresArgs args) {
        return args.searchLeagueStats() ? "isn't on the league hiscores" : "doesn't exist cunt";
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
     * @param name           Player name
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Player stats object
     */
    @Nullable
    private OSRSPlayerStats fetchStats(String name, OSRSHiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        String url = args.searchLeagueStats() ? getLeagueAccount(name) : getNormalAccount(name);
        String[] normal = hiscoresRequest(url);

        if(normal == null) {
            return null;
        }

        completeLoadingMessageStage(loadingMessage);

        List<BossStats> bossStats = parseBossKills(normal);
        Clue[] clues = parseClueScrolls(normal);
        LastManStanding lmsInfo = parseLmsInfo(normal);

        OSRSPlayerStats normalAccount = new OSRSPlayerStats(
                name,
                url,
                parseSkills(normal),
                clues,
                bossStats,
                lmsInfo,
                args.searchLeagueStats() ? PlayerStats.ACCOUNT.LEAGUE : PlayerStats.ACCOUNT.NORMAL
        );

        if(args.searchLeagueStats()) {
            normalAccount.setLeaguePoints(Integer.parseInt(normal[73]), Long.parseLong(normal[72]));
            String leagueJSON = DiscordUser.getOSRSLeagueData(name);
            if(leagueJSON == null) {
                updateLoadingMessage(FAIL, "Unable to connect to the API", loadingMessage);
                return normalAccount;
            }
            JSONObject leagueData = new JSONObject(leagueJSON);
            normalAccount.setRegions(Region.parseRegions(leagueData.getJSONArray("regions")));
            normalAccount.setRelicTiers(RelicTier.parseRelics(leagueData.getJSONArray("relics")));

            if(normalAccount.hasLeagueUnlockData()) {
                completeLoadingMessageStage(loadingMessage);
            }
            else {
                updateLoadingMessage(FAIL, "Try 'trailblazer' command to store unlocks", loadingMessage);
            }
            return normalAccount;
        }

        updateLoadingMessage(UPDATE, "Player exists, checking ironman hiscores", loadingMessage);

        String ironURL = getIronmanAccount(name);
        String[] iron = hiscoresRequest(ironURL);

        if(iron == null) {
            updateLoadingMessage(COMPLETE, "Player is a normal account!", loadingMessage);
            return normalAccount;
        }

        OSRSPlayerStats ironAccount = new OSRSPlayerStats(
                name,
                ironURL,
                parseSkills(iron),
                clues,
                bossStats,
                lmsInfo,
                PlayerStats.ACCOUNT.IRON
        );

        if(normalAccount.getTotalXP() > ironAccount.getTotalXP()) {
            updateLoadingMessage(COMPLETE, "Player is a de-ironed normal account!", loadingMessage);
            return normalAccount;
        }

        updateLoadingMessage(UPDATE, "Player is an Ironman, checking Hardcore Ironman hiscores", loadingMessage);

        String hardcoreURL = getHardcoreAccount(name);
        String[] hardcore = hiscoresRequest(hardcoreURL);

        if(hardcore != null) {
            OSRSPlayerStats hardcoreAccount = new OSRSPlayerStats(
                    name,
                    hardcoreURL,
                    parseSkills(hardcore),
                    clues,
                    bossStats,
                    lmsInfo,
                    PlayerStats.ACCOUNT.HARDCORE
            );

            if(ironAccount.getTotalXP() > hardcoreAccount.getTotalXP()) {
                updateLoadingMessage(COMPLETE, "Player was a Hardcore Ironman and died! What a loser!", loadingMessage);
                return ironAccount;
            }
            updateLoadingMessage(COMPLETE, "Player is a Hardcore Ironman!", loadingMessage);
            return hardcoreAccount;
        }

        updateLoadingMessage(UPDATE, "Player is not hardcore, checking Ultimate Ironman hiscores", loadingMessage);

        String ultimateURL = getUltimateAccount(name);
        String[] ultimate = hiscoresRequest(ultimateURL);

        if(ultimate != null) {
            OSRSPlayerStats ultimateAccount = new OSRSPlayerStats(
                    name,
                    ultimateURL,
                    parseSkills(ultimate),
                    clues,
                    bossStats,
                    lmsInfo,
                    PlayerStats.ACCOUNT.ULTIMATE
            );

            if(ironAccount.getTotalXP() > ultimateAccount.getTotalXP()) {
                updateLoadingMessage(COMPLETE, "Player is an Ironman who chickened out of Ultimate Ironman!", loadingMessage);
                return ironAccount;
            }
            updateLoadingMessage(COMPLETE, "Player is an Ultimate Ironman!", loadingMessage);
            return ultimateAccount;
        }
        updateLoadingMessage(COMPLETE, "Player is an Ironman!", loadingMessage);
        return ironAccount;
    }

    @Override
    protected OSRSPlayerStats fetchPlayerData(String name, OSRSHiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        OSRSPlayerStats stats = fetchStats(name, args, loadingMessage);

        if(stats == null) {
            return null;
        }

        if(args.searchLeagueStats()) {
            LeagueTier leagueTier = stats.getLeagueTier();
            leagueTier.setTier(calculateTier(leagueTier.getRank()));
            updateLoadingMessage(COMPLETE, "Player is " + leagueTier.getTierName() + "!", loadingMessage);
        }
        else if(shouldFetchAchievements(args)) {
            addPlayerAchievements(stats, args, loadingMessage);
        }

        if(shouldFetchXpTracker(args)) {
            getTrackerData(stats, loadingMessage);
        }
        return stats;
    }

    /**
     * Fetch and add recent achievements for the player
     *
     * @param stats          Player stats
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     */
    private void addPlayerAchievements(OSRSPlayerStats stats, OSRSHiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking player is tracked...", loadingMessage);
            NetworkResponse response = fetchRecentPlayerAchievementData(stats.getName(), args.searchLeagueStats());

            if(response.code == NetworkResponse.TIMEOUT_CODE) {
                updateLoadingMessage(FAIL, "Achievement tracker didn't respond, unlucky cunt", loadingMessage);
                return;
            }

            if(response.code == 404) {
                updatePlayerTracking(stats.getName(), args.searchLeagueStats(), false); // Tracking a new player can take 20+ seconds, don't wait
                updateLoadingMessage(COMPLETE, "Player not tracked - They will be *soon*™", loadingMessage);
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
            updateLoadingMessage(COMPLETE, stats.getAchievementSummary(), loadingMessage);
        }
        catch(Exception e) {
            updateLoadingMessage(FAIL, "Failed to parse achievement data!", loadingMessage);
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
     * @param playerStats    Player stats
     * @param loadingMessage Optional loading message
     */
    private void getTrackerData(OSRSPlayerStats playerStats, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking player is tracked...", loadingMessage);
            boolean league = playerStats.isLeague();
            String name = playerStats.getName();
            NetworkResponse response = fetchPlayerTrackingData(name, league); // Check if player exists

            if(response.code == NetworkResponse.TIMEOUT_CODE) {
                updateLoadingMessage(FAIL, "XP tracker didn't respond, unlucky cunt", loadingMessage);
                return;
            }

            if(response.code == 404) {
                updateLoadingMessage(COMPLETE, "Player not tracked - They will be *soon*™", loadingMessage);
                return;
            }

            updateLoadingMessage(UPDATE, "Player is tracked, refreshing tracker...", loadingMessage);
            updatePlayerTracking(name, league, true);
            updateLoadingMessage(UPDATE, "Player is tracked, getting stats...", loadingMessage);
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

            updateLoadingMessage(COMPLETE, details + " for week beginning at: " + beginningAt, loadingMessage);
        }
        catch(Exception e) {
            updateLoadingMessage(FAIL, "Failed to parse Weekly XP", loadingMessage);
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
     * Build an image displaying the given list of bosses.
     * This image consists of vertical rows, where each row contains an image of the boss, and a summary detailing the
     * rank and total kills that the player has for that boss.
     * If the list is empty, the image will have a red overlay indicating no boss kills.
     *
     * @param bossStats List of bosses to display
     * @return Image displaying player boss kills
     */
    public BufferedImage buildBossSection(List<BossStats> bossStats) {
        BufferedImage container = copyImage(bossContainer);
        Graphics g = container.getGraphics();

        // Adjusted dimensions after factoring in border that surrounds boss container image
        final int adjustedHeight = container.getHeight() - (border * 2);
        final int adjustedWidth = container.getWidth() - (border * 2);

        // Draw a red overlay over the empty boss container
        if(bossStats.isEmpty()) {
            g.setColor(redOverlay);
            g.fillRect(border, border, adjustedWidth, adjustedHeight);
            g.dispose();
            return container;
        }

        // Calculate the height to use for each row (always calculate for max bosses)
        final int bossRowHeight = adjustedHeight / MAX_BOSSES;

        final int bossDisplayCount = Math.min(MAX_BOSSES, bossStats.size());
        int y = border;

        for(int i = 0; i < bossDisplayCount; i++) {
            final BufferedImage bossImage = buildBossRowImage(
                    bossStats.get(i),
                    adjustedWidth,
                    bossRowHeight
            );
            g.drawImage(bossImage, border, y, null);
            y += bossRowHeight;
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given boss and the player's total kills.
     * This image has a transparent background to be drawn on top of the boss section.
     *
     * @param bossStats Boss to display
     * @param width     Width of boss image to build
     * @param height    Height of clue image to build
     * @return Image displaying a boss and the player's kills/rank for that boss
     */
    private BufferedImage buildBossRowImage(BossStats bossStats, int width, int height) {
        final int boxWidth = (width - border) / 2;
        final int centreVertical = height / 2;
        final int centreHorizontal = boxWidth / 2;

        BufferedImage bossBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        final BufferedImage bossImage = bossStats.getBoss().getFullImage();
        if(bossImage == null) {
            return bossBox;
        }

        Graphics g = bossBox.getGraphics();
        // Draw boss image centred in boss box
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
        final String kills = bossStats.getFormattedKills();
        g.drawString(
                kills,
                centreHorizontal - (fm.stringWidth(kills) / 2),
                centreVertical
        );

        // Rank title will be different colour so must be drawn as two separate Strings
        final String rankTitle = "Rank: ";
        final String rankValue = bossStats.getFormattedRank(); // 1,234
        final int rankTitleWidth = fm.stringWidth(rankTitle);

        /*
         * Calculate where to begin drawing the rank title such that when both Strings are drawn beside
         * each other, the full String is centred.
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
     * Build an image displaying the given player's skills.
     * This image is a replica of the in-game skills panel, which is a 3x8 grid where each box contains an image of
     * the skill and a blank area to draw the level.
     * Optionally, the virtual skill levels may be drawn instead of the normal skill levels.
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Image displaying player skills
     */
    private BufferedImage buildSkillsSection(OSRSPlayerStats stats, OSRSHiscoresArgs args) {
        BufferedImage container = copyImage(skillsContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(65f));

        /*
         * Text co-ordinates of first skill level, all skills can be drawn by transforming from these
         * co-ordinates, either vertically or horizontally
         */
        int x = border + 160, ogX = x;
        int y = border + 75;

        /*
         * Skills are in the display order when reading the skill image grid from left to right.
         * By iterating in this order and incrementing the Y co-ordinate/resetting the X co-ordinate after every
         * third skill, the skills will align with the grid.
         */
        Skill[] skills = stats.getSkills();

        for(int i = 0; i < skills.length - 1; i++) {
            Skill skill = skills[i];
            int displayLevel = args.displayVirtualLevels() ? skill.getVirtualLevel() : skill.getLevel();
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
        String level = String.valueOf(
                args.displayVirtualLevels() ? stats.getVirtualTotalLevel() : stats.getTotalLevel()
        );
        g.drawString(
                level,
                825 - (g.getFontMetrics().stringWidth(level) / 2),
                y + 65
        );
        g.dispose();
        return container;
    }

    @Override
    public BufferedImage buildHiscoresImage(OSRSPlayerStats playerStats, OSRSHiscoresArgs args, ImageLoadingMessage... loadingMessage) {
        BufferedImage image = new BufferedImage(
                calculateImageWidth(playerStats, args),
                calculateImageHeight(playerStats, args),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = image.getGraphics();
        BufferedImage titleSection = buildTitleSection(playerStats);
        g.drawImage(titleSection, 0, 0, null);

        BufferedImage skillsSection = buildSkillsSection(playerStats, args);
        g.drawImage(skillsSection, 0, titleSection.getHeight(), null);

        BufferedImage bossSection = buildBossSection(playerStats.getBossStats());
        g.drawImage(bossSection, skillsSection.getWidth(), titleSection.getHeight(), null);

        // When optional sections are displayed vertically, they should be displayed below the base image
        int y = titleSection.getHeight() + skillsSection.getHeight();

        if(shouldDisplayClues(playerStats)) {
            BufferedImage clueSection = buildClueSection(playerStats.getClues());
            g.drawImage(clueSection, 0, y, null);
            y += clueSection.getHeight();
        }

        if(shouldDisplayAchievements(playerStats, args)) {
            BufferedImage achievementsSections = buildAchievementsSections(playerStats);
            g.drawImage(achievementsSections, 0, y, null);
            y += achievementsSections.getHeight();
        }

        // Player unlocked regions/relics
        if(shouldDisplayLeagueUnlocks(playerStats)) {
            BufferedImage leagueUnlockSection = buildLeagueUnlockSection(playerStats);
            g.drawImage(leagueUnlockSection, 0, y, null);
            y += leagueUnlockSection.getHeight();
        }

        // League points/tier
        if(shouldDisplayLeagueInfo(playerStats)) {
            BufferedImage leagueInfoSection = buildLeagueInfoSection(playerStats);
            g.drawImage(leagueInfoSection, 0, y, null);
        }

        // Display off to the right of the image
        if(shouldDisplayXpTracker(args, playerStats)) {
            BufferedImage xpTrackerSection = buildXpTrackerSection(playerStats);
            g.drawImage(xpTrackerSection, titleSection.getWidth(), 0, null);
        }

        g.dispose();
        return image;
    }

    /**
     * Build an image displaying the player's league tier & points.
     * This image uses a similar image to the title section
     *
     * @param stats Player stats
     * @return Image displaying player's league tier & points
     */
    private BufferedImage buildLeagueInfoSection(OSRSPlayerStats stats) {
        BufferedImage container = copyImage(leagueInfoContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(titleFontSize));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int edgePadding = 2 * border; // Padding from outer edges
        final int imagePadding = 15; // Padding from text to image
        final int centreVertical = (container.getHeight() / 2);
        final int textY = centreVertical + (fm.getMaxAscent() / 2); // Centre text vertically

        LeagueTier leagueTier = stats.getLeagueTier();

        // Draw league tier on the left side of the image
        if(leagueTier.hasTier()) {
            BufferedImage tierIcon = getResourceHandler().getImageResource(leagueTier.getTierImagePath());

            g.drawImage(tierIcon, edgePadding, centreVertical - (tierIcon.getHeight() / 2), null);
            g.drawString(
                    leagueTier.getTierName(),
                    edgePadding + tierIcon.getWidth() + imagePadding,
                    textY
            );
        }

        // Draw league points on the right side of the image
        BufferedImage leaguePointImage = getResourceHandler().getImageResource(
                PlayerStats.getAccountTypeImagePath(stats.getAccountType())
        );
        String points = new DecimalFormat("#,### points").format(leagueTier.getPoints());
        int pointsX = container.getWidth() - edgePadding - fm.stringWidth(points);
        g.drawString(points, pointsX, textY);
        g.drawImage(
                leaguePointImage,
                pointsX - imagePadding - leaguePointImage.getWidth(),
                centreVertical - (leaguePointImage.getHeight() / 2),
                null
        );

        g.dispose();
        return container;
    }

    /**
     * Calculate the height of the hiscores image to create.
     * This is based on the elements which will be displayed which is determined both by the hiscores arguments
     * and the presence of certain stats for the player.
     * E.g the clue scroll section is typically displayed below the boss section, however if the player has no clue
     * scroll completions, there is no need to build this section.
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Height of hiscores image to create
     */
    private int calculateImageHeight(OSRSPlayerStats stats, OSRSHiscoresArgs args) {
        /*
         * Base height - title section, skills section, and boss section are always displayed
         * (boss section is equal in height to the skills section)
         */
        int height = titleContainer.getHeight() + skillsContainer.getHeight();

        // Clue scroll section optional and is displayed below the base image
        if(shouldDisplayClues(stats)) {
            height += cluesContainer.getHeight();
        }

        // Achievements section is optional and is displayed below clue section (or base image)
        if(shouldDisplayAchievements(stats, args)) {
            height += achievementsTitleContainer.getHeight() + achievementsContainer.getHeight();
        }

        // League unlocks are optional and are displayed at the bottom of the image
        if(shouldDisplayLeagueUnlocks(stats)) {
            height += mapContainer.getHeight();
        }
        // League info attaches a section to the bottom of the image similar to the title section
        if(shouldDisplayLeagueInfo(stats)) {
            height += leagueInfoContainer.getHeight();
        }
        return height;
    }

    /**
     * Calculate the width of the hiscores image to create.
     * This is based on the elements which will be displayed which is determined both by the hiscores arguments
     * and the presence of certain stats for the player.
     * E.g the XP tracker section is typically displayed beside the boss section, however if the hiscores arguments
     * haven't specified to display the XP tracker (or the player has no gained XP for the week), there is no need to
     * build this section.
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Width of hiscores image to create
     */
    private int calculateImageWidth(OSRSPlayerStats stats, OSRSHiscoresArgs args) {
        /*
         * Base width - title section, skills section, and boss section are always displayed,
         * the title section is equal in width to the skills section & boss section combined.
         */
        int width = titleContainer.getWidth();

        // XP tracker is optional and is displayed beside the base image, add its width to the base width
        return shouldDisplayXpTracker(args, stats) ? width + xpContainer.getWidth() : width;
    }

    /**
     * Check if clue scroll section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display player clue scroll completions in hiscores image
     */
    private boolean shouldDisplayClues(OSRSPlayerStats stats) {
        return stats.hasClueCompletions();
    }

    /**
     * Check if the player's league unlocks should be drawn on to the hiscores image.
     * This is the map & relic container
     *
     * @param stats Player stats
     * @return Should display league map & relic unlocks in hiscores image
     */
    private boolean shouldDisplayLeagueUnlocks(OSRSPlayerStats stats) {
        return stats.isLeague() && stats.hasLeagueUnlockData();
    }

    /**
     * Check if the player's league points & tier should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display league points/tier in hiscores image
     */
    private boolean shouldDisplayLeagueInfo(OSRSPlayerStats stats) {
        return stats.isLeague();
    }

    /**
     * Check if the XP tracker data should be fetched for the player.
     * Don't fetch xp if league stats are requested.
     *
     * @param args Hiscores arguments
     * @return XP tracker data should be fetched
     */
    private boolean shouldFetchXpTracker(OSRSHiscoresArgs args) {
        return args.fetchXpGains() && !args.searchLeagueStats();
    }

    /**
     * Check if the player's recent achievements should be fetched
     *
     * @param args Hiscores arguments
     * @return Recent achievements should be fetched
     */
    private boolean shouldFetchAchievements(OSRSHiscoresArgs args) {
        return args.fetchAchievements();
    }

    /**
     * Check if the XP tracker section should be drawn on to the hiscores image
     *
     * @param args  Hiscores arguments
     * @param stats Player stats
     * @return Should display XP tracker in hiscores image
     */
    private boolean shouldDisplayXpTracker(OSRSHiscoresArgs args, OSRSPlayerStats stats) {
        return shouldFetchXpTracker(args) && stats.hasWeeklyGains();
    }

    /**
     * Check if achievements section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Should display player achievements in hiscores image
     */
    private boolean shouldDisplayAchievements(OSRSPlayerStats stats, OSRSHiscoresArgs args) {
        return !args.searchLeagueStats() && shouldFetchAchievements(args) && stats.hasAchievements();
    }

    /**
     * Build an image displaying the given player's achievements.
     * This image consists of up to two sections, a section displaying the player's completed achievements,
     * and a section displaying the player's in progress achievements.
     * Only one section is guaranteed to be present
     * e.g a player has completed all achievements therefore has none in progress, or has completed
     * no achievements therefore every achievement is in progress.
     *
     * @param stats Player stats
     * @return Image displaying the player's achievements
     */
    private BufferedImage buildAchievementsSections(OSRSPlayerStats stats) {
        final boolean displayCompleted = !stats.getCompletedAchievements().isEmpty();
        final boolean displayInProgress = !stats.getInProgressAchievements().isEmpty();

        BufferedImage container = new BufferedImage(
                displayInProgress && displayCompleted
                        ? achievementsContainer.getWidth() * 2
                        : achievementsContainer.getWidth(),
                achievementsTitleContainer.getHeight() + achievementsContainer.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = container.getGraphics();
        int x = 0;

        if(displayCompleted) {
            // Sort by most recent completion first
            BufferedImage completedAchievements = buildAchievementsSection(
                    stats.getCompletedAchievements(),
                    (o1, o2) -> o2.getCompletionDate().compareTo(o1.getCompletionDate()),
                    "Recent Achievements"
            );
            g.drawImage(completedAchievements, x, 0, null);
            x += completedAchievements.getWidth();
        }
        if(displayInProgress) {
            // Sort by closest to completion first
            BufferedImage inProgressAchievements = buildAchievementsSection(
                    stats.getInProgressAchievements(),
                    (o1, o2) -> Double.compare(o2.getProgressPercent(), o1.getProgressPercent()),
                    "Upcoming Achievements"
            );
            g.drawImage(inProgressAchievements, x, 0, null);
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the top 5 achievements from the given list of achievements.
     * The image consists of a title and a box containing rows for the achievements.
     * Each row consists of a description of the achievement, and a pie chart displaying
     * the progress & optional image.
     *
     * @param achievements Achievements to display
     * @param comparator   Comparator for sorting achievements (the first 5 will be displayed after sorting)
     * @param title        Title to display
     * @return Image displaying achievements
     */
    private BufferedImage buildAchievementsSection(ArrayList<Achievement> achievements, Comparator<Achievement> comparator, String title) {
        achievements.sort(comparator);
        BufferedImage titleImage = buildAchievementsTitleSection(title);
        BufferedImage achievementsImage = copyImage(achievementsContainer);
        Graphics g = achievementsImage.getGraphics();

        final int max = 5;
        final int rowCount = Math.min(achievements.size(), max);

        // Always calculate for max achievements
        final int rowHeight = (achievementsImage.getHeight() - (2 * border)) / max;
        final int rowWidth = achievementsImage.getWidth() - (2 * border);

        int y = border;
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

        BufferedImage container = new BufferedImage(
                titleImage.getWidth(),
                titleImage.getHeight() + achievementsImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        g = container.getGraphics();
        g.drawImage(titleImage, 0, 0, null);
        g.drawImage(achievementsImage, 0, titleImage.getHeight(), null);
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given title for an achievements section.
     * This image should be displayed above an achievements section.
     *
     * @param title Title to display
     * @return Image displaying the given title
     */
    private BufferedImage buildAchievementsTitleSection(String title) {
        BufferedImage container = copyImage(achievementsTitleContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(80f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        g.drawString(
                title,
                (container.getWidth() / 2) - (fm.stringWidth(title) / 2),
                (container.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given achievement details.
     * This image is displayed as a row.
     * The row contains a summary of the achievement (including description, progress, and optional completion date)
     * and a pie chart image indicating the achievement progress.
     * The chart may optionally have an image displayed in the centre.
     *
     * @param achievement Achievement to display
     * @param rowColour   Background colour to use behind row
     * @param chartColour Background colour to use behind chart
     * @param width       Width of row
     * @param height      Height of row
     * @return Achievement row image
     */
    private BufferedImage buildAchievementRow(Achievement achievement, Color rowColour, Color chartColour, int width, int height) {
        final BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics g = row.getGraphics();
        g.setFont(trackerFont.deriveFont(35f));

        final int chartSectionWidth = width / 3;
        final int midY = height / 2;
        final int padding = 10;

        g.setColor(rowColour);
        g.fillRect(0, 0, width - chartSectionWidth, height);
        g.setColor(chartColour);
        g.fillRect(width - chartSectionWidth, 0, width, height);

        final BufferedImage progressImage = getAchievementChart(achievement, (height / 2) - padding);

        g.drawImage(
                progressImage,
                (width - chartSectionWidth / 2) - (progressImage.getWidth() / 2),
                midY - (progressImage.getHeight() / 2),
                null
        );


        g.setColor(achievement.isCompleted() ? Color.GREEN : Color.WHITE);
        final FontMetrics fm = g.getFontMetrics();

        final int midLeftX = (width - chartSectionWidth) / 2;

        final DecimalFormat df = new DecimalFormat("#,###");
        final ArrayList<String> summary = new ArrayList<>();
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

        final int lineHeight = fm.getMaxAscent();
        final int gap = 20;
        final int textHeight = (lineHeight * summary.size()) + (gap * (summary.size() - 1)); // total size occupied by text

        // Text draws from bottom up, add line height so first line is drawn with the top at textHeight
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
                                new Color(2, 215, 59) // Green
                        ),
                        new PieChart.Section(
                                "Incomplete",
                                achievement.getRemaining(),
                                new Color(255, 40, 32) // Red
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
     * Build an image displaying the given player's name, account type, combat level, and rank.
     *
     * @param stats Player stats
     * @return Image displaying player overview
     */
    private BufferedImage buildTitleSection(OSRSPlayerStats stats) {
        final BufferedImage container = copyImage(titleContainer);
        final Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(140f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final String name = stats.getName();
        final int centreVertical = container.getHeight() / 2;
        final int x = (container.getWidth() / 2) - (fm.stringWidth(name) / 2);

        g.drawString(name.toUpperCase(), x, centreVertical + (fm.getMaxAscent() / 2));

        g.setFont(getGameFont().deriveFont(titleFontSize));
        fm = g.getFontMetrics();

        final PlayerStats.ACCOUNT accountType = stats.getAccountType();

        if(accountType != PlayerStats.ACCOUNT.NORMAL && !stats.isLeague()) {
            final BufferedImage accountImage = getResourceHandler().getImageResource(
                    PlayerStats.getAccountTypeImagePath(stats.getAccountType())
            );
            g.drawImage(
                    accountImage,
                    x - (int) (accountImage.getWidth() * 1.5),
                    centreVertical - (accountImage.getHeight() / 2),
                    null
            );
        }

        final int edgePadding = 2 * border; // Padding from outer edges
        final int imagePadding = 15; // Padding from text to image
        final int textY = centreVertical + (fm.getMaxAscent() / 2); // Centre text vertically

        // Draw combat level on left side of title section
        final String combat = String.valueOf(stats.getCombatLevel());
        final BufferedImage combatImage = getResourceHandler().getImageResource(Skill.LARGE_COMBAT_IMAGE_PATH);
        g.drawImage(
                combatImage,
                edgePadding,
                centreVertical - (combatImage.getHeight() / 2),
                null
        );
        g.drawString(combat, edgePadding + combatImage.getWidth() + imagePadding, textY);

        // Draw rank info on right side of title section
        final String rank = "Rank: " + stats.getFormattedRank();
        final BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
        final int rankX = container.getWidth() - edgePadding - fm.stringWidth(rank);

        g.drawString(rank, rankX, textY);
        g.drawImage(
                rankImage,
                rankX - imagePadding - rankImage.getWidth(),
                centreVertical - (rankImage.getHeight() / 2),
                null
        );
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given clue scroll completions.
     * This image is a horizontal list of clue scroll images.
     * Each clue scroll image contains the type of clue, an image of the clue, and the player's completions of the clue.
     * These are displayed left to right in ascending order of difficulty.
     * Clue scrolls without any completions are displayed with a red overlay.
     *
     * @param clues List of clue scroll completions
     * @return Image displaying player clue scroll completions
     */
    private BufferedImage buildClueSection(Clue[] clues) {
        final BufferedImage container = copyImage(cluesContainer);

        Graphics g = container.getGraphics();

        final int maxClues = clues.length - 1; // Not drawing "ALL" type clue

        // Always calculate for max clues
        final int clueWidth = (container.getWidth() - (2 * border)) / maxClues;
        final int clueHeight = container.getHeight() - (2 * border);

        int x = border;

        // Build clue stat images with transparent backgrounds to place in to the clue section
        for(Clue clue : clues) {
            if(clue.getType() == Clue.TYPE.ALL) {
                continue;
            }
            final BufferedImage clueImage = buildClueImage(clue, clueWidth, clueHeight);
            g.drawImage(clueImage, x, border, null);
            x += clueWidth;
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given clue and a summary detailing the player's completions and rank.
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

        // Draw clue centred in background
        g.drawImage(
                clueImage,
                centreHorizontal - (clueImage.getWidth() / 2),
                centreVertical - (clueImage.getHeight() / 2),
                null
        );

        // Height of area above & below clue
        final int textBoxHeight = (height - clueImage.getHeight()) / 2;
        final int centreTextVertical = textBoxHeight / 2;

        // Draw name centred in the area above the clue image
        final String name = clue.getType().getName();
        g.drawString(
                name,
                centreHorizontal - (fm.stringWidth(name) / 2),
                centreTextVertical + (fm.getMaxAscent() / 2)
        );

        g.setFont(getGameFont().deriveFont(50f));
        fm = g.getFontMetrics();

        // Centre of the area below the clue image
        final int centreBottom = (height - textBoxHeight) + centreTextVertical;

        // Draw completions above the centre line of the area below the clue image
        final String completions = clue.getFormattedCompletions();
        g.drawString(
                completions,
                centreHorizontal - (fm.stringWidth(completions) / 2),
                centreBottom
        );

        // Rank title will be different colour so must be drawn as two separate Strings
        final String rankTitle = "Rank: ";
        final String rankValue = clue.getFormattedRank(); // 1,234
        final int rankTitleWidth = fm.stringWidth(rankTitle);

        /*
         * Calculate where to begin drawing the rank title such that when both Strings are drawn beside
         * each other, the full String is centred.
         */
        final int rankX = centreHorizontal - ((rankTitleWidth + fm.stringWidth(rankValue)) / 2);

        // Draw rank below the centre line of the area below the clue image
        final int y = centreBottom + fm.getMaxAscent();
        g.drawString(rankValue, rankX + rankTitleWidth, y); // Add rank title width to leave room for rank title to be drawn

        g.setColor(Color.WHITE);
        g.drawString(rankTitle, rankX, y);

        // Draw a red overlay on incomplete clues
        if(!clue.hasCompletions()) {
            g.setColor(redOverlay);
            g.fillRect(0, 0, width, height);
        }

        g.dispose();
        return background;
    }

    /**
     * Build an image displaying the player's weekly gained XP
     *
     * @param stats Player stats
     * @return Image displaying player's weekly gained XP
     */
    private BufferedImage buildXpTrackerSection(OSRSPlayerStats stats) {
        BufferedImage container = new BufferedImage(
                xpHeader.getWidth(),
                xpHeader.getHeight() + xpContainer.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        BufferedImage tracker = copyImage(xpContainer);
        Graphics g = tracker.getGraphics();

        Skill[] skills = stats.getSkills();
        int rowHeight = (tracker.getHeight() - (2 * border)) / skills.length;
        int rowWidth = tracker.getWidth() - (2 * border);
        int y = border;

        for(int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            Color colour = i % 2 == 0 ? dark : light;
            BufferedImage row = buildSkillXpRow(rowWidth, rowHeight, colour, skill);
            g.drawImage(row, border, y, null);
            y += rowHeight;
        }

        BufferedImage header = buildXpTrackerHeader(stats);
        g = container.getGraphics();
        g.drawImage(header, 0, 0, null);
        g.drawImage(tracker, 0, header.getHeight(), null);
        g.dispose();
        return container;
    }

    /**
     * Build the XP tracker header image.
     * This displays the weekly period for which the XP gains are from.
     *
     * @param stats Player stats
     * @return XP tracker header image
     */
    private BufferedImage buildXpTrackerHeader(OSRSPlayerStats stats) {
        BufferedImage header = copyImage(xpHeader);
        Graphics g = header.getGraphics();
        setXpTrackerFont(g);

        String trackerPeriod = displayFormat.format(stats.getTrackerStartDate())
                + " - "
                + displayFormat.format(stats.getTrackerEndDate());

        g.drawString(
                trackerPeriod,
                660 - (g.getFontMetrics().stringWidth(trackerPeriod) / 2),
                166
        );
        g.dispose();
        return header;
    }

    /**
     * Set the font and font size for drawing in the XP container
     *
     * @param g Graphics instance to set font on
     */
    private void setXpTrackerFont(Graphics g) {
        g.setFont(trackerFont.deriveFont(40f));
        g.setColor(Color.YELLOW);
    }

    /**
     * Build an image displaying the XP gained in the given skill alongside the icon and skill name
     *
     * @param width  Width of image
     * @param height Height of image
     * @param colour Colour to fill row
     * @param skill  Skill to display XP from
     * @return XP displaying gained XP in the given skill
     */
    private BufferedImage buildSkillXpRow(int width, int height, Color colour, Skill skill) {
        BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = row.getGraphics();

        g.setColor(colour);
        g.fillRect(0, 0, width, height);
        setXpTrackerFont(g);

        final int iconWidth = 50;

        int iconX = border;
        int expX = row.getWidth() / 2;
        int sectionMid = row.getHeight() / 2;

        BufferedImage icon = skill.getImage();
        if(icon == null) {
            g.dispose();
            return row;
        }

        g.drawImage(icon, iconX, sectionMid - (icon.getHeight() / 2), null);
        int textY = sectionMid + (g.getFontMetrics().getMaxAscent() / 2);

        String name = StringUtils.capitalize(skill.getName().name().toLowerCase());
        String xp = new DecimalFormat("#,### XP").format(skill.getGainedXP());

        if(skill.hasGainedXP()) {
            g.setColor(Color.GREEN);
            xp = "+" + xp;
        }
        else {
            g.setColor(Color.WHITE);
        }
        g.drawString(name, iconX + iconWidth + border, textY);
        g.drawString(xp, expX, textY);
        g.dispose();
        return row;
    }

    /**
     * Build an image displaying league relics and region unlocks.
     * These are displayed side by side
     *
     * @param stats Player stats
     * @return Image displaying league relics/region unlocks
     */
    private BufferedImage buildLeagueUnlockSection(OSRSPlayerStats stats) {
        BufferedImage regionImage = buildRegionSection(stats.getRegions());
        BufferedImage relicImage = buildRelicSection(stats.getRelicTiers());

        BufferedImage leagueImage = new BufferedImage(
                regionImage.getWidth() + relicImage.getWidth(),
                regionImage.getHeight(), // Same height as relic image
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = leagueImage.getGraphics();
        g.drawImage(relicImage, 0, 0, null);
        g.drawImage(regionImage, relicImage.getWidth(), 0, null);
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
        BufferedImage relicContainer = copyImage(this.relicContainer);
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
        BufferedImage mapContainer = copyImage(this.mapContainer);
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
    private List<BossStats> parseBossKills(String[] csv) {
        String[] stats = Arrays.copyOfRange(csv, BossStats.BOSS_START_INDEX, BossStats.BOSS_END_INDEX);
        List<BossStats> bossStats = new ArrayList<>();

        int i = 0;
        for(BOSS_ID bossId : bossIds) {
            int kills = Integer.parseInt(stats[i + 1]);
            if(kills > -1) {
                bossStats.add(
                        new BossStats(
                                bossManager.getBossById(bossId),
                                Integer.parseInt(stats[i]),
                                kills
                        )
                );
            }
            i += 2;
        }
        // Sort in descending order of kills
        Collections.sort(bossStats);
        return bossStats;
    }
}
