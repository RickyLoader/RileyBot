package Vape;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Product variant
 */
public class Variant {
    private final double price;
    private final boolean inStock;
    private final HashSet<String> optionValueSet;
    private final ArrayList<String> optionValues;

    /**
     * Create the variant
     *
     * @param price   Price of the variant
     * @param inStock Variant is in stock
     */
    public Variant(double price, boolean inStock) {
        this.price = price;
        this.inStock = inStock;
        this.optionValueSet = new HashSet<>();
        this.optionValues = new ArrayList<>();
    }

    /**
     * Get the list of option values in the variant
     * E.g product variant is a 60ml bottle of 6mg strength e-liquid -> option values are 60ml & 6mg
     *
     * @return List of option values
     */
    public ArrayList<String> getOptionValues() {
        return optionValues;
    }

    /**
     * Add an option value to the set
     *
     * @param optionValue Option value - e.g "6mg"
     */
    public void addOptionValue(String optionValue) {
        optionValue = optionValue.toLowerCase();
        if(optionValueSet.contains(optionValue)){
            return;
        }
        this.optionValueSet.add(optionValue);
        this.optionValues.add(optionValue);
    }

    /**
     * Get the price of the variant
     *
     * @return Variant price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Check if the variant is in stock
     *
     * @return Variant is in stock
     */
    public boolean inStock() {
        return inStock;
    }
}
