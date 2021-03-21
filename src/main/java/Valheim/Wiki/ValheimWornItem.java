package Valheim.Wiki;

/**
 * Worn item in Valheim - armour & accessories
 */
public class ValheimWornItem extends ValheimItem {
    private final ArmourStats armourStats;

    /**
     * Create a Valheim worn item
     *
     * @param pageSummary Wiki page summary
     * @param imageUrl    URL to item image
     * @param description Item description
     * @param itemStats   Item stats
     * @param armourStats Armour stats
     * @param type        Item type
     */
    public ValheimWornItem(ValheimPageSummary pageSummary, String imageUrl, String description, ItemStats itemStats, ArmourStats armourStats, TYPE type) {
        super(pageSummary, imageUrl, description, itemStats, type);
        this.armourStats = armourStats;
    }

    /**
     * Get the worn item's armour stats
     *
     * @return Armour stats
     */
    public ArmourStats getArmourStats() {
        return armourStats;
    }

    /**
     * Armour stat values
     */
    public static class ArmourStats {
        private final int craftingLevel, repairLevel, movementSpeed;
        private final String armour, durability;

        /**
         * Create the armour stats
         *
         * @param armour        Armour protection by level - displayed as incremental values e.g - 14/16/18/20
         * @param craftingLevel Crafting level required
         * @param repairLevel   Repair level required
         * @param durability    Measure of durability/health - displayed as incremental values e.g - 1000/1200/1400..
         * @param movementSpeed Movement speed modifier (percentage)
         */
        public ArmourStats(String armour, int craftingLevel, int repairLevel, String durability, int movementSpeed) {
            this.armour = armour;
            this.craftingLevel = craftingLevel;
            this.repairLevel = repairLevel;
            this.durability = durability;
            this.movementSpeed = movementSpeed;
        }

        /**
         * Check if the item has a level requirement to craft
         *
         * @return Item has craft requirement
         */
        public boolean hasCraftingRequirement() {
            return craftingLevel > -1;
        }

        /**
         * Check if the item has a level requirement to repair
         *
         * @return Item has repair requirement
         */
        public boolean hasRepairRequirement() {
            return repairLevel > -1;
        }

        /**
         * Check if the item has a modifier to the player's movement speed
         *
         * @return Item has movement speed modifier
         */
        public boolean hasMovementModifier() {
            return movementSpeed != -1;
        }

        /**
         * Check if the item has an armour value
         *
         * @return Item has armour value
         */
        public boolean hasArmour() {
            return armour != null;
        }

        /**
         * Check if the item has a durability value
         *
         * @return Item has durability value
         */
        public boolean hasDurability() {
            return durability != null;
        }

        /**
         * Get the armour protection by level
         *
         * @return Armour protection by level
         */
        public String getArmour() {
            return armour;
        }

        /**
         * Get the level required to craft the item
         *
         * @return Crafting level required
         */
        public int getCraftingLevel() {
            return craftingLevel;
        }

        /**
         * Get the items durability/health by level
         *
         * @return Item durability by level
         */
        public String getDurability() {
            return durability;
        }

        /**
         * Get the movement speed modifier (percentage)
         *
         * @return Movement speed modifier
         */
        public int getMovementSpeed() {
            return movementSpeed;
        }

        /**
         * Get the level required to repair the item
         *
         * @return Repair level required
         */
        public int getRepairLevel() {
            return repairLevel;
        }
    }
}
