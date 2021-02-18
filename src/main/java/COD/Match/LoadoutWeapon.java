package COD.Match;

import COD.Assets.Attachment;
import COD.Assets.Attributes;
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
    private final Attributes attributes;

    /**
     * Create a loadout weapon
     *
     * @param weapon      Weapon
     * @param attachments List of weapon attachments
     */
    public LoadoutWeapon(Weapon weapon, ArrayList<Attachment> attachments) {
        this.weapon = weapon;
        this.attachments = attachments;
        this.attributes = calculateAttributes(attachments);
    }

    /**
     * Calculate the attributes for the weapon with the given attachments
     *
     * @param attachments List of attachments
     * @return Weapon attributes (Combined effect of attachments)
     */
    private Attributes calculateAttributes(ArrayList<Attachment> attachments) {
        int accuracy = 0, damage = 0, range = 0, firerate = 0, mobility = 0, control = 0;
        for(Attachment attachment : attachments) {
            if(attachment.getCategory() == Attachment.CATEGORY.UNKNOWN) {
                continue;
            }
            Attributes attachmentAttributes = attachment.getAttributes();
            accuracy += attachmentAttributes.getAccuracyStat().getValue();
            damage += attachmentAttributes.getDamageStat().getValue();
            range += attachmentAttributes.getRangeStat().getValue();
            firerate += attachmentAttributes.getFirerateStat().getValue();
            mobility += attachmentAttributes.getMobilityStat().getValue();
            control += attachmentAttributes.getControlStat().getValue();
        }
        return new Attributes.AttributesBuilder()
                .setAccuracyStat(accuracy)
                .setDamageStat(damage)
                .setRangeStat(range)
                .setFireRateStat(firerate)
                .setMobilityStat(mobility)
                .setControlStat(control)
                .build();
    }

    /**
     * Get the attributes for the loadout weapon (combined effect of attachments)
     *
     * @return Attributes
     */
    public Attributes getAttributes() {
        return attributes;
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
