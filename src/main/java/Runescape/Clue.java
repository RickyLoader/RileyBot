package Runescape;

import Bot.ResourceHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Hold data on a clue scroll
 */
public class Clue {
    public static final int
            CLUE_START_INDEX = 78,
            CLUE_END_INDEX = 92;
    private static final String BASE_IMAGE_PATH = "Clues/";
    private final int rank, completions;
    private final TYPE type;

    public enum TYPE {
        ALL,
        BEGINNER,
        EASY,
        MEDIUM,
        HARD,
        ELITE,
        MASTER,
        UNKNOWN;

        /**
         * Get the path to the full image of the clue (relative to a Runescape directory)
         * e.g "/Clues/Full/MEDIUM.png"
         *
         * @return Path to full clue image
         */
        public String getFullImagePath() {
            return BASE_IMAGE_PATH + "Full/" + this.name() + ".png";
        }

        /**
         * Get the path to the icon image of the clue (relative to a Runescape directory)
         * e.g "/Clues/Icons/MEDIUM.png"
         *
         * @return Path to clue icon image
         */
        public String getIconImagePath() {
            return BASE_IMAGE_PATH + "Icons/" + this.name() + ".png";
        }

        /**
         * Get a clue type from the given clue type name String
         *
         * @param clueTypeName Boss name String e.g "easy"
         * @return Clue type e.g EASY (or UNKNOWN if unable to find a match)
         */
        public static TYPE fromTypeName(String clueTypeName) {
            try {
                return TYPE.valueOf(clueTypeName.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return UNKNOWN;
            }
        }

        /**
         * Get the colour of a given clue type
         *
         * @return Colour
         */
        public Color getColour() {
            switch(this) {
                case BEGINNER:
                    return new Color(169, 158, 157); // Grey
                case EASY:
                    return new Color(29, 115, 32); // Green
                case MEDIUM:
                    return new Color(86, 150, 153); // Cyan
                case HARD:
                    return new Color(131, 55, 152); // Purple
                case ALL:
                case ELITE:
                    return new Color(189, 165, 24); // Orange
                case MASTER:
                    return new Color(155, 48, 38); // Red
                default:
                    return new Color(255, 0, 0); // Red
            }
        }

        /**
         * Get the name of the clue type
         *
         * @return Clue type name
         */
        public String getName() {
            return StringUtils.capitalize(this.name().toLowerCase());
        }
    }

    /**
     * Create a clue scroll
     *
     * @param type        Clue scroll type
     * @param rank        Clue completions rank
     * @param completions Clue completions
     */
    public Clue(TYPE type, int rank, int completions) {
        this.type = type;
        this.rank = rank;
        this.completions = completions;
    }

    /**
     * Get the type of the clue scroll
     *
     * @return Clue scroll type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the number of completions of the clue scroll
     *
     * @return Number of completions
     */
    public int getCompletions() {
        return completions;
    }

    /**
     * Check if the clue has any completions
     *
     * @return Clue has completions
     */
    public boolean hasCompletions() {
        return completions > 0;
    }

    /**
     * Get the completions as a String e.g 5000 -> "x5,000"
     *
     * @return Clue completions String
     */
    public String getFormattedCompletions() {
        return new DecimalFormat("x#,###").format(completions == -1 ? 0 : completions);
    }

    /**
     * Get the rank of the clue scroll completions
     *
     * @return Completions rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the rank formatted with commas e.g 1234 -> 1,234
     * If the rank is -1 (unranked) return a dash ("-")
     * @return Formatted rank
     */
    public String getFormattedRank() {
        if(rank == PlayerStats.UNRANKED) {
            return "-";
        }
        return NumberFormat.getNumberInstance().format(rank);
    }

    /**
     * Get the full clue scroll image
     *
     * @param basePath Base path to runescape folder e.g "/Runescape/OSRS/"
     * @return Full clue scroll image (May be null if the clue has no image)
     */
    @Nullable
    public BufferedImage getFullImage(String basePath) {
        return new ResourceHandler().getImageResource(basePath + type.getFullImagePath());
    }

    /**
     * Get the clue scroll icon image
     *
     * @param basePath Base path to runescape folder e.g "/Runescape/OSRS/"
     * @return Clue scroll icon image (May be null if the clue has no image)
     */
    @Nullable
    public BufferedImage getIconImage(String basePath) {
        return new ResourceHandler().getImageResource(basePath + type.getIconImagePath());
    }
}