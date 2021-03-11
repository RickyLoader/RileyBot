package Stock;

/**
 * Crypto/Stock symbol
 */
public class Symbol {
    private final String symbol, name, id;
    private final boolean crypto;
    private String logoUrl;

    /**
     * Create a symbol
     *
     * @param symbol Stock/crypto ticker symbol e.g "AAPL"
     * @param name   Stock/crypto name e.g "APPLE INC"
     * @param id     Unique id - for stock this is a FIGI identifier, and for crypto it is the Messari asset id
     * @param crypto Crypto currency symbol
     */
    public Symbol(String symbol, String name, String id, boolean crypto) {
        this.symbol = symbol;
        this.name = name;
        this.id = id;
        this.crypto = crypto;
    }

    /**
     * Get the unique identifier of the symbol.
     * This is a FIGI identifier for a stock symbol, and a Messari asset id for crypto
     *
     * @return Symbol id
     */
    public String getId() {
        return id;
    }

    /**
     * Check if the symbol is a crypto currency
     *
     * @return Symbol is a crypto currency
     */
    public boolean isCrypto() {
        return crypto;
    }

    /**
     * Set the logo to use for the symbol
     *
     * @param logoUrl Symbol logo URL
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * Get the symbol name e.g "APPLE INC"
     *
     * @return Symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the logo URL
     *
     * @return Logo URL
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * Get the symbol e.g "AAPL"
     *
     * @return Stock/crypto symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Check if the symbol has a logo URL
     *
     * @return Symbol has logo URL
     */
    public boolean hasLogoUrl() {
        return logoUrl != null;
    }
}
