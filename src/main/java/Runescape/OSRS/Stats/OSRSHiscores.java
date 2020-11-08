package Runescape.OSRS.Stats;

import Bot.DiscordUser;
import Command.Structure.EmoteHelper;

import Network.NetworkRequest;
import Runescape.Boss;
import Runescape.Hiscores;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.PlayerStats;
import Runescape.Skill;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static Runescape.Skill.SKILL_NAME.*;

/**
 * Build an image displaying a player's OSRS stats
 */
public class OSRSHiscores extends Hiscores {
    private final String[] bossNames;
    private final boolean league, virtual;
    public final static String leagueThumbnail = "https://i.imgur.com/xksIl6S.png";

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
    }

    @Override
    public ArrayList<String> getLoadingCriteria() {
        ArrayList<String> criteria = new ArrayList<>();
        criteria.add(league ? "Player has League stats..." : "Player exists...");
        if(!league) {
            criteria.add("Checking account type...");
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
            normalAccount.setLeaguePoints(Integer.parseInt(normal[73]));
            JSONObject leagueData = new JSONObject(DiscordUser.getOSRSLeagueData(name));
            normalAccount.setRegions(Region.parseRegions(leagueData.getJSONArray("regions")));
            normalAccount.setRelicTiers(RelicTier.parseRelics(leagueData.getJSONArray("relics")));
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

        if(playerStats == null) {
            return null;
        }

        getTrackerData((OSRSPlayerStats) playerStats);
        return playerStats;
    }

    /**
     * Update player tracking data/Begin tracking a player
     *
     * @param name Player name
     * @param wait Wait for response
     */
    private void updatePlayerTracking(String name, boolean wait) {
        new NetworkRequest(
                "https://wiseoldman.net/api/players/track",
                false
        ).post(
                new JSONObject().put("username", name).toString(),
                !wait
        );
    }

    /**
     * Fetch XP tracking data for the given player
     *
     * @param name Player name
     * @return XP tracking data
     */
    private String fetchPlayerTrackingData(String name) {
        return new NetworkRequest(
                "https://wiseoldman.net/api/players/username/" + name + "/gained",
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
        String name = playerStats.getName();
        String json = fetchPlayerTrackingData(name); // Check if player exists

        if(json.equals("err")) {
            updatePlayerTracking(name, false); // Tracking a new player can take 20+ seconds, don't wait
            loading.completeStage("Player not tracked - They will be *soon*™");
            return;
        }

        updatePlayerTracking(name, true); // Update player data before fetching again
        json = fetchPlayerTrackingData(name); // Get updated player data

        JSONObject stats = new JSONObject(json).getJSONObject("week").getJSONObject("data");

        for(String key : stats.keySet()) {
            JSONObject entry = stats.getJSONObject(key);
            if(!entry.has("experience")) {
                continue;
            }
            Skill.SKILL_NAME skillName = key.equals("overall")
                    ?
                    Skill.SKILL_NAME.TOTAL_LEVEL
                    :
                    Skill.SKILL_NAME.valueOf(key.toUpperCase().replace(" ", "_"));

            JSONObject experienceData = entry.getJSONObject("experience");
            playerStats.addGainedXP(skillName, experienceData.getLong("gained"));
        }
        loading.completeStage(playerStats.hasWeeklyGains() ? "Weekly XP obtained" : "No XP gained this week");
    }

    @Override
    public BufferedImage buildHiscoresImage(PlayerStats playerStats) {
        OSRSPlayerStats stats = (OSRSPlayerStats) playerStats;
        BufferedImage image = null;
        int fontSize = 65;
        boolean league = stats.isLeague();
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/stats_template.png");
            Graphics g = image.getGraphics();
            setGameFont(new Font("RuneScape Chat '07", Font.PLAIN, fontSize));
            g.setFont(getGameFont());
            FontMetrics fm = g.getFontMetrics();

            // First skill location
            int x = 200, ogX = x;
            int y = 315;

            Skill[] skills = playerStats.getSkills();
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
                    825 - (fm.stringWidth(level) / 2),
                    y + 65
            );

            // Clues
            String[] clues = playerStats.getClues();
            x = 170;
            y = 1960;
            g.setFont(getGameFont().deriveFont(fontSize));
            for(String quantity : clues) {
                int quantityWidth = fm.stringWidth(quantity) / 2;
                g.drawString(quantity, x - quantityWidth, y);
                x += 340;
            }
            List<Boss> bosses = stats.getBossKills();
            if(bosses.size() > 0) {
                int max = Math.min(5, bosses.size());

                // All images have 220px height, and the top name banner + bottom border has 260px total height, clue section has height of 425
                int padding = ((image.getHeight() - 260 - 425) - (5 * 220)) / 6;

                // Height of top name banner
                y = 230 + padding;

                int bossCentre = (int) (image.getWidth() * 0.625); // mid point of boss image section

                for(int i = 0; i < max; i++) {
                    Boss boss = bosses.get(i);
                    BufferedImage bossImage = boss.getImage();
                    g.drawImage(bossImage, bossCentre - (bossImage.getWidth() / 2), y, null);

                    g.setColor(Color.YELLOW);
                    g.setFont(getGameFont().deriveFont(getGameFont().getSize() * 1.2F));
                    String kills = boss.formatKills();
                    int killWidth = fm.stringWidth(kills);
                    g.drawString(kills, (int) ((image.getWidth() * 0.875) - killWidth / 2), (y + (bossImage.getHeight() / 2) + (fm.getHeight() / 2)));
                    y += 220 + padding;
                }
            }
            else {
                BufferedImage noBoss = getResourceHandler().getImageResource(getResourcePath() + "Templates/no_boss.png");
                g.drawImage(noBoss, (int) ((image.getWidth() * 0.75)) - (noBoss.getWidth() / 2), 200 + (((image.getHeight() - 200 - 425) / 2) - (noBoss.getHeight() / 2)), null);
            }

            // Name, rank, and optional league points
            String name = playerStats.getName();
            g.setFont(getGameFont().deriveFont(140f));
            fm = g.getFontMetrics();

            int nameSectionMid = 115;
            int nameWidth = fm.stringWidth(name);
            x = (image.getWidth() / 2) - (nameWidth / 2);
            y = nameSectionMid + (fm.getMaxAscent() / 2);
            g.drawString(name.toUpperCase(), x, y);

            g.setFont(getGameFont().deriveFont(75f));
            fm = g.getFontMetrics();
            int nameSectionTextY = nameSectionMid + (fm.getMaxAscent() / 2);

            if(playerStats.getAccountType() != PlayerStats.ACCOUNT.NORMAL) {
                BufferedImage accountType = getResourceHandler().getImageResource(
                        PlayerStats.getAccountTypeImagePath(playerStats.getAccountType())
                );
                int accountWidth = accountType.getWidth();
                x = league ? 50 : (x - (int) (accountWidth * 1.5));
                g.drawImage(accountType, x, nameSectionMid - (accountType.getHeight() / 2), null);

                if(league) {
                    String points = stats.getLeaguePoints();
                    g.drawString(points, x + accountWidth + 15, nameSectionTextY);
                }
            }

            String rank = "Rank: " + stats.getFormattedRank();
            BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
            int rankX = image.getWidth() - fm.stringWidth(rank) - 50;

            g.drawString(rank, rankX, nameSectionTextY);
            g.drawImage(
                    rankImage,
                    rankX - rankImage.getWidth() - 15,
                    nameSectionMid - (rankImage.getHeight() / 2),
                    null
            );

            if(league && stats.hasLeagueUnlockData()) {
                return buildLeagueImage(image, stats);
            }

            if(stats.hasWeeklyGains()) {
                System.out.println(stats.getName() + " has gained " + stats.getGainedXP() + " XP this week!\n");
                for(Skill skill : stats.getSkills()) {
                    if(!skill.hasGainedXP()) {
                        continue;
                    }
                    System.out.println(skill.getName() + ": +" + skill.getGainedXP() + " XP");
                }
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Append league relics and region unlocks to the player image
     *
     * @param baseImage Base Player image
     * @param stats     Player stats
     * @return Base player image with league unlocks appended
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
                new Skill(TOTAL_LEVEL, 0, csv)
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
