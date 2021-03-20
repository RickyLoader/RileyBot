package Stock;

/**
 * Crypto/stock market info
 */
public class MarketQuote {
    private final double openPrice, highPrice, lowPrice, currentPrice, previousClosePrice, diff, diffPercent;
    private final Symbol symbol;

    /**
     * Create the market quote from the builder
     *
     * @param builder Builder
     */
    private MarketQuote(MarketQuoteBuilder builder) {
        this.openPrice = builder.openPrice;
        this.highPrice = builder.highPrice;
        this.lowPrice = builder.lowPrice;
        this.currentPrice = builder.currentPrice;
        this.previousClosePrice = builder.previousClosePrice;
        this.symbol = builder.symbol;
        double denominator = symbol.isCrypto() ? openPrice : previousClosePrice;
        this.diff = currentPrice - denominator;
        this.diffPercent = Math.abs(diff) / (denominator / 100);
    }

    public static class MarketQuoteBuilder {
        private final Symbol symbol;
        private double openPrice, highPrice, lowPrice, currentPrice, previousClosePrice;

        /**
         * Initialise the builder with a required crypto/stock symbol
         *
         * @param symbol Crypto/stock symbol
         */
        public MarketQuoteBuilder(Symbol symbol) {
            this.symbol = symbol;
        }

        /**
         * Set the closing price of the symbol.
         * For stock, this is the market close price of the previous market day.
         * For crypto, this is the closing price of the given time period (usually equal to the current price)
         *
         * @param previousClosePrice Previous losing price
         * @return Builder
         */
        public MarketQuoteBuilder setPreviousClosePrice(double previousClosePrice) {
            this.previousClosePrice = previousClosePrice;
            return this;
        }

        /**
         * Set the highest price of the day for the symbol
         *
         * @param highPrice Highest price of the day
         * @return Builder
         */
        public MarketQuoteBuilder setHighPrice(double highPrice) {
            this.highPrice = highPrice;
            return this;
        }

        /**
         * Set the lowest price of the day for the symbol
         *
         * @param lowPrice Lowest price of the day
         * @return Builder
         */
        public MarketQuoteBuilder setLowPrice(double lowPrice) {
            this.lowPrice = lowPrice;
            return this;
        }

        /**
         * Set the opening price of the day for the symbol
         *
         * @param openPrice Opening price of the day
         * @return Builder
         */
        public MarketQuoteBuilder setOpenPrice(double openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        /**
         * Set the current price of the symbol
         *
         * @param currentPrice Current price of symbol
         * @return Builder
         */
        public MarketQuoteBuilder setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        /**
         * Build the market quote
         *
         * @return Market quote from builder values
         */
        public MarketQuote build() {
            return new MarketQuote(this);
        }
    }

    /**
     * Check if the market quote has open, high, low, and close data
     *
     * @return Market quote has OHLCV data
     */
    public boolean hasOHLCV() {
        return openPrice > 0 && highPrice > 0 && lowPrice > 0 && previousClosePrice > 0;
    }

    /**
     * Get the symbol that the quote belongs to
     *
     * @return Symbol that quote belongs to
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Get the current price of the symbol
     *
     * @return Current price
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Get the highest price of the day for the symbol
     *
     * @return Highest price
     */
    public double getHighPrice() {
        return highPrice;
    }

    /**
     * Get the lowest price of the day for the symbol
     *
     * @return Lowest price
     */
    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * Get the opening price of the day for the symbol
     *
     * @return Opening price
     */
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * Get the closing price of the symbol.
     * For stock, this is the market close price of the previous market day.
     * For crypto, this is the closing price of the given time period (usually equal to the current price)
     *
     * @return Closing price
     */
    public double getPreviousClosePrice() {
        return previousClosePrice;
    }

    /**
     * Get the monetary difference for the symbol.
     * For stock, this is the difference between the current price and the previous day's market close price.
     * For crypto, this is the difference between the current price and the opening price for the given time period.
     *
     * @return Monetary difference
     */
    public double getDiff() {
        return diff;
    }

    /**
     * Get the monetary difference as a percentage
     * For stock, this is the difference between the current price and the previous day's market close price.
     * For crypto, this is the difference between the current price and the opening price for the given time period.
     *
     * @return Difference percentage
     */
    public double getDiffPercent() {
        return diffPercent;
    }
}
