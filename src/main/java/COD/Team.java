package COD;

import java.util.ArrayList;

/**
 * Hold data on a team in a COD match
 */
public class Team {
    private final String name;
    private final ArrayList<MatchPlayer> players;

    /**
     * Create a team
     *
     * @param name Team name
     */
    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    /**
     * Add a player to the team
     *
     * @param player Player to add
     */
    public void addPlayer(MatchPlayer player) {
        this.players.add(player);
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
     * Get the team name
     *
     * @return Team name
     */
    public String getName() {
        return name;
    }
}
