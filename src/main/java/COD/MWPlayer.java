package COD;

import Bot.ResourceHandler;

/**
 * Hold information on a player's Modern Warfare stats
 */
public class MWPlayer extends CODPlayer {
    public MWPlayer(String name, String platform) {
        super(name, platform, "modernwarfare", "MW", new ResourceHandler());
    }
}
