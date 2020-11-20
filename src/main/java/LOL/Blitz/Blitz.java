package LOL.Blitz;

import Bot.ResourceHandler;
import Network.NetworkRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Blitz.gg champion data
 */
public class Blitz {
    private final String ddragonPrefix = "https://blitz-cdn-plain.blitz.gg/blitz/ddragon/";
    private final String[] versions;
    private final HashMap<String, Champion> champions;
    private final HashMap<String, Item> items;
    private final HashMap<String, SummonerSpell> spells;
    private final HashMap<Integer, Rune> runes;
    private final BufferedImage deletedItemImage;

    /**
     * Create the Blitz wrapper
     */
    public Blitz() {
        this.versions = getVersions();
        String versionString = versions[0] + "/data/en_US/";
        this.champions = getChampions(versionString);
        this.items = getItems(versionString);
        this.spells = getSummonerSpells(versionString);
        this.runes = getRunes(versionString);
        this.deletedItemImage = new ResourceHandler().getImageResource("/LOL/items/missing.png");
        getStaticRunes();
    }

    /**
     * Add the static runes to the map
     */
    private void getStaticRunes() {
        String prefix = "StatMods";
        runes.put(5001, new Rune(5001, "Scaling Health", prefix + "HealthScalingIcon.png", false));
        runes.put(5002, new Rune(5002, "Armor", prefix + "ArmorIcon.png", false));
        runes.put(5003, new Rune(5003, "Magic Resist", prefix + "MagicResIcon.png", false));
        runes.put(5005, new Rune(5005, "Attack Speed", prefix + "AttackSpeedIcon.png", false));
        runes.put(5007, new Rune(5007, "Scaling Cooldown Reduction", prefix + "CDRScalingIcon.png", false));
        runes.put(5008, new Rune(5008, "Adaptive Force", prefix + "AdaptiveForceIcon.png", false));
    }

    /**
     * Get the most recent league versions
     *
     * @return Latest 5 league versions
     */
    private String[] getVersions() {
        JSONArray versionData = new JSONArray(
                new NetworkRequest(ddragonPrefix + "versions.json", false).get()
        );
        String[] versions = new String[5];
        for(int i = 0; i < 5; i++) {
            versions[i] = versionData.getString(i);
        }
        return versions;
    }

    /**
     * Get the rune data from Blitz and map rune id to rune data
     *
     * @param version Game version String
     * @return Rune id to rune data map
     */
    private HashMap<Integer, Rune> getRunes(String version) {
        JSONArray runeData = new JSONArray(
                new NetworkRequest(ddragonPrefix + version + "runes.json", false).get()
        );
        HashMap<Integer, Rune> runes = new HashMap<>();
        for(int i = 0; i < runeData.length(); i++) {
            JSONObject rune = runeData.getJSONObject(i);
            Rune keyRune = parseRune(rune);
            runes.put(keyRune.getId(), keyRune);
            JSONArray slots = rune.getJSONArray("slots");
            for(int j = 0; j < slots.length(); j++) {
                JSONArray children = slots.getJSONObject(j).getJSONArray("runes");
                for(int k = 0; k < children.length(); k++) {
                    Rune childRune = parseRune(children.getJSONObject(k));
                    runes.put(childRune.getId(), childRune);
                }
            }
        }
        return runes;
    }

    /**
     * Parse a rune from the data
     *
     * @param rune Rune data
     * @return Rune
     */
    private Rune parseRune(JSONObject rune) {
        return new Rune(
                rune.getInt("id"),
                rune.getString("name"),
                rune.getString("icon"),
                rune.has("slots")
        );
    }

    /**
     * Get the summoner spell data from Blitz and map spell id to spell data
     *
     * @param version Game version String
     * @return Spell id to spell data map
     */
    private HashMap<String, SummonerSpell> getSummonerSpells(String version) {
        JSONObject spellData = new JSONObject(
                new NetworkRequest(ddragonPrefix + version + "summoners.json", false).get()
        ).getJSONObject("data");
        HashMap<String, SummonerSpell> spells = new HashMap<>();
        for(String spellKey : spellData.keySet()) {
            JSONObject spell = spellData.getJSONObject(spellKey);
            spells.put(
                    spell.getString("key"),
                    new SummonerSpell(
                            spell.getString("name"),
                            spellKey
                    )
            );
        }
        return spells;
    }

    /**
     * Get the champion data from Blitz and map champion key to champion data
     *
     * @param version Game version String
     * @return Champion id to champion data map
     */
    private HashMap<String, Champion> getChampions(String version) {
        JSONObject championData = new JSONObject(
                new NetworkRequest(ddragonPrefix + version + "champions.json", false).get()
        ).getJSONObject("data");

        HashMap<String, Champion> champions = new HashMap<>();
        String[] buttons = new String[]{"Q", "W", "E", "R"};
        for(String championKey : championData.keySet()) {
            JSONObject champion = championData.getJSONObject(championKey);
            JSONArray abilityData = champion.getJSONArray("spells");

            Ability[] abilities = new Ability[4];

            // Skip passive
            for(int i = 1; i < abilityData.length(); i++) {
                JSONObject ability = abilityData.getJSONObject(i);
                int index = i - 1;
                abilities[index] = new Ability(
                        buttons[index],
                        ability.getJSONObject("image").getString("full")
                );
            }
            champions.put(
                    championKey,
                    new Champion(
                            champion.getString("name"),
                            champion.getString("key"),
                            championKey,
                            abilities
                    )
            );
        }
        return champions;
    }

