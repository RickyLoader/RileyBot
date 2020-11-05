package Runescape.OSRS.League;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Hold data on a Region from OSRS Trailblazer League
 */
public class Region {
    private final String name, imagePath;
    public static final String baseMap = "base_map.png", res = "/Runescape/OSRS/League/Regions/";
    private final int unlockIndex;
    public static final int MAX_REGIONS = 5;

    /**
     * Create a region
     *
     * @param name        Region name
     * @param unlockIndex Index of unlock
     */
    public Region(String name, int unlockIndex) {
        this.name = name;
        this.imagePath = res + name + ".png";
        this.unlockIndex = unlockIndex;
    }

    /**
     * Get the name of the region
     *
     * @return Region name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the path to an image of the region
     *
     * @return Image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Get the index of when the region was unlocked
     *
     * @return Unlock index
     */
    public int getUnlockIndex() {
        return unlockIndex;
    }

    /**
     * Parse and return regions from a JSON array
     *
     * @param regionData Region JSON array
     * @return List of regions in order of unlock
     */
    public static ArrayList<Region> parseRegions(JSONArray regionData) {
        ArrayList<Region> regions = new ArrayList<>();
        for(int i = 0; i < regionData.length(); i++) {
            JSONObject region = regionData.getJSONObject(i);
            if(region.isNull("region_name")) {
                continue;
            }
            regions.add(
                    new Region(
                            region.getString("region_name"),
                            region.getInt("unlock_index")
                    )
            );
        }
        regions.sort(Comparator.comparingInt(Region::getUnlockIndex));
        return regions;
    }
}
