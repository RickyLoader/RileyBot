package Runescape.OSRS.League;

import Bot.ResourceHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Hold league points/tier
 */
public class LeagueTier {
    public static final int
            LEAGUE_POINTS_INDEX = 73,
            LEAGUE_POINTS_RANK_INDEX = 72;
    private final int points;
    private final long rank;
    private final LEAGUE_TIER tier;

    public enum LEAGUE_TIER {
        DRAGON,
        RUNE,
        ADAMANT,
        MITHRIL,
        STEEL,
        IRON,
        BRONZE,
        UNQUALIFIED
    }

    /**
     * Create a league tier
     *
     * @param tier   Tier
     * @param points League points
     * @param rank   League point rank
     */
    public LeagueTier(LEAGUE_TIER tier, int points, long rank) {
        this.tier = tier;
        this.points = points;
        this.rank = rank;
    }

    /**
     * Get the player's league point rank
     *
     * @return League point rank
     */
    public long getRank() {
        return rank;
    }

    /**
     * Check if the tier is set
     *
     * @return Tier is set
     */
    public boolean hasTier() {
        return tier != null;
    }

    /**
     * Get the tier name
     *
     * @return Tier name
     */
    public String getTierName() {
        String tierName = StringUtils.capitalize(tier.name().toLowerCase());
        if(tier == LEAGUE_TIER.UNQUALIFIED) {
            return tierName;
        }
        return tierName + " tier";
    }

    /**
     * Get the league points
     *
     * @return League points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Get the league tier
     *
     * @return Tier
     */
    public LEAGUE_TIER getTier() {
        return tier;
    }

    /**
     * Get the path to the tier image
     *
     * @return Tier image path
     */
    public String getTierImagePath() {
        return ResourceHandler.OSRS_LEAGUE_PATH + "Tiers/" + tier.name() + ".png";
    }
}