    /**
     * Get the item data from Blitz and map item id to item data
     *
     * @param version Game version String
     * @return Item id to item data map
     */
    private HashMap<String, Item> getItems(String version) {
        JSONObject itemData = new JSONObject(
                new NetworkRequest(ddragonPrefix + version + "items.json", false).get()
        ).getJSONObject("data");
        HashMap<String, Item> items = new HashMap<>();
        for(String itemId : itemData.keySet()) {
            JSONObject item = itemData.getJSONObject(itemId);
            items.put(
                    itemId,
                    new Item(
                            item.getString("name"),
                            itemId
                    )
            );
        }
        return items;
    }

    /**
     * Get the champion data for the given query
     *
     * @param query Champion name query
     * @return Champion data
     */
    private Champion getChampion(String query) {
        String[] words = query.split(" ");

        for(int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }

        Champion champion = champions.getOrDefault(StringUtils.join(words, ""), null);
        if(champion == null) {
            for(Champion c : champions.values()) {
                if(c.getName().toLowerCase().contains(query)) {
                    return c;
                }
            }
        }
        return champion;
    }

    /**
     * Get the build data for a champion name query
     *
     * @param query Champion name query
     * @param role  Champion role
     * @return Champion build data
     */
    public BuildData getBuildData(String query, String role) {
        Champion champion = getChampion(query);

        if(champion == null) {
            return null;
        }

        JSONObject data = null;
        String buildVersion = null;

        for(String version : versions) {
            JSONArray current = new JSONObject(
                    getBlitzData(
                            champion,
                            role,
                            version.substring(0, version.lastIndexOf("."))
                    )).getJSONArray("data");

            if(current.isEmpty()) {
                continue;
            }

            JSONObject currentData = current.getJSONObject(0);

            JSONObject stats = currentData.getJSONObject("stats");
            JSONObject build = stats.getJSONObject("most_common_big_item_builds");
            JSONArray items = build.isNull("build") ? null : build.getJSONArray("build");

            if(items == null || !(items.get(items.length() - 1) instanceof Integer)) {
                continue;
            }

            if(stats.getJSONObject("most_common_skills").isNull("build")) {
                continue;
            }

            data = currentData;
            buildVersion = version;
            break;
        }

        if(data == null) {
            return null;
        }

        JSONObject stats = data.getJSONObject("stats");

        JSONArray spellData = getBuildList(stats, "spells");
        SummonerSpell[] spells = new SummonerSpell[]{
                this.spells.get(String.valueOf(spellData.getInt(0))),
                this.spells.get(String.valueOf(spellData.getInt(1)))
        };

        JSONArray abilityData = getBuildList(stats, "most_common_skills");
        int[] abilityOrder = new int[abilityData.length()];
        for(int i = 0; i < abilityData.length(); i++) {
            abilityOrder[i] = abilityData.getInt(i) - 1;
        }
        JSONArray runeData = getBuildList(stats, "most_common_runes");
        JSONArray runeShardData = getBuildList(stats, "most_common_rune_stat_shards");
        ArrayList<Rune> runes = new ArrayList<>();

        for(int i = 0; i < runeData.length(); i++) {
            runes.add(this.runes.get(runeData.getInt(i)));
        }

        for(int i = 0; i < runeShardData.length(); i++) {
            runes.add(this.runes.get(runeShardData.getInt(i)));
        }

        return new BuildData(
                buildVersion,
                versions[0],
                champion,
                role,
                spells,
                parseItemList(getBuildList(stats, "most_common_starting_items")),
                parseItemList(getBuildList(stats, "most_common_core_builds")),
                parseItemList(getBuildList(stats, "most_common_big_item_builds")),
                abilityOrder,
                runes.toArray(new Rune[0]),
                stats.getJSONObject("most_common_big_item_builds").getDouble("win_rate"),
                stats.getJSONObject("most_common_big_item_builds").getInt("games")
        );
    }

    /**
     * Get a JSON array from the Blitz data
     *
     * @param source Source JSON object
     * @param key    Key to object holding array
     * @return JSON array
     */
    private JSONArray getBuildList(JSONObject source, String key) {
        return source.getJSONObject(key).getJSONArray("build");
    }

    /**
     * Get the Blitz.gg champion build data
     *
     * @param champion Champion
     * @param role     Champion role
     * @param version  Game version
     * @return Blitz.gg champion data for given role
     */
    private String getBlitzData(Champion champion, String role, String version) {
        String url = "https://beta.iesdev.com/api/lolstats/champions/"
                + champion.getId()
                + "?patch="
                + version
                + "&queue=420&region=world&tier=PLATINUM_PLUS&role="
                + role;

        return new NetworkRequest(url, false).get();
    }

    /**
     * Parse a list of item ids from the API to a list of items
     *
     * @param itemData List of item ids
     * @return Array of items
     */
    private Item[] parseItemList(JSONArray itemData) {
        ArrayList<Item> items = new ArrayList<>();
        for(int i = 0; i < itemData.length(); i++) {
            Object o = itemData.get(i);
            if(!(o instanceof Integer)) {
                continue;
            }
            String id = String.valueOf(o);
            items.add(
                    this.items.getOrDefault(
                            id,
                            getDeletedItem(id)
                    )
            );
        }
        return items.toArray(new Item[0]);
    }

    /**
     * Create an item object for an item that has been deleted
     * Build an image to use as the item icon displaying the item overlaid with a removed icon
     *
     * @param id Item id
     * @return Deleted item
     */
    private Item getDeletedItem(String id) {
        BufferedImage itemImage = new ResourceHandler().getImageResource(Item.getImagePath(id));
        Graphics g = itemImage.getGraphics();
        g.drawImage(
                deletedItemImage,
                itemImage.getWidth() / 2 - deletedItemImage.getWidth() / 2,
                itemImage.getHeight() / 2 - deletedItemImage.getHeight() / 2,
                null
        );
        g.dispose();
        return new Item(
                "Deleted",
                itemImage
        );
    }
}
