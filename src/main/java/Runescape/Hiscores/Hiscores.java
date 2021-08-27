package Runescape.Hiscores;

import Command.Commands.Lookup.RunescapeLookupCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.EmbedLoadingMessage;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Runescape.Stats.PlayerStats;
import Runescape.Stats.Skill;
import Runescape.Stats.TotalLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Fetch player Runescape stats from the Hiscores API
 */
public abstract class Hiscores<S extends PlayerStats> {
    protected static final String BASE_URL = "https://secure.runescape.com/";
    private final String mValue, rankingPageName;

    // Max players displayed on a hiscores page
    private static final int MAX_PAGE_LIMIT = 25;

    private boolean timeout = false;

    public enum LOADING_UPDATE_TYPE {
        COMPLETE,
        FAIL,
        UPDATE
    }

    /**
     * Create the Runescape Hiscores instance
     *
     * @param mValue          Hiscores URL 'm' value - e.g [BASE_URL]m=[mValue]
     * @param rankingPageName Ranking page name - e.g [BASE_URL]m=[mValue]/[rankingPageName]?rank=1
     */
    public Hiscores(String mValue, String rankingPageName) {
        this.mValue = mValue;
        this.rankingPageName = rankingPageName;
    }

    /**
     * Get a hiscores API request URL using the given account type and player name.
     * E.g https://secure.runescape.com/m=hiscore_oldschool_tournament/index_lite.ws?player=dave
     *
     * @param name        Player name
     * @param accountType Account type
     * @return URL to hiscores CSV
     */
    protected String getStatsApiUrl(String name, PlayerStats.ACCOUNT accountType) {

        // When locating, it's a process of elimination beginning with the normal hiscores
        if(accountType == PlayerStats.ACCOUNT.LOCATE) {
            accountType = PlayerStats.ACCOUNT.NORMAL;
        }

        return getBaseHiscoresUrl(accountType) + "index_lite.ws?player=" + EmbedHelper.urlEncode(name);
    }

    /**
     * Get the entry URL to the hiscores of the given account type.
     * E.g https://secure.runescape.com/m=hiscore_oldschool_tournament/
     *
     * @param type Account type
     * @return Hiscores entry URL
     */
    private String getBaseHiscoresUrl(PlayerStats.ACCOUNT type) {
        return BASE_URL + "m=" + mValue + type.getUrlSuffix() + "/";
    }

    /**
     * Get the URL to the hiscores ranking page of a given type
     * E.g https://secure.runescape.com/m=hiscore_oldschool_ironman/overall
     *
     * @param type Account type
     * @return Hiscores ranking URL
     */
    protected String getHiscoresRankingUrl(PlayerStats.ACCOUNT type) {
        return getBaseHiscoresUrl(type) + rankingPageName;
    }

    /**
     * Get the URL to the hiscores ranking page containing the player of the given name.
     * E.g https://secure.runescape.com/m=hiscore_oldschool_ironman/overall?user=name;
     *
     * @param type Account type
     * @param name Player name
     * @return Hiscores rank page URL
     */
    protected String getHiscoresUrlByName(PlayerStats.ACCOUNT type, String name) {
        return getHiscoresRankingUrl(type) + "?user=" + name;
    }

    /**
     * Get the URL to the hiscores ranking page containing the player of the given rank.
     * E.g https://secure.runescape.com/m=hiscore_hardcore_ironman/ranking?rank=1;
     *
     * @param type Account type
     * @param rank Player rank
     * @return Hiscores rank page URL
     */
    public String getHiscoresUrlByRank(PlayerStats.ACCOUNT type, int rank) {
        return getHiscoresRankingUrl(type) + "?rank=" + rank;
    }

