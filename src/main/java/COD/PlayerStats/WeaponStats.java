package COD.PlayerStats;

import COD.Assets.Weapon;

public abstract class WeaponStats extends AssetStats<Weapon> {
    /**
     * Create the COD weapon stats
     *
     * @param weapon COD weapon
     */
    public WeaponStats(Weapon weapon) {
        super(weapon);
    }
}
