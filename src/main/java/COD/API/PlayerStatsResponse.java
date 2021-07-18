package COD.API;

import COD.PlayerStats.PlayerAssetStats;
import COD.PlayerStats.CODPlayerStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hold COD player stats and optional API response messages
 */
public class PlayerStatsResponse<E extends PlayerAssetStats, T extends CODPlayerStats<E>> {
    private final T stats;
    private final String message;
    private final boolean success;
    public static final String API_FAILURE = "Failed to communicate with API, try again later.";

    /**
     * Create the successful player stats response
     *
     * @param stats Player stats
     */
    public PlayerStatsResponse(@NotNull T stats) {
        this(stats, null);
    }

    /**
     * Create a successful player stats response with an optional message - e.g "x details were unavailable" etc.
     *
     * @param stats          Player stats
     * @param successMessage Optional message from the API
     */
    public PlayerStatsResponse(@NotNull T stats, @Nullable String successMessage) {
        this(stats, successMessage, true);
    }

    /**
     * Create an unsuccessful player stats response.
     *
     * @param failMessage Message indicating why the player stats were unable to be retrieved
     */
    public PlayerStatsResponse(@NotNull String failMessage) {
        this(null, failMessage, false);
    }

    /**
     * Create a player stats response
     *
     * @param stats   Optional player stats
     * @param message Optional API message
     * @param success Response success
     */
    private PlayerStatsResponse(@Nullable T stats, @Nullable String message, boolean success) {
        this.stats = stats;
        this.message = message;
        this.success = success;
    }

    /**
     * Check if the stats were successfully retrieved, if not, a {@code message} will be available
     * indicating why.
     *
     * @return Stats successfully retrieved
     */
    public boolean statsRetrieved() {
        return success;
    }

    /**
     * Check if the player stats response has a message from the API.
     * This message could indicate why the stats were unable to be retrieved or any issues that were found etc.
     *
     * @return Message is available
     */
    public boolean hasMessage() {
        return message != null;
    }

    /**
     * Get the player stats
     *
     * @return Player stats
     */
    public T getStats() {
        return stats;
    }

    /**
     * Get a message about the stats request from the API.
     * This message could indicate why the stats were unable to be retrieved or any issues that were found etc.
     *
     * @return Stats message
     */
    public String getMessage() {
        return message;
    }
}