    /**
     * Fetch the player data from the hiscores and update an optionally provided loading message during the process.
     * Return the hiscores stats response which holds the player name & stats.
     * If the stats are null, either the player doesn't exist or the request failed.
     * The stats response indicates whether the request failed through
     * the {@link HiscoresStatsResponse#requestFailed()} method.
     *
     * @param name           Player name
     * @param accountType    Account type to fetch stats for
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Hiscores stats response
     */
    public HiscoresStatsResponse<S> getHiscoresStatsResponse(String name, PlayerStats.ACCOUNT accountType, HashSet<RunescapeLookupCommand.ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        return new HiscoresStatsResponse<>(
                name,
                getStatsApiUrl(name, accountType),
                fetchPlayerStats(name, accountType, args, loadingMessage),
                timeout
        );
    }

    /**
     * Fetch a player's stats for the given account type.
     * Update the optionally provided loading message during this process.
     * Pass the parsed stats to {@link Hiscores#applyStatArgs(PlayerStats, HashSet, ImageLoadingMessage...)} for any
     * further requests/loading message updates.
     *
     * @param name           Player name
     * @param accountType    Account type to fetch stats for
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Player stats or null
     */
    private @Nullable S fetchPlayerStats(String name, PlayerStats.ACCOUNT accountType, HashSet<RunescapeLookupCommand.ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        final S stats = accountType == PlayerStats.ACCOUNT.LOCATE
                ? locatePlayerStats(name, loadingMessage)
                : getAccountTypeStats(name, accountType, loadingMessage);

        // Issue retrieving stats
        if(stats == null) {
            return null;
        }

        return applyStatArgs(stats, args, loadingMessage);
    }

    /**
     * Post processing for player stats. Apply any further arguments. Before returning, all loading stages prior to
     * the "Building image" stage must be completed (if a loading message is provided).
     *
     * @param stats          Player stats
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Stats
     */
    protected abstract S applyStatArgs(S stats, HashSet<RunescapeLookupCommand.ARGUMENT> args, ImageLoadingMessage... loadingMessage);

    /**
     * Complete the current stage of the given loading message.
     *
     * @param loadingMessages Optional loading message
     */
    protected void completeLoadingMessageStage(@NotNull ImageLoadingMessage[] loadingMessages) {
        updateLoadingMessage(LOADING_UPDATE_TYPE.COMPLETE, null, loadingMessages);
    }

    /**
     * Pass/fail/update the current stage of the given loading message (if provided).
     *
     * @param type            COMPLETE/FAIL/UPDATE to perform
     * @param message         Optional message to display
     * @param loadingMessages Optional loading message
     */
    protected void updateLoadingMessage(LOADING_UPDATE_TYPE type, @Nullable String message, @NotNull ImageLoadingMessage[] loadingMessages) {
        if(loadingMessages.length == 0) {
            return;
        }

        ImageLoadingMessage loadingMessage = loadingMessages[0];
        boolean displayMessage = message != null;

        switch(type) {
            case COMPLETE:
                if(displayMessage) {
                    loadingMessage.completeStage(message);
                }
                else {
                    loadingMessage.completeStage();
                }
                break;
            case FAIL:
                loadingMessage.failStage(displayMessage ? message : EmbedLoadingMessage.LoadingStage.EMPTY_MESSAGE);
                break;
            case UPDATE:
                loadingMessage.updateStage(displayMessage ? message : EmbedLoadingMessage.LoadingStage.EMPTY_MESSAGE);
                break;
        }
    }

    /**
     * Get the hiscores API response (stats CSV, name, account type, URL) for the given name and account type.
     *
     * @param name        Player name
     * @param accountType Account type to get response for
     * @return Hiscores response or null
     */
    @Nullable
    protected HiscoresApiResponse getHiscoresApiResponse(String name, PlayerStats.ACCOUNT accountType) {
        final String url = getStatsApiUrl(name, accountType);
        final NetworkResponse response = new NetworkRequest(url, false).get();

        // Reset timeout
        timeout = false;

        // Hiscores didn't respond
        if(response.code == 504 || response.code == 408 || response.code == NetworkResponse.TIMEOUT_CODE) {
            timeout = true;
            return null;
        }

        // Player doesn't exist
        if(response.code == 404) {
            return null;
        }

        final String[] statsCsv = response.body.replace("\n", ",").split(",");
        return new HiscoresApiResponse(name, url, accountType, statsCsv);
    }

