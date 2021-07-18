package COD.API.Parsing;

import COD.API.MWManager;

/**
 * Parsing player stats from Modern Warfare JSON
 */
public class MWStatsParser {
    private final MWManager manager;

    /**
     * Create the stats parser
     *
     * @param manager Modern Warfare manager
     */
    public MWStatsParser(MWManager manager) {
        this.manager = manager;
    }

    /**
     * Get the Modern Warfare manager
     *
     * @return Modern Warfare manager
     */
    public MWManager getManager() {
        return manager;
    }
}
