package LOL.Blitz;


import Command.Structure.CachedImage;

import java.awt.image.BufferedImage;

/**
 * Champion ability
 */
public class Ability {
    private final CachedImage buttonImage, abilityImage;

    /**
     * Create an ability
     *
     * @param buttonText Button to activate ability
     * @param image      Ability image name - JaxLeapStrike.png
     */
    public Ability(String buttonText, String image) {
        this.abilityImage = new CachedImage("src/main/resources/LOL/Champions/Abilities/" + image);
        this.buttonImage = new CachedImage("src/main/resources/LOL/Champions/Abilities/Order/" + buttonText + ".png");
    }

    /**
     * Get the ability button image
     *
     * @return Ability button image
     */
    public BufferedImage getButtonImage() {
        return buttonImage.getImage();
    }

    /**
     * Get the ability image
     *
     * @return Ability image
     */
    public BufferedImage getAbilityImage() {
        return abilityImage.getImage();
    }
}