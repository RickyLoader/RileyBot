package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import Command.Structure.ImageLoadingMessage;
import Command.Structure.PieChart;
import Network.NetworkRequest;
import Runescape.*;
import Runescape.OSRS.Boss.Boss;
import Runescape.OSRS.Boss.Boss.BOSS_ID;
import Runescape.OSRS.Boss.BossManager;
import Runescape.OSRS.Boss.BossStats;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.Skill.SKILL_NAME;
import Runescape.Stats.WiseOldMan;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Command.Commands.Lookup.OSRSLookupCommand.*;
import static Runescape.Hiscores.LOADING_UPDATE_TYPE.*;
import static Runescape.OSRS.League.LeagueTier.*;
import static Runescape.Skill.SKILL_NAME.*;
import static Runescape.Stats.WiseOldMan.TrackerResponse.RECORDS_KEY;
import static Runescape.Stats.WiseOldMan.TrackerResponse.XP_KEY;

/**
 * Build an image displaying a player's OSRS stats
 */
public class OSRSHiscores extends Hiscores<ARGUMENT, OSRSPlayerStats> {
    private final BOSS_ID[] bossIds;
    private final BossManager bossManager;
    private final Font trackerFont;
    private final String displayFormatDate = "dd/MM/yyyy";
    private final Color redOverlay, greenOverlay, blackOverlay, dark, light;
    private static final float STANDARD_FONT_SIZE = 65;
    public static final String
            LEAGUE_THUMBNAIL = "https://i.imgur.com/xksIl6S.png",
            DMM_THUMBNAIL = "https://i.imgur.com/O2HpIt3.png";

    public static final int
            MAX_BOSSES = 5,
            UPPER_SKILL_TEXT_Y = 70, // Y for upper skill number in a skill box
            UPPER_SKILL_TEXT_X = 160, // X for upper skill numbers in a skill box
            SKILL_TEXT_OFFSET = 60, // Upper/Lower skill numbers are directly diagonal
            LOWER_SKILL_TEXT_Y = UPPER_SKILL_TEXT_Y + SKILL_TEXT_OFFSET, // Y for lower skill number in a skill box
            LOWER_SKILL_TEXT_X = UPPER_SKILL_TEXT_X + SKILL_TEXT_OFFSET, // X for lower skill number in a skill box
            SHADOW_OFFSET = 5, // X & Y offset for drawing skill text shadow
            SKILL_NOTCH_SIZE = 25, // Width/height of notches in the corners of skill boxes
            SKILL_BORDER = 10, // Horizontal & vertical borders of skill boxes
            BORDER = 25; // Horizontal & vertical borders on template images

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
            skillBox,
            xpSkillBox,
            totalLevelBox,
            totalXpBox,
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

        final int opacity = 127; // 50% opacity
        this.redOverlay = new Color(255, 0, 0, opacity);
        this.greenOverlay = new Color(0, 255, 0, opacity);
        this.blackOverlay = new Color(0, 0, 0, opacity);

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
        this.totalXpBox = handler.getImageResource(templates + "total_xp_box.png");
        this.totalLevelBox = handler.getImageResource(templates + "total_level_box.png");
        this.skillBox = handler.getImageResource(templates + "normal_skill_box.png");
        this.xpSkillBox = handler.getImageResource(templates + "xp_skill_box.png");
    }

    @Override
    public ArrayList<String> getLoadingCriteria(HashSet<ARGUMENT> args) {
        ArrayList<String> criteria = new ArrayList<>();
        if(args.contains(ARGUMENT.LEAGUE)) {
            criteria.add("Player has League stats...");
            criteria.add("Player has stored Relic/Region data...");
            criteria.add("Calculating League Tier...");
        }
        else if(args.contains(ARGUMENT.DMM)) {
            criteria.add("Player has DMM stats...");
        }
        else {
            criteria.add("Player exists...");
            criteria.add("Checking account type...");
        }

        if(shouldFetchAchievements(args)) {
            criteria.add("Fetching achievements...");
        }

        if(shouldFetchXpTracker(args)) {
            criteria.add("Checking XP tracker...");
        }

        return criteria;
    }

    @Override
    public String getLoadingTitle(String name, HashSet<ARGUMENT> args) {
        String lookupPrefix = "Hiscores";
        if(args.contains(ARGUMENT.LEAGUE)) {
            lookupPrefix = "league";
        }
        else if(args.contains(ARGUMENT.DMM)) {
            lookupPrefix = "DMM";
        }
        return "OSRS " + lookupPrefix + " lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail(HashSet<ARGUMENT> args) {
        if(args.contains(ARGUMENT.LEAGUE)){
            return LEAGUE_THUMBNAIL;
        }
        else if(args.contains(ARGUMENT.DMM)){
            return DMM_THUMBNAIL;
        }
        return EmbedHelper.OSRS_LOGO;
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
    public String getDefaultURL(String name, HashSet<ARGUMENT> args) {
        if(args.contains(ARGUMENT.LEAGUE)) {
            return getLeagueAccount(name);
        }
        else if(args.contains(ARGUMENT.DMM)) {
            return getDeadmanAccount(name);
        }
        return getNormalAccount(name);
    }

    @Override
    public String getNotFoundMessage(String name, HashSet<ARGUMENT> args) {
        if(args.contains(ARGUMENT.LEAGUE)) {
            return "isn't on the league hiscores";
        }
        else if(args.contains(ARGUMENT.DMM)) {
            return "isn't on the DMM hiscores";
        }
        else {
            return "doesn't exist cunt";
        }
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
     * Get the URL to request an account's deadman stats from the hiscores.
     *
     * @param name Player name
     * @return URL to deadman account hiscores CSV
     */
    private String getDeadmanAccount(String name) {
        return getURL("_tournament", name);
    }

    /**
     * Fetch and parse player stats from the hiscores
     *
     * @param name Player name
     * @param type Account type to fetch
     * @return Player stats or null (if hiscores are down or the name does not appear on the hiscores of the given type)
     */
    @Nullable
    private OSRSPlayerStats parseStats(String name, PlayerStats.ACCOUNT type) {
        String url;

        switch(type) {
            case IRON:
                url = getIronmanAccount(name);
                break;
            case LEAGUE:
                url = getLeagueAccount(name);
                break;
            case NORMAL:
                url = getNormalAccount(name);
                break;
            case HARDCORE:
                url = getHardcoreAccount(name);
                break;
            case DMM:
                url = getDeadmanAccount(name);
                break;

            // ULTIMATE
            default:
                url = getUltimateAccount(name);
                break;
        }

        String[] data = hiscoresRequest(url);

        // Player has no data as the given account type (or hiscores are down)
        if(data == null) {
            return null;
        }

        final Skill[] skills = parseSkills(data);
        final Clue[] clues = parseClueScrolls(data);
        final List<BossStats> bossStats = parseBossKills(data);
        final LastManStanding lmsInfo = parseLmsInfo(data);

        // Fetch extra data required to build seasonal league stats
        if(type == PlayerStats.ACCOUNT.LEAGUE) {
            int leaguePoints = Integer.parseInt(data[LeagueTier.LEAGUE_POINTS_INDEX]);
            long rank = Long.parseLong(data[LEAGUE_POINTS_RANK_INDEX]);

            LeagueTier leagueTier = new LeagueTier(
                    calculateTier(rank),
                    leaguePoints == PlayerStats.UNRANKED ? 0 : leaguePoints,
                    rank
            );

            ArrayList<RelicTier> relicTiers = new ArrayList<>();
            ArrayList<Region> regions = new ArrayList<>();

            String leagueJSON = DiscordUser.getOSRSLeagueData(name);

            // Parse relics & regions
            if(leagueJSON != null) {
                JSONObject leagueData = new JSONObject(leagueJSON);
                relicTiers = RelicTier.parseRelics(leagueData.getJSONArray("relics"));
                regions = Region.parseRegions(leagueData.getJSONArray("regions"));
            }

            return new OSRSLeaguePlayerStats(
                    name,
                    url,
                    skills,
                    clues,
                    bossStats,
                    lmsInfo,
                    leagueTier,
                    relicTiers,
                    regions
            );
        }

        // No more data required
        else {
            return new OSRSPlayerStats(
                    name,
                    url,
                    skills,
                    clues,
                    bossStats,
                    lmsInfo,
                    type
            );
        }
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
    private OSRSPlayerStats fetchStats(String name, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        PlayerStats.ACCOUNT type;

        // Fetch league stats
        if(args.contains(ARGUMENT.LEAGUE)) {
            type = PlayerStats.ACCOUNT.LEAGUE;
        }

        // Fetch DMM stats
        else if(args.contains(ARGUMENT.DMM)) {
            type = PlayerStats.ACCOUNT.DMM;
        }

        // Locate account type
        else {
            type = PlayerStats.ACCOUNT.NORMAL;
        }

        OSRSPlayerStats normalAccount = parseStats(
                name,
                type
        );

        // Player does not exist
        if(normalAccount == null) {
            return null;
        }

        // Player exists
        completeLoadingMessageStage(loadingMessage);

        // DMM stats
        if(type == PlayerStats.ACCOUNT.DMM) {
            return normalAccount;
        }

        // Update league loading messages
        if(normalAccount instanceof OSRSLeaguePlayerStats) {
            OSRSLeaguePlayerStats leagueAccount = (OSRSLeaguePlayerStats) normalAccount;

            if(leagueAccount.hasLeagueUnlockData()) {
                completeLoadingMessageStage(loadingMessage);
            }
            else {
                updateLoadingMessage(FAIL, "Try `trailblazer` command to store unlocks", loadingMessage);
            }
            return normalAccount;
        }

        updateLoadingMessage(UPDATE, "Player exists, checking ironman hiscores", loadingMessage);
        OSRSPlayerStats ironAccount = parseStats(name, PlayerStats.ACCOUNT.IRON);

        // Player does not appear on the ironman hiscores, only possible if they are a normal account
        if(ironAccount == null) {
            updateLoadingMessage(COMPLETE, "Player is a normal account!", loadingMessage);
            return normalAccount;
        }

        // Player has more XP on the normal hiscores, only possible if they have reverted their account
        if(normalAccount.getTotalXp() > ironAccount.getTotalXp()) {
            updateLoadingMessage(COMPLETE, "Player is a de-ironed normal account!", loadingMessage);
            return normalAccount;
        }

        /*
         * Player appears on the ironman hiscores and has not reverted back to a normal account.
         * They may be an ultimate ironman, hardcore ironman, or normal ironman, have to check all of them.
         */
        updateLoadingMessage(UPDATE, "Player is an Ironman, checking Hardcore Ironman hiscores", loadingMessage);
        OSRSPlayerStats hardcoreAccount = parseStats(name, PlayerStats.ACCOUNT.HARDCORE);

        // Player appears on the hardcore ironman hiscores, may be dead
        if(hardcoreAccount != null) {

            // Player has more XP on the normal ironman hiscores, only possible if they have died
            if(ironAccount.getTotalXp() > hardcoreAccount.getTotalXp()) {
                updateLoadingMessage(
                        COMPLETE,
                        "Player was a Hardcore Ironman and died! What a loser!",
                        loadingMessage
                );
                return ironAccount;
            }

            updateLoadingMessage(COMPLETE, "Player is a Hardcore Ironman!", loadingMessage);
            return hardcoreAccount;
        }

        updateLoadingMessage(UPDATE, "Player is not hardcore, checking Ultimate Ironman hiscores", loadingMessage);
        OSRSPlayerStats ultimateAccount = parseStats(name, PlayerStats.ACCOUNT.ULTIMATE);

        // Player appears on the ultimate ironman hiscores
        if(ultimateAccount != null) {

            // Player has more XP on the normal ironman hiscores, only possible if they have reverted their account
            if(ironAccount.getTotalXp() > ultimateAccount.getTotalXp()) {
                updateLoadingMessage(
                        COMPLETE,
                        "Player is an Ironman who chickened out of Ultimate Ironman!",
                        loadingMessage
                );
                return ironAccount;
            }

            updateLoadingMessage(COMPLETE, "Player is an Ultimate Ironman!", loadingMessage);
            return ultimateAccount;
        }

        // Player is a normal ironman account
        updateLoadingMessage(COMPLETE, "Player is an Ironman!", loadingMessage);
        return ironAccount;
    }

    @Override
    protected OSRSPlayerStats fetchPlayerData(String name, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        OSRSPlayerStats stats = fetchStats(name, args, loadingMessage);

        // Player doesn't exist/hiscores down
        if(stats == null) {
            return null;
        }

        if(stats instanceof OSRSLeaguePlayerStats) {
            LeagueTier leagueTier = ((OSRSLeaguePlayerStats) stats).getLeagueTier();
            updateLoadingMessage(COMPLETE, "Player is " + leagueTier.getTierName() + "!", loadingMessage);
        }

        if(shouldFetchAchievements(args)) {
            addPlayerAchievements(stats, args, loadingMessage);
        }

        if(shouldFetchXpTracker(args)) {
            addPlayerXpTracker(stats, args, loadingMessage);
        }

        return stats;
    }

    /**
     * Fetch and add weekly XP gains for the player
     *
     * @param stats          Player stats
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     */
    private void addPlayerXpTracker(OSRSPlayerStats stats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking Weekly XP...", loadingMessage);
            WiseOldMan.TrackerResponse response = WiseOldMan.getInstance().getXpTrackerData(
                    stats.getName(),
                    args.contains(ARGUMENT.LEAGUE)
            );

            JSONObject responseData = response.getData();

            // Error occurred
            if(responseData == null) {
                updateLoadingMessage(FAIL, response.getError(), loadingMessage);
                return;
            }

            JSONObject week = responseData.getJSONObject(XP_KEY);

            Date startDate = null;
            Date endDate = null;

            // Attempt to set the tracker period (the period for which the XP gains were between)
            try {
                startDate = parseFormat.parse(week.getString("startsAt"));
                endDate = parseFormat.parse(week.getString("endsAt"));
            }

            // Shouldn't happen but possible if the dates change format, set period to now
            catch(ParseException e) {
                System.out.println("Error parsing tracker dates");
            }

            stats.setTrackerPeriod(startDate, endDate);

            JSONObject skills = week.getJSONObject("data");

            // Each key is the name of a skill/boss/etc and contains the amount gained for the week (XP for a skill)
            for(String key : skills.keySet()) {
                JSONObject entry = skills.getJSONObject(key);

                // Not a skill (could be a boss etc)
                if(!entry.has("experience")) {
                    continue;
                }

                SKILL_NAME skillName = SKILL_NAME.fromName(key);

                // New skill that hasn't been mapped
                if(skillName == UNKNOWN) {
                    continue;
                }

                JSONObject experienceData = entry.getJSONObject("experience");
                stats.addGainedXP(skillName, experienceData.getLong("gained"));
            }

            JSONArray records = responseData.getJSONArray(RECORDS_KEY);

            // Each object is a record - a record pertains to a time period (e.g week/year) and a metric (skill/boss/etc)
            for(int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                SKILL_NAME skillName = SKILL_NAME.fromName(record.getString("metric"));

                // Only concerned with weekly XP records
                if(skillName == UNKNOWN || !record.getString("period").equals("week")) {
                    continue;
                }

                stats.getSkill(skillName).setRecordXp(record.getLong("value"));
            }

            String beginningAt = new SimpleDateFormat(displayFormatDate + "  HH:mm:ss")
                    .format(stats.getTrackerStartDate());

            String trackerUrl = WiseOldMan.getInstance().getXpTrackerBrowserUrl(
                    stats.getName(), args.contains(ARGUMENT.LEAGUE)
            );

            String details = stats.hasWeeklyGains()
                    ? "Weekly XP " + EmbedHelper.embedURL("obtained", trackerUrl)
                    : "No XP " + EmbedHelper.embedURL("gained", trackerUrl);

            updateLoadingMessage(COMPLETE, details + " for week beginning at: " + beginningAt, loadingMessage);
        }
        catch(Exception e) {
            updateLoadingMessage(FAIL, "Failed to parse XP tracker data!", loadingMessage);
        }
    }

    /**
     * Fetch and add recent achievements for the player
     *
     * @param stats          Player stats
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     */
    private void addPlayerAchievements(OSRSPlayerStats stats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking tracker...", loadingMessage);
            WiseOldMan.TrackerResponse response = WiseOldMan.getInstance().getPlayerAchievementsData(
                    stats.getName(),
                    args.contains(ARGUMENT.LEAGUE)
            );

            JSONObject responseData = response.getData();

            // Error occurred
            if(responseData == null) {
                updateLoadingMessage(FAIL, response.getError(), loadingMessage);
                return;
            }

            JSONArray achievements = responseData.getJSONArray(WiseOldMan.TrackerResponse.ACHIEVEMENTS_KEY);
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
     * Calculate a player's league tier manually from the given rank
     *
     * @param rank Player league point rank
     * @return League tier
     */
    private LEAGUE_TIER calculateTier(long rank) {
        LEAGUE_TIER tier = LEAGUE_TIER.UNQUALIFIED;
        try {
            String json = new NetworkRequest(
                    "https://trailblazer.wiseoldman.net/api/league/tiers",
                    false
            ).get().body;
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
     * @param args      Hiscores arguments
     * @return Image displaying player boss kills
     */
    public BufferedImage buildBossSection(List<BossStats> bossStats, HashSet<ARGUMENT> args) {
        BufferedImage container = copyImage(bossContainer);
        Graphics g = container.getGraphics();

        // Adjusted dimensions after factoring in border that surrounds boss container image
        final int adjustedHeight = container.getHeight() - (BORDER * 2);
        final int adjustedWidth = container.getWidth() - (BORDER * 2);

        // Draw a red overlay over the empty boss container
        if(bossStats.isEmpty()) {
            g.setColor(redOverlay);
            g.fillRect(BORDER, BORDER, adjustedWidth, adjustedHeight);
            g.dispose();
            return container;
        }

        // Calculate the height to use for each row (always calculate for max bosses)
        final int bossRowHeight = adjustedHeight / MAX_BOSSES;

        final int bossDisplayCount = Math.min(MAX_BOSSES, bossStats.size());
        int y = BORDER;

        for(int i = 0; i < bossDisplayCount; i++) {
            final BufferedImage bossImage = buildBossRowImage(
                    bossStats.get(i),
                    adjustedWidth,
                    bossRowHeight,
                    args
            );
            g.drawImage(bossImage, BORDER, y, null);
            y += bossRowHeight;
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given boss with the player's total kills & rank.
     * This image has a background depicting the bosses environment.
     *
     * @param bossStats Boss to display
     * @param width     Width of boss image to build
     * @param height    Height of clue image to build
     * @param args      Hiscores arguments
     * @return Image displaying a boss and the player's kills/rank for that boss
     */
    private BufferedImage buildBossRowImage(BossStats bossStats, int width, int height, HashSet<ARGUMENT> args) {
        final int boxWidth = (width - BORDER) / 2;
        final int centreVertical = height / 2;
        final int centreHorizontal = boxWidth / 2;
        final Boss boss = bossStats.getBoss();
        final BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics g;

        // Draw boss background (if requested & available)
        final BufferedImage bossBackground = boss.getBackgroundImage();
        if(bossBackground != null && args.contains(ARGUMENT.BOSS_BACKGROUNDS)) {
            g = row.getGraphics();
            g.drawImage(bossBackground, 0, 0, null);
        }

        // Boss image side of the boss row
        BufferedImage bossBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        // Boss kill count/rank side of the boss row
        final BufferedImage textBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        // Fill the background of the boss row sections with random colours
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(bossBox);
            fillImage(textBox);
        }

        final BufferedImage bossImage = boss.getFullImage();

        // Image may be missing, skip drawing boss but still draw rank/kill count
        if(bossImage != null) {
            g = bossBox.getGraphics();

            // Draw boss image centred in boss box
            g.drawImage(
                    bossImage,
                    centreHorizontal - (bossImage.getWidth() / 2),
                    centreVertical - (bossImage.getHeight() / 2),
                    null
            );
        }

        // Draw rank & kill count
        g = textBox.getGraphics();
        g.setFont(getGameFont().deriveFont(70f));
        g.setColor(Color.YELLOW);

        final FontMetrics fm = g.getFontMetrics();

        // Where bottom of kill text will be
        final int midTextBottom = centreVertical + (fm.getMaxAscent() / 2);
        final int gap = 5;

        // Draw kills centred on centre line
        final String kills = bossStats.getFormattedKills();
        g.drawString(
                kills,
                centreHorizontal - (fm.stringWidth(kills) / 2),
                midTextBottom
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

        // Draw rank below kills
        final int y = midTextBottom + fm.getMaxAscent() + gap;

        // Add rank title width to leave room for rank title to be drawn
        g.drawString(rankValue, x + rankTitleWidth, y);

        g.setColor(Color.WHITE);
        g.drawString(rankTitle, x, y);

        // Draw boss name above kills
        final String bossName = boss.getShortName();
        g.drawString(
                bossName,
                centreHorizontal - (fm.stringWidth(bossName) / 2),
                midTextBottom - fm.getMaxAscent() - gap
        );

        // Combine in to row with border-sized gap in centre for mid border
        g = row.getGraphics();
        g.drawImage(bossBox, 0, 0, null);
        g.drawImage(textBox, boxWidth + BORDER, 0, null);
        g.dispose();
        return row;
    }

    /**
     * Fill the given image with a random colour
     *
     * @param image Image to fill
     */
    private void fillImage(BufferedImage image) {
        Graphics g = image.getGraphics();
        g.setColor(EmbedHelper.getRandomColour());
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * Build an image displaying the given player's skills.
     * Optionally, the virtual skill levels may be drawn instead of the normal skill levels.
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Image displaying player skills
     */
    private BufferedImage buildSkillsSection(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {
        BufferedImage container = copyImage(skillsContainer);

        // Adjusted dimensions after factoring in border that surrounds skills container image
        final int adjustedHeight = container.getHeight() - (BORDER * 2);
        final int adjustedWidth = container.getWidth() - (BORDER * 2);

        Skill[] skills = stats.getSkills();

        // Skills placed horizontally
        final int columns = 3;

        // Equal gap between columns (including before first column and after last)
        final int horizontalGap = (adjustedWidth - (columns * skillBox.getWidth())) / (columns + 1);

        final int totalRows = (skills.length / columns) + 1; // +1 for total xp row

        // Equal gap at the top and bottom
        final int verticalGap = (adjustedHeight - (totalRows * skillBox.getHeight())) / 2;

        Graphics g = container.getGraphics();
        final int startX = BORDER + horizontalGap;
        int x = startX, y = BORDER + verticalGap;

        for(int i = 0; i < skills.length; i++) {
            BufferedImage skillImage = buildSkillImage(skills[i], args);
            g.drawImage(skillImage, x, y, null);

            // Drawing final column, move to next row
            if((i + 1) % columns == 0) {
                x = startX;
                y += skillImage.getHeight();
            }

            // Move to next column
            else {
                x += skillImage.getWidth() + horizontalGap;
            }
        }

        g.drawImage(buildTotalXpImage(stats, args), startX, y, null);
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given player's total experience
     *
     * @param stats Player stats to draw total experience from
     * @param args  Hiscores arguments
     * @return Total experience image
     */
    private BufferedImage buildTotalXpImage(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {
        BufferedImage totalXpImage = copyImage(totalXpBox);

        // Fill the background of the total XP box with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(totalXpImage);
        }

        Graphics g = totalXpImage.getGraphics();
        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int centreX = totalXpImage.getWidth() / 2;
        final String title = "Total Experience:";

        // Match alignment with the top number of a skill box
        g.drawString(
                title,
                centreX - (fm.stringWidth(title) / 2),
                UPPER_SKILL_TEXT_Y
        );

        final Skill total = stats.getTotalLevel();
        String xp = total.getFormattedXp();

        // Match alignment with the bottom number of a skill box
        g.drawString(
                xp,
                centreX - (fm.stringWidth(xp) / 2),
                LOWER_SKILL_TEXT_Y
        );

        BufferedImage icon = total.getImage(true);

        // Skip total icon if missing
        if(icon != null) {
            g.drawImage(
                    icon,
                    (skillBox.getWidth() / 4) - (icon.getWidth() / 2), // Align with skill boxes above it
                    (totalXpImage.getHeight() / 2) - (icon.getHeight() / 2),
                    null
            );
        }

        g.dispose();
        return totalXpImage;
    }

    /**
     * Build an image displaying the given skill level and icon.
     *
     * @param skill Skill to draw
     * @param args  Hiscores arguments
     * @return Skill image
     */
    private BufferedImage buildSkillImage(Skill skill, HashSet<ARGUMENT> args) {
        boolean totalLevel = skill.getName() == OVERALL;
        BufferedImage skillImage = totalLevel
                ? copyImage(totalLevelBox)
                : args.contains(ARGUMENT.XP) ? copyImage(xpSkillBox) : copyImage(skillBox);
        Graphics g = skillImage.getGraphics();
        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));


        // Fill the background of the skill box with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(skillImage);
        }

        final int displayLevel = args.contains(ARGUMENT.VIRTUAL) ? skill.getVirtualLevel() : skill.getLevel();
        final String level = skill.isRanked() ? String.valueOf(displayLevel) : "-";
        final int centreX = skillImage.getWidth() / 2;

        // Drawing total level
        if(totalLevel) {
            g.setColor(Color.YELLOW);
            FontMetrics fm = g.getFontMetrics();

            final String title = "Total Level:";

            // Match alignment with the top number of a skill box
            g.drawString(
                    title,
                    centreX - (fm.stringWidth(title) / 2),
                    UPPER_SKILL_TEXT_Y
            );


            // Match alignment with the bottom number of a skill box
            g.drawString(
                    level,
                    (skillImage.getWidth() / 2) - (fm.stringWidth(level) / 2),
                    LOWER_SKILL_TEXT_Y
            );
            g.dispose();
            return skillImage;
        }

        // Drawing skill with xp
        if(args.contains(ARGUMENT.XP)) {
            g.setColor(Color.YELLOW);
            final int centreY = skillImage.getHeight() / 2;

            final String xp = skill.isRanked() ? skill.getFormattedXp() : "-";
            g.drawString(level, centreX - (g.getFontMetrics().stringWidth(level) / 2), centreY);

            g.setFont(getGameFont().deriveFont(50f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(xp, centreX - (fm.stringWidth(xp) / 2), centreY + fm.getMaxAscent());

            final BufferedImage icon = skill.getImage(false);

            // Skip icon if it is missing
            if(icon != null) {
                g.drawImage(icon, BORDER, BORDER, null);
            }

            // Draw a progress bar at the bottom of the skill image indicating progress to the next level
            if(skill.isRanked() && skill.getLevel() < Skill.DEFAULT_MAX) {
                final int progressBarHeight = 20;

                // XP required to be the current level (not current XP)
                final int xpAtCurrentLevel = skill.getXpAtCurrentLevel();

                // Total XP required to go from current level (not current XP) to the next level
                final int totalXpInLevel = skill.getXpAtNextLevel() - xpAtCurrentLevel;

                // Percentage progress until next level (e.g 0.42)
                double levelProgress = ((skill.getXp() - xpAtCurrentLevel) / (double) totalXpInLevel);

                Rectangle underlay = new Rectangle(
                        SKILL_NOTCH_SIZE,
                        skillImage.getHeight() - SKILL_BORDER - progressBarHeight,
                        skillImage.getWidth() - 2 * SKILL_NOTCH_SIZE,
                        progressBarHeight
                );

                final int arc = progressBarHeight / 2;

                // Draw base progress bar
                g.setColor(blackOverlay);
                g.fillRoundRect(
                        underlay.x,
                        underlay.y,
                        underlay.width,
                        underlay.height,
                        arc,
                        arc
                );

                // Draw progress on top
                g.setColor(greenOverlay);
                g.fillRoundRect(
                        underlay.x,
                        underlay.y,
                        (int) (underlay.width * levelProgress),
                        underlay.height,
                        arc,
                        arc
                );

                // Draw outline
                g.setColor(Color.BLACK);
                g.drawRoundRect(
                        underlay.x,
                        underlay.y,
                        underlay.width,
                        underlay.height,
                        arc,
                        arc
                );

                // Draw progress percentage
                g.setColor(Color.WHITE);
                g.setFont(getGameFont().deriveFont(16f));

                final String progress = new DecimalFormat("0.00%").format(levelProgress);
                g.drawString(
                        progress,
                        underlay.x + (underlay.width / 2) - (g.getFontMetrics().stringWidth(progress) / 2),
                        underlay.y + (underlay.height / 2) + (g.getFontMetrics().getMaxAscent() / 2)
                );
            }
        }

        // Drawing normal skill
        else {
            boolean master = displayLevel > Skill.DEFAULT_MAX;

            // Offset x when skill level is higher than 99 (to allow more room to draw)
            final int masterOffset = 10;

            final int upperTextX = master ? UPPER_SKILL_TEXT_X - masterOffset : UPPER_SKILL_TEXT_X;
            final int lowerTextX = master ? LOWER_SKILL_TEXT_X - masterOffset : LOWER_SKILL_TEXT_X;

            // Draw skill level shadow first
            g.setColor(Color.BLACK);

            g.drawString(level, upperTextX + SHADOW_OFFSET, UPPER_SKILL_TEXT_Y + SHADOW_OFFSET);
            g.drawString(level, lowerTextX + SHADOW_OFFSET, LOWER_SKILL_TEXT_Y + SHADOW_OFFSET);

            // Draw skill level over shadow
            g.setColor(Color.YELLOW);

            g.drawString(level, upperTextX, UPPER_SKILL_TEXT_Y);
            g.drawString(level, lowerTextX, LOWER_SKILL_TEXT_Y);

            // Where centre of icon should be
            BufferedImage icon = skill.getImage(true);

            // Skip icon if it is missing
            if(icon != null) {
                g.drawImage(
                        icon,
                        (skillImage.getWidth() / 4) - (icon.getWidth() / 2),
                        (skillImage.getHeight() / 2) - (icon.getHeight() / 2),
                        null
                );
            }
        }

        /*
         * Draw a red overlay on unranked skills (not when showing boxes).
         * This is because unranked skills are reported as 1 regardless of what the player's actual level is.
         */
        if(!skill.isRanked() && !args.contains(ARGUMENT.SHOW_BOXES)) {
            highlightSkillBox(skillImage, redOverlay);
        }

        // Draw a green overlay on maxed skills (if specified)
        if(args.contains(ARGUMENT.MAX) && skill.getLevel() == Skill.DEFAULT_MAX) {
            highlightSkillBox(skillImage, greenOverlay);
        }

        g.dispose();
        return skillImage;
    }

    /**
     * Highlight the given skill box image with the given colour.
     * Skill boxes do not fill the image as they have angled corners, use {@link Graphics#drawPolygon(int[], int[], int)}
     * to fill within these corners (leaving the border around the skill box exposed).
     *
     * @param skillBox Skill box image
     * @param colour   Colour to highlight
     */
    private void highlightSkillBox(BufferedImage skillBox, Color colour) {
        Graphics g = skillBox.getGraphics();
        g.setColor(colour);

        final int[] xPoints = new int[]{
                SKILL_NOTCH_SIZE, // Start at top of top left notch
                skillBox.getWidth() - SKILL_NOTCH_SIZE, // Move to top of top right notch
                skillBox.getWidth() - SKILL_BORDER, // Move to bottom of top right notch
                skillBox.getWidth() - SKILL_BORDER, // Move to top of bottom right notch
                skillBox.getWidth() - SKILL_NOTCH_SIZE, // Move to bottom of bottom right notch
                SKILL_NOTCH_SIZE, // Move to bottom of bottom left notch
                SKILL_BORDER, // Move to top of bottom left notch
                SKILL_BORDER, // Move to bottom of top left notch
                SKILL_NOTCH_SIZE, // Return to top of top left notch
        };

        final int[] yPoints = new int[]{
                SKILL_BORDER, // Start at top of top left notch
                SKILL_BORDER, // Move to top of top right notch
                SKILL_NOTCH_SIZE, // Move to bottom of top right notch
                skillBox.getHeight() - SKILL_NOTCH_SIZE, // Move to top of bottom right notch
                skillBox.getHeight() - SKILL_BORDER, // Move to bottom of bottom right notch
                skillBox.getHeight() - SKILL_BORDER, // Move to bottom of bottom left notch
                skillBox.getHeight() - SKILL_NOTCH_SIZE, // Move to top of bottom left notch
                SKILL_NOTCH_SIZE, // Move to bottom of top left notch
                SKILL_BORDER, // Return to top of top left notch
        };

        g.fillPolygon(xPoints, yPoints, xPoints.length);
        g.dispose();
    }

    @Override
    public BufferedImage buildHiscoresImage(OSRSPlayerStats playerStats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
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

        BufferedImage bossSection = buildBossSection(playerStats.getBossStats(), args);
        g.drawImage(bossSection, skillsSection.getWidth(), titleSection.getHeight(), null);

        // When optional sections are displayed vertically, they should be displayed below the base image
        int y = titleSection.getHeight() + skillsSection.getHeight();

        if(shouldDisplayClues(playerStats)) {
            BufferedImage clueSection = buildClueSection(playerStats.getClues(), args);
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
            BufferedImage leagueUnlockSection = buildLeagueUnlockSection((OSRSLeaguePlayerStats) playerStats);
            g.drawImage(leagueUnlockSection, 0, y, null);
            y += leagueUnlockSection.getHeight();
        }

        // League points/tier
        if(shouldDisplayLeagueInfo(playerStats)) {
            BufferedImage leagueInfoSection = buildLeagueInfoSection((OSRSLeaguePlayerStats) playerStats);
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
    private BufferedImage buildLeagueInfoSection(OSRSLeaguePlayerStats stats) {
        BufferedImage container = copyImage(leagueInfoContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int edgePadding = 2 * BORDER; // Padding from outer edges
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
    private int calculateImageHeight(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {

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
    private int calculateImageWidth(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {

        /*
         * Base width - title section, skills section, and boss section are always displayed,
         * the title section is equal in width to the skills section & boss section combined.
         */
        int width = titleContainer.getWidth();

        // XP tracker is optional and is displayed beside the base image, add its width to the base width
        return shouldDisplayXpTracker(args, stats) ? width + xpHeader.getWidth() : width;
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
        return stats instanceof OSRSLeaguePlayerStats && ((OSRSLeaguePlayerStats) stats).hasLeagueUnlockData();
    }

    /**
     * Check if the player's league points & tier should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display league points/tier in hiscores image
     */
    private boolean shouldDisplayLeagueInfo(OSRSPlayerStats stats) {
        return stats instanceof OSRSLeaguePlayerStats;
    }

    /**
     * Check if the XP tracker data should be fetched for the player.
     * Don't fetch xp if league stats are requested.
     *
     * @param args Hiscores arguments
     * @return XP tracker data should be fetched
     */
    private boolean shouldFetchXpTracker(HashSet<ARGUMENT> args) {
        return args.contains(ARGUMENT.XP) && !args.contains(ARGUMENT.LEAGUE) && !args.contains(ARGUMENT.DMM);
    }

    /**
     * Check if the player's recent achievements should be fetched
     *
     * @param args Hiscores arguments
     * @return Recent achievements should be fetched
     */
    private boolean shouldFetchAchievements(HashSet<ARGUMENT> args) {
        return args.contains(ARGUMENT.ACHIEVEMENTS) && !args.contains(ARGUMENT.LEAGUE) && !args.contains(ARGUMENT.DMM);
    }

    /**
     * Check if the XP tracker section should be drawn on to the hiscores image
     *
     * @param args  Hiscores arguments
     * @param stats Player stats
     * @return Should display XP tracker in hiscores image
     */
    private boolean shouldDisplayXpTracker(HashSet<ARGUMENT> args, OSRSPlayerStats stats) {
        return shouldFetchXpTracker(args) && (stats.hasWeeklyGains() || stats.hasWeeklyRecords());
    }

    /**
     * Check if achievements section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Should display player achievements in hiscores image
     */
    private boolean shouldDisplayAchievements(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {
        return shouldFetchAchievements(args) && stats.hasAchievements();
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
        final int rowHeight = (achievementsImage.getHeight() - (2 * BORDER)) / max;
        final int rowWidth = achievementsImage.getWidth() - (2 * BORDER);

        int y = BORDER;
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
                    BORDER,
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

        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        fm = g.getFontMetrics();

        final PlayerStats.ACCOUNT accountType = stats.getAccountType();

        // Draw account type image
        if(accountType != PlayerStats.ACCOUNT.NORMAL && accountType != PlayerStats.ACCOUNT.LEAGUE && accountType != PlayerStats.ACCOUNT.DMM) {
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

        final int edgePadding = 2 * BORDER; // Padding from outer edges
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
        final String rank = "Rank: " + stats.getTotalLevel().getFormattedRank();
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
     * @param args  Hiscores arguments
     * @return Image displaying player clue scroll completions
     */
    private BufferedImage buildClueSection(Clue[] clues, HashSet<ARGUMENT> args) {
        final BufferedImage container = copyImage(cluesContainer);

        Graphics g = container.getGraphics();

        final int maxClues = clues.length - 1; // Not drawing "ALL" type clue

        // Always calculate for max clues
        final int clueWidth = (container.getWidth() - (2 * BORDER)) / maxClues;
        final int clueHeight = container.getHeight() - (2 * BORDER);

        int x = BORDER;

        // Build clue stat images with transparent backgrounds to place in to the clue section
        for(Clue clue : clues) {
            if(clue.getType() == Clue.TYPE.ALL) {
                continue;
            }
            final BufferedImage clueImage = buildClueImage(clue, clueWidth, clueHeight, args);
            g.drawImage(clueImage, x, BORDER, null);
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
     * @param args   Hiscores arguments
     * @return Clue image
     */
    private BufferedImage buildClueImage(Clue clue, int width, int height, HashSet<ARGUMENT> args) {
        final BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = background.getGraphics();

        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int centreVertical = height / 2;
        final int centreHorizontal = width / 2;

        // Fill the background of the clue section with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(background);
        }

        final BufferedImage clueImage = clue.getFullImage(ResourceHandler.OSRS_BASE_PATH);

        // Clue image may be missing
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

        // Draw a red overlay on incomplete clues (not when showing boxes)
        if(!clue.hasCompletions() && !args.contains(ARGUMENT.SHOW_BOXES)) {
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

        // Adjusted height after factoring in border that surrounds XP container image
        final int adjustedHeight = tracker.getHeight() - (2 * BORDER);

        final int rowHeight = adjustedHeight / skills.length;

        // Pad out final skill (total level) row height with what is left after integer division
        final int finalPadding = adjustedHeight - (skills.length * rowHeight);

        final int rowWidth = tracker.getWidth() - (2 * BORDER);
        int y = BORDER;

        for(int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            Color colour = i % 2 == 0 ? dark : light;
            BufferedImage row = buildSkillXpRow(
                    rowWidth,
                    i == skills.length - 1 ? rowHeight + finalPadding : rowHeight,
                    colour,
                    skill
            );
            g.drawImage(row, BORDER, y, null);
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

        Date startDate = stats.getTrackerStartDate();
        Date endDate = stats.getTrackerEndDate();
        final String unknown = "UNKNOWN";

        String trackerPeriod = (startDate == null ? unknown : displayFormat.format(stats.getTrackerStartDate()))
                + " - "
                + (endDate == null ? unknown : displayFormat.format(stats.getTrackerEndDate()));

        // x & y aligning with hardcoded text in header
        final int headerX = 793;
        final int headerY = 166;

        g.drawString(
                trackerPeriod,
                headerX - (g.getFontMetrics().stringWidth(trackerPeriod) / 2),
                headerY
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

        final int iconX = BORDER, expX = 481, recordX = 962, sectionMid = row.getHeight() / 2;

        BufferedImage icon = skill.getImage(false);

        // Draw skill icon (may be missing but shouldn't ever happen)
        if(icon != null) {
            g.drawImage(icon, iconX, sectionMid - (icon.getHeight() / 2), null);
        }

        // Draw text here to be centred in row
        final int textY = sectionMid + (g.getFontMetrics().getMaxAscent() / 2);
        final DecimalFormat xpFormat = new DecimalFormat("#,### XP");

        // Gained XP is equal/higher than the current record (or no current record)
        final boolean recordBroken = skill.getGainedXp() >= skill.getRecordXp();

        String name = StringUtils.capitalize(skill.getName().name().toLowerCase());

        String xp = xpFormat.format(skill.getGainedXp());

        // Draw XP gained as either green (if higher than the current record) or yellow (lower) in the format '+100 XP'
        if(skill.hasGainedXp()) {
            g.setColor(recordBroken ? Color.GREEN : Color.YELLOW);
            xp = "+" + xp;
        }

        // Draw XP gained as white without a plus sign e.g '0 XP'
        else {
            g.setColor(skill.getGainedXp() < 0 ? Color.RED : Color.WHITE);
        }

        g.drawString(name, iconX + Skill.ICON_WIDTH + BORDER, textY);
        g.drawString(xp, expX, textY);

        String recordXp;

        // Draw either the weekly XP record or a message indicating that the record has been broken
        if(skill.hasRecordXp()) {
            if(recordBroken) {
                g.setColor(Color.MAGENTA);
                recordXp = "New record!";
            }
            else {
                g.setColor(Color.GREEN);
                recordXp = xpFormat.format(skill.getRecordXp());
            }
        }

        // No weekly record for the skill, draw a message in white noting this
        else {
            g.setColor(Color.WHITE);
            recordXp = "No record for this skill";
        }

        g.drawString(recordXp, recordX, textY);
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
    private BufferedImage buildLeagueUnlockSection(OSRSLeaguePlayerStats stats) {
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
            if(kills > PlayerStats.UNRANKED) {
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