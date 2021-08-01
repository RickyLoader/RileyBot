package Runescape.OSRS.Boss;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

/**
 * OSRS boss details
 */
public class Boss {
    private final BOSS_ID id;
    private final String name, shortName, qualifier;
    private final BufferedImage iconImage, fullImage, backgroundImage;
    static final String
            CHAMBERS_OF_XERIC_FILENAME = "CHAMBERS_OF_XERIC.png",
            NIGHTMARE_FILENAME = "NIGHTMARE.png",
            THEATRE_OF_BLOOD_FILENAME = "THEATRE_OF_BLOOD.png";

    public enum BOSS_ID {
        ABYSSAL_SIRE,
        ALCHEMICAL_HYDRA,
        BARROWS_CHESTS,
        BRYOPHYTA,
        CALLISTO,
        CERBERUS,
        CHAMBERS_OF_XERIC,
        CHAMBERS_OF_XERIC_CHALLENGE_MODE,
        CHAOS_ELEMENTAL,
        CHAOS_FANATIC,
        COMMANDER_ZILYANA,
        CORPOREAL_BEAST,
        CRAZY_ARCHAEOLOGIST,
        DAGANNOTH_PRIME,
        DAGANNOTH_REX,
        DAGANNOTH_SUPREME,
        DERANGED_ARCHAEOLOGIST,
        GENERAL_GRAARDOR,
        GIANT_MOLE,
        GROTESQUE_GUARDIANS,
        HESPORI,
        KALPHITE_QUEEN,
        KING_BLACK_DRAGON,
        KRAKEN,
        KREEARRA,
        KRIL_TSUTSAROTH,
        MIMIC,
        NIGHTMARE,
        PHOSANIS_NIGHTMARE,
        OBOR,
        SARACHNIS,
        SCORPIA,
        SKOTIZO,
        TEMPORASS,
        THE_GAUNTLET,
        THE_CORRUPTED_GAUNTLET,
        THEATRE_OF_BLOOD,
        THEATRE_OF_BLOOD_HARD_MODE,
        THERMONUCLEAR_SMOKE_DEVIL,
        TZKAL_ZUK,
        TZTOK_JAD,
        VENENATIS,
        VETION,
        VORKATH,
        WINTERTODT,
        ZALCANO,
        ZULRAH,
        UNKNOWN;

        /**
         * Get a boss ID from the given boss name String
         *
         * @param bossName Boss name String e.g "kalphite queen"
         * @return Boss ID e.g KALPHITE_QUEEN (or UNKNOWN if unable to find a match)
         */
        public static BOSS_ID byName(String bossName) {
            try {
                return BOSS_ID.valueOf(bossName.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    /**
     * Create a boss
     *
     * @param id              ID of boss
     * @param name            Name of boss - e.g "Commander Zilyana"
     * @param shortName       Short name of boss  - e.g "Zilyana"
     * @param qualifier       Kill qualifier - e.g "kill" or "clear"
     * @param iconImage       Small icon image of boss
     * @param fullImage       Full sized image of boss
     * @param backgroundImage Background image related to the boss - e.g God Wars Dungeon for a God Wars boss
     */
    public Boss(BOSS_ID id, String name, String shortName, String qualifier, @Nullable BufferedImage iconImage, @Nullable BufferedImage fullImage, @Nullable BufferedImage backgroundImage) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.qualifier = qualifier;
        this.iconImage = iconImage;
        this.fullImage = fullImage;
        this.backgroundImage = backgroundImage;
    }

    /**
     * Get the name of the boss
     *
     * @return Boss name e.g "Commander Zilyana"
     */
    public String getName() {
        return name;
    }

    /**
     * Get the shortened name of the boss.
     * If the boss name is already short, the short name will be the boss name.
     *
     * @return Shortened boss name e.g "Zilyana"
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Get the ID of the boss
     *
     * @return Boss ID
     */
    public BOSS_ID getId() {
        return id;
    }

    /**
     * Get the kill qualifier for the given number of kills.
     * E.g 1 kill or 2 kills
     *
     * @param kills Number of kills to get qualifier for
     * @return Kill qualifier
     */
    public String getKillQualifier(int kills) {
        return kills == 1 ? qualifier : qualifier + "s";
    }

    /**
     * Get the full boss image
     *
     * @return Full boss image (May be null if the boss has no image)
     */
    @Nullable
    public BufferedImage getFullImage() {
        return fullImage;
    }

    /**
     * Get the boss icon image
     *
     * @return Boss icon image (May be null if the boss has no icon)
     */
    @Nullable
    public BufferedImage getIconImage() {
        return iconImage;
    }

    /**
     * Get the boss background image.
     * This is an image related to the boss e.g the environment that the boss is located in etc.
     *
     * @return Boss background image (May be null if the boss has no background image)
     */
    @Nullable
    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }
}
