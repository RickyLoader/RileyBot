package Runescape.OSRS.League;

import org.apache.commons.lang3.StringUtils;

/**
 * Hold league points/tier
 */
public class LeagueTier {
    private final int points;
    private LEAGUE_TIER tier;

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
     * @param points League points
     */
    public LeagueTier(int points) {
        this.points = points;
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
     * Set the league tier
     *
     * @param tier League tier
     */
    public void setTier(LEAGUE_TIER tier) {
        this.tier = tier;
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
        return "/Runescape/OSRS/League/Tiers/" + tier.name() + ".png";
    }
}
