package Valheim.Wiki;

import Valheim.Wiki.ValheimItem.TYPE;
import Valheim.Wiki.ValheimPageSummary.CATEGORY;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Search Valheim wiki pages
 */
public class ValheimWiki {
    private final ArrayList<ValheimPageSummary> pageSummaries;
    public static final String BASE_URL = "https://valheim.fandom.com/wiki/";

    /**
     * Create a list of summaries for all searchable Valheim wiki pages
     */
    public ValheimWiki() {
        this.pageSummaries = fetchPageTitles();
    }

    /**
     * Search the Valheim wiki for pages matching the given query
     *
     * @param query Search query
     * @return List of results
     */
    public ArrayList<ValheimPageSummary> searchWiki(String query) {
        ArrayList<ValheimPageSummary> matches = getMatchingPages(query);
        if(matches.size() == 1) {
            return matches;
        }
        return getFuzzyPages(query);
    }

    /**
     * Get the page summaries of all searchable pages
     *
     * @return Page summaries
     */
    public ArrayList<ValheimPageSummary> getPageSummaries() {
        return pageSummaries;
    }

    /**
     * Get pages where the title matches the given search query
     *
     * @param query Search query
     * @return List of pages matching search query
     */
    public ArrayList<ValheimPageSummary> getMatchingPages(String query) {
        return pageSummaries
                .stream()
                .filter(pageSummary -> pageSummary.getTitle().equalsIgnoreCase(query))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get pages where the title contains the given search query
     *
     * @param query Search query
     * @return List of pages containing search query
     */
    public ArrayList<ValheimPageSummary> getFuzzyPages(String query) {
        return pageSummaries
                .stream()
                .filter(pageSummary -> pageSummary.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Fetch the Valheim wiki page summaries for all categories that will be searched
     *
     * @return Valheim page summaries
     */
    private ArrayList<ValheimPageSummary> fetchPageTitles() {
        ArrayList<ValheimPageSummary> pageTitles = new ArrayList<>();
        pageTitles.addAll(fetchCategoryPageSummaries(CATEGORY.BIOME));
        pageTitles.addAll(fetchCategoryPageSummaries(CATEGORY.CREATURE));
        pageTitles.addAll(fetchItemPageSummaries());
        return pageTitles;
    }

    /**
     * Fetch the summaries of pages in the given category.
     *
     * @param category Category to fetch page summaries for
     * @return List of summaries for pages in the given category
     */
    private ArrayList<ValheimPageSummary> fetchCategoryPageSummaries(CATEGORY category) {
        ArrayList<ValheimPageSummary> pageSummaries = new ArrayList<>();
        Document categoryPage = fetchWikiPage(CATEGORY.getWikiPageUrl(category));
        if(categoryPage == null) {
            return pageSummaries;
        }
        Elements tables = categoryPage.select("table.article-table");
        for(Element table : tables) {
            Elements pages = table
                    .selectFirst("tbody")
                    .select("tr:has(td)");

            for(Element page : pages) {
                Element pageInfo = page.getElementsByAttribute("title").get(0);
                pageSummaries.add(
                        new ValheimPageSummary(
                                pageInfo.attr("title"),
                                pageInfo.absUrl("href"),
                                category
                        )
                );
            }
        }
        return pageSummaries;
    }

    /**
     * Fetch the summaries of all pages in the items category.
     * Items are not displayed in a table as with the other categories and must be parsed differently.
     *
     * @return List of summaries for pages in the items category
     */
    private ArrayList<ValheimPageSummary> fetchItemPageSummaries() {
        ArrayList<ValheimPageSummary> itemPageSummaries = new ArrayList<>();
        Document itemsPage = fetchWikiPage(CATEGORY.getWikiPageUrl(CATEGORY.ITEM));
        if(itemsPage == null) {
            return itemPageSummaries;
        }

        Elements alphabetLetters = itemsPage.getElementsByClass("mw-headline");
        for(Element alphabetLetter : alphabetLetters) {
            Element itemList = alphabetLetter.parent().nextElementSibling();
            if(!itemList.getElementsByClass("mw-empty-elt").isEmpty()) {
                continue;
            }
            Elements items = itemList.children();
            for(Element item : items) {
                Element pageInfo = item.selectFirst("a");
                itemPageSummaries.add(
                        new ValheimPageSummary(
                                pageInfo.text(),
                                pageInfo.absUrl("href"),
                                CATEGORY.ITEM
                        )
                );
            }
        }
        return itemPageSummaries;
    }

    /**
     * Get an item by its page summary
     *
     * @param itemSummary Page summary of item
     * @return Item
     */
    public ValheimItem getItem(ValheimPageSummary itemSummary) {
        Document itemPage = fetchWikiPage(itemSummary.getUrl());
        if(itemPage == null) {
            return null;
        }
        String[] type = getAttributeValue(itemPage, "type");
        TYPE itemType = TYPE.discernType(type == null ? null : type[0]);
        switch(itemType) {
            case FOOD:
            case MEAD:
                return getFoodItem(itemPage, itemSummary, itemType);
            case WORN:
                return getWornItem(itemPage, itemSummary);
            case PICKAXE:
            case AXE:
            case ARROW:
            case BOW:
            case SPEAR:
            case KNIFE:
            case CLUB:
            case SHIELD:
            case TOOL:
                return getWieldedItem(itemPage, itemSummary, itemType);
            case SEED:
            case TROPHY:
            case MISC:
            case MATERIAL:
            case UNKNOWN:
            default:
                return parseItem(itemPage, itemSummary, itemType);
        }
    }

    /**
     * Get a creature by its page summary
     *
     * @param creatureSummary Page summary of creature
     * @return Creature
     */
    public ValheimCreature getCreature(ValheimPageSummary creatureSummary) {
        try {
            Document creaturePage = fetchWikiPage(creatureSummary.getUrl());
            if(creaturePage == null) {
                return null;
            }
            return new ValheimCreature.ValheimCreatureBuilder(
                    creatureSummary,
                    getImageUrl(creaturePage),
                    getDescription(creaturePage),
                    getAttributeValue(creaturePage, "behavior")[0],
                    getAttributeValue(creaturePage, "location"),
                    Integer.parseInt(getAttributeValue(creaturePage, "health 0star")[0]),
                    getAttributeValue(creaturePage, "tameable")[0].equalsIgnoreCase("yes"),
                    getAttributeValue(creaturePage, "damage 0star")
            )
                    .setAbilities(getAttributeValue(creaturePage, "abilities"))
                    .setDrops(getAttributeValue(creaturePage, "drops"))
                    .setImmuneTo(getAttributeValue(creaturePage, "immune"))
                    .setResistantTo(getAttributeValue(creaturePage, "resistant"))
                    .setVeryResistantTo(getAttributeValue(creaturePage, "veryresistant"))
                    .setWeakTo(getAttributeValue(creaturePage, "weak"))
                    .setVeryWeakTo(getAttributeValue(creaturePage, "veryweak"))
                    .setSummonItems(getAttributeValue(creaturePage, "summon"))
                    .build();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse a standard item from the given item wiki page
     *
     * @param itemPage    HTML document of item wiki page
     * @param itemSummary Page summary of item page
     * @return Item
     */
    private ValheimItem parseItem(Document itemPage, ValheimPageSummary itemSummary, TYPE type) {
        try {
            return new ValheimItem(
                    itemSummary,
                    getImageUrl(itemPage),
                    getDescription(itemPage),
                    parseItemStats(itemPage),
                    type
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse an items stats from the given item wiki page
     *
     * @param itemPage HTML document of item wiki page
     * @return Item stats
     */
    private ValheimItem.ItemStats parseItemStats(Document itemPage) {
        String[] crafting = getAttributeValue(itemPage, "materials");
        String[] stack = getAttributeValue(itemPage, "stack");
        String[] teleport = getAttributeValue(itemPage, "teleport");
        String[] cost = getAttributeValue(itemPage, "buy");
        String[] size = getAttributeValue(itemPage, "size");
        String[] weight = getAttributeValue(itemPage, "weight");
        return new ValheimItem.ItemStats(
                stack == null ? -1 : Integer.parseInt(stack[0]),
                teleport == null || teleport[0].equalsIgnoreCase("yes"),
                getAttributeValue(itemPage, "usage"),
                crafting,
                getAttributeValue(itemPage, "source"),
                weight == null ? -1 : Double.parseDouble(weight[0]),
                cost == null ? -1 : Integer.parseInt(cost[0].replace("Coins", "").trim()),
                size == null ? null : size[0]
        );
    }

    /**
     * Get a worn item
     *
     * @param itemPage    HTML document of worn item wiki page
     * @param itemSummary Page summary of worn item
     * @return Worn item
     */
    private ValheimWornItem getWornItem(Document itemPage, ValheimPageSummary itemSummary) {
        try {
            return new ValheimWornItem(
                    itemSummary,
                    getImageUrl(itemPage),
                    getDescription(itemPage),
                    parseItemStats(itemPage),
                    parseArmourStats(itemPage),
                    TYPE.WORN
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a wielded item
     *
     * @param itemPage    HTML document of wielded item wiki page
     * @param itemSummary Page summary of wielded item
     * @param wieldType   Wielded item type - e.g tool etc
     * @return Wielded item
     */
    private ValheimWieldedItem getWieldedItem(Document itemPage, ValheimPageSummary itemSummary, TYPE wieldType) {
        try {
            ValheimWieldedItem.WieldStats wieldStats = parseWieldStats(itemPage);
            return new ValheimWieldedItem(
                    itemSummary,
                    getImageUrl(itemPage),
                    getDescription(itemPage),
                    parseItemStats(itemPage),
                    parseArmourStats(itemPage),
                    wieldStats,
                    wieldType
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse a wielded items stats from the given item wiki page
     *
     * @param itemPage HTML document of wielded item wiki page
     * @return Wielded item stats
     */
    private ValheimWieldedItem.WieldStats parseWieldStats(Document itemPage) {
        ValheimWieldedItem.WieldStats.WieldStatsBuilder builder = new ValheimWieldedItem.WieldStats.WieldStatsBuilder();
        String[] wieldStyle = getAttributeValue(itemPage, "wielding");
        String[] backstab = getAttributeValue(itemPage, "backstab");
        String[] blockPower = getAttributeValue(itemPage, "block power");
        String[] blunt = getAttributeValue(itemPage, "blunt");
        String[] knockback = getAttributeValue(itemPage, "knockback");
        String[] parryBonus = getAttributeValue(itemPage, "parry bonus");
        String[] parryForce = getAttributeValue(itemPage, "parry force");
        String[] pierce = getAttributeValue(itemPage, "pierce");
        String[] slash = getAttributeValue(itemPage, "slash");

        if(backstab != null) {
            builder.addBackStabBonus(backstab[0]);
        }

        if(wieldStyle != null) {
            builder.setWieldStyle(wieldStyle[0]);
        }

        if(blockPower != null) {
            builder.addBlockPowerStat(blockPower[0]);
        }

        if(blunt != null) {
            builder.addBluntStat(blunt[0]);
        }

        if(knockback != null) {
            builder.addKnockBackStat(knockback[0]);
        }

        if(parryBonus != null) {
            builder.addParryBonus(parryBonus[0]);
        }

        if(parryForce != null) {
            builder.addParryForceStat(parryForce[0]);
        }

        if(pierce != null) {
            builder.addPierceStat(pierce[0]);
        }

        if(slash != null) {
            builder.addSlashStat(slash[0]);
        }

        return builder.build();
    }

    /**
     * Parse a worn items armour stats from the given item wiki page
     *
     * @param itemPage HTML document of worn item wiki page
     * @return Item armour stats
     */
    private ValheimWornItem.ArmourStats parseArmourStats(Document itemPage) {
        String[] armour = getAttributeValue(itemPage, "armor");
        String[] crafting = getAttributeValue(itemPage, "crafting level");
        String[] repair = getAttributeValue(itemPage, "repair level");
        String[] durability = getAttributeValue(itemPage, "durability");
        String[] movementSpeed = getAttributeValue(itemPage, "movement speed");
        return new ValheimWornItem.ArmourStats(
                armour == null ? null : armour[0],
                crafting == null || crafting[0].equals("N/A") ? -1 : Integer.parseInt(crafting[0]),
                repair == null || repair[0].equals("N/A") ? -1 : Integer.parseInt(repair[0]),
                durability == null ? null : durability[0],
                movementSpeed == null ? -1 : Integer.parseInt(movementSpeed[0].replace("%", ""))
        );
    }

    /**
     * Get a food item
     *
     * @param foodPage    HTML document of food wiki page
     * @param foodSummary Page summary of food item
     * @param foodType    Food item type - e.g food, mead, etc
     * @return Food item
     */
    private ValheimFood getFoodItem(Document foodPage, ValheimPageSummary foodSummary, TYPE foodType) {
        try {
            ValheimFood.ValheimFoodBuilder builder = new ValheimFood.ValheimFoodBuilder(
                    foodSummary,
                    getImageUrl(foodPage),
                    getDescription(foodPage),
                    parseItemStats(foodPage),
                    foodType
            );

            String[] maxHealth = getAttributeValue(foodPage, "health");
            String[] maxStamina = getAttributeValue(foodPage, "stamina");
            String[] healing = getAttributeValue(foodPage, "healing");

            return builder
                    .setMaxHealth(maxHealth == null ? -1 : Integer.parseInt(maxHealth[0]))
                    .setMaxStamina(maxStamina == null ? -1 : Integer.parseInt(maxStamina[0]))
                    .setHealing(healing == null ? null : healing[0])
                    .setDuration(
                            Integer.parseInt(
                                    getAttributeValue(foodPage, "duration")[0].replace("s", "").trim()
                            )
                    )
                    .setOtherEffects(getAttributeValue(foodPage, "effect"))
                    .build();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find the element with a data-source attribute matching the given name.
     * Return the value(s) that this element represents.
     * The value(s) may be stored in a list or a singular div, possibly separated by commas
     *
     * @param wikiPage HTML document of Valheim wiki page
     * @param name     Attribute value
     * @return Value attribute represents
     */
    private String[] getAttributeValue(Document wikiPage, String name) {
        String attribute = "data-source";
        // TODO find out why "resistant" returns "resistant" AND "veryresistant" for getElementsByAttributeValueMatching
        ArrayList<Element> elements = wikiPage
                .getElementsByAttributeValueMatching("data-source", name)
                .stream()
                .filter(e -> e.attr(attribute).equalsIgnoreCase(name))
                .collect(Collectors.toCollection(ArrayList::new));

        if(elements.isEmpty()) {
            return null;
        }
        // Members of horizontal table - table header containing label & table data containing value
        if(elements.size() == 2) {
            Element value = elements.get(1);
            // Table data contains value
            if(value.childrenSize() == 0) {
                return new String[]{value.text()};
            }
            // Table data holds an unordered list
            return getListValues(value.child(0));
        }
        // Single element with a singular child containing the desired text
        Element valueParent = elements.get(0).child(1);
        if(valueParent.childrenSize() == 0 || valueParent.child(0).childrenSize() == 0) {
            String value = valueParent.text();
            return value.contains(",") ? value.split(",") : new String[]{value};
        }
        // Child is an unordered list - pull all of the values
        return getListValues(valueParent.child(0));
    }

    /**
     * Get an array of values from an HTML unordered list
     *
     * @param list HTML unordered list element
     * @return Array of table values
     */
    private String[] getListValues(Element list) {
        return list
                .children()
                .stream()
                .map(Element::text)
                .toArray(String[]::new);
    }

    /**
     * Get a biome by its page summary
     *
     * @param biomeSummary Page summary of biome
     * @return Biome
     */
    public ValheimBiome getBiome(ValheimPageSummary biomeSummary) {
        Document biomePage = fetchWikiPage(biomeSummary.getUrl());
        if(biomePage == null) {
            return null;
        }
        ValheimBiome.ValheimBiomeBuilder builder = new ValheimBiome.ValheimBiomeBuilder(
                biomeSummary,
                getDescription(biomePage),
                getImageUrl(biomePage)
        );
        Elements biomeSections = biomePage.select("section.pi-group");
        if(biomeSections.isEmpty()) {
            return builder.build();
        }
        for(Element category : biomeSections) {
            HashMap<String, String[]> attributes = ValheimBiome.parseAttributes(category);
            switch(category.selectFirst("h2").text().toLowerCase()) {
                case "creatures":
                    builder.setCreatures(attributes);
                    break;
                case "resources":
                    builder.setResources(attributes);
                    break;
                case "points of interest":
                    builder.setInterestPoints(attributes);
            }
        }
        return builder.build();
    }

    /**
     * Get the image URL for the given wiki page
     *
     * @param wikiPage HTML document of Valheim wiki page
     * @return Page image URL
     */
    public String getImageUrl(Document wikiPage) {
        Elements images = wikiPage.select(".pi-image-thumbnail");
        if(images.isEmpty()) {
            return null;
        }
        int index = images.size() == 1 ? 0 : 1;
        return images.get(index).attr("src");
    }

    /**
     * Get the description for the given wiki page
     *
     * @param wikiPage HTML document of Valheim wiki page
     * @return Page description
     */
    public String getDescription(Document wikiPage) {
        Elements description = wikiPage.select("#Description");
        if(!description.isEmpty()) {
            return description.get(0).parent().nextElementSibling().text();
        }
        Elements imageCaption = wikiPage.select("figcaption.pi-caption");
        return imageCaption.isEmpty() ? "-" : imageCaption.get(0).text();
    }

    /**
     * Fetch the HTML document of a given Valheim wiki page URL
     *
     * @param url Valheim wiki page URL
     * @return HTML document of given wiki page
     */
    private Document fetchWikiPage(String url) {
        try {
            return Jsoup.connect(url).get();
        }
        catch(IOException e) {
            return null;
        }
    }
}
