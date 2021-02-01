package COD.Match;

import COD.Assets.Attachment;
import COD.Assets.Weapon;

import java.util.ArrayList;

/**
 * Loadout weapon with attachments
 */
public class LoadoutWeapon {
    private final Weapon weapon;
    private final ArrayList<Attachment> attachments;

    /**
     * Create a loadout weapon
     *
     * @param weapon      Weapon
     * @param attachments List of weapon attachments
     */
    public LoadoutWeapon(Weapon weapon, ArrayList<Attachment> attachments) {
        this.weapon = weapon;
        this.attachments = attachments;
    }

    /**
     * Get the weapon
     *
     * @return Weapon
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Get the list of attachments on the weapon
     *
     * @return List of attachments
     */
    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }
}
