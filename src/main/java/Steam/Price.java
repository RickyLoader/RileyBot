package Steam;

import java.text.NumberFormat;

/**
 * Steam store price
 */
public class Price {
    private final double price;
    private final String currency;

    /**
     * Create the price
     *
     * @param price    Price
     * @param currency Currency code - e.g "NZD"
     */
    public Price(double price, String currency) {
        this.price = price;
        this.currency = currency;
    }

    /**
     * Get the price
     *
     * @return Price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Get the currency code of the price - e.g "NZD"
     *
     * @return Currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Get the price in the format "$PRICE (CURRENCY CODE)"
     *
     * @return Formatted price
     */
    public String getPriceFormatted() {
        return NumberFormat.getCurrencyInstance().format(price) + " (" + currency + ")";
    }
}
