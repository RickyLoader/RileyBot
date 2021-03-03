package COD.Match;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hold data on a team in a COD match
 */
public class Team {
    private final String name;
    private final ArrayList<MatchPlayer> players;
    private final HashMap<String, MatchPlayer> playerMap;

    /**
     * Create a team
     *
     * @param name Team name
     */
    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.playerMap = new HashMap<>();
    }

    /**
     * Add a player to the team
     *
     * @param player Player to add
     */
    public void addPlayer(MatchPlayer player) {
        this.players.add(player);
        this.playerMap.put(player.getName(), player);
    }

    /**
     * Get the list of team players
     *
     * @return Team players
     */
    public ArrayList<MatchPlayer> getPlayers() {
        return players;
    }

    /**
     * Get a player by name
     *
     * @param name Name of player to get
     * @return Player
     */
    public MatchPlayer getPlayerByName(String name) {
        return playerMap.get(name);
    }

    /**
     * Get the team name
     *
     * @return Team name
     */
    public String getName() {
        return name;
    }
}
