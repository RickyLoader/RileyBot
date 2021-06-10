package Runescape;

import Bot.ResourceHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

/**
 * Hold information about a boss, sortable by kill count
 */
public class Boss implements Comparable<Boss> {
    private static final String BASE_IMAGE_PATH = ResourceHandler.OSRS_BASE_PATH + "Bosses/";
    private final BOSS_NAME name;
    private final int kills;

    public enum BOSS_NAME {
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
         * Get the boss names in the order they appear on the hiscores
         *
         * @return Boss names in hiscores order
         */
        public static BOSS_NAME[] getNamesInHiscoresOrder() {
            return new BOSS_NAME[]{
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
                    ZULRAH
            };
        }

        /**
         * Get a boss name from the given boss name String
         *
         * @param bossName Boss name String e.g "kalphite queen"
         * @return Boss name e.g KALPHITE_QUEEN (or UNKNOWN if unable to find a match)
         */
        public static BOSS_NAME fromName(String bossName) {
            try {
                return BOSS_NAME.valueOf(bossName.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return UNKNOWN;
            }
        }

        /**
         * Get the name of the boss
         *
         * @return Boss name
         */
        public String getName() {
            switch(this) {
                case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
                    return "Chambers of Xeric: Challenge Mode";
                case KREEARRA:
                    return "Kree'Arra";
                case KRIL_TSUTSAROTH:
                    return "K'ril Tsutsaroth";
                case THEATRE_OF_BLOOD_HARD_MODE:
                    return "Theatre of Blood: Hard Mode";
                case TZKAL_ZUK:
                    return "TzKal-Zuk";
                case TZTOK_JAD:
                    return "TzTok-Jad";
                case VETION:
                    return "Vet'ion";
                default:
                    // Capitalise first letter of each word
                    String[] nameArgs = this.name().split("_");
                    for(int i = 0; i < nameArgs.length; i++) {
                        nameArgs[i] = StringUtils.capitalize(nameArgs[i].toLowerCase());
                    }
                    return StringUtils.join(nameArgs, " ");
            }
        }

        /**
         * Get the qualifier for the boss, the default value is kills
         * but games, clears etc are an option too where applicable
         *
         * @param kills Number of kills
         * @return Formatted string containing kill count and qualifier (kills, games, etc)
         */
        public String formatKills(int kills) {
            String type;
            switch(this) {
                case BARROWS_CHESTS:
                case CHAMBERS_OF_XERIC:
                case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
                case THEATRE_OF_BLOOD:
                case THEATRE_OF_BLOOD_HARD_MODE:
                case THE_GAUNTLET:
                case THE_CORRUPTED_GAUNTLET:
                    type = "Clears";
                    break;
                case TZKAL_ZUK:
                case TZTOK_JAD:
                    type = "Caves";
                    break;
                case WINTERTODT:
                case ZALCANO:
                    type = "Games";
                    break;
                default:
                    type = (kills == 1) ? "Kill" : "Kills";
            }

            // Comma for large numbers
            return NumberFormat.getNumberInstance().format(kills) + " " + type;
        }

        /**
         * Get the path to the full image of the boss
         *
         * @return Path to full boss image
         */
        public String getFullImagePath() {
            return BASE_IMAGE_PATH + "Full/" + this.name() + ".png";
        }

        /**
         * Get the path to the icon image of the boss
         *
         * @return Path to boss icon image
         */
        public String getIconImagePath() {
            return BASE_IMAGE_PATH + "Icons/" + this.name() + ".png";
        }
    }

    /**
     * Create a boss
     *
     * @param name  Boss name
     * @param kills Boss kills
     */
    public Boss(BOSS_NAME name, int kills) {
        this.name = name;
        this.kills = kills;
    }

    /**
     * Format the boss kills with the qualifier (kills, clears, etc) and the kills
     *
     * @return Formatted boss kills - 1000 GIANT_MOLE -> 1,000 Kills
     */
    public String formatKills() {
        return name.formatKills(kills);
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
     * Get the full boss image
     *
     * @return Full boss image (May be null if the boss has no image)
     */
    @Nullable
    public BufferedImage getFullImage() {
        return new ResourceHandler().getImageResource(name.getFullImagePath());
    }

    /**
     * Get the boss icon image
     *
     * @return Boss icon image (May be null if the boss has no icon)
     */
    @Nullable
    public BufferedImage getIconImage() {
        return new ResourceHandler().getImageResource(name.getIconImagePath());
    }

    /**
     * Sort in descending order of kills
     */
    @Override
    public int compareTo(Boss o) {
        return o.getKills() - kills;
    }
}