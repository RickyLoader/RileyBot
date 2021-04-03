package Runescape.OSRS.GE;

import Network.NetworkRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Tradeable OSRS item manager
 */
public class ItemManager {
    private HashMap<Integer, Item> items;
    private long lastUpdate;

    /**
     * Get all tradeable OSRS items
     */
    public ItemManager() {
        refreshData();
    }

    /**
     * Refresh the item data if an hour has passed
     */
    private void refreshData() {
        long now = System.currentTimeMillis();
        if(items != null && now - lastUpdate < 60000) {
            return;
        }
        this.items = fetchItems();
        this.lastUpdate = now;
    }

    /**
     * Get an array of items of the given name.
     * If a singular match is not found, return an array of items containing the given query in the name.
     *
     * @param name Item name
     * @return Array of items containing/matching name
     */
    public Item[] getItemsByName(String name) {
        refreshData();
        Item[] matchingItems = items
                .values()
                .stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .toArray(Item[]::new);

        if(matchingItems.length == 1) {
            return matchingItems;
        }

        return items
                .values()
                .stream()
                .filter(item -> item.getName().toLowerCase().contains(name.toLowerCase()))
                .toArray(Item[]::new);
    }

    /**
     * Get an item by its id
     *
     * @param id Item id
     * @return Item with id or null
     */
    public Item getItemByID(int id) {
        refreshData();
        return items.get(id);
    }

    /**
     * Fetch item info from the OSRS wiki API and map to the unique item id
     *
     * @return Map of item id -> item
     */
    private HashMap<Integer, Item> fetchItems() {
        HashMap<Integer, Item> itemMap = new HashMap<>();
        JSONArray items = new JSONArray(
                new NetworkRequest(GrandExchange.BASE_URL + "mapping", false).get().body
        );
        for(int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            int id = item.getInt("id");
            itemMap.put(
                    id,
                    new Item(
                            id,
                            item.getString("name"),
                            item.getString("examine"),
                            item.has("members") && item.getBoolean("members"),
                            item.has("highalch") ? item.getInt("highalch") : -1,
                            item.has("lowalch") ? item.getInt("lowalch") : -1,
                            item.has("limit") ? item.getInt("limit") : -1,
                            new Item.ItemImage(item.getString("icon"), id)
                    )
            );
        }
        return itemMap;
    }
}