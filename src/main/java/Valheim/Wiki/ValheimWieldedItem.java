package Valheim.Wiki;

import java.util.HashMap;

/**
 * Wielded item in Valheim - tools & weapons
 */
public class ValheimWieldedItem extends ValheimWornItem {
    private final WieldStats wieldStats;

    /**
     * Create a Valheim worn item
     *
     * @param pageSummary Wiki page summary
     * @param imageUrl    URL to item image
     * @param description Item description
     * @param itemStats   Item stats
     * @param armourStats Armour stats
     * @param wieldStats  Wielded item stats
     * @param type        Wielded item type - e.g tool
     */
    public ValheimWieldedItem(ValheimPageSummary pageSummary, String imageUrl, String description, ItemStats itemStats, ArmourStats armourStats, WieldStats wieldStats, TYPE type) {
        super(pageSummary, imageUrl, description, itemStats, armourStats, type);
        this.wieldStats = wieldStats;
    }

    /**
     * Get the wielded stats of the item
     *
     * @return Wielded stats
     */
    public WieldStats getWieldStats() {
        return wieldStats;
    }

    /**
     * Wielded stat values
     */
    public static class WieldStats {
        private final String style;
        private final HashMap<String, String> offensive, defensive;

        /**
         * Create the wield stats
         *
         * @param builder Builder to create wield stats from
         */
        private WieldStats(WieldStatsBuilder builder) {
            this.style = builder.style;
            this.offensive = builder.offensive;
            this.defensive = builder.defensive;
        }

        public static class WieldStatsBuilder {
            private final HashMap<String, String> offensive, defensive;
            private String style;

            /**
             * Initialise the builder
             */
            public WieldStatsBuilder() {
                this.offensive = new HashMap<>();
                this.defensive = new HashMap<>();
            }

            /**
             * Set the wield style of the item
             *
             * @param wieldStyle Wielded style - "Two-handed" etc
             */
            public void setWieldStyle(String wieldStyle) {
                this.style = wieldStyle;
            }

            /**
             * Add a blunt stat to the offensive stats
             *
             * @param blunt Blunt stat
             * @return Builder
             */
            public WieldStatsBuilder addBluntStat(String blunt) {
                this.offensive.put("Blunt", blunt);
                return this;
            }

            /**
             * Add a pierce stat to the offensive stats
             *
             * @param pierce Pierce stat
             * @return Builder
             */
            public WieldStatsBuilder addPierceStat(String pierce) {
                this.offensive.put("Pierce", pierce);
                return this;
            }

            /**
             * Add a backstab bonus to the offensive stats
             *
             * @param backStabBonus Backstab bonus
             * @return Builder
             */
            public WieldStatsBuilder addBackStabBonus(String backStabBonus) {
                this.offensive.put("Backstab Bonus", backStabBonus);
                return this;
            }

            /**
             * Add a parry bonus to the defensive stats
             *
             * @param parryBonus Parry bonus
             * @return Builder
             */
            public WieldStatsBuilder addParryBonus(String parryBonus) {
                this.defensive.put("Parry Bonus", parryBonus);
                return this;
            }

            /**
             * Add a block power stat to the defensive stats
             *
             * @param blockPower Block power stat
             * @return Builder
             */
            public WieldStatsBuilder addBlockPowerStat(String blockPower) {
                this.defensive.put("Block Power", blockPower);
                return this;
            }

            /**
             * Add a parry force stat to the defensive stats
             *
             * @param parryForce Parry force stat
             * @return Builder
             */
            public WieldStatsBuilder addParryForceStat(String parryForce) {
                this.defensive.put("Parry Force", parryForce);
                return this;
            }

            /**
             * Add a slash stat to the offensive stats
             *
             * @param slash Slash stat
             * @return Builder
             */
            public WieldStatsBuilder addSlashStat(String slash) {
                this.offensive.put("Slash", slash);
                return this;
            }

            /**
             * Add a knock back stat to the offensive stats
             *
             * @param knockBack Knock back stat
             * @return Builder
             */
            public WieldStatsBuilder addKnockBackStat(String knockBack) {
                this.offensive.put("Knockback", knockBack);
                return this;
            }

            /**
             * Create the wield stats from the builder values
             *
             * @return Wield stats from builder values
             */
            public WieldStats build() {
                return new WieldStats(this);
            }
        }

        /**
         * Check if the wielded item has any offensive stats
         *
         * @return Item has offensive stats
         */
        public boolean hasOffensiveStats() {
            return !offensive.isEmpty();
        }

        /**
         * Get the wield style of the item - e.g "Two-handed" etc
         *
         * @return Wield style
         */
        public String getWieldStyle() {
            return style;
        }

        /**
         * Check if the wielded item has a wield style
         *
         * @return Item has wield style
         */
        public boolean hasWieldStyle() {
            return style != null;
        }

        /**
         * Check if the wielded item has any defensive stats
         *
         * @return Item has defensive stats
         */
        public boolean hasDefensiveStats() {
            return !defensive.isEmpty();
        }

        /**
         * Get a map of offensive stats
         * Map from stat name e.g "Pierce" to stat value e.g "18"
         *
         * @return Map of offensive stats
         */
        public HashMap<String, String> getOffensiveStats() {
            return offensive;
        }

        /**
         * Get a map of defensive stats
         * Map from stat name e.g "Block Power" to stat value e.g "10"
         *
         * @return Map of defensive stats
         */
        public HashMap<String, String> getDefensiveStats() {
            return defensive;
        }
    }
}
