package Valheim;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Player steam profile
 */
public class SteamProfile {
    private final long id;
    private static final String UNKNOWN = "Unknown";
    private final String name, url;
    private final ArrayList<String> characterNames;
    private final HashSet<String> characterNameSet;

    /**
     * Create the steam profile
     *
     * @param id               Steam id
     * @param name             Profile name
     * @param url              URL to profile
     * @param characterNames   List of Valheim character names
     * @param characterNameSet Set of unique Valheim character names
     */
    public SteamProfile(long id, String name, String url, ArrayList<String> characterNames, HashSet<String> characterNameSet) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.characterNames = characterNames;
        this.characterNameSet = characterNameSet;
    }

    /**
     * Create the steam profile
     *
     * @param id   Steam id
     * @param name Profile name
     * @param url  URL to profile
     */
    public SteamProfile(long id, String name, String url) {
        this(id, name, url, new ArrayList<>(), new HashSet<>());
    }

    /**
     * Create an unknown steam profile (where the Steam name and URL are unknown)
     *
     * @param id Steam ID
     */
    public SteamProfile(long id) {
        this(id, UNKNOWN, null);
    }

    /**
     * Check if the Steam profile is unknown
     *
     * @return Profile is unknown
     */
    public boolean isUnknown() {
        return name.equals(UNKNOWN) && url == null;
    }

    /**
     * Get the unique Valheim character names associated with the Steam profile
     *
     * @return Valheim character name set
     */
    public HashSet<String> getCharacterNameSet() {
        return characterNameSet;
    }

    /**
     * Get the list of Valheim character names associated with the Steam profile
     *
     * @return List of Valheim character names
     */
    public ArrayList<String> getCharacterNameList() {
        return characterNames;
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
