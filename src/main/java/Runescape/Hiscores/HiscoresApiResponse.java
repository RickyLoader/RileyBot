package Runescape.Hiscores;

import Runescape.Stats.PlayerStats;

/**
 * Runescape hiscores response
 */
public class HiscoresApiResponse {
    private final String name, url;
    private final String[] statsCsv;
    private final PlayerStats.ACCOUNT accountType;

    /**
     * Create the hiscores API response
     *
     * @param name        Player name
     * @param url         URL used to fetch stats
     * @param accountType Account type that stats were fetched for
     * @param statsCsv    Stats CSV (Each item in the array represents a value on the player's hiscores page)
     */
    public HiscoresApiResponse(String name, String url, PlayerStats.ACCOUNT accountType, String[] statsCsv) {
        this.name = name;
        this.url = url;
        this.accountType = accountType;
        this.statsCsv = statsCsv;
    }

    /**
     * Get the player's stats CSV.
     * Each item in the returned array represents a value on the player's hiscores page
     *
     * @return Stats CSV
     */
    public String[] getStatsCsv() {
        return statsCsv;
    }

    /**
     * Get the player name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL used to fetch the stats
     *
     * @return Stats URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the account type that the stats were fetched for
     *
     * @return Stats account type
     */
    public PlayerStats.ACCOUNT getAccountType() {
        return accountType;
    }
}