    /**
     * Parse the given hiscores API CSV in to player stats.
     *
     * @param statsResponse  Hiscores API stats response
     * @param loadingMessage Optional loading message
     * @return Player stats
     */
    protected abstract S parseStats(HiscoresApiResponse statsResponse, ImageLoadingMessage... loadingMessage);

    /**
     * Get the stats of a player without knowing their account type.
     * Update and complete the loading stage of "Checking account type" if a loading message is provided.
     *
     * @param name           Player name
     * @param loadingMessage Optional loading message
     * @return Player stats or null
     */
    protected abstract @Nullable S locatePlayerStats(String name, ImageLoadingMessage... loadingMessage);

    /**
     * Get the stats of a player for the given account type.
     * Update and complete the loading stage of "Player has [account type] stats" if a loading message is provided.
     *
     * @param name           Player name
     * @param accountType    Account type
     * @param loadingMessage Optional loading message
     * @return Player stats or null
     */
    @Nullable
    protected S getAccountTypeStats(String name, PlayerStats.ACCOUNT accountType, ImageLoadingMessage... loadingMessage) {
        final HiscoresApiResponse statsResponse = getHiscoresApiResponse(name, accountType);

        // Issue retrieving stats
        if(statsResponse == null) {
            return null;
        }

        // Optionally complete the loading stage of "Player has [account type] stats".
        completeLoadingMessageStage(loadingMessage);
        return parseStats(statsResponse, loadingMessage);
    }


    /**
     * Get the name of a player for the given rank and account type.
     *
     * @param rank        Rank e.g 1
     * @param accountType Account type to check rank for e.g IRON
     * @return Player name at given rank and account type or null
     */
    @Nullable
    public String getNameByRank(int rank, PlayerStats.ACCOUNT accountType) {
        final HiscoresPlayer player = getPlayerDetailsByRank(accountType, rank);

        // Failed to locate player
        if(player == null) {
            return null;
        }

        return player.getName();
    }

    /**
     * Attempt to get the lowest rank player for the given account type.
     *
     * @param accountType Account type to get lowest rank player for
     * @return Lowest rank player or null
     */
    public abstract @Nullable HiscoresPlayer getLowestRankPlayer(PlayerStats.ACCOUNT accountType);

