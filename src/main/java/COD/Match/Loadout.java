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
     * Create the player loadout from the builder values
     */
    private Loadout(LoadoutBuilder builder) {
        this.primary = builder.primary;
        this.secondary = builder.secondary;
        this.lethal = builder.lethal;
        this.tactical = builder.tactical;
        this.perks = builder.perks;
    }

    public static class LoadoutBuilder {
        private LoadoutWeapon primary, secondary;
        private Weapon lethal;
        private TacticalWeapon tactical;
        private Perk[] perks;

        /**
         * Set the player's primary loadout weapon
         *
         * @param primary Primary loadout weapon
         * @return Builder
         */
        public LoadoutBuilder setPrimaryWeapon(LoadoutWeapon primary) {
            this.primary = primary;
            return this;
        }

        /**
         * Set the player's secondary loadout weapon
         *
         * @param secondary Secondary loadout weapon
         * @return Builder
         */
        public LoadoutBuilder setSecondaryWeapon(LoadoutWeapon secondary) {
            this.secondary = secondary;
            return this;
        }

        /**
         * Set the player's lethal equipment
         *
         * @param lethal Lethal equipment
         * @return Builder
         */
        public LoadoutBuilder setLethalEquipment(Weapon lethal) {
            this.lethal = lethal;
            return this;
        }

        /**
         * Set the player's tactical equipment
         *
         * @param tactical Tactical equipment
         * @return Builder
         */
        public LoadoutBuilder setTacticalEquipment(TacticalWeapon tactical) {
            this.tactical = tactical;
            return this;
        }

        /**
         * Set the player's loadout perks
         *
         * @param perks Array of perks
         * @return Builder
         */
        public LoadoutBuilder setPerks(Perk[] perks) {
            this.perks = perks;
            return this;
        }

        /**
         * Build the loadout from the provided values
         *
         * @return Loadout
         */
        public Loadout build() {
            return new Loadout(this);
        }
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
