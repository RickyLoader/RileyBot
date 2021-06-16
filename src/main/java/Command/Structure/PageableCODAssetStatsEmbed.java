package Command.Structure;

import COD.Assets.CODAsset;
import COD.PlayerStats.*;

import java.util.List;

/**
 * Page through COD assets
 */
public abstract class PageableCODAssetStatsEmbed extends PageableTableEmbed {

    /**
     * Display a list of player COD asset stats in an embedded message that can be paged through with buttons
     * and displays as a table.
     *
     * @param context     Command context
     * @param items       List of player's available COD asset stats to be displayed
     * @param title       Title to use for the embed
     * @param thumbnail   Thumbnail to use for the embed
     * @param footer      Footer to use in the embed
     * @param description Description to use in the embed
     */
    public PageableCODAssetStatsEmbed(CommandContext context, List<AssetStats<? extends CODAsset>> items, String title, String thumbnail, String footer, String description) {
        super(
                context,
                items,
                thumbnail,
                title,
                description,
                footer,
                new String[]{"Name", "Type"},
                5,
                items.isEmpty() ? EmbedHelper.RED : EmbedHelper.FIRE_ORANGE
        );
    }

    @Override
    public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
        AssetStats<? extends CODAsset> assetStats = (AssetStats<? extends CODAsset>) items.get(index);
        return new String[]{assetStats.getAsset().getName(), getStatsType(assetStats)};
    }

    /**
     * Get the stats type for the given asset stats.
     * E.g "Killstreak" or "Weapon" etc
     *
     * @param assetStats Asset stats to get type for
     * @return Name of stats type
     */
    private String getStatsType(AssetStats<? extends CODAsset> assetStats) {
        if(assetStats instanceof FieldUpgradeStats) {
            return "Field Upgrade";
        }
        else if(assetStats instanceof CommendationStats) {
            return "Commendation";
        }
        else if(assetStats instanceof KillstreakStats) {
            return "Killstreak";
        }
        else if(assetStats instanceof LethalStats) {
            return "Lethal";
        }
        else if(assetStats instanceof TacticalStats) {
            return "Tactical";
        }
        // Standard Weapon
        else {
            return "Weapon";
        }
    }
}
