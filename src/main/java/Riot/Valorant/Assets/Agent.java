package Riot.Valorant.Assets;

import Bot.ResourceHandler;
import Riot.Valorant.Abilities;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Valorant agent (playable character)
 */
public class Agent extends ValorantAsset {
    private final Role role;
    private final Color colour;
    private final Abilities abilities;

    // Base path to agent images
    public static final String BASE_PATH = ResourceHandler.VALORANT_BASE_PATH + "Agents/";

    // Agent IDs
    public static final String
            BRIMSTONE_ID = "brimstone",
            BREACH_ID = "breach",
            YORU_ID = "yoru",
            JETT_ID = "jett",
            RAZE_ID = "raze",
            REYNA_ID = "reyna",
            SKYE_ID = "skye",
            PHOENIX_ID = "phoenix",
            SOVA_ID = "sova",
            OMEN_ID = "omen",
            CYPHER_ID = "cypher",
            SAGE_ID = "sage",
            KILLJOY_ID = "killjoy",
            VIPER_ID = "viper",
            ASTRA_ID = "astra",
            KAYO_ID = "kayo";

    /**
     * Create a Valorant agent
     *
     * @param id        Unique ID of the agent, usually just the name in lowercase - e.g "brimstone"
     * @param name      Name of the agent - e.g "Brimstone"
     * @param role      Role that the agent plays e.g Initiator
     * @param colour    Optional colour to represent the agent
     * @param abilities Agent abilities
     * @param image     Icon for the agent
     */
    public Agent(String id, String name, Role role, @Nullable Color colour, Abilities abilities, BufferedImage image) {
        super(id, name, image);
        this.role = role;
        this.colour = colour;
        this.abilities = abilities;
    }

    /**
     * Check if the agent has a colour used to represent them
     *
     * @return Agent has colour
     */
    public boolean hasColour() {
        return colour != null;
    }

    /**
     * Get the agent's abilities
     *
     * @return Agent abilities
     */
    public Abilities getAbilities() {
        return abilities;
    }

    /**
     * Get the colour used to represent the agent
     *
     * @return Agent colour
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Get the role that the agent plays, e.g Initiator
     *
     * @return Agent role
     */
    public Role getRole() {
        return role;
    }
}
