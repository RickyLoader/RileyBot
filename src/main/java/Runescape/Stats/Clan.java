package Runescape.Stats;

import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * RS3 Clan
 */
public class Clan {
    private final String name;
    private final BufferedImage banner;
    private final HashMap<String, ROLE> rolesByPlayer;
    private final HashMap<ROLE, ArrayList<String>> playersByRole;

    public enum ROLE {
        RECRUIT,
        ADMIN,
        CORPORAL,
        SERGEANT,
        LIEUTENANT,
        CAPTAIN,
        GENERAL,
        ORGANISER,
        COORDINATOR,
        OVERSEER,
        DEPUTY_OWNER,
        OWNER,
        UNKNOWN_ROLE;

        /**
         * Get the name of the role
         *
         * @return Role name - DEPUTY_OWNER -> "Deputy owner"
         */
        public String getName() {
            return StringUtils.capitalize(this.name().toLowerCase().replace("_", " "));
        }

        /**
         * Get a role by name - "Deputy owner" -> DEPUTY_OWNER
         *
         * @param name Role name
         * @return Role with name or UNKNOWN_ROLE
         */
        public static ROLE byName(String name) {
            try {
                return ROLE.valueOf(name.toUpperCase().replace(" ", "_"));
            }
            catch(Exception e) {
                return UNKNOWN_ROLE;
            }
        }

        /**
         * Get the prefix to use for the role - e.g "an" OWNER, "a" CAPTAIN
         *
         * @return Role prefix
         */
        public String getPrefix() {
            switch(this) {
                default:
                    return "a";
                case ADMIN:
                case ORGANISER:
                case OVERSEER:
                case OWNER:
                case UNKNOWN_ROLE:
                    return "an";
            }
        }
    }

    /**
     * Create a clan
     *
     * @param name   Clan name
     * @param banner Clan banner image
     */
    public Clan(String name, BufferedImage banner) {
        this.name = name;
        this.banner = banner;
        this.rolesByPlayer = new HashMap<>();
        this.playersByRole = new HashMap<>();
    }

    /**
     * Get the role of a player by their name
     *
     * @param name Player name
     * @return Player clan role or UNKNOWN_ROLE
     */
    public ROLE getRoleByPlayerName(String name) {
        return rolesByPlayer.getOrDefault(name.toLowerCase(), ROLE.UNKNOWN_ROLE);
    }

    /**
     * Get the number of players in the clan
     *
     * @return Clan player count
     */
    public int getMemberCount() {
        return rolesByPlayer.size();
    }

    /**
     * Get the players with the given role
     *
     * @param role Role to get players for
     * @return List of player names with role
     */
    public ArrayList<String> getPlayersByRole(ROLE role) {
        return playersByRole.getOrDefault(role, new ArrayList<>());
    }

    /**
     * Add a player to the clan. Map from role -> players & player->role
     *
     * @param name Player name
     * @param role Player role in clan
     */
    public void addPlayer(String name, ROLE role) {
        this.rolesByPlayer.put(name.toLowerCase(), role);
        ArrayList<String> playersByRole = this.playersByRole.get(role);
        if(playersByRole == null) {
            playersByRole = new ArrayList<>();
        }
        playersByRole.add(name);
        this.playersByRole.put(role, playersByRole);
    }

    /**
     * Get the clan name
     *
     * @return Clan name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the clan banner image
     *
     * @return Banner image
     */
    public BufferedImage getBanner() {
        return banner;
    }
}
