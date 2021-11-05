package Runescape.Hiscores;

import Bot.DiscordUser;
import Command.Commands.Lookup.RunescapeLookupCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import Runescape.OSRS.Boss.Boss;
import Runescape.OSRS.Boss.BossManager;
import Runescape.OSRS.Boss.BossStats;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.RelicTier;
import Runescape.Stats.*;
import net.dv8tion.jda.api.entities.Emote;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Runescape.Hiscores.Hiscores.LOADING_UPDATE_TYPE.*;
import static Runescape.OSRS.League.LeagueTier.LEAGUE_POINTS_RANK_INDEX;
import static Runescape.Stats.Skill.SKILL_NAME.*;
import static Runescape.Stats.Skill.SKILL_NAME.HUNTER;
import static Runescape.Hiscores.WiseOldMan.TrackerResponse.RECORDS_KEY;
import static Runescape.Hiscores.WiseOldMan.TrackerResponse.XP_KEY;

/**
 * Fetch player OSRS stats from the Hiscores API
 */
public class OSRSHiscores extends Hiscores<OSRSPlayerStats> {
    public static final String
            MVALUE = "hiscore_oldschool",
            RANKING_PAGE = "overall",
            LEAGUE_THUMBNAIL = "https://i.imgur.com/xksIl6S.png",
            TOURNAMENT_THUMBNAIL = "https://i.imgur.com/O2HpIt3.png",
            DMM_THUMBNAIL = "https://i.imgur.com/nJVs5Ey.png";

    private final HashMap<PlayerStats.ACCOUNT, Integer> lowestRankMap = new HashMap<>();
    private final Boss.BOSS_ID[] bossIds;
    private final BossManager bossManager;
    private long lastRankCheck;

    private final SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Create the OSRS Hiscores instance
     */
    public OSRSHiscores() {
        super(
                MVALUE,
                RANKING_PAGE
        );
        this.bossIds = BossManager.getIdsInHiscoresOrder();
        this.bossManager = BossManager.getInstance();
        this.parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        updateLowestRanks();
    }

    /**
     * Create a map of account type to the current lowest rank.
     * This will only be done if an hour or more has passed.
     */
    private void updateLowestRanks() {
        if(lastRankCheck != 0 && System.currentTimeMillis() - lastRankCheck < 3600000) {
            return;
        }

        final JSONObject lowestRanks = fetchLowestRanks();

        // Failed to fetch stored ranks
        if(lowestRanks == null) {
            return;
        }

        for(PlayerStats.ACCOUNT accountType : PlayerStats.ACCOUNT.values()) {
            final String databaseKey = accountType.getDatabaseKey();

            // No stored data for this account type
            if(databaseKey == null) {
                continue;
            }

            // Contains lowest known rank, timestamp, etc
            final JSONObject lowestRankDetails = lowestRanks.getJSONObject(databaseKey);

            lowestRankMap.put(accountType, lowestRankDetails.getInt("lowest_rank"));
        }
        lastRankCheck = System.currentTimeMillis();
    }

    /**
     * Fetch the lowest known ranks for all account types from the API.
     * The response JSON is in the format {@link PlayerStats.ACCOUNT#getDatabaseKey()} -> details.
     * If the API fails to respond, null will be returned.
     *
     * @return Lowest known ranks JSON response or null
     */
    @Nullable
    public static JSONObject fetchLowestRanks() {
        final String url = "http://" + Secret.LOCAL_IP + "/OSRSRanks/ranks";
        final NetworkResponse response = new NetworkRequest(url, false).get();

        // No response
        if(response.body == null) {
            return null;
        }

        return new JSONObject(response.body);
    }

    @Override
    protected ArrayList<String> getOtherLoadingCriteria(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        ArrayList<String> criteria = new ArrayList<>();

        if(accountType == PlayerStats.ACCOUNT.LEAGUE) {
            criteria.add("Player has stored Relic/Region data...");
            criteria.add("Calculating League Tier...");
        }

        if(shouldFetchAchievements(args, accountType)) {
            criteria.add("Fetching achievements...");
        }

        if(shouldFetchXpTracker(args, accountType)) {
            criteria.add("Checking XP tracker...");
        }

        return criteria;
    }

