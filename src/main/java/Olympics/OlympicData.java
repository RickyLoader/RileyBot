package Olympics;

/**
 * Details about various Olympic metrics e.g sports, athletes etc
 */
public class OlympicData {
    private final String name, code;

    /**
     * Create the Olympic data
     *
     * @param code Unique code for data e.g athlete ID
     * @param name Data name e.g athlete name
     */
    public OlympicData(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Get the unique code for the data e.g athlete ID
     *
     * @return Unique data code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the name for the data e.g athlete name
     *
     * @return Data name
     */
    public String getName() {
        return name;
    }

    /**
     * Get a summary String of the Olympic data.
     * E.g for an athlete this would be in the format "athlete name (athlete id)"
     *
     * @return Summary String
     */
    public String getSummary() {
        return name + " (" + code + ")";
    }
}
