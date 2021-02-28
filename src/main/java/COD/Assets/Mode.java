package COD.Assets;

/**
 * Hold mode info
 */
public class Mode extends CODAsset {

    /**
     * Create a mode
     *
     * @param codename Mode codename e.g "war"
     * @param name     Mode real name e.g "Team Deathmatch"
     */
    public Mode(String codename, String name) {
        super(codename, name, null);
    }
}