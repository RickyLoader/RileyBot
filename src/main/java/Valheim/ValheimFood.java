package Valheim;

/**
 * Valheim food details
 */
public class ValheimFood extends ValheimItem {
    private final int maxHealth, maxStamina, duration;
    private final String[] otherEffects;
    private final String healing;

    /**
     * Create a Valheim food from the given builder
     *
     * @param builder Builder to use values from
     */
    private ValheimFood(ValheimFoodBuilder builder) {
        super(builder.pageSummary, builder.imageUrl, builder.description, builder.itemStats, builder.type);
        this.maxHealth = builder.maxHealth;
        this.maxStamina = builder.maxStamina;
        this.duration = builder.duration;
        this.healing = builder.healing;
        this.otherEffects = builder.otherEffects;
    }

    public static class ValheimFoodBuilder {
        private final ItemStats itemStats;
        private final ValheimPageSummary pageSummary;
        private final String imageUrl, description;
        private final TYPE type;
        private int maxHealth, maxStamina, duration;
        private String[] otherEffects;
        private String healing;

        /**
         * Initialise the builder
         *
         * @param pageSummary Summary of wiki page
         * @param description Food description
         * @param imageUrl    URL to asset image
         * @param type        Food item type - food, mead, etc
         */
        public ValheimFoodBuilder(ValheimPageSummary pageSummary, String imageUrl, String description, ItemStats itemStats, TYPE type) {
            this.pageSummary = pageSummary;
            this.imageUrl = imageUrl;
            this.description = description;
            this.itemStats = itemStats;
            this.type = type;
        }

        /**
         * Set the healing details of the food.
         * May be "50HP/10s" or "3 hp/tick" etc
         *
         * @param healing Healing details
         * @return Builder
         */
        public ValheimFoodBuilder setHealing(String healing) {
            this.healing = healing;
            return this;
        }

        /**
         * Set the healing duration of the food
         *
         * @param duration Healing duration (seconds)
         * @return Builder
         */
        public ValheimFoodBuilder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Set the other effects of the food
         *
         * @param otherEffects Other effects
         * @return Builder
         */
        public ValheimFoodBuilder setOtherEffects(String[] otherEffects) {
            this.otherEffects = otherEffects;
            return this;
        }

        /**
         * Set the max stamina recovered by the food
         *
         * @param maxStamina Max stamina recovered
         * @return Builder
         */
        public ValheimFoodBuilder setMaxStamina(int maxStamina) {
            this.maxStamina = maxStamina;
            return this;
        }

        /**
         * Set the max health healed by the food
         *
         * @param maxHealth Max health healed
         * @return Builder
         */
        public ValheimFoodBuilder setMaxHealth(int maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }

        /**
         * Build a Valheim food from the builder values
         *
         * @return Valheim food
         */
        public ValheimFood build() {
            return new ValheimFood(this);
        }
    }

    /**
     * Get the healing details of the food.
     * May be "50HP/10s" or "3 hp/tick" etc
     *
     * @return Healing details
     */
    public String getHealing() {
        return healing;
    }

    /**
     * Get the healing duration of the food
     *
     * @return Healing duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the max stamina recovered by the food
     *
     * @return Max stamina recovered
     */
    public int getMaxStamina() {
        return maxStamina;
    }

    /**
     * Get the max health healed by the food
     *
     * @return Max health healed
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Check if the food has any other effects
     *
     * @return Food has other effects
     */
    public boolean hasOtherEffects() {
        return otherEffects != null;
    }

    /**
     * Get the other effects the food has
     *
     * @return Other effects
     */
    public String[] getOtherEffects() {
        return otherEffects;
    }

    /**
     * Check if the food has a max stamina value
     *
     * @return Food has max stamina value
     */
    public boolean hasMaxStamina() {
        return maxStamina > -1;
    }

    /**
     * Check if the food has a max health value
     *
     * @return Food has max health value
     */
    public boolean hasMaxHealth() {
        return maxHealth > -1;
    }

    /**
     * Check if the food has any healing details
     *
     * @return Food has healing details
     */
    public boolean hasHealingDetails() {
        return healing != null;
    }
}
