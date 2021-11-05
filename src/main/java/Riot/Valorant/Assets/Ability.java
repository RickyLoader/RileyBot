package Riot.Valorant.Assets;

import java.awt.image.BufferedImage;

/**
 * Valorant agent ability
 */
public class Ability extends ValorantAsset {

    // Ability IDs (slot names)
    public static final String
            FIRST_BASIC_ABILITY_ID = "ability1",
            SECOND_BASIC_ABILITY_ID = "ability2",
            GRENADE_ABILITY_ID = "grenade",
            ULTIMATE_ABILITY_ID = "ultimate";

    /**
     * Create a Valorant agent ability
     *
     * @param id    ID of the ability, this is the slot name e.g "ability1" or "grenade"
     * @param name  Name of the ability -  e.g "Incendiary"
     * @param image Image of the ability
     */
    public Ability(String id, String name, BufferedImage image) {
        super(id, name, image);
    }
}
