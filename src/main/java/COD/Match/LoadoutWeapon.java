package COD.Match;

import COD.Assets.Attachment;
import COD.Assets.Attributes;
import COD.Assets.Variant;
import COD.Assets.Weapon;

import java.awt.image.BufferedImage;
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
    private final Variant variant;


    /**
     * Create a loadout weapon
     *
     * @param weapon      Weapon
     * @param attachments List of weapon attachments
     * @param variantId   Weapon variant id
     */
    public LoadoutWeapon(Weapon weapon, ArrayList<Attachment> attachments, int variantId) {
        this.weapon = weapon;
        this.attachments = attachments;
        this.attributes = calculateAttributes(attachments);
        this.variant = weapon.getVariantById(variantId);
    }

    /**
     * Get the loadout weapon image.
     * Return either the base weapon image, or the weapon variant image if present.
     *
     * @return Weapon image
     */
    public BufferedImage getImage() {
        return variant == null ? weapon.getImage() : variant.getImage();
    }

    /**
     * Get the name of the loadout weapon.
     * Return the base weapon name - e.g "Combat Knife" with the
     * variant name if present - e.g "Combat Knife (Espionage)"
     *
     * @return Weapon name
     */
    public String getName() {
        return variant == null ? weapon.getName() : weapon.getName() + " (" + variant.getName() + ")";
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

    /**
     * Get the weapon variant
     *
     * @return Weapon variant
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Check if the loadout weapon has a weapon variant
     *
     * @return Loadout weapon has weapon variant
     */
    public boolean hasVariant() {
        return variant != null;
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
        if(!hasVariant() && loadoutWeapon.hasVariant() || hasVariant() && !loadoutWeapon.hasVariant()) {
            return false;
        }
        if(hasVariant() && !variant.equals(loadoutWeapon.getVariant())) {
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
