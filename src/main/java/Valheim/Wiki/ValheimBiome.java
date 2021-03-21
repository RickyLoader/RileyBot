package Valheim.Wiki;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

/**
 * Valheim biome details
 */
public class ValheimBiome extends ValheimWikiAsset {
    private final HashMap<String, String[]> resources, creatures, interestPoints;

    /**
     * Create a Valheim biome from the given builder
     *
     * @param builder Builder to use values from
     */
    public ValheimBiome(ValheimBiomeBuilder builder) {
        super(builder.pageSummary, builder.imageUrl, builder.description);
        this.creatures = builder.creatures;
        this.resources = builder.resources;
        this.interestPoints = builder.interestPoints;
    }

    public static class ValheimBiomeBuilder {
        private final ValheimPageSummary pageSummary;
        private final String description, imageUrl;
        private HashMap<String, String[]> resources, creatures, interestPoints;

        /**
         * Initialise the builder
         *
         * @param pageSummary Summary of wiki page
         * @param description Biome description
         * @param imageUrl    URL to asset image
         */
        public ValheimBiomeBuilder(ValheimPageSummary pageSummary, String description, String imageUrl) {
            this.pageSummary = pageSummary;
            this.description = description;
            this.imageUrl = imageUrl;
        }

        /**
         * Set the biome resources - a map of resource name e.g "Food" to an array of resources e.g [Blueberries]
         *
         * @param resources Map of resources
         * @return Builder
         */
        public ValheimBiomeBuilder setResources(HashMap<String, String[]> resources) {
            this.resources = resources;
            return this;
        }

        /**
         * Set the biome points of interest - a map of interest type e.g "NPCs"
         * to an array of interests e.g [Haldor]
         *
         * @param interestPoints Map of interest points
         * @return Builder
         */
        public ValheimBiomeBuilder setInterestPoints(HashMap<String, String[]> interestPoints) {
            this.interestPoints = interestPoints;
            return this;
        }

        /**
         * Set the biome creatures - a map of creature type e.g "Passive"
         * to an array of creatures e.g [Deer, Crow]
         *
         * @param creatures Map of creatures
         * @return Builder
         */
        public ValheimBiomeBuilder setCreatures(HashMap<String, String[]> creatures) {
            this.creatures = creatures;
            return this;
        }

        /**
         * Build a Valheim biome from the builder values
         *
         * @return Valheim biome
         */
        public ValheimBiome build() {
            return new ValheimBiome(this);
        }
    }

    /**
     * Parse a biome attribute from a wiki HTML element.
     * Biome attributes e.g "Creatures" are displayed as a titled list of items.
     * Create a map of the title -> items
     *
     * @param element HTML element containing biome attribute
     * @return Map of biome attribute
     */
    public static HashMap<String, String[]> parseAttributes(Element element) {
        HashMap<String, String[]> attributes = new HashMap<>();
        Elements categories = element.getElementsByClass("pi-data-label");
        for(Element category : categories) {
            Elements items = category.nextElementSibling().selectFirst("ul").children();
            String[] itemArr = new String[items.size()];
            for(int i = 0; i < itemArr.length; i++) {
                itemArr[i] = items.get(i).text();
            }
            attributes.put(category.text(), itemArr);
        }
        return attributes;
    }

    /**
     * Get a map of the resources found in the biome.
     * Map resource type e.g Food to an array of resources of that type
     *
     * @return Biome resources
     */
    public HashMap<String, String[]> getResources() {
        return resources;
    }

    /**
     * Get a map of the points of interest found in the biome.
     * Map interest type e.g Dungeons to an array of points of interest of that type
     *
     * @return Points of interest in the biome
     */
    public HashMap<String, String[]> getInterestPoints() {
        return interestPoints;
    }

    /**
     * Get a map of the creatures found in the biome.
     * Map creature type e.g Passive to an array of creatures of that type
     *
     * @return Biome creatures
     */
    public HashMap<String, String[]> getCreatures() {
        return creatures;
    }

    /**
     * Return whether the biome has any creatureS
     *
     * @return Biome has creatures
     */
    public boolean hasCreatures() {
        return creatures != null;
    }

    /**
     * Return whether the biome has any points of interest
     *
     * @return Biome has points of interest
     */
    public boolean hasInterestPoints() {
        return interestPoints != null;
    }

    /**
     * Return whether the biome has any resources
     *
     * @return Biome has resources
     */
    public boolean hasResources() {
        return resources != null;
    }

}
