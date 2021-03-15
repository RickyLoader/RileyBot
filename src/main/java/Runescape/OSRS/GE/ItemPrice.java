package Runescape.OSRS.GE;

import java.util.Date;

/**
 * Latest Grand Exchange price info for an item - most recent high & low prices
 */
public class ItemPrice {
    private final Item item;
    private final Price high, low;
    private final long dailyVolume;

    /**
     * Create an item price
     *
     * @param item        Item which prices belong to
     * @param high        Most recent high buy price seen
     * @param low         Most recent low sell price seen
     * @param dailyVolume Daily trade volume
     */
    public ItemPrice(Item item, Price high, Price low, long dailyVolume) {
        this.item = item;
        this.high = high;
        this.low = low;
        this.dailyVolume = dailyVolume;
    }

    /**
     * Get the amount of the item traded in the last day
     *
     * @return Daily trade volume
     */
    public long getDailyVolume() {
        return dailyVolume;
    }

    /**
     * Get the item
     *
     * @return Item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Get the most recent high buy price of the item
     *
     * @return Most recent high buy price
     */
    public Price getHigh() {
        return high;
    }

    /**
     * Get the most recent low sell price of the item
     *
     * @return Most recent low sell price
     */
    public Price getLow() {
        return low;
    }

    /**
     * Transaction price & date pair
     */
    public static class Price {
        private final long price;
        private final Date date;

        /**
         * Create a price
         *
         * @param price Price
         * @param date  Date of transaction
         */
        public Price(long price, Date date) {
            this.price = price;
            this.date = date;
        }

        /**
         * Get the transaction date
         *
         * @return Transaction date
         */
        public Date getDate() {
            return date;
        }

        /**
         * Get the price
         *
         * @return Price
         */
        public long getPrice() {
            return price;
        }
    }
}
