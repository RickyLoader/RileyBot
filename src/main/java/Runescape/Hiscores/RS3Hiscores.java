package Runescape.Hiscores;

import Command.Commands.Lookup.RunescapeLookupCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Runescape.Stats.*;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Runescape.Hiscores.Hiscores.LOADING_UPDATE_TYPE.*;
import static Runescape.Stats.Skill.SKILL_NAME.*;
import static Runescape.Stats.Skill.SKILL_NAME.ARCHAEOLOGY;

/**
 * Fetch player RS3 stats from the Hiscores API
 */
public class RS3Hiscores extends Hiscores<RS3PlayerStats> {
    public static final String
            MVALUE = "hiscore",
            RANKING_PAGE = "ranking";

    /**
     * Create the RS3 Hiscores instance
     */
    public RS3Hiscores() {
        super(MVALUE, RANKING_PAGE);
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
                new Skill(ARCHAEOLOGY, 84, csv)
        };
    }

    /**
     * Parse and format the player's clue scroll data from the hiscores CSV
     *
     * @param data CSV data from API
     * @return Clue scroll data
     */
    private Clue[] parseClueScrolls(String[] data) {
        data = Arrays.copyOfRange(data, 137, data.length);
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
     * Get the RuneMetrics stats of the player if they are public.
     * Complete the RuneMetrics loading stages if a loading message is provided.
     *
     * @param name           Player name
     * @param loadingMessage Optional loading message
     * @return Player RuneMetrics stats or null
     */
    @Nullable
    private RuneMetrics getRuneMetricsByPlayerName(String name, ImageLoadingMessage... loadingMessage) {
        final String url = "https://apps.runescape.com/runemetrics/profile/profile?user=" + EmbedHelper.urlEncode(name);
        final String embeddedUrl = EmbedHelper.embedURL("RuneMetrics", url);

        try {
            final String message = "Player's " + embeddedUrl + " is";
            JSONObject profile = new JSONObject(new NetworkRequest(url, false).get().body);

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
        catch(Exception e) {
            updateLoadingMessage(FAIL, embeddedUrl + " down!", loadingMessage);
            return null;
        }
    }

    /**
     * Get the clan that the given player is a member of (if they are in a clan).
     * Complete the clan loading stages if a loading message is provided.
     *
     * @param name           Player name
     * @param loadingMessage Optional loading message
     * @return Player clan or null
     */
    @Nullable
    private Clan getClanByPlayerName(String name, ImageLoadingMessage... loadingMessage) {

        // URL to the name of the clan that the player is a member of
        final String clanUrl = BASE_URL
                + "m=website-data/playerDetails.ws?names=%5B%22"
                + EmbedHelper.urlEncode(name)
                + "%22%5D&callback=jQuery000000000000000_0000000000&_=0";

        final String embedText = EmbedHelper.embedURL("clan", clanUrl);

        try {
            final String clanResponse = new NetworkRequest(clanUrl, false).get().body;
            Matcher matcher = Pattern.compile("\\{.+}").matcher(clanResponse);

            // Error fetching clan details
            if(!matcher.find()) {
                throw new Exception();
            }

            JSONObject playerClanDetails = new JSONObject(clanResponse.substring(matcher.start(), matcher.end()));
            final String clanKey = "clan";

            // Player is not a member of a clan
            if(!playerClanDetails.has(clanKey)) {
                updateLoadingMessage(FAIL, "Player is not part of a " + embedText, loadingMessage);
                return null;
            }

            final String clanName = playerClanDetails.getString(clanKey);

            updateLoadingMessage(
                    UPDATE,
                    "Player is in the **" + clanName + "** clan, fetching details...",
                    loadingMessage
            );

            final Clan clan = new Clan(clanName, getClanImage(clanName));
            final String clanMembersUrl = "http://services.runescape.com/m=clan-hiscores/members_lite.ws?clanName="
                    + EmbedHelper.urlEncode(clanName);

            final String[] clanMembers = new NetworkRequest(clanMembersUrl, false)
                    .get()
                    .body
                    .replace("�", " ")
                    .split("\n");

            // First row is redundant column info
            for(int i = 1; i < clanMembers.length; i++) {
                String[] memberDetails = clanMembers[i].split(",");
                clan.addPlayer(memberDetails[0], Clan.ROLE.byName(memberDetails[1]));
            }

            final Clan.ROLE playerRole = clan.getRoleByPlayerName(name);
            updateLoadingMessage(
                    COMPLETE,
                    "Player is "
                            + playerRole.getPrefix()
                            + " "
                            + EmbedHelper.embedURL(playerRole.getName(), clanMembersUrl)
                            + " of the **" + clanName + "** " + embedText + "!",
                    loadingMessage
            );
            return clan;
        }
        catch(Exception e) {
            updateLoadingMessage(FAIL, "Unable to determine if player is part of a " + embedText, loadingMessage);
            return null;
        }
    }

    /**
     * Attempt to get the banner image of a clan of the given name
     *
     * @param clanName Clan name
     * @return Clan banner image or null (unable to retrieve)
     */
    @Nullable
    private BufferedImage getClanImage(String clanName) {
        return EmbedHelper.downloadImage(
                BASE_URL + "m=avatar-rs/" + EmbedHelper.urlEncode(clanName) + "/clanmotif.png"
        );
    }

    /**
     * Attempt to get the avatar image for the given player.
     * This will return a default image if the player has not set one.
     *
     * @param playerName Player name
     * @return Player avatar image
     */
    public BufferedImage getPlayerAvatar(String playerName) {
        return EmbedHelper.downloadImage(
                "http://services.runescape.com/m=avatar-rs/" + EmbedHelper.urlEncode(playerName) + "/chat.png"
        );
    }

    @Override
    protected RS3PlayerStats applyStatArgs(RS3PlayerStats stats, HashSet<RunescapeLookupCommand.ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        final String name = stats.getName();

        // Fetch RuneMetrics if requested and update the loading message
        if(shouldFetchRuneMetrics(args)) {
            final RuneMetrics runeMetrics = getRuneMetricsByPlayerName(name, loadingMessage);
            if(runeMetrics != null) {
                stats.setRuneMetrics(runeMetrics);
            }
        }

        // Fetch clan details if requested and update the loading message
        if(shouldFetchClanDetails(args)) {
            final Clan clan = getClanByPlayerName(name, loadingMessage);
            if(clan != null) {
                stats.setClan(clan);
            }
        }

        return stats;
    }

    @Override
    public @Nullable HiscoresPlayer getLowestRankPlayer(PlayerStats.ACCOUNT accountType) {
        try {

            // First hiscores page of the given account type
            Document document = Jsoup.connect(getHiscoresRankingUrl(accountType)).get();

            // Buttons for first 5 pages + the final page
            Elements pagingButtons = document
                    .getElementsByClass("pagination")
                    .first()
                    .getElementsByClass("pageNumbers")
                    .first()
                    .children();

            // URL to final page of hiscores for the given account type
            final String finalPageUrl = pagingButtons
                    .last()
                    .getElementsByTag("a")
                    .first()
                    .absUrl("href");

            // List of players on the final page of the hiscores
            ArrayList<HiscoresPlayer> players = getAllPlayerDetailsOnHiscoresPage(finalPageUrl);
            if(players == null) {
                throw new Exception("Error getting final page players on " + accountType + " hiscores!");
            }

            return players.get(players.size() - 1);
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected RS3PlayerStats parseStats(HiscoresApiResponse statsResponse, ImageLoadingMessage... loadingMessage) {
        final String[] statsCsv = statsResponse.getStatsCsv();
        final Skill[] skills = parseSkills(statsCsv);

        RS3PlayerStats stats = new RS3PlayerStats(
                statsResponse.getName(),
                statsResponse.getUrl(),
                parseSkills(statsCsv),
                parseClueScrolls(statsCsv),
                parseTotalLevel(statsCsv, skills),
                statsResponse.getAccountType()
        );

        // Death status
        if(stats.getAccountType() == PlayerStats.ACCOUNT.HARDCORE) {
            stats.setHcimStatus(parseHCIMStatus(stats.getName()));
        }

        return stats;
    }

    /**
     * Get the hardcore ironman status for the account of the given name.
     * This is whether or not the account has died as a HCIM and the details about how it happened.
     * Do this by scraping the hiscores ranking page that contains the player and looking for the death symbol.
     *
     * @param playerName Name of the player to check
     * @return HCIM status or null (unable to retrieve)
     */
    @Nullable
    private HCIMStatus parseHCIMStatus(String playerName) {
        final String url = getHiscoresUrlByName(PlayerStats.ACCOUNT.HARDCORE, playerName);

        try {

            // Player death details beside their name in the hiscores list
            final Element playerDeathDetails = Jsoup
                    .connect(url)
                    .get()
                    .selectFirst(".hover .col2 .death-icon .death-icon__details");

            // No death details, the player is alive
            if(playerDeathDetails == null) {
                return new HCIMStatus();
            }

            // [0] -> "Player died" (title) [1] -> date [2] -> location [3] -> cause
            final Elements values = playerDeathDetails.children();

            return new HCIMStatus(
                    values.get(3).text(),
                    values.get(2).text(),
                    values.get(1).text()
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected @Nullable RS3PlayerStats locatePlayerStats(String name, ImageLoadingMessage... loadingMessage) {
        RS3PlayerStats normalAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.NORMAL);

        // Player doesn't exist
        if(normalAccount == null) {
            return null;
        }

        updateLoadingMessage(UPDATE, "Player exists, checking ironman hiscores", loadingMessage);
        RS3PlayerStats ironAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.IRON);

        if(ironAccount == null) {
            updateLoadingMessage(COMPLETE, "Player is a normal account!", loadingMessage);
            return normalAccount;
        }

        if(normalAccount.getTotalXp() > ironAccount.getTotalXp()) {
            updateLoadingMessage(COMPLETE, "Player is a de-ironed normal account!", loadingMessage);
            return normalAccount;
        }

        updateLoadingMessage(UPDATE, "Player is an Ironman, checking Hardcore Ironman hiscores", loadingMessage);
        RS3PlayerStats hardcoreAccount = getAccountTypeStats(name, PlayerStats.ACCOUNT.HARDCORE);

        if(hardcoreAccount != null) {
            final HCIMStatus hcimStatus = hardcoreAccount.getHcimStatus();

            if(hcimStatus.isDead()) {
                String status = "Player was a Hardcore Ironman and died.";

                // Died and reverted to a normal ironman
                if(ironAccount.getTotalXp() > hardcoreAccount.getTotalXp()) {
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

                // Died and the account is disabled
                else {
                    updateLoadingMessage(COMPLETE, status + " What a loser!", loadingMessage);
                    return hardcoreAccount;
                }
            }

            // Still alive
            updateLoadingMessage(COMPLETE, "Player is a Hardcore Ironman!", loadingMessage);
            return hardcoreAccount;
        }

        updateLoadingMessage(COMPLETE, "Player is an Ironman!", loadingMessage);
        return ironAccount;
    }

    /**
     * Check if the player's RuneMetrics should be fetched.
     * This includes their total quest completions.
     *
     * @param args Hiscores arguments
     * @return RuneMetrics should be fetched
     */
    private boolean shouldFetchRuneMetrics(HashSet<RunescapeLookupCommand.ARGUMENT> args) {
        return args.contains(RunescapeLookupCommand.ARGUMENT.RUNEMETRICS);
    }

    /**
     * Check if the player's clan details should be fetched.
     * This includes the clan that they are a part of (if they belong to a clan), and the
     * role that they have in the clan.
     *
     * @param args Hiscores arguments
     * @return Clan details should be fetched
     */
    private boolean shouldFetchClanDetails(HashSet<RunescapeLookupCommand.ARGUMENT> args) {
        return args.contains(RunescapeLookupCommand.ARGUMENT.CLAN);
    }

    @Override
    protected @Nullable Elements parseHiscoresPlayerElements(Document rankPage) {
        try {
            return rankPage
                    .getElementsByClass("tableWrap")
                    .first()
                    .getElementsByTag("table")// [0] -> table with only headers [1] -> table with players
                    .get(1)
                    .getElementsByTag("tr");
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected @Nullable String parseNameFromPlayerElement(Element playerElement) {
        try {
            return playerElement
                    .getElementsByClass("col2")
                    .first()
                    .selectFirst(".avatar")
                    .attr("alt");
        }
        catch(Exception e) {
            return null;
        }
    }

    @Override
    protected @Nullable Integer parseRankFromPlayerElement(Element playerElement) {
        try {
            final String rankString = playerElement.getElementsByClass("col1")
                    .first()
                    .child(0)
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
    protected ArrayList<String> getOtherLoadingCriteria(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        ArrayList<String> loadingCriteria = new ArrayList<>();

        if(shouldFetchRuneMetrics(args)) {
            loadingCriteria.add("Checking RuneMetrics...");
        }

        if(shouldFetchClanDetails(args)) {
            loadingCriteria.add("Checking clan...");
        }

        return loadingCriteria;
    }

    @Override
    public String getLoadingTitle(String name, HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType, EmoteHelper emoteHelper) {
        return "RS3 Hiscores lookup: " + name.toUpperCase();
    }

    @Override
    public String getLoadingThumbnail(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        return "https://vignette.wikia.nocookie.net/runescape2/images/a/a7/RuneScape_Companion_logo.png";
    }
}
