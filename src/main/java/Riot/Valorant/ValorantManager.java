package Riot.Valorant;

import Bot.ResourceHandler;
import Riot.Valorant.Assets.Ability;
import Riot.Valorant.Assets.Agent;
import Riot.Valorant.Assets.Role;
import Riot.Valorant.Assets.Weapon;
import Riot.Valorant.Stats.CompetitiveRank;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;

import static Riot.Valorant.Stats.CompetitiveRank.*;

/**
 * Valorant asset manager
 */
public class ValorantManager {
    private static ValorantManager instance;
    private final HashMap<String, Weapon> weapons;
    private final HashMap<String, Agent> agents;
    private final HashMap<String, Role> roles;
    private final HashMap<DIVISION, CompetitiveRank[]> ranks;
    private final ResourceHandler resourceHandler;
    private static final String
            COLOUR_KEY = "colour",
            NAME_KEY = "name";

    /**
     * Read in Valorant assets
     */
    private ValorantManager() {
        this.resourceHandler = new ResourceHandler();

        JSONObject agentsData = getAssetJson("agents.json");

        // Agents require roles so read them first
        this.roles = readRoles(agentsData.getJSONArray("roles"));

        this.agents = readAgents(agentsData.getJSONObject("agents"));
        this.weapons = readWeapons();
        this.ranks = createRankMap();
    }

    /**
     * Create a map of ranked division to an array of tiers
     *
     * @return Map of rank division to tiers
     */
    private HashMap<DIVISION, CompetitiveRank[]> createRankMap() {
        HashMap<DIVISION, CompetitiveRank[]> ranks = new HashMap<>();
        ranks.put(DIVISION.IRON, createRankTiers(DIVISION.IRON, 3));
        ranks.put(DIVISION.BRONZE, createRankTiers(DIVISION.BRONZE, 3));
        ranks.put(DIVISION.SILVER, createRankTiers(DIVISION.SILVER, 3));
        ranks.put(DIVISION.GOLD, createRankTiers(DIVISION.GOLD, 3));
        ranks.put(DIVISION.PLATINUM, createRankTiers(DIVISION.PLATINUM, 3));
        ranks.put(DIVISION.DIAMOND, createRankTiers(DIVISION.DIAMOND, 3));
        ranks.put(DIVISION.IMMORTAL, createRankTiers(DIVISION.IMMORTAL, 3));

        // No tiers
        ranks.put(DIVISION.UNRATED, createRankTier(DIVISION.UNRATED));
        ranks.put(DIVISION.RADIANT, createRankTier(DIVISION.RADIANT));
        return ranks;
    }

    /**
     * Create an array containing a singular tier for a division.
     * E.g unranked doesn't have unranked 1, unranked 2, etc.
     *
     * @param division Division with singular tier
     * @return Tier for division
     */
    private CompetitiveRank[] createRankTier(DIVISION division) {
        return createRankTiers(division, 1);
    }

    /**
     * Create an array of ranks up to the given count (inclusive & excluding 0).
     * The index of these ranks will be the tier - 1;
     *
     * @param division Rank division - e.g BRONZE
     * @param count    Count - e.g 3 -> BRONZE 1, BRONZE 2, BRONZE 3
     * @return Array of ranks
     */
    private CompetitiveRank[] createRankTiers(DIVISION division, int count) {
        CompetitiveRank[] ranks = new CompetitiveRank[count];
        for(int i = 0; i < ranks.length; i++) {
            ranks[i] = createRank(division, i + 1);
        }
        return ranks;
    }

    /**
     * Create a competitive rank
     *
     * @param division Division - e.g BRONZE
     * @param tier     Tier within division - e.g 1
     * @return Competitive rank
     */
    private CompetitiveRank createRank(DIVISION division, int tier) {
        return new CompetitiveRank(
                division,
                tier,
                resourceHandler.getImageResource(BASE_PATH + division.name() + "/" + tier + ".png")
        );
    }

    /**
     * Read in the JSON of the given filename in the Valorant resource directory.
     *
     * @param filename JSON filename e.g "weapons.json"
     * @return JSON object of file at given path
     */
    private JSONObject getAssetJson(String filename) {
        return new JSONObject(
                resourceHandler.getResourceFileAsString(ResourceHandler.VALORANT_BASE_PATH + "Data/" + filename)
        );
    }

    /**
     * Create a map of agent role name -> agent role. E.g "Controller" -> Controller role
     *
     * @param roleArray JSON array of role names
     * @return Map of agent role name -> agent role
     */
    private HashMap<String, Role> readRoles(JSONArray roleArray) {
        HashMap<String, Role> roles = new HashMap<>();

        for(int i = 0; i < roleArray.length(); i++) {
            final String roleName = roleArray.getString(i);
            roles.put(
                    roleName,
                    new Role(
                            roleName,
                            resourceHandler.getImageResource(Role.BASE_PATH + roleName + ".png")
                    )
            );
        }
        return roles;
    }