    /**
     * Check if the XP tracker data should be fetched for the player.
     * Don't fetch xp if league/DMM stats are requested.
     *
     * @param args        Hiscores arguments
     * @param accountType Account type
     * @return XP tracker data should be fetched
     */
    private boolean shouldFetchXpTracker(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        return args.contains(RunescapeLookupCommand.ARGUMENT.XP_TRACKER)
                && !args.contains(RunescapeLookupCommand.ARGUMENT.BOSSES)
                && WiseOldMan.isSupportedAccountType(accountType);
    }

    /**
     * Check if the player's recent achievements should be fetched
     *
     * @param args        Hiscores arguments
     * @param accountType Account type
     * @return Recent achievements should be fetched
     */
    private boolean shouldFetchAchievements(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        return args.contains(RunescapeLookupCommand.ARGUMENT.ACHIEVEMENTS)
                && !args.contains(RunescapeLookupCommand.ARGUMENT.BOSSES)
                && WiseOldMan.isSupportedAccountType(accountType);
    }

    @Override
    public String getLoadingTitle(String name, HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType, EmoteHelper emoteHelper) {
        String lookupPrefix = "Hiscores";

        switch(accountType) {
            case LEAGUE:
                lookupPrefix = "League";
                break;
            case DMM:
            case TOURNAMENT:
                lookupPrefix = "DMM";
                break;
        }

        String title = "OSRS " + lookupPrefix + " Lookup: ";

        final Emote accountTypeEmote = accountType.getEmote(emoteHelper);

        // Add the optional account type emote as a prefix to the player name (e.g an ironman helmet)
        if(accountTypeEmote != null) {
            title += accountTypeEmote.getAsMention() + " ";
        }

        return title + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        switch(accountType) {
            case LEAGUE:
                return LEAGUE_THUMBNAIL;
            case TOURNAMENT:
                return TOURNAMENT_THUMBNAIL;
            case DMM:
                return DMM_THUMBNAIL;
            default:
                return EmbedHelper.OSRS_LOGO;
        }
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
    protected @Nullable Elements parseHiscoresPlayerElements(Document rankPage) {
        try {
            return rankPage.getElementsByClass("personal-hiscores__row");
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected @Nullable String parseNameFromPlayerElement(Element playerElement) {
        try {
            return playerElement.getElementsByClass("left").first().text();
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected @Nullable Integer parseRankFromPlayerElement(Element playerElement) {
        try {
            final String rankString = playerElement.getElementsByClass("right")
                    .first()
                    .text()
                    .replaceAll(",", "")
                    .trim();

            return Integer.parseInt(rankString);
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected OSRSPlayerStats applyStatArgs(OSRSPlayerStats stats, HashSet<RunescapeLookupCommand.ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        final PlayerStats.ACCOUNT accountType = stats.getAccountType();

        // Fetch achievements if requested and update the loading message
        if(shouldFetchAchievements(args, accountType)) {
            addPlayerAchievements(stats, loadingMessage);
        }

        // Fetch the XP tracker data if requested and update the loading message
        if(shouldFetchXpTracker(args, accountType)) {
            addPlayerXpTracker(stats, loadingMessage);
        }

        return stats;
    }

    @Override
    protected @Nullable OSRSPlayerStats locatePlayerStats(String name, ImageLoadingMessage... loadingMessage) {
        updateLoadingMessage(UPDATE, "Checking name exists...", loadingMessage);
        OSRSPlayerStats normalAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.NORMAL);

        // Player doesn't exist
        if(normalAccount == null) {
            return null;
        }

        updateLoadingMessage(UPDATE, "Player exists, checking ironman hiscores", loadingMessage);
        OSRSPlayerStats ironAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.IRON);

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
        OSRSPlayerStats hardcoreAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.HARDCORE);

        // Player appears on the hardcore ironman hiscores, may be dead
        if(hardcoreAccount != null) {

            // Player has died and/or reverted to a normal ironman
            if(ironAccount.getTotalXp() > hardcoreAccount.getTotalXp() || ((OSRSHardcorePlayerStats) hardcoreAccount).isDead()) {
                updateLoadingMessage(
                        COMPLETE,
                        "Player was a Hardcore Ironman and died/reverted! What a loser!",
                        loadingMessage
                );
                return ironAccount;
            }

            updateLoadingMessage(COMPLETE, "Player is a Hardcore Ironman!", loadingMessage);
            return hardcoreAccount;
        }

        updateLoadingMessage(UPDATE, "Player is not hardcore, checking Ultimate Ironman hiscores", loadingMessage);
        OSRSPlayerStats ultimateAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.ULTIMATE);

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
    protected OSRSPlayerStats parseStats(HiscoresApiResponse statsResponse, ImageLoadingMessage... loadingMessage) {
        final String[] statsCsv = statsResponse.getStatsCsv();

        switch(statsResponse.getAccountType()) {

            // Fetch extra data required to build seasonal league stats
            case LEAGUE:
                return parseLeagueStats(statsResponse, loadingMessage);

            // Check if the player has died and create hardcore ironman stats
            case HARDCORE:
                return parseHardcoreStats(statsResponse);

            // Nothing more required
            default:
                final Skill[] skills = parseSkills(
                        statsCsv,
                        Skill.DEFAULT_XP_AT_MAX_LEVEL,
                        Skill.DEFAULT_MAX_XP
                );
                return new OSRSPlayerStats(
                        statsResponse.getName(),
                        statsResponse.getUrl(),
                        skills,
                        parseClueScrolls(statsCsv),
                        parseTotalLevel(statsCsv, skills),
                        parseBossStats(statsCsv),
                        parseLmsInfo(statsCsv),
                        statsResponse.getAccountType()
                );
        }
    }

    /**
     * Parse the given hardcore ironman hiscores API CSV in to hardcore ironman player stats.
     * Make a further request to find out if the player has died (lost their hardcore status).
     *
     * @param statsResponse Hardcore ironman hiscores API stats response
     * @return Hardcore ironman player stats
     */
    private OSRSHardcorePlayerStats parseHardcoreStats(HiscoresApiResponse statsResponse) {
        final String[] statsCsv = statsResponse.getStatsCsv();

        final Skill[] skills = parseSkills(
                statsCsv,
                Skill.DEFAULT_XP_AT_MAX_LEVEL,
                Skill.DEFAULT_MAX_XP
        );

        return new OSRSHardcorePlayerStats(
                statsResponse.getName(),
                statsResponse.getUrl(),
                skills,
                parseClueScrolls(statsCsv),
                parseTotalLevel(statsCsv, skills),
                parseBossStats(statsCsv),
                parseLmsInfo(statsCsv),
                isHardcoreDead(statsResponse.getName())
        );
    }

    /**
     * Attempt to check whether the given player has died as a hardcore ironman.
     * This is done by scraping the hiscores and looking for the death icon,
     * it may be null if there is an issue scraping.
     *
     * @param name Player name
     * @return Player has died as a hardcore ironman
     */
    @Nullable
    private Boolean isHardcoreDead(String name) {
        final Element playerElement = getPlayerElementByName(PlayerStats.ACCOUNT.HARDCORE, name);

        // Unable to find player on hardcore ironman hiscores
        if(playerElement == null) {
            return null;
        }

        final Elements deathIcon = playerElement.getElementsByClass("hiscore-death");
        return !deathIcon.isEmpty();
    }

    /**
     * Parse the given league hiscores API CSV in to league player stats.
     * Make further requests for stored relics/regions & league tier info.
     * Complete the league loading stages if a loading message is provided.
     *
     * @param statsResponse  League hiscores API stats response
     * @param loadingMessage Optional loading message
     * @return League player stats
     */
    private OSRSLeaguePlayerStats parseLeagueStats(HiscoresApiResponse statsResponse, ImageLoadingMessage... loadingMessage) {
        final String[] statsCsv = statsResponse.getStatsCsv();
        final int leaguePoints = Integer.parseInt(statsCsv[LeagueTier.LEAGUE_POINTS_INDEX]);
        final long rank = Long.parseLong(statsCsv[LEAGUE_POINTS_RANK_INDEX]);

        LeagueTier leagueTier = new LeagueTier(
                WiseOldMan.getInstance().calculateLeagueTier(rank),
                leaguePoints == RankedMetric.UNRANKED ? 0 : leaguePoints,
                rank
        );

        ArrayList<RelicTier> relicTiers = new ArrayList<>();
        ArrayList<Region> regions = new ArrayList<>();

        final String leagueJSON = DiscordUser.getOSRSLeagueData(statsResponse.getName());
        try {
            JSONObject leagueData = new JSONObject(leagueJSON);
            relicTiers = RelicTier.parseRelics(leagueData.getJSONArray("relics"));
            regions = Region.parseRegions(leagueData.getJSONArray("regions"));

            // Nothing stored
            if(relicTiers.isEmpty() || regions.isEmpty()) {
                throw new Exception("No stored league data!");
            }

            // Optionally complete "Player has stored Relic/Region data..." loading stage
            completeLoadingMessageStage(loadingMessage);
        }

        // No stored relics & regions
        catch(Exception e) {
            updateLoadingMessage(FAIL, "Try `trailblazer` command to store unlocks", loadingMessage);
        }

        // Optionally complete "Calculating League Tier" loading stage
        updateLoadingMessage(COMPLETE, "Player is " + leagueTier.getTierName() + "!", loadingMessage);

        final Skill[] skills = parseSkills(
                statsCsv,
                Skill.DEFAULT_XP_AT_MAX_LEVEL,
                Skill.DEFAULT_MAX_XP
        );

        return new OSRSLeaguePlayerStats(
                statsResponse.getName(),
                statsResponse.getUrl(),
                skills,
                parseClueScrolls(statsCsv),
                parseTotalLevel(statsCsv, skills),
                parseBossStats(statsCsv),
                parseLmsInfo(statsCsv),
                leagueTier,
                relicTiers,
                regions
        );
    }

    @Override
    public @Nullable HiscoresPlayer getLowestRankPlayer(PlayerStats.ACCOUNT accountType) {
        updateLowestRanks();
        if(lowestRankMap.containsKey(accountType)) {
            return getPlayerDetailsByRank(accountType, lowestRankMap.get(accountType));
        }
        return null;
    }

    /**
     * Fetch and add weekly XP gains for the player
     * Update and complete the loading stage of "Checking XP tracker..." if a loading message is provided.
     *
     * @param stats          Player stats
     * @param loadingMessage Optional loading message
     */
    private void addPlayerXpTracker(OSRSPlayerStats stats, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking Weekly XP...", loadingMessage);
            WiseOldMan.TrackerResponse response = WiseOldMan.getInstance().getXpTrackerData(
                    stats.getName(),
                    stats.isLeague()
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

                Skill.SKILL_NAME skillName = Skill.SKILL_NAME.fromName(key);

                // New skill that hasn't been mapped
                if(skillName == UNKNOWN) {
                    continue;
                }

                JSONObject experienceData = entry.getJSONObject("experience");
                stats.getSkill(skillName).setGainedXp(experienceData.getLong("gained"));
            }

            JSONArray records = responseData.getJSONArray(RECORDS_KEY);

            // Each object is a record - a record pertains to a time period (e.g week/year) and a metric (skill/boss/etc)
            for(int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                Skill.SKILL_NAME skillName = Skill.SKILL_NAME.fromName(record.getString("metric"));

                // Only concerned with weekly XP records
                if(skillName == UNKNOWN || !record.getString("period").equals("week")) {
                    continue;
                }

                stats.getSkill(skillName).setRecordXp(record.getLong("value"));
            }

            String beginningAt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .format(stats.getTrackerStartDate());

            String trackerUrl = WiseOldMan.getInstance().getXpTrackerBrowserUrl(stats.getName(), stats.isLeague());

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
     * Fetch and add recent achievements for the player.
     * Update and complete the loading stage of "Fetching achievements..." if a loading message is provided.
     *
     * @param stats          Player stats
     * @param loadingMessage Optional loading message
     */
    private void addPlayerAchievements(OSRSPlayerStats stats, ImageLoadingMessage... loadingMessage) {
        try {
            updateLoadingMessage(UPDATE, "Checking tracker...", loadingMessage);
            WiseOldMan.TrackerResponse response = WiseOldMan.getInstance().getPlayerAchievementsData(
                    stats.getName(),
                    stats.isLeague()
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
     * Parse and sort skill data from the API CSV to the in-game display order
     *
     * @param csv          CSV from API
     * @param xpAtMaxLevel XP at the max level for the skill - e.g the XP for level 99
     * @param maxXp        Max possible XP for skill - e.g 200,000,000
     * @return Sorted CSV
     */
    private Skill[] parseSkills(String[] csv, long xpAtMaxLevel, long maxXp) {
        return new Skill[]{
                new Skill(ATTACK, 3, csv, xpAtMaxLevel, maxXp),
                new Skill(HITPOINTS, 12, csv, xpAtMaxLevel, maxXp),
                new Skill(MINING, 45, csv, xpAtMaxLevel, maxXp),
                new Skill(STRENGTH, 9, csv, xpAtMaxLevel, maxXp),
                new Skill(AGILITY, 51, csv, xpAtMaxLevel, maxXp),
                new Skill(SMITHING, 42, csv, xpAtMaxLevel, maxXp),
                new Skill(DEFENCE, 6, csv, xpAtMaxLevel, maxXp),
                new Skill(HERBLORE, 48, csv, xpAtMaxLevel, maxXp),
                new Skill(FISHING, 33, csv, xpAtMaxLevel, maxXp),
                new Skill(RANGED, 15, csv, xpAtMaxLevel, maxXp),
                new Skill(THIEVING, 54, csv, xpAtMaxLevel, maxXp),
                new Skill(COOKING, 24, csv, xpAtMaxLevel, maxXp),
                new Skill(PRAYER, 18, csv, xpAtMaxLevel, maxXp),
                new Skill(CRAFTING, 39, csv, xpAtMaxLevel, maxXp),
                new Skill(FIREMAKING, 36, csv, xpAtMaxLevel, maxXp),
                new Skill(MAGIC, 21, csv, xpAtMaxLevel, maxXp),
                new Skill(FLETCHING, 30, csv, xpAtMaxLevel, maxXp),
                new Skill(WOODCUTTING, 27, csv, xpAtMaxLevel, maxXp),
                new Skill(RUNECRAFTING, 63, csv, xpAtMaxLevel, maxXp),
                new Skill(SLAYER, 57, csv, xpAtMaxLevel, maxXp),
                new Skill(FARMING, 60, csv, xpAtMaxLevel, maxXp),
                new Skill(CONSTRUCTION, 69, csv, xpAtMaxLevel, maxXp),
                new Skill(HUNTER, 66, csv, xpAtMaxLevel, maxXp)
        };
    }

    /**
     * Build a sorted list of player boss kill data
     * Sort in descending order of kill count
     *
     * @param csv csv from API
     * @return Sorted list of player boss kill data
     */
    private List<BossStats> parseBossStats(String[] csv) {
        String[] stats = Arrays.copyOfRange(csv, BossStats.BOSS_START_INDEX, BossStats.BOSS_END_INDEX);
        List<BossStats> bossStats = new ArrayList<>();

        int i = 0;
        for(Boss.BOSS_ID bossId : bossIds) {
            int kills = Integer.parseInt(stats[i + 1]);
            if(kills > RankedMetric.UNRANKED) {
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
