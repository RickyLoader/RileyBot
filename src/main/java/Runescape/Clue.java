package Runescape;

import Bot.ResourceHandler;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 * Hold data on a clue scroll
 */
public class Clue {
    private static final String BASE_IMAGE_PATH = "Clues/";
    private final int rank, completions;
    private final TYPE type;

    public enum TYPE {
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
     * Get the completions as a String e.g 5000 -> "x5,000"
     *
     * @return Clue completions String
     */
    public String formatCompletions() {
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