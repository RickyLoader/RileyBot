package Runescape.Hiscores;

import Runescape.Stats.PlayerStats;
import org.jetbrains.annotations.Nullable;

/**
 * Hold a stats response from the Runescape API.
 * This holds the player's name, stats and a boolean indicating a timeout.
 *
 * @param <T> PlayerStats type
 */
public class HiscoresStatsResponse<T extends PlayerStats> {
    private final boolean requestFailed;
    private final String name, url;
    private final T stats;

    /**
     * Create the hiscores stats response
     *
     * @param name          Player name
     * @param stats         Player stats (will be null if the player doesn't exist/the request timed out/failed)
     * @param url           URL used to fetch the stats
     * @param requestFailed Request timed out/failed
     */
    public HiscoresStatsResponse(String name, String url, @Nullable T stats, boolean requestFailed) {
        this.name = name;
        this.url = url;
        this.stats = stats;
        this.requestFailed = requestFailed;
    }

    /**
     * Get the URL used to fetch the stats
     *
     * @return URL for fetching stats
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the player stats.
     * This is null if the player doesn't exist, or the hiscores request timed out/failed.
     *
     * @return Player stats
     */
    @Nullable
    public T getStats() {
        return stats;
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
     * Check if the request to the hiscores timed out/failed.
     * The player stats will be null if this is the case.
     *
     * @return Request timed out
     */
    public boolean requestFailed() {
        return requestFailed;
    }
}
