package COD.Assets;

/**
 * Hold mode info
 */
public class Mode {
    private final String codename, name;

    /**
     * Create a mode
     *
     * @param codename Mode codename e.g "war"
     * @param name     Mode real name e.g "Team Deathmatch"
     */
    public Mode(String codename, String name) {
        this.codename = codename;
        this.name = name;
    }

    /**
     * Get the mode name
     *
     * @return Mode name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the mode codename e.g "war"
     *
     * @return Mode codename
     */
    public String getCodename() {
        return codename;
    }
}