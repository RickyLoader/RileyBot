package COD.Match;

import COD.Assets.Attachment;
import COD.Assets.Weapon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LoadoutWeapon)) {
            return false;
        }
        LoadoutWeapon loadoutWeapon = (LoadoutWeapon) obj;
        if(!weapon.equals(loadoutWeapon.getWeapon())) {
            return false;
        }
        if(attachments.size() != loadoutWeapon.getAttachments().size()) {
            return false;
        }
        HashSet<Attachment> attachments = new HashSet<>(this.attachments);
        for(Attachment attachment : loadoutWeapon.getAttachments()) {
            if(!attachments.contains(attachment)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weapon, attachments);
    }
}
