package Valheim.Wiki;

/**
 * Valheim item details
 */
public class ValheimItem extends ValheimWikiAsset {
    private final TYPE type;
    private final ItemStats itemStats;

    public enum TYPE {
        FOOD,
        SEED,
        WORN,
        MATERIAL,
        MEAD,
        MISC,
        PICKAXE,
        ARROW,
        BOW,
        SPEAR,
        KNIFE,
        CLUB,
        SHIELD,
        AXE,
        TOOL,
        TROPHY,
        UNKNOWN;

        /**
         * Discern the type from the given String
         *
         * @param type Type String
         * @return Type from String
         */
        public static TYPE discernType(String type) {
            if(type == null) {
                return UNKNOWN;
            }
            type = type.toUpperCase();
            try {
                return TYPE.valueOf(type);
            }
            catch(IllegalArgumentException e) {
                switch(type) {
                    case "HEAD":
                    case "BODY":
                    case "LEGS":
                    case "ACCESSORY":
                    case "CAPE":
                        return WORN;
                    default:
                        return UNKNOWN;
                }
            }
        }
    }

    /**
     * Create a Valheim item
     *
     * @param pageSummary Wiki page summary
     * @param imageUrl    URL to item image
     * @param description Item description
     * @param itemStats   Item stats
     * @param type        Item type
     */
    public ValheimItem(ValheimPageSummary pageSummary, String imageUrl, String description, ItemStats itemStats, TYPE type) {
        super(pageSummary, imageUrl, description);
        this.type = type;
        this.itemStats = itemStats;
    }

    /**
     * Get the item type
     *
     * @return Item type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the item stats
     *
     * @return Item stats
     */
    public ItemStats getItemStats() {
        return itemStats;
    }

    /**
     * Item stat values
     */
    public static class ItemStats {
        private final int stack, cost;
        private final boolean teleportable;
        private final String[] usage, craftingMaterials, droppedBy;
        private final double weight;
        private final String size;

        /**
         * Create the item stats
         *
         * @param stack             Item stack size
         * @param teleportable      Item can be carried through teleporter
         * @param usage             Item usage
         * @param craftingMaterials Crafting materials required for item
         * @param droppedBy         Where item is dropped
         * @param weight            Weight of item
         * @param cost              Cost of item (in coins) from the trader
         * @param size              Dimensional size of the item e.g 22x16
         */
        public ItemStats(int stack, boolean teleportable, String[] usage, String[] craftingMaterials, String[] droppedBy, double weight, int cost, String size) {
            this.stack = stack;
            this.teleportable = teleportable;
            this.usage = usage;
            this.craftingMaterials = craftingMaterials;
            this.droppedBy = droppedBy;
            this.weight = weight;
            this.cost = cost;
            this.size = size;
        }

        /**
         * Check if the item has a weight
         *
         * @return Item has weight
         */
        public boolean hasWeight() {
            return weight > -1;
        }

        /**
         * Get the dimensional size of the item - e.g 22x16
         *
         * @return Item size
         */
        public String getSize() {
            return size;
        }

        /**
         * Check if the item has a size
         *
         * @return Item has size
         */
        public boolean hasSize() {
            return size != null;
        }

        /**
         * Check if the item is bought from a trader
         *
         * @return Item is bought
         */
        public boolean isBought() {
            return cost > -1;
        }

        /**
         * Get the cost of the item (in coins) from the trader
         *
         * @return Cost of item
         */
        public int getCost() {
            return cost;
        }

        /**
         * Check if the item has a stack size
         *
         * @return Item has stack size
         */
        public boolean hasStackSize() {
            return stack > -1;
        }

        /**
         * Get the stack size of the item
         *
         * @return Stack size
         */
        public int getStackSize() {
            return stack;
        }

        /**
         * Get the weight of the item
         *
         * @return Weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Get the usage of the item
         *
         * @return Item usage
         */
        public String[] getUsage() {
            return usage;
        }

        /**
         * Return whether the item has any crafting material details
         *
         * @return Item has crafting material details
         */
        public boolean isCraftable() {
            return craftingMaterials != null;
        }

        /**
         * Check whether the item has any usage details
         *
         * @return Item has usage details
         */
        public boolean hasUsage() {
            return usage != null;
        }

        /**
         * Get the drop sources of the item
         *
         * @return Item drop sources
         */
        public String[] getDroppedBy() {
            return droppedBy;
        }

        /**
         * Check if the item has any drop sources
         *
         * @return Item has drop sources
         */
        public boolean isDropped() {
            return droppedBy != null;
        }

        /**
         * Check whether the item can be taken through a teleporter
         *
         * @return Item can be taken through a teleporter
         */
        public boolean isTeleportable() {
            return teleportable;
        }

        /**
         * Get the materials required to craft the item
         *
         * @return Crafting materials
         */
        public String[] getCraftingMaterials() {
            return craftingMaterials;
        }
    }
}
