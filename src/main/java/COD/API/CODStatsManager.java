package COD.API;

import COD.PlayerStats.PlayerAssetStats;
import COD.PlayerStats.CODPlayerStats;

/**
 * Get player COD stats
 */
public abstract class CODStatsManager<T extends CODManager, V extends PlayerAssetStats, E extends CODPlayerStats<V>> {
    private final T manager;

    public enum PLATFORM {
        BATTLE,
        XBOX,
        PSN,
        NONE;

        /**
         * Get a platform by name
         *
         * @param name Name of platform - "battle"
         * @return Platform
         */
        public static PLATFORM byName(String name) {
            name = name.toUpperCase();
            try {
                return valueOf(name);
            }
            catch(IllegalArgumentException e) {
                switch(name) {
                    case "XBL":
                        return XBOX;
                    case "BATTLENET":
                        return BATTLE;
                    default:
                        return NONE;
                }
            }
        }
    }

    /**
     * Initialise the resource manager
     *
     * @param manager COD resource manager
     */
    public CODStatsManager(T manager) {
        this.manager = manager;
    }

    /**
     * Get the COD resource manager
     *
     * @return COD resource manager
     */
    public T getManager() {
        return manager;
    }

    /**
     * Fetch the COD stats for a player of the given name and platform.
     * The response will contain the player's stats and optionally messages from the API indicating failure etc.
     *
     * @param name     Player name - adhering to platform rules
     * @param platform Player platform
     * @return Stats response - contains player stats and optionally messages from the API
     */
    public abstract PlayerStatsResponse<V, E> fetchPlayerStats(String name, PLATFORM platform);
}
