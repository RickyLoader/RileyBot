package Valheim.Wiki;

/**
 * Base Valheim wiki asset details
 */
public class ValheimWikiAsset {
    private final ValheimPageSummary pageSummary;
    private final String imageUrl, description;

    /**
     * Create a Valheim wiki asset
     *
     * @param pageSummary Summary of wiki page
     * @param imageUrl    URL to asset image
     * @param description Asset description
     */
    public ValheimWikiAsset(ValheimPageSummary pageSummary, String imageUrl, String description) {
        this.pageSummary = pageSummary;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    /**
     * Get the description of the asset
     *
     * @return Asset description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if the wiki asset has an image
     *
     * @return Wiki asset has image
     */
    public boolean hasImageUrl() {
        return imageUrl != null;
    }

    /**
     * Get the URL to the asset image
     *
     * @return URL to asset image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get the summary of the asset's wiki page - title, url, and category
     *
     * @return Summary of wiki page
     */
    public ValheimPageSummary getPageSummary() {
        return pageSummary;
    }
}
