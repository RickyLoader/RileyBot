package Vape;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Product option/variant
 */
public class Option {
    private final String name;
    private final ArrayList<Value> values;

    /**
     * Create the product option
     *
     * @param name Option name - e.g "Colour"
     */
    public Option(String name) {
        this.name = StringUtils.capitalize(name.toLowerCase());
        this.values = new ArrayList<>();
    }

    /**
     * Add a value to the option
     *
     * @param value Value to add - e.g "Blue"
     */
    public void addValue(Value value) {
        this.values.add(value);
    }

    /**
     * Get the option name - e.g "Colour"
     *
     * @return Option name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of values for the option - e.g "Black, Gold, Blue"
     *
     * @return List of option values
     */
    public ArrayList<Value> getValues() {
        return values;
    }

    /**
     * Check if the option has any associated values
     *
     * @return Option has values
     */
    public boolean hasValues() {
        return !values.isEmpty();
    }

    /**
     * Option value
     */
    public static class Value {
        private final String name;
        private final boolean available;

        /**
         * Create the option value
         *
         * @param name      Value name - e.g "Blue"
         * @param available Available in stock
         */
        public Value(String name, boolean available) {
            this.name = name;
            this.available = available;
        }

        /**
         * Get the name of the value - e.g "Blue"
         *
         * @return Value name
         */
        public String getName() {
            return name;
        }

        /**
         * Check if the value is available in stock
         *
         * @return Value is available
         */
        public boolean isAvailable() {
            return available;
        }
    }
}
