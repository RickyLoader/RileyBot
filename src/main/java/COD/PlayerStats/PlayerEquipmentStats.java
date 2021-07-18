package COD.PlayerStats;

import java.util.ArrayList;

/**
 * Hold COD player's equipment stats
 */
public class PlayerEquipmentStats {
    private final ArrayList<LethalStats> lethalStats;
    private final ArrayList<TacticalStats> tacticalStats;

    /**
     * Create the player equipment stats.
     * Initialise an empty list for lethal equipment and an empty list for tactical equipment.
     */
    public PlayerEquipmentStats() {
        this.lethalStats = new ArrayList<>();
        this.tacticalStats = new ArrayList<>();
    }

    /**
     * Add the given lethal equipment stats to the list
     *
     * @param lethalStats Add stats for a lethal equipment
     */
    public void addLethalStats(LethalStats lethalStats) {
        this.lethalStats.add(lethalStats);
    }

    /**
     * Add the given tactical equipment stats to the list
     *
     * @param tacticalStats Add stats for a tactical equipment
     */
    public void addTacticalStats(TacticalStats tacticalStats) {
        this.tacticalStats.add(tacticalStats);
    }

    /**
     * Get a list of the player's lethal equipment stats
     *
     * @return List of player lethal equipment stats
     */
    public ArrayList<LethalStats> getLethalStats() {
        return lethalStats;
    }

    /**
     * Get a list of the player's tactical equipment stats
     *
     * @return List of player tactical equipment stats
     */
    public ArrayList<TacticalStats> getTacticalStats() {
        return tacticalStats;
    }
}
