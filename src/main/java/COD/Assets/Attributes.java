package COD.Assets;

/**
 * Weapon/Attachment stat attributes
 */
public class Attributes {
    private final int accuracy, damage, range, firerate, mobility, control;

    /**
     * Create the Attributes from the builder values
     *
     * @param builder Builder to create Attributes from
     */
    private Attributes(AttributesBuilder builder) {
        this.accuracy = builder.accuracy;
        this.damage = builder.damage;
        this.range = builder.range;
        this.firerate = builder.firerate;
        this.mobility = builder.mobility;
        this.control = builder.control;
    }

    public static class AttributesBuilder {
        private int accuracy, damage, range, firerate, mobility, control;

        /**
         * Set the accuracy stat
         *
         * @param accuracy Accuracy stat - Effect on weapon accuracy
         * @return Builder
         */
        public AttributesBuilder setAccuracyStat(int accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        /**
         * Set the damage stat
         *
         * @param damage Damage stat - Effect on weapon damage
         * @return Builder
         */
        public AttributesBuilder setDamageStat(int damage) {
            this.damage = damage;
            return this;
        }


        /**
         * Set the control stat
         *
         * @param control Control stat - Effect on weapon recoil
         * @return Builder
         */
        public AttributesBuilder setControlStat(int control) {
            this.control = control;
            return this;
        }

        /**
         * Set the range stat
         *
         * @param range Range stat - Effect on weapon effective range
         * @return Builder
         */
        public AttributesBuilder setRangeStat(int range) {
            this.range = range;
            return this;
        }

        /**
         * Set the fire rate stat
         *
         * @param firerate Fire rate stat - Effect on weapon fire rate
         * @return Builder
         */
        public AttributesBuilder setFireRateStat(int firerate) {
            this.firerate = firerate;
            return this;
        }

        /**
         * Set the mobility stat
         *
         * @param mobility Mobility stat - Effect on weapon mobility
         * @return Builder
         */
        public AttributesBuilder setMobilityStat(int mobility) {
            this.mobility = mobility;
            return this;
        }

        /**
         * Create the Attributes from the builder values
         *
         * @return Attributes from builder values
         */
        public Attributes build() {
            return new Attributes(this);
        }
    }

    /**
     * Get the accuracy stat - Effect on weapon accuracy
     *
     * @return Accuracy stat
     */
    public int getAccuracyStat() {
        return accuracy;
    }

    /**
     * Get the damage stat - Effect on weapon damage
     *
     * @return Damage stat
     */
    public int getDamageStat() {
        return damage;
    }

    /**
     * Get the control stat - Effect on weapon recoil
     *
     * @return Control stat
     */
    public int getControlStat() {
        return control;
    }

    /**
     * Get the fire rate stat - Effect on weapon fire rate
     *
     * @return Fire rate stat
     */
    public int getFirerateStat() {
        return firerate;
    }

    /**
     * Get the mobility stat - Effect on weapon mobility
     *
     * @return Mobility stat
     */
    public int getMobilityStat() {
        return mobility;
    }

    /**
     * Get the range stat - Effect on weapon effective range
     *
     * @return Range stat
     */
    public int getRangeStat() {
        return range;
    }

    /**
     * Format an attribute in to a String either "x", "+x", or "-x"
     *
     * @param attribute Attribute to format
     * @return Formatted attribute
     */
    public static String formatAttribute(int attribute) {
        if(attribute == 0) {
            return String.valueOf(attribute);
        }
        return attribute < 0 ? String.valueOf(attribute) : "+" + attribute;
    }
}
