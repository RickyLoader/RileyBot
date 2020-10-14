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
        ResourceHandler handler = new ResourceHandler();
        this.abilityImage = handler.getImageResource("/LOL/Champions/Abilities/" + image);
        this.buttonImage = handler.getImageResource("/LOL/Champions/Abilities/Order/" + buttonText + ".png");
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