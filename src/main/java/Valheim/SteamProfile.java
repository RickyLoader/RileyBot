package Valheim;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Player steam profile
 */
public class SteamProfile {
    private final long id;
    private final String name, url;
    private final ArrayList<String> characterNames;
    private final HashSet<String> characterNameSet;

    /**
     * Create the steam profile
     *
     * @param id   Steam id
     * @param name Profile name
     * @param url  URL to profile
     */
    public SteamProfile(long id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.characterNames = new ArrayList<>();
        this.characterNameSet = new HashSet<>();
    }

    /**
     * Add a known character name to the steam profile (if it does not already exist)
     *
     * @param characterName Valheim character name
     */
    public void addCharacterName(String characterName) {
        if(characterNameSet.contains(characterName)) {
            return;
        }
        characterNameSet.add(characterName);
        characterNames.add(characterName);
    }

    /**
     * Get the list of known Valheim character names associated with the steam profile
     *
     * @return Comma separated list of character names
     */
    public String getCharacterNames() {
        if(characterNames.isEmpty()) {
            return "UNKNOWN_CHARACTER";
        }
        return StringUtils.join(characterNames, "/");
    }

    /**
     * Get the URL to the steam profile
     *
     * @return URL to steam profile
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the profile name
     *
     * @return Profile name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique steam id
     *
     * @return Steam id
     */
    public long getId() {
        return id;
    }
}
