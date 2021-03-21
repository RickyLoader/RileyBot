package Valheim.Wiki;

/**
 * Valheim creature details
 */
public class ValheimCreature extends ValheimWikiAsset {
    private final String behaviour;
    private final boolean tameable;
    private final String[] locations, drops, abilities, weakTo, veryResistantTo, resistantTo, immuneTo, veryWeakTo, summonItems, damage;
    private final int health;

    /**
     * Create a Valheim creature from the given builder
     *
     * @param builder Builder to use values from
     */
    public ValheimCreature(ValheimCreatureBuilder builder) {
        super(builder.pageSummary, builder.imageUrl, builder.description);
        this.behaviour = builder.behaviour;
        this.weakTo = builder.weakTo;
        this.resistantTo = builder.resistantTo;
        this.immuneTo = builder.immuneTo;
        this.veryWeakTo = builder.veryWeakTo;
        this.tameable = builder.tameable;
        this.locations = builder.locations;
        this.drops = builder.drops;
        this.damage = builder.damage;
        this.abilities = builder.abilities;
        this.health = builder.health;
        this.summonItems = builder.summonItems;
        this.veryResistantTo = builder.veryResistantTo;
    }

    public static class ValheimCreatureBuilder {
        private final ValheimPageSummary pageSummary;
        private final String description, imageUrl, behaviour;
        private final int health;
        private final String[] locations, damage;
        private final boolean tameable;
        private String[] drops, abilities, weakTo, resistantTo, immuneTo, veryWeakTo, summonItems, veryResistantTo;

        /**
         * Initialise the builder
         *
         * @param pageSummary Summary of wiki page
         * @param description Creature description
         * @param imageUrl    URL to asset image
         * @param behaviour   Creature hostility behaviour - e.g Aggressive
         * @param locations   Array of creature spawn locations
         * @param health      Creature health
         * @param tameable    Creature can be tamed
         * @param damage      Damage values e.g 60 Blunt, 35 Pierce, etc
         */
        public ValheimCreatureBuilder(ValheimPageSummary pageSummary, String imageUrl, String description, String behaviour, String[] locations, int health, boolean tameable, String[] damage) {
            this.pageSummary = pageSummary;
            this.imageUrl = imageUrl;
            this.description = description;
            this.behaviour = behaviour;
            this.locations = locations;
            this.health = health;
            this.tameable = tameable;
            this.damage = damage;
        }

        /**
         * Set the items required to summon the creature
         *
         * @param summonItems Items required to summon
         * @return Builder
         */
        public ValheimCreatureBuilder setSummonItems(String[] summonItems) {
            this.summonItems = summonItems;
            return this;
        }

        /**
         * Set the resistance of the creature e.g Blunt
         *
         * @param resistantTo Array of resistances
         * @return Builder
         */
        public ValheimCreatureBuilder setResistantTo(String[] resistantTo) {
            this.resistantTo = resistantTo;
            return this;
        }

        /**
         * Set the extreme resistance of the creature e.g Blunt
         *
         * @param veryResistantTo Array of resistances
         * @return Builder
         */
        public ValheimCreatureBuilder setVeryResistantTo(String[] veryResistantTo) {
            this.veryResistantTo = veryResistantTo;
            return this;
        }

        /**
         * Set the immunity of the creature e.g Spirit
         *
         * @param immuneTo Array of immunities
         * @return Builder
         */
        public ValheimCreatureBuilder setImmuneTo(String[] immuneTo) {
            this.immuneTo = immuneTo;
            return this;
        }

        /**
         * Set the creature's drops
         *
         * @param drops Array of creature drops
         * @return Builder
         */
        public ValheimCreatureBuilder setDrops(String[] drops) {
            this.drops = drops;
            return this;
        }

        /**
         * Set the creature's abilities
         *
         * @param abilities Array of creature abilities
         * @return Builder
         */
        public ValheimCreatureBuilder setAbilities(String[] abilities) {
            this.abilities = abilities;
            return this;
        }

