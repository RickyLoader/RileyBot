package COD.PlayerStats;

import COD.Assets.Weapon;
import org.jetbrains.annotations.NotNull;

/**
 * Hold player weapon stats
 */
public abstract class WeaponStats implements Comparable<WeaponStats> {
    private final Weapon weapon;

    /**
     * Create the weapon stats
     *
     * @param weapon Weapon that the stats pertain to
     */
    public WeaponStats(Weapon weapon) {
        this.weapon = weapon;
    }

    /**
     * Get the weapon that the player stats pertain to
     *
     * @return Weapon
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Get the value used in sorting
     * Tactical - uses
     * Standard, Lethal - kills
     *
     * @return Sort value
     */
    public abstract int getSortValue();

    @Override
    public int compareTo(@NotNull WeaponStats o) {
        return o.getSortValue() - getSortValue();
    }
}