    /**
     * Create a map of agent ID -> agent. E.g "brimstone" -> Brimstone agent
     *
     * @param agentsData JSON object containing all agents
     * @return Map of agent ID -> agent
     */
    private HashMap<String, Agent> readAgents(JSONObject agentsData) {
        HashMap<String, Agent> agents = new HashMap<>();

        for(String agentId : agentsData.keySet()) {
            final String agentPath = Agent.BASE_PATH + agentId + "/";
            JSONObject agentData = agentsData.getJSONObject(agentId);
            agents.put(
                    agentId,
                    new Agent(
                            agentId,
                            agentData.getString(NAME_KEY),
                            roles.get(agentData.getString("role")),
                            agentData.has(COLOUR_KEY) ? Color.decode(agentData.getString(COLOUR_KEY)) : null,
                            parseAbilities(agentData.getJSONObject("abilities"), agentPath),
                            resourceHandler.getImageResource(agentPath + "thumb.png")
                    )
            );
        }

        return agents;
    }

    /**
     * Parse an agent's abilities from the given JSON object
     *
     * @param abilitiesData Agent abilities JSON object (contains all abilities)
     * @param agentPath     Path to agent image resources - e.g "/Valorant/Agents/brimstone/"
     * @return Abilities from JSON
     */
    private Abilities parseAbilities(JSONObject abilitiesData, String agentPath) {
        return new Abilities(
                parseAbility(abilitiesData, Ability.FIRST_BASIC_ABILITY_ID, agentPath),
                parseAbility(abilitiesData, Ability.SECOND_BASIC_ABILITY_ID, agentPath),
                parseAbility(abilitiesData, Ability.GRENADE_ABILITY_ID, agentPath),
                parseAbility(abilitiesData, Ability.ULTIMATE_ABILITY_ID, agentPath)
        );
    }

    /**
     * Parse an agent ability from the given JSON object
     *
     * @param abilitiesData Agent abilities JSON object (contains all abilities)
     * @param abilityId     ID of the ability - e.g "ability1"
     * @param agentPath     Path to agent image resources - e.g "/Valorant/Agents/brimstone/"
     * @return Ability from JSON
     */
    private Ability parseAbility(JSONObject abilitiesData, String abilityId, String agentPath) {
        JSONObject abilityData = abilitiesData.getJSONObject(abilityId);
        return new Ability(
                abilityId,
                abilityData.getString(NAME_KEY),
                resourceHandler.getImageResource(agentPath + "Abilities/" + abilityId + ".png")
        );
    }

    /**
     * Create a map of weapon ID -> weapon. E.g "vandal" -> Vandal weapon
     *
     * @return Map of agent ID -> agent
     */
    private HashMap<String, Weapon> readWeapons() {
        JSONObject weaponsData = getAssetJson("weapons.json").getJSONObject("weapons");
        HashMap<String, Weapon> weapons = new HashMap<>();

        for(String categoryName : weaponsData.keySet()) {
            Weapon.CATEGORY category = Weapon.CATEGORY.fromName(categoryName);
            JSONObject categoryWeaponsData = weaponsData.getJSONObject(categoryName);
            final String categoryPath = Weapon.BASE_PATH + categoryName + "/";

            for(String weaponId : categoryWeaponsData.keySet()) {
                JSONObject weaponData = categoryWeaponsData.getJSONObject(weaponId);

                weapons.put(
                        weaponId,
                        new Weapon(
                                weaponId,
                                weaponData.getString(NAME_KEY),
                                category,
                                weaponData.getInt("cost"),
                                resourceHandler.getImageResource(categoryPath + weaponId + ".png")
                        )
                );
            }
        }

        return weapons;
    }

    /**
     * Get an instance of the ValorantManager class
     *
     * @return Instance
     */
    public static ValorantManager getInstance() {
        if(instance == null) {
            instance = new ValorantManager();
        }
        return instance;
    }

    /**
     * Get an agent by their unique ID.
     *
     * @param agentId Agent ID - e.g "brimstone"
     * @return Agent by ID or null (if no match)
     */
    @Nullable
    public Agent getAgentById(String agentId) {
        return agents.get(agentId.replaceAll("/", "").trim());
    }

    /**
     * Get a weapon by its unique ID.
     *
     * @param weaponId Weapon ID - e.g "judge"
     * @return Weapon by ID or null (if no match)
     */
    @Nullable
    public Weapon getWeaponById(String weaponId) {
        return weapons.get(weaponId);
    }

    /**
     * Get a rank by its name
     *
     * @param rankName Rank & tier - e.g "Bronze 1"
     * @return Rank from name or null (if it doesn't exist)
     */
    @Nullable
    public CompetitiveRank getRankByName(String rankName) {
        final String[] args = rankName.split(" ");
        try {
            DIVISION division = DIVISION.valueOf(args[0].toUpperCase());
            CompetitiveRank[] ranks = this.ranks.get(division);
            return division.hasTiers() ? ranks[(Integer.parseInt(args[1]) - 1)] : ranks[0];
        }
        catch(Exception e) {
            return null;
        }
    }
}
