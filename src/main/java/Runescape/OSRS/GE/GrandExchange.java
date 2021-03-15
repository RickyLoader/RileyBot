package Runescape.OSRS.GE;

import Network.NetworkRequest;
import Runescape.OSRS.GE.ItemPrice.Price;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * OSRS Grand Exchange pricing data
 */
public class GrandExchange {
    public static String BASE_URL = "https://prices.runescape.wiki/api/v1/osrs/";
    private final ItemManager itemManager;
    private HashMap<Integer, Long> itemTradeVolumes;
    private long lastUpdate, volumeTimestamp;

    public GrandExchange() {
        this.itemManager = new ItemManager();
        refreshData();
    }

    /**
     * Get price info for the given item - most recent high & low prices, as well as daily trade volume
     *
     * @param item Item to get price info for
     * @return Item price info
     */
    public ItemPrice getItemPrice(Item item) {
        int id = item.getId();
        JSONObject priceData = new JSONObject(
                new NetworkRequest(BASE_URL + "latest?id=" + id, false).get().body
        ).getJSONObject("data").getJSONObject(String.valueOf(id));

        Price low = priceData.isNull("low")
                ? null
                : new Price(priceData.getLong("low"), new Date(priceData.getLong("lowTime") * 1000));

        Price high = priceData.isNull("high")
                ? null
                : new Price(priceData.getLong("high"), new Date(priceData.getLong("highTime") * 1000));

        return new ItemPrice(item, high, low, itemTradeVolumes.get(id));
    }

    /**
     * Get the OSRS item manager
     *
     * @return Item manager
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Refresh the trade volume data if an hour has passed
     */
    private void refreshData() {
        long now = System.currentTimeMillis();
        if(itemTradeVolumes != null && now - lastUpdate < 60000) {
            return;
        }
        JSONObject volumeData = new JSONObject(
                new NetworkRequest(BASE_URL + "volumes", false).get().body
        );

        this.itemTradeVolumes = fetchItemTradeVolumes(volumeData.getJSONObject("data"));
        this.lastUpdate = now;
        this.volumeTimestamp = volumeData.getLong("timestamp") * 1000;
    }

    /**
     * Fetch the daily item trade volumes from the OSRS wiki API and map to the unique item id
     *
     * @param volumeData JSON object containing volume data
     * @return Map of item id -> item daily trade volume
     */
    private HashMap<Integer, Long> fetchItemTradeVolumes(JSONObject volumeData) {
        HashMap<Integer, Long> tradeVolumesMap = new HashMap<>();
        for(String itemId : volumeData.keySet()) {
            tradeVolumesMap.put(
                    Integer.parseInt(itemId),
                    volumeData.getLong(itemId)
            );
        }
        return tradeVolumesMap;
    }

    /**
     * Get the timestamp of the volume data
     *
     * @return Volume data timestamp
     */
    public long getVolumeTimestamp() {
        return volumeTimestamp;
    }
}
