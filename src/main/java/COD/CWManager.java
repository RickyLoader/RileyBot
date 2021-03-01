package COD;

public class CWManager extends CODManager {
    /**
     * Create the CW manager
     */
    public CWManager() {
        super(GAME.CW);
    }

    @Override
    public String getMissingModeImageURL() {
        return "https://i.imgur.com/7Qt239S.png";
    }
}
