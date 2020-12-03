package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Command.Structure.EmoteHelper;

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
import java.text.ParseException;
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
    private final boolean league, virtual;
    public final static String leagueThumbnail = "https://i.imgur.com/xksIl6S.png";
    private final Font trackerFont;

    /**
     * Create the OSRS Hiscores instance
     *
     * @param channel     Channel to send message to
     * @param emoteHelper Emote helper
     * @param league      Use league hiscores or normal
     * @param virtual     Calculate virtual levels or display hiscores provided levels
     */
    public OSRSHiscores(MessageChannel channel, EmoteHelper emoteHelper, boolean league, boolean virtual) {
        super(channel, emoteHelper, "/Runescape/OSRS/", "osrs.ttf");
        this.bossNames = Boss.getBossNames();
        this.league = league;
        this.virtual = virtual;
        this.trackerFont = registerFont(getResourcePath() + "wise_old_man.ttf", getResourceHandler());
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
        }
        criteria.add("Checking XP tracker...");
        return criteria;
    }

    @Override
    public String getLoadingTitle(String name) {
        return "OSRS " + (league ? "League" : "Hiscores") + " lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail() {
        return league ? leagueThumbnail : "https://i.imgur.com/Hoke7jA.png";
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
        return "https://secure.runescape.com/m=hiscore_oldschool" + type + "/index_lite.ws?player=" + encodeName(name);
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

        getTrackerData(stats);
        return playerStats;
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
            String json = fetchPlayerTrackingData(name, league); // Check if player exists

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

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            playerStats.setTrackerPeriod(
                    dateFormat.parse(week.getString("startsAt")),
                    dateFormat.parse(week.getString("endsAt"))
            );
            loading.completeStage(playerStats.hasWeeklyGains() ? "Weekly XP obtained" : "No XP gained this week");
        }
        catch(ParseException e) {
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
        JSONArray tiers = new JSONArray(
                new NetworkRequest("https://trailblazer.wiseoldman.net/api/league/tiers", false).get()
        );
        LEAGUE_TIER tier = LEAGUE_TIER.UNQUALIFIED;
        for(int i = 0; i < tiers.length(); i++) {
            JSONObject tierInfo = tiers.getJSONObject(i);
            if(rank > tierInfo.getLong("threshold")) {
                break;
            }
            tier = LEAGUE_TIER.valueOf(tierInfo.getString("name").toUpperCase());
        }
        return tier;
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

        if(stats.hasWeeklyGains()) {
            baseImage = addXPTracker(baseImage, stats);
        }
        return baseImage;
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String trackerPeriod = dateFormat.format(stats.getTrackerStartDate())
                + " - "
                + dateFormat.format(stats.getTrackerEndDate());

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
}