    /**
     * Get the HTML element representing a player for the given name and account type.
     * Navigate to the page of the hiscores where the player is located and attempt to retrieve the element.
     *
     * @param accountType Account type to check rank for e.g IRON
     * @param name        Player name
     * @return HTML element at given name and account type or null
     */
    @Nullable
    protected Element getPlayerElementByName(PlayerStats.ACCOUNT accountType, String name) {
        try {

            // List of player elements  on the hiscores page
            final Elements playerElements = getPlayerElementsOnHiscoresPage(getHiscoresUrlByName(accountType, name));

            // Issue retrieving list of player elements
            if(playerElements == null) {
                throw new Exception("Unable to retrieve player list");
            }

            // Iterate through and try match the name
            for(Element playerElement : playerElements) {
                final String targetName = parseNameFromPlayerElement(playerElement);

                // Not the desired player
                if(targetName == null || !targetName.equalsIgnoreCase(name)) {
                    continue;
                }
                return playerElement;
            }
            throw new Exception("Unable to locate " + name + " on page");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the HTML element representing a player for the given rank and account type.
     * Navigate to the page of the hiscores where the rank is located and attempt retrieve the element.
     *
     * @param rank        Rank e.g 1
     * @param accountType Account type to check rank for e.g IRON
     * @return HTML element at given rank and account type or null
     */
    @Nullable
    protected Element getPlayerElementByRank(PlayerStats.ACCOUNT accountType, int rank) {
        try {

            // List of player elements  on the hiscores page
            final Elements playerElements = getPlayerElementsOnHiscoresPage(getHiscoresUrlByRank(accountType, rank));

            // Issue retrieving list of player elements
            if(playerElements == null) {
                throw new Exception("Unable to retrieve player list");
            }

            // Position of player on page
            final int pagePos = rank % MAX_PAGE_LIMIT == 0 ? playerElements.size() : rank % MAX_PAGE_LIMIT;

            // Index outside list size
            if(pagePos > playerElements.size()) {
                throw new Exception(
                        "Attempted to retrieve player at position " + pagePos + " with only "
                                + playerElements.size() + " players on the page!"
                );
            }

            final Element targetPlayerElement = playerElements.get(pagePos - 1);
            final Integer targetRank = parseRankFromPlayerElement(targetPlayerElement);

            // Desired rank not where it should be (if rank is too high, page will redirect to the first page)
            if(targetRank == null || targetRank != rank) {
                throw new Exception("Invalid rank found: Expected: " + rank + " Got: " + targetRank);
            }

            return targetPlayerElement;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the player details for the given rank and account type.
     * Player details are
     * Navigate to the page of the hiscores where the rank is located and attempt to scrape the player name & rank from
     * the HTML.
     *
     * @param rank        Rank e.g 1
     * @param accountType Account type to check rank for e.g IRON
     * @return Player details at given rank and account type or null
     */
    @Nullable
    protected HiscoresPlayer getPlayerDetailsByRank(PlayerStats.ACCOUNT accountType, int rank) {
        final Element playerElement = getPlayerElementByRank(accountType, rank);
        return playerElement == null ? null : parsePlayerDetailsFromElement(playerElement);
    }

    /**
     * Takes an HTML element representing a player from a hiscores ranking page and returns it wrapped
     * as a player details object. This contains the player name and rank.
     *
     * @param playerElement Player on hiscores ranking page (contains rank, name, etc)
     * @return Player or null
     */
    @Nullable
    protected HiscoresPlayer parsePlayerDetailsFromElement(Element playerElement) {
        final Integer rank = parseRankFromPlayerElement(playerElement);
        final String name = parseNameFromPlayerElement(playerElement);

        // Issue parsing player
        if(rank == null || name == null) {
            return null;
        }

        return new HiscoresPlayer(name, rank);
    }

    /**
     * Attempt to get the current lowest rank for the given account type.
     * This is not the best rank (e.g 1) but the worst (e.g 500000).
     *
     * @param accountType Account type to get lowest current rank for
     * @return Lowest current rank (if unable to be retrieved the lowest possible rank of 2 million will be returned)
     */
    public int getLowestRank(PlayerStats.ACCOUNT accountType) {
        final HiscoresPlayer lowestRankPlayer = getLowestRankPlayer(accountType);
        return lowestRankPlayer == null ? Skill.MAX_RANK : lowestRankPlayer.getRank();
    }

    /**
     * Parse the player's total level from the stats CSV. Calculate the virtual total.
     *
     * @param statsCsv CSV from API
     * @param skills   Array of player skills (excluding total level)
     * @return Total level
     */
    protected TotalLevel parseTotalLevel(String[] statsCsv, Skill[] skills) {
        return new TotalLevel(0, statsCsv, skills);
    }

    /**
     * Get a list of HTML elements representing players from the given URL to a hiscores ranking page.
     *
     * @param url URL to ranking page
     * @return List of player HTML elements
     */
    @Nullable
    protected Elements getPlayerElementsOnHiscoresPage(String url) {
        try {
            // Hiscores page containing a list of players (not a player's specific page)
            Document document = Jsoup.connect(url).get();

            // Each element is a player on the page, containing name, rank, etc
            return parseHiscoresPlayerElements(document);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get a list the player details for all players on a hiscores ranking page.
     * This is done by scraping the page for names & ranks.
     *
     * @param url URL to ranking page
     * @return List of player details
     */
    @Nullable
    protected ArrayList<HiscoresPlayer> getAllPlayerDetailsOnHiscoresPage(String url) {
        try {
            Elements playerElements = getPlayerElementsOnHiscoresPage(url);

            if(playerElements == null) {
                throw new Exception("Unable to retrieve player list from: " + url);
            }

            ArrayList<HiscoresPlayer> players = new ArrayList<>();
            for(Element playerElement : playerElements) {
                final HiscoresPlayer player = parsePlayerDetailsFromElement(playerElement);

                // Throw the list out as having all players is important
                if(player == null) {
                    throw new Exception("Failed to parse player!");
                }

                players.add(player);
            }
            return players;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Takes an HTML document representing a ranking page on the hiscores (not a player's page), and returns
     * the list of elements of which each represents a player. Each element should contain the name, rank, etc.
     *
     * @param rankPage Hiscores ranking page HTML document
     * @return List of player elements
     */
    @Nullable
    protected abstract Elements parseHiscoresPlayerElements(Document rankPage);

    /**
     * Takes an HTML element representing a player from a hiscores ranking page and returns the player name.
     *
     * @param playerElement Player on hiscores ranking page (contains rank, name, etc)
     * @return Player name
     */
    @Nullable
    protected abstract String parseNameFromPlayerElement(Element playerElement);

    /**
     * Takes an HTML element representing a player from a hiscores ranking page and returns the player rank.
     *
     * @param playerElement Player on hiscores ranking page (contains rank, name, etc)
     * @return Player rank - e.g 12345 or null
     */
    protected abstract @Nullable Integer parseRankFromPlayerElement(Element playerElement);

    /**
     * Get the default loading stages to show in the loading message.
     *
     * @param accountType Account type to fetch stats for
     * @return Loading criteria
     */
    private ArrayList<String> getDefaultLoadingCriteria(PlayerStats.ACCOUNT accountType) {
        ArrayList<String> loadingCriteria = new ArrayList<>();

        if(accountType == PlayerStats.ACCOUNT.LOCATE) {
            loadingCriteria.add("Checking account type...");
        }
        else {
            loadingCriteria.add("Fetching player " + accountType.name().toLowerCase() + "  stats...");
        }

        return loadingCriteria;
    }

    /**
     * Get the other loading criteria to use based on arguments/account type.
     * These are appended to the default first stages
     * from {@link Hiscores#getDefaultLoadingCriteria(PlayerStats.ACCOUNT)}.
     *
     * @param args        Hiscores arguments
     * @param accountType Account type to fetch stats for
     * @return Loading criteria
     */
    protected abstract ArrayList<String> getOtherLoadingCriteria(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType);

    /**
     * Get the loading criteria to use when building a loading message for this hiscores.
     *
     * @param args        Hiscores arguments
     * @param accountType Account type to fetch stats for
     * @return Loading criteria
     */
    public ArrayList<String> getLoadingCriteria(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType) {
        final ArrayList<String> defaultLoadingCriteria = getDefaultLoadingCriteria(accountType);
        defaultLoadingCriteria.addAll(getOtherLoadingCriteria(args, accountType));
        return defaultLoadingCriteria;
    }

    /**
     * Get the title to be used in the loading message
     *
     * @param name        Player name
     * @param args        Hiscores arguments
     * @param accountType Account type
     * @return Loading message title
     */
    public abstract String getLoadingTitle(String name, HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType);

    /**
     * Get the thumbnail to be used in the loading message
     *
     * @param args        Hiscores arguments
     * @param accountType Account type
     * @return Loading message thumbnail
     */
    public abstract String getLoadingThumbnail(HashSet<RunescapeLookupCommand.ARGUMENT> args, PlayerStats.ACCOUNT accountType);
}
