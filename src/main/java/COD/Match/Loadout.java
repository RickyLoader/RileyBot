package COD.Match;

import COD.Assets.Perk;
import COD.Assets.TacticalWeapon;
import COD.Assets.Weapon;

/**
 * Player match loadout
 */
public class Loadout {
    private final LoadoutWeapon primary, secondary;
    private final Weapon lethal;
    private final TacticalWeapon tactical;
    private final Perk[] perks;

    /**
     * Create the player loadout
     *
     * @param primary   Primary weapon
     * @param secondary Secondary weapon
     * @param lethal    Lethal equipment
     * @param tactical  Tactical equipment
     * @param perks     Perks
     */
    public Loadout(LoadoutWeapon primary, LoadoutWeapon secondary, Weapon lethal, TacticalWeapon tactical, Perk[] perks) {
        this.primary = primary;
        this.secondary = secondary;
        this.lethal = lethal;
        this.tactical = tactical;
        this.perks = perks;
    }

    /**
     * Get the loadout perks
     *
     * @return Array of perks
     */
    public Perk[] getPerks() {
        return perks;
    }

    /**
     * Get the player's tactical equipment
     *
     * @return Tactical equipment
     */
    public TacticalWeapon getTactical() {
        return tactical;
    }

    /**
     * Get the player's lethal equipment
     *
     * @return Lethal equipment
     */
    public Weapon getLethal() {
        return lethal;
    }

    /**
     * Get the player's primary weapon
     *
     * @return Primary weapon
     */
    public LoadoutWeapon getPrimary() {
        return primary;
    }

    /**
     * Get the player's secondary weapon
     *
     * @return Secondary weapon
     */
    public LoadoutWeapon getSecondary() {
        return secondary;
    }
}
