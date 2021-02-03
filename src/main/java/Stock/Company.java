package Stock;

/**
 * Publicly traded company
 */
public class Company {
    private final String symbol, name;
    private String logoURL;

    /**
     * Create a company
     *
     * @param symbol Stock ticker symbol e.g "AAPL"
     * @param name   Company name e.g "APPLE INC"
     */
    public Company(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    /**
     * Set the URL to the company logo
     *
     * @param logoURL URL to company logo
     */
    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    /**
     * Check if the company has a logo URL
     *
     * @return Company has logo URL
     */
    public boolean hasLogoURL() {
        return logoURL != null;
    }

    /**
     * Get the logo URL for the company
     *
     * @return URL to logo image
     */
    public String getLogoURL() {
        return logoURL;
    }

    /**
     * Get the company name e.g "APPLE INC"
     *
     * @return Company name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the company stock ticker symbol e.g "AAPL"
     *
     * @return Company symbol
     */
    public String getSymbol() {
        return symbol;
    }
}
