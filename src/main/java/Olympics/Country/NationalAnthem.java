package Olympics.Country;

/**
 * National anthem of a country
 */
public class NationalAnthem {
    private final String title, composer;

    /**
     * Create a national anthem
     *
     * @param title    Title of the anthem
     * @param composer Composer of the anthem
     */
    public NationalAnthem(String title, String composer) {
        this.title = title;
        this.composer = composer;
    }

    /**
     * Get the title of the anthem
     *
     * @return Anthem title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the composer of the anthem
     *
     * @return Anthem composer
     */
    public String getComposer() {
        return composer;
    }
}
