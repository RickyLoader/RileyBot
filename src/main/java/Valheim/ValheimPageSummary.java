package Valheim;

import Command.Structure.EmbedHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * Hold a Valheim wiki page summary - title, url, and category
 */
public class ValheimPageSummary {
    private final String title, url;
    private final CATEGORY category;

    public enum CATEGORY {
        BIOME,
        ITEM,
        CREATURE;

        /**
         * Get the URL to the Valheim wiki page displaying all pages in the the given category
         *
         * @param category Category to get URL for e.g - BIOME
         * @return Valheim wiki page URL e.g - https://valheim.fandom.com/wiki/Biomes
         */
        public static String getWikiPageUrl(CATEGORY category) {
            return ValheimWiki.BASE_URL + StringUtils.capitalize(category.name().toLowerCase()) + "s";
        }

        /**
         * Get the colour to use for the page category
         *
         * @return Category colour
         */
        public int getColour() {
            switch(this) {
                case BIOME:
                    return EmbedHelper.BLUE;
                case CREATURE:
                    return EmbedHelper.ORANGE;
                default:
                    return EmbedHelper.GREEN;
            }
        }
    }

    /**
     * Create a Valheim page summary
     *
     * @param title    Page title
     * @param url      Page url
     * @param category Page category
     */
    public ValheimPageSummary(String title, String url, CATEGORY category) {
        this.title = title;
        this.url = url;
        this.category = category;
    }

    /**
     * Get the wiki URL to the page
     *
     * @return Wiki URL to page
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the page title
     *
     * @return Page title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the page category
     *
     * @return Page category
     */
    public CATEGORY getCategory() {
        return category;
    }
}
