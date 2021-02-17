package COD.Match;

import COD.Assets.Perk;
import COD.Assets.TacticalWeapon;
import COD.Assets.Weapon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

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

    /**
     * Check if the loadout contains a primary weapon
     *
     * @return Loadout contains a primary weapon
     */
    public boolean hasPrimary() {
        return primary != null;
    }

    /**
     * Check if the loadout contains a secondary weapon
     *
     * @return Loadout contains a secondary weapon
     */
    public boolean hasSecondary() {
        return secondary != null;
    }

    /**
     * Check if the loadout contains tactical equipment
     *
     * @return Loadout contains tactical equipment
     */
    public boolean hasTactical() {
        return tactical != null;
    }

    /**
     * Check if the loadout contains lethal equipment
     *
     * @return Loadout contains lethal equipment
     */
    public boolean hasLethal() {
        return lethal != null;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Loadout)) {
            return false;
        }
        Loadout loadout = (Loadout) object;
        if(hasTactical() != loadout.hasTactical() || hasLethal() != loadout.hasLethal() || hasPrimary() != loadout.hasPrimary() || hasSecondary() != loadout.hasSecondary() || perks.length != loadout.getPerks().length) {
            return false;
        }
        boolean tactical = !hasTactical() || getTactical().equals(loadout.getTactical());
        boolean lethal = !hasLethal() || getLethal().equals(loadout.getLethal());
        boolean primary = !hasPrimary() || getPrimary().equals(loadout.getPrimary());
        boolean secondary = !hasSecondary() || getSecondary().equals(loadout.getSecondary());

        HashSet<Perk> perks = new HashSet<>(Arrays.asList(this.perks));
        for(Perk perk : loadout.getPerks()) {
            if(!perks.contains(perk)) {
                return false;
            }
        }
        return primary && secondary && lethal && tactical;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tactical, lethal, primary, secondary);
    }
}
