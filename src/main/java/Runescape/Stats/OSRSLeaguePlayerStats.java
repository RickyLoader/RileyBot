package Runescape.Stats;

import Runescape.OSRS.Boss.BossStats;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.RelicTier;

import java.util.ArrayList;
import java.util.List;

/**
 * OSRS seasonal league player stats from hiscores
 */
public class OSRSLeaguePlayerStats extends OSRSPlayerStats {
    private final LeagueTier leagueTier;
    private final ArrayList<RelicTier> relicTiers;
    private final ArrayList<Region> regions;

    /**
     * Create OSRS seasonal league player stats.
     * These hold extra info for the league, e.g league points/tier and stored relics/regions.
     *
     * @param name       Player name
     * @param url        URL to hiscores CSV
     * @param skills     Array of player skills (excluding total level)
     * @param clues      Array of clue data
     * @param total      Total level
     * @param bossStats  List of boss stats
     * @param lmsInfo    Last man standing info
     * @param leagueTier League tier & points
     * @param relicTiers List of relic tiers the player has unlocked
     * @param regions    List of regions the player has unlocked
     */
    public OSRSLeaguePlayerStats(String name, String url, Skill[] skills, Clue[] clues, TotalLevel total, List<BossStats> bossStats, LastManStanding lmsInfo, LeagueTier leagueTier, ArrayList<RelicTier> relicTiers, ArrayList<Region> regions) {
        super(name, url, skills, clues, total, bossStats, lmsInfo, ACCOUNT.LEAGUE);
        this.leagueTier = leagueTier;
        this.relicTiers = relicTiers;
        this.regions = regions;
    }

    /**
     * Get the league tier info
     *
     * @return League tier
     */
    public LeagueTier getLeagueTier() {
        return leagueTier;
    }

    /**
     * Get the player unlocked relic tiers
     *
     * @return Player unlocked relic tiers
     */
    public ArrayList<RelicTier> getRelicTiers() {
        return relicTiers;
    }

    /**
     * Get the player unlocked regions
     *
     * @return Player unlocked regions
     */
    public ArrayList<Region> getRegions() {
        return regions;
    }

    /**
     * Check if the player has any stored relic & region
     * unlock data
     *
     * @return Player has stored relic & region data
     */
    public boolean hasLeagueUnlockData() {
        return !relicTiers.isEmpty() && !regions.isEmpty();
    }
}