package COD.API;

import Bot.DiscordUser;
import Bot.FontManager;

public class CWManager extends CODManager {
    private static CWManager instance = null;
    public static String THUMBNAIL = "https://i.imgur.com/0uCij2q.png";

    /**
     * Create the CW manager
     */
    private CWManager() {
        super("Cold War", DiscordUser.CW, "CW", FontManager.COLD_WAR_FONT);
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
