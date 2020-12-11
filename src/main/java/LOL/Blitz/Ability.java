package LOL.Blitz;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * Champion ability
 */
public class Ability {
    private final BufferedImage buttonImage, abilityImage;

    /**
     * Create an ability
     *
     * @param buttonText Button to activate ability
     * @param image      Ability image name - JaxLeapStrike.png
     */
    public Ability(String buttonText, String image) {
        ResourceHandler resourceHandler = new ResourceHandler();
        String path = "/LOL/Champions/Abilities/";
        BufferedImage ability = resourceHandler.getImageResource(path + image);
        this.abilityImage = (ability == null) ? resourceHandler.getImageResource(path + "Default.png") : ability;
        this.buttonImage = resourceHandler.getImageResource(path + "/Order/" + buttonText + ".png");
    }

    /**
     * Get the ability button image
     *
     * @return Ability button image
     */
    public BufferedImage getButtonImage() {
        return buttonImage;
    }

    /**
     * Get the ability image
     *
     * @return Ability image
     */
    public BufferedImage getAbilityImage() {
        return abilityImage;
    }
}