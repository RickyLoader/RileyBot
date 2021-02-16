package COD;

import COD.Assets.Commendation;
import COD.Assets.FieldUpgrade;
import COD.Assets.Killstreak;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * MW asset manager
 */
public class MWManager extends CODManager {
    private final HashMap<String, FieldUpgrade> supers;
    private final HashMap<String, Commendation> commendations;
    private final HashMap<String, Killstreak> killstreaks;

    /**
     * Create the MW manager
     */
    public MWManager() {
        super(GAME.MW);
        this.supers = readSupers();
        this.commendations = readCommendations();
        this.killstreaks = readKillstreaks();
    }

    /**
     * Parse killstreak JSON in to objects and map to the codename
     *
     * @return Map of codename -> killstreak
     */
    private HashMap<String, Killstreak> readKillstreaks() {
        HashMap<String, Killstreak> killstreaks = new HashMap<>();
        JSONObject killstreakList = getAssetJSON("streaks.json", "streaks");

        for(String name : killstreakList.keySet()) {
            JSONObject killstreakData = killstreakList.getJSONObject(name);
            killstreaks.put(
                    name,
                    new Killstreak(
                            name,
                            killstreakData.getString("real_name"),
                            killstreakData.has("extra_stat")
                                    ? killstreakData.getString("extra_stat")
                                    : null,
                            resourceHandler.getImageResource(
                                    basePath + "Killstreaks/" + name + ".png"
                            )
                    )
            );
        }
        return killstreaks;
    }

    /**
     * Parse commendation JSON in to objects and map to the codename
     *
     * @return Map of codename -> commendation
     */
    private HashMap<String, Commendation> readCommendations() {
        HashMap<String, Commendation> commendations = new HashMap<>();
        JSONObject commendationList = getAssetJSON("commendations.json", "commendations");

        for(String name : commendationList.keySet()) {
            JSONObject commendationData = commendationList.getJSONObject(name);
            commendations.put(
                    name,
                    new Commendation(
                            name,
                            commendationData.getString("title"),
                            commendationData.getString("desc"),
                            resourceHandler.getImageResource(basePath + "Accolades/" + name + ".png")
                    )
            );
        }
        return commendations;
    }

    /**
     * Parse supers JSON in to objects and map to the codename
     *
     * @return Map of codename -> super
     */
    private HashMap<String, FieldUpgrade> readSupers() {
        HashMap<String, FieldUpgrade> supers = new HashMap<>();
        JSONObject supersList = getAssetJSON("supers.json", "supers");

        for(String name : supersList.keySet()) {
            JSONObject superData = supersList.getJSONObject(name);
            supers.put(
                    name,
                    new FieldUpgrade(
                            name,
                            superData.getString("real_name"),
                            superData.has("misc1_name")
                                    ? superData.getString("misc1_name")
                                    : null,
                            resourceHandler.getImageResource(basePath + "Supers/" + name + ".png")
                    )
            );
        }
        return supers;
    }

    /**
     * Get a super by its codename
     *
     * @param codename Super codename
     * @return Super with codename or null
     */
    public FieldUpgrade getSuperByCodename(String codename) {
        return supers.get(codename);
    }

    /**
     * Get a commendation by its codename
     *
     * @param codename Commendation codename
     * @return Commendation with codename or null
     */
    public Commendation getCommendationByCodename(String codename) {
        return commendations.get(codename);
    }

    /**
     * Get a killstreak by its codename
     *
     * @param codename Killstreak codename
     * @return Killstreak with codename or null
     */
    public Killstreak getKillstreakByCodename(String codename) {
        return killstreaks.get(codename);
    }
}
