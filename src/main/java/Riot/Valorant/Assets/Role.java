package Riot.Valorant.Assets;

import java.awt.image.BufferedImage;

/**
 * Valorant agent role - e.g Initiator
 */
public class Role extends ValorantAsset {

    // Base path to role images
    public static final String BASE_PATH = Agent.BASE_PATH + "Roles/";

    // Role IDs
    public static final String
            CONTROLLER_ID = "controller",
            INITIATOR_ID = "initiator",
            SENTINEL_ID = "sentinel",
            DUELIST_ID = "duelist";

    /**
     * Create an agent role
     *
     * @param name  Role name - e.g "Initiator"
     * @param image Icon for the role
     */
    public Role(String name, BufferedImage image) {
        super(name, image);
    }
}