        /**
         * Set the creature weakness e.g Pierce
         *
         * @param weakTo Array of weaknesses
         * @return Builder
         */
        public ValheimCreatureBuilder setWeakTo(String[] weakTo) {
            this.weakTo = weakTo;
            return this;
        }

        /**
         * Set the creature's extreme weakness e.g Pierce
         *
         * @param veryWeakTo Array of weaknesses
         * @return Builder
         */
        public ValheimCreatureBuilder setVeryWeakTo(String[] veryWeakTo) {
            this.veryWeakTo = veryWeakTo;
            return this;
        }

        /**
         * Build a Valheim creature from the builder values
         *
         * @return Valheim creature
         */
        public ValheimCreature build() {
            return new ValheimCreature(this);
        }
    }

    /**
     * Get the items required to summon the creature
     *
     * @return Items required to summon creature
     */
    public String[] getSummonItems() {
        return summonItems;
    }

    /**
     * Get the creature hostility - Aggressive/passive
     *
     * @return Creature hostility behaviour
     */
    public String getBehaviour() {
        return behaviour;
    }

    /**
     * Check whether the creature can be tamed
     *
     * @return Creature can be tamed
     */
    public boolean isTameable() {
        return tameable;
    }

    /**
     * Get the health of the creature
     *
     * @return Creature health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Get the immunity of the creature e.g Spirit
     *
     * @return Immunity
     */
    public String[] getImmuneTo() {
        return immuneTo;
    }

    /**
     * Get the resistance of the creature e.g Blunt
     *
     * @return Resistance
     */
    public String[] getResistantTo() {
        return resistantTo;
    }

    /**
     * Get the creature extreme weakness e.g Pierce
     *
     * @return Creature extreme weakness
     */
    public String[] getVeryWeakTo() {
        return veryWeakTo;
    }

    /**
     * Get the creature weakness e.g Pierce
     *
     * @return Creature weakness
     */
    public String[] getWeakTo() {
        return weakTo;
    }

    /**
     * Get the creature's abilities e.g Punch, ground slam, etc
     *
     * @return Creature abilities
     */
    public String[] getAbilities() {
        return abilities;
    }

    /**
     * Get the creature extreme resistance e.g Pierce
     *
     * @return Creature extreme resistance
     */
    public String[] getVeryResistantTo() {
        return veryResistantTo;
    }

    /**
     * Get the damage values e.g 60 Blunt, 35 Pierce, etc
     *
     * @return Damage values
     */
    public String[] getDamage() {
        return damage;
    }

    /**
     * Get the creature drops e.g Coins, Troll hide, etc
     *
     * @return Creature drops
     */
    public String[] getDrops() {
        return drops;
    }

    /**
     * Get an array of creature spawn locations
     *
     * @return Creature spawn locations
     */
    public String[] getLocations() {
        return locations;
    }

    /**
     * Check if the creature has a specific weakness
     *
     * @return Creature has a weakness
     */
    public boolean hasWeakness() {
        return weakTo != null;
    }

    /**
     * Check if the creature has an extreme (very weak to) weakness
     *
     * @return Creature has extreme weakness
     */
    public boolean hasExtremeWeakness() {
        return veryWeakTo != null;
    }

    /**
     * Check if the creature has an extreme (very resistant to) resistance
     *
     * @return Creature has extreme resistance
     */
    public boolean hasExtremeResistance() {
        return veryResistantTo != null;
    }

    /**
     * Check if the creature has a specific resistance
     *
     * @return Creature has a resistance
     */
    public boolean hasResistance() {
        return resistantTo != null;
    }

    /**
     * Check if the creature has a specific immunity
     *
     * @return Creature has an immunity
     */
    public boolean hasImmunity() {
        return immuneTo != null;
    }

    /**
     * Check if the creature has any drops
     *
     * @return Creature has drops
     */
    public boolean hasDrops() {
        return drops != null;
    }

    /**
     * Check if the creature has any abilities
     *
     * @return Creature has abilities
     */
    public boolean hasAbilities() {
        return abilities != null;
    }

    /**
     * Check if the creature can be summoned
     *
     * @return Creature can be summoned
     */
    public boolean isSummonable() {
        return summonItems != null;
    }
}
