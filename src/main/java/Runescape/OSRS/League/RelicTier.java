package Runescape.OSRS.League;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Hold data on a relic tier from OSRS Trailblazer League
 */
public class RelicTier {
    private final int tier;
    private final String name;
    private final ArrayList<Relic> relics;
    public static final int MAX_RELICS = 6;

    /**
     * Create a relic tier
     *
     * @param tier   Tier index (order of unlock)
     * @param name   Tier name
     */
    public RelicTier(int tier, String name) {
        this.tier = tier;
        this.name = name;
        this.relics = new ArrayList<>();
    }

    /**
     * Get a relic from the tier via the index
     *
     * @param index Index of relic
     * @return Relic
     */
    public Relic getRelicByIndex(int index) {
        return relics.get(index);
    }

    /**
     * Add the given relic to the tier
     *
     * @param relic Relic to add
     */
    public void addRelic(Relic relic) {
        relics.add(relic);
        relics.sort(Comparator.comparingInt(Relic::getTierIndex));
    }

    /**
     * Get the tier name
     *
     * @return Tier name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the tier index
     *
     * @return Tier index
     */
    public int getTier() {
        return tier;
    }


    /**
     * Parse and return relic tiers from a JSON array
     *
     * @param relicData Relic JSON array
     * @return List of RelicTiers in order of unlock
     */
    public static ArrayList<RelicTier> parseRelics(JSONArray relicData) {
        HashMap<Integer, RelicTier> relicTierMap = new HashMap<>();
        for(int i = 0; i < relicData.length(); i++) {
            JSONObject relicInfo = relicData.getJSONObject(i);
            if(relicInfo.isNull("relic_id")) {
                continue;
            }
            int relicTierID = relicInfo.getInt("relic_tier");
            RelicTier tier = relicTierMap.get(relicTierID);
            if(tier == null) {
                tier = new RelicTier(relicTierID, relicInfo.getString("tier_name"));
            }
            tier.addRelic(
                    new Relic(
                            relicInfo.getInt("relic_id"),
                            relicInfo.getInt("relic_index"),
                            relicInfo.getString("relic_name")
                    )
            );
            relicTierMap.put(relicTierID, tier);
        }
        ArrayList<RelicTier> relics = new ArrayList<>(relicTierMap.values());
        relics.sort(Comparator.comparingInt(RelicTier::getTier));
        return relics;
    }
}
