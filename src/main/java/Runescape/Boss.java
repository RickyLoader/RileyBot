package Runescape;


import Bot.ResourceHandler;

import java.awt.image.BufferedImage;
import java.text.NumberFormat;

/**
 * Hold information about a boss, sortable by kill count
 */
public class Boss implements Comparable<Boss> {
    private final String name, filename;
    private final int kills;

    /**
     * Create a boss
     *
     * @param name  Boss name
     * @param kills Boss kills
     */
    public Boss(String name, int kills) {
        this.name = name;
        this.kills = kills;
        this.filename = name + ".png";
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
     * Get the qualifier for the boss, default is kills but games etc is an option too where applicable
     *
     * @return Formatted string containing kill count and qualifier (kills, games, etc)
     */
    public String formatKills() {
        String type;
        switch(name) {
            case "Barrows Chests":
            case "Chambers of Xeric":
            case "Chambers of Xeric Challenge Mode":
            case "Theatre of Blood":
            case "The Gauntlet":
            case "The Corrupted Gauntlet":
                type = "Clears";
                break;
            case "TzKal-Zuk":
            case "TzTok-Jad":
                type = "Caves";
                break;
            case "Wintertodt":
            case "Zalcano":
                type = "Games";
                break;
            default:
                type = (kills == 1) ? "Kill" : "Kills";
        }

        // Comma for large numbers
        return NumberFormat.getNumberInstance().format(kills) + " " + type;
    }

    /**
     * Get the boss image
     *
     * @return Boss image
     */
    public BufferedImage getImage() {
        return new ResourceHandler().getImageResource("/Runescape/OSRS/Bosses/" + filename);
    }

    @Override
    public int compareTo(Boss o) {
        return o.getKills() - kills;
    }

    /**
     * Get a list of boss names
     *
     * @return List of boss names
     */
    public static String[] getBossNames() {
        return new String[]{
                "Abyssal Sire",
                "Alchemical Hydra",
                "Barrows Chests",
                "Bryophyta",
                "Callisto",
                "Cerberus",
                "Chambers of Xeric",
                "Chambers of Xeric Challenge Mode",
                "Chaos Elemental",
                "Chaos Fanatic",
                "Commander Zilyana",
                "Corporeal Beast",
                "Crazy Archaeologist",
                "Dagannoth Prime",
                "Dagannoth Rex",
                "Dagannoth Supreme",
                "Deranged Archaeologist",
                "General Graardor",
                "Giant Mole",
                "Grotesque Guardians",
                "Hespori",
                "Kalphite Queen",
                "King Black Dragon",
                "Kraken",
                "Kree'Arra",
                "K'ril Tsutsaroth",
                "Mimic",
                "Nightmare",
                "Obor",
                "Sarachnis",
                "Scorpia",
                "Skotizo",
                "Tempoross",
                "The Gauntlet",
                "The Corrupted Gauntlet",
                "Theatre of Blood",
                "Thermonuclear Smoke Devil",
                "TzKal-Zuk",
                "TzTok-Jad",
                "Venenatis",
                "Vet'ion",
                "Vorkath",
                "Wintertodt",
                "Zalcano",
                "Zulrah"
        };
    }
}