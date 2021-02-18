package COD.Assets;

import java.util.ArrayList;

/**
 * Weapon/Attachment stat attributes
 */
public class Attributes {
    private final Attribute accuracy, damage, range, firerate, mobility, control;
    private final ArrayList<Attribute> attributes;

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
        this.attributes = builder.attributes;
    }

    public static class AttributesBuilder {
        private final ArrayList<Attribute> attributes = new ArrayList<>();
        private Attribute accuracy, damage, range, firerate, mobility, control;

        /**
         * Set the accuracy stat
         *
         * @param accuracy Accuracy stat - Effect on weapon accuracy
         * @return Builder
         */
        public AttributesBuilder setAccuracyStat(int accuracy) {
            this.accuracy = new Attribute(accuracy, "Accuracy:");
            attributes.add(this.accuracy);
            return this;
        }

        /**
         * Set the damage stat
         *
         * @param damage Damage stat - Effect on weapon damage
         * @return Builder
         */
        public AttributesBuilder setDamageStat(int damage) {
            this.damage = new Attribute(damage, "Damage:");
            attributes.add(this.damage);
            return this;
        }


        /**
         * Set the control stat
         *
         * @param control Control stat - Effect on weapon recoil
         * @return Builder
         */
        public AttributesBuilder setControlStat(int control) {
            this.control = new Attribute(control, "Recoil Control:");
            attributes.add(this.control);
            return this;
        }

        /**
         * Set the range stat
         *
         * @param range Range stat - Effect on weapon effective range
         * @return Builder
         */
        public AttributesBuilder setRangeStat(int range) {
            this.range = new Attribute(range, "Range:");
            attributes.add(this.range);
            return this;
        }

        /**
         * Set the fire rate stat
         *
         * @param firerate Fire rate stat - Effect on weapon fire rate
         * @return Builder
         */
        public AttributesBuilder setFireRateStat(int firerate) {
            this.firerate = new Attribute(firerate, "Fire Rate:");
            attributes.add(this.firerate);
            return this;
        }

        /**
         * Set the mobility stat
         *
         * @param mobility Mobility stat - Effect on weapon mobility
         * @return Builder
         */
        public AttributesBuilder setMobilityStat(int mobility) {
            this.mobility = new Attribute(mobility, "Mobility:");
            attributes.add(this.mobility);
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
     * Get a list of all initialised attributes
     *
     * @return List of initialised attributes
     */
    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Get the accuracy stat - Effect on weapon accuracy
     *
     * @return Accuracy stat
     */
    public Attribute getAccuracyStat() {
        return accuracy;
    }

    /**
     * Get the damage stat - Effect on weapon damage
     *
     * @return Damage stat
     */
    public Attribute getDamageStat() {
        return damage;
    }

    /**
     * Get the control stat - Effect on weapon recoil
     *
     * @return Control stat
     */
    public Attribute getControlStat() {
        return control;
    }

    /**
     * Get the fire rate stat - Effect on weapon fire rate
     *
     * @return Fire rate stat
     */
    public Attribute getFirerateStat() {
        return firerate;
    }

    /**
     * Get the mobility stat - Effect on weapon mobility
     *
     * @return Mobility stat
     */
    public Attribute getMobilityStat() {
        return mobility;
    }

    /**
     * Get the range stat - Effect on weapon effective range
     *
     * @return Range stat
     */
    public Attribute getRangeStat() {
        return range;
    }

    /**
     * Stat attribute
     */
    public static class Attribute {
        private final int value;
        private final String name;

        /**
         * Create the attribute
         *
         * @param value Attribute value e.g 5
         * @param name  Attribute name e.g "Range"
         */
        public Attribute(int value, String name) {
            this.value = value;
            this.name = name;
        }

        /**
         * Get the attribute name
         *
         * @return Attribute name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the attribute value
         *
         * @return Attribute value
         */
        public int getValue() {
            return value;
        }

        /**
         * Format the value in to a String either "x", "+x", or "-x"
         *
         * @return Formatted value
         */
        public String formatValue() {
            if(value == 0) {
                return String.valueOf(value);
            }
            return value < 0 ? String.valueOf(value) : "+" + value;
        }
    }
}
