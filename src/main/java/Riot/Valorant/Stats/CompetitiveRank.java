package Riot.Valorant.Stats;

import Bot.ResourceHandler;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;

/**
 * Rank details
 */
public class CompetitiveRank {
    private final DIVISION division;
    private final int tier;
    private final BufferedImage image;

    // Base path to rank images
    public static final String BASE_PATH = ResourceHandler.VALORANT_BASE_PATH + "Ranks/";

    public enum DIVISION {
        UNRATED,
        IRON,
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND,
        IMMORTAL,
        RADIANT;

        /**
         * Check if the division has any tiers
         *
         * @return Division has tiers
         */
        public boolean hasTiers() {
            return this != UNRATED && this != RADIANT;
        }
    }

    /**
     * Create a competitive rank
     *
     * @param division Rank division - e.g BRONZE
     * @param tier     Tier within division - e.g 1
     * @param image    Image of rank icon
     */
    public CompetitiveRank(DIVISION division, int tier, BufferedImage image) {
        this.division = division;
        this.tier = tier;
        this.image = image;
    }

    /**
     * Get the image of the rank icon
     *
     * @return Rank icon
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the name of the competitive rank - e.g "Bronze 1"
     *
     * @return Rank name
     */
    public String getName() {
        final String divisionName = StringUtils.capitalize(division.name().toLowerCase());
        return division.hasTiers() ? divisionName + " " + tier : divisionName;
    }
}
