package COD.PlayerStats;

import COD.Assets.Weapon;

import java.util.ArrayList;

/**
 * Hold COD player's weapon stats
 */
public class PlayerWeaponStats {
    private final ArrayList<StandardWeaponStats> primaryWeaponStats, secondaryWeaponStats;

    /**
     * Create the player weapon stats.
     * Initialise an empty list for primary weapons and an empty list for secondary weapons.
     */
    public PlayerWeaponStats() {
        this.primaryWeaponStats = new ArrayList<>();
        this.secondaryWeaponStats = new ArrayList<>();
    }

    /**
     * Add the given weapon stats to the appropriate list based on the type of weapon (primary/secondary)
     * Weapons of other types will be ignored.
     *
     * @param weaponStats Weapon stats to add
     */
    public void addWeaponStats(StandardWeaponStats weaponStats) {
        Weapon.TYPE type = weaponStats.getAsset().getType();
        if(type == Weapon.TYPE.PRIMARY) {
            primaryWeaponStats.add(weaponStats);
        }
        else if(type == Weapon.TYPE.SECONDARY) {
            secondaryWeaponStats.add(weaponStats);
        }
    }

    /**
     * Get a list of the player's primary weapon stats
     *
     * @return List of player primary weapon stats
     */
    public ArrayList<StandardWeaponStats> getPrimaryWeaponStats() {
        return primaryWeaponStats;
    }

    /**
     * Get a list of the player's secondary weapon stats
     *
     * @return List of player secondary weapon stats
     */
    public ArrayList<StandardWeaponStats> getSecondaryWeaponStats() {
        return secondaryWeaponStats;
    }
}
