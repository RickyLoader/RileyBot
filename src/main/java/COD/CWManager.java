package COD;

public class CWManager extends CODManager {
    private static CWManager instance = null;
    public static String THUMBNAIL = "https://i.imgur.com/0uCij2q.png";

    /**
     * Create the CW manager
     */
    private CWManager() {
        super(GAME.CW);
    }

    /**
     * Get an instance of the CWManager class
     *
     * @return Instance
     */
    public static CWManager getInstance() {
        if(instance == null) {
            instance = new CWManager();
        }
        return instance;
    }

    @Override
    public String getMissingModeImageURL() {
        return "https://i.imgur.com/7Qt239S.png";
    }
}
