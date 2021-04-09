package Bot;

import COD.CWManager;
import COD.MWManager;
import Steam.SteamStore;
import Twitch.TwitchTV;
import Valheim.Wiki.ValheimWiki;

/**
 * Global references
 */
public class GlobalReference {
    public static final MWManager MW_ASSET_MANAGER = new MWManager();
    public static final CWManager CW_ASSET_MANAGER = new CWManager();
    public static final ValheimWiki VALHEIM_WIKI = new ValheimWiki();
    public static final SteamStore STEAM_STORE = new SteamStore();
    public static final TwitchTV TWITCH_TV = new TwitchTV();
}
