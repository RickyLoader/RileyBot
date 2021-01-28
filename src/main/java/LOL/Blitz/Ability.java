package LOL.Blitz;

import java.awt.image.BufferedImage;

/**
 * Champion ability
 */
public class Ability {
    private final BufferedImage buttonImage, abilityImage;
    public static final String PATH = "/LOL/Champions/Abilities/", ORDER_PATH = PATH + "Order/";

    /**
     * Create an ability
     *
     * @param buttonImage  Image of button used to activate ability
     * @param abilityImage Ability image
     */
    public Ability(BufferedImage buttonImage, BufferedImage abilityImage) {
        this.buttonImage = buttonImage;
        this.abilityImage = abilityImage;
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