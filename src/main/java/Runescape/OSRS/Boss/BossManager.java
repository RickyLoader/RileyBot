package Runescape.OSRS.Boss;

import Bot.ResourceHandler;
import Runescape.OSRS.Boss.Boss.BOSS_ID;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static Runescape.OSRS.Boss.Boss.*;
import static Runescape.OSRS.Boss.Boss.BOSS_ID.*;

/**
 * Hold OSRS boss details
 */
public class BossManager {
    private static final String BASE_IMAGE_PATH = ResourceHandler.OSRS_BASE_PATH + "Bosses/";
    private static BossManager instance;
    private final HashMap<BOSS_ID, Boss> bosses;

    /**
     * Initialise the bosses
     */
    private BossManager() {
        this.bosses = new HashMap<>();
        ResourceHandler resourceHandler = new ResourceHandler();

        for(BOSS_ID id : BOSS_ID.values()) {
            Boss boss = new Boss(
                    id,
                    getNameById(id),
                    getShortNameById(id),
                    getKillQualifierById(id),
                    resourceHandler.getImageResource(getIconImagePathById(id)),
                    resourceHandler.getImageResource(getFullImagePathById(id)),
                    resourceHandler.getImageResource(getBackgroundImagePathById(id))
            );
            bosses.put(id, boss);
        }
    }

    /**
     * Get the path to the full image of a boss from the given boss ID.
     *
     * @param id Boss ID
     * @return Path to full boss image
     */
    public String getFullImagePathById(BOSS_ID id) {
        return BASE_IMAGE_PATH + "Full/" + id.name() + ".png";
    }

    /**
     * Get the path to a background image of a boss from the given boss ID.
     *
     * @param id Boss ID
     * @return Path to boss background image
     */
    public String getBackgroundImagePathById(BOSS_ID id) {
        final String backgroundPath = BASE_IMAGE_PATH + "Background/";
        switch(id) {
            case THEATRE_OF_BLOOD:
            case THEATRE_OF_BLOOD_HARD_MODE:
                return backgroundPath + THEATRE_OF_BLOOD_FILENAME;
            case CHAMBERS_OF_XERIC:
            case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
                return backgroundPath + CHAMBERS_OF_XERIC_FILENAME;
            case NIGHTMARE:
            case PHOSANIS_NIGHTMARE:
                return backgroundPath + NIGHTMARE_FILENAME;
            default:
                return backgroundPath + id.name() + ".png";
        }
    }

    /**
     * Get the path to the icon image of a boss from the given boss ID.
     *
     * @param id Boss ID
     * @return Path to boss icon image
     */
    public String getIconImagePathById(BOSS_ID id) {
        return BASE_IMAGE_PATH + "Icons/" + id.name() + ".png";
    }

    /**
     * Get the boss IDs in the order they appear on the hiscores
     *
     * @return Boss IDs in hiscores order
     */
    public static BOSS_ID[] getIdsInHiscoresOrder() {
        return new BOSS_ID[]{
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
                ZULRAH
        };
    }

    /**
     * Get the shortened name of a boss by the given boss ID.
     * If the boss name is already short, the shortened name will be the boss name.
     *
     * @param id Boss ID
     * @return Shortened boss name e.g "Zilyana"
     */
    public String getShortNameById(BOSS_ID id) {
        final String archaeologist = "Archae.";
        final String chambers = "C.O.X";

        switch(id) {
            case COMMANDER_ZILYANA:
                return "Zilyana";
            case THERMONUCLEAR_SMOKE_DEVIL:
                return "Thermy";
            case THEATRE_OF_BLOOD_HARD_MODE:
                return "T.O.B: Hard Mode";
            case THE_CORRUPTED_GAUNTLET:
                return "The Gauntlet (C)";
            case PHOSANIS_NIGHTMARE:
                return "Phosani's NM";
            case GROTESQUE_GUARDIANS:
                return "Grot. Guardians";
            case DERANGED_ARCHAEOLOGIST:
                return "Deranged " + archaeologist;
            case DAGANNOTH_SUPREME:
                return "Dag. Supreme";
            case CRAZY_ARCHAEOLOGIST:
                return "Crazy " + archaeologist;
            case CHAMBERS_OF_XERIC:
                return chambers;
            case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
                return chambers + ": (CM)";
            default:
                return getNameById(id);
        }
    }

    /**
     * Get the name of a boss by the given boss ID.
     *
     * @param id Boss ID
     * @return Boss name e.g "Commander Zilyana"
     */
    public String getNameById(BOSS_ID id) {
        switch(id) {
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
            case PHOSANIS_NIGHTMARE:
                return "Phosani's Nightmare";
            default:
                // Capitalise first letter of each word
                String[] nameArgs = id.name().split("_");
                for(int i = 0; i < nameArgs.length; i++) {
                    nameArgs[i] = StringUtils.capitalize(nameArgs[i].toLowerCase());
                }
                return StringUtils.join(nameArgs, " ");
        }
    }

    /**
     * Get the kill qualifier for a boss from the given boss ID.
     * The default value is kill, but game, clear, etc are an option too where applicable
     *
     * @param id Boss ID
     * @return Boss kill qualifier e.g "Clear"
     */
    public String getKillQualifierById(BOSS_ID id) {
        String type;
        switch(id) {
            case BARROWS_CHESTS:
            case CHAMBERS_OF_XERIC:
            case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
            case THEATRE_OF_BLOOD:
            case THEATRE_OF_BLOOD_HARD_MODE:
            case THE_GAUNTLET:
            case THE_CORRUPTED_GAUNTLET:
                type = "Clear";
                break;
            case TZKAL_ZUK:
            case TZTOK_JAD:
                type = "Cave";
                break;
            case WINTERTODT:
            case ZALCANO:
                type = "Game";
                break;
            default:
                type = "Kill";
        }
        return type;
    }

    /**
     * Get a boss from the given boss name String
     *
     * @param name Boss name String e.g "kalphite queen"
     * @return Boss or unknown
     */
    public Boss getBossByName(String name) {
        return bosses.get(BOSS_ID.byName(name));
    }

    /**
     * Get a boss from the given boss ID.
     *
     * @param id Boss ID
     * @return Boss with given ID
     */
    public Boss getBossById(BOSS_ID id) {
        return bosses.get(id);
    }

    /**
     * Get an instance of the BossManager class
     *
     * @return Instance
     */
    public static BossManager getInstance() {
        if(instance == null) {
            instance = new BossManager();
        }
        return instance;
    }
}
