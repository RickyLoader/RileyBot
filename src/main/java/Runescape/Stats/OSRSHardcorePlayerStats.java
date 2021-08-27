package Runescape.Stats;

import Runescape.OSRS.Boss.BossStats;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * OSRS hardcore ironman player stats from hiscores
 */
public class OSRSHardcorePlayerStats extends OSRSPlayerStats {
    private final Boolean dead;

    /**
     * Create OSRS hardcore ironman player stats.
     * This holds the current death status of the player.
     *
     * @param name      Player name
     * @param url       URL to hiscores CSV
     * @param skills    Array of player skills (excluding total level)
     * @param clues     Array of clue data
     * @param total     Total level
     * @param bossStats List of boss stats
     * @param lmsInfo   Last man standing info
     * @param dead      Player is dead
     */
    public OSRSHardcorePlayerStats(String name, String url, Skill[] skills, Clue[] clues, TotalLevel total, List<BossStats> bossStats, LastManStanding lmsInfo, @Nullable Boolean dead) {
        super(name, url, skills, clues, total, bossStats, lmsInfo, ACCOUNT.HARDCORE);
        this.dead = dead;
    }

    /**
     * Check if the player has died as a hardcore ironman.
     *
     * @return Player has died
     */
    public boolean isDead() {
        return dead != null && dead;
    }
}
