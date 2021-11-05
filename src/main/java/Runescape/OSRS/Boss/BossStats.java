package Runescape.OSRS.Boss;

import Runescape.Stats.RankedMetric;

import java.text.NumberFormat;

/**
 * Hold a OSRS players stats for a given boss, sortable by kill count
 */
public class BossStats extends RankedMetric implements Comparable<BossStats> {
    public static final int
            BOSS_START_INDEX = 96,
            BOSS_END_INDEX = 190;
    private final Boss boss;
    private final int kills;

    /**
     * Create the boss stats
     *
     * @param boss  Boss that stats apply to
     * @param rank  Boss kills rank
     * @param kills Boss kills
     */
    public BossStats(Boss boss, int rank, int kills) {
        super(rank);
        this.boss = boss;
        this.kills = kills;
    }

    /**
     * Format the boss kills with the qualifier (kills, clears, etc) and the kills
     *
     * @return Formatted boss kills - 1000 GIANT_MOLE -> 1,000 Kills
     */
    public String getFormattedKills() {
        return NumberFormat.getNumberInstance().format(kills) + " " + boss.getKillQualifier(kills);
    }

    /**
     * Get the boss kills
     *
     * @return Boss kills
     */
    public int getKills() {
        return kills;
    }

    /**
     * Get the boss which the stats apply to
     *
     * @return Boss
     */
    public Boss getBoss() {
        return boss;
    }

    /**
     * Sort in descending order of kills
     */
    @Override
    public int compareTo(BossStats o) {
        return o.getKills() - kills;
    }
}