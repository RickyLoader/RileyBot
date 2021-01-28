package COD.PlayerStats;

import COD.Assets.Ratio;
import COD.Assets.Weapon;

/**
 * Standard weapon player stats
 */
public class StandardWeaponStats extends WeaponStats {
    private final int headshots;
    private final Ratio kd, accuracy;

    /**
     * Create player stats for the given standard weapon (primary/secondary)
     *
     * @param builder Builder
     */
    private StandardWeaponStats(StandardWeaponStatsBuilder builder) {
        super(builder.weapon);
        this.kd = builder.kd;
        this.accuracy = builder.accuracy;
        this.headshots = builder.headshots;
    }

    public static class StandardWeaponStatsBuilder {
        private int headshots;
        private Ratio kd, accuracy;
        private Weapon weapon;

        /**
         * Set the kill death ratio for the weapon stats
         *
         * @param kills  Number of kills with weapon
         * @param deaths Number of deaths with weapon
         * @return Builder
         */
        public StandardWeaponStatsBuilder setKillDeath(int kills, int deaths) {
            this.kd = new Ratio(kills, deaths);
            return this;
        }

        /**
         * Set the accuracy of the weapon stats
         *
         * @param shotsLanded Shots that have hit an enemy
         * @param shotsFired  Total shots that have been fired
         * @return Builder
         */
        public StandardWeaponStatsBuilder setAccuracy(int shotsLanded, int shotsFired) {
            this.accuracy = new Ratio(shotsLanded, shotsFired);
            return this;
        }

        /**
         * Set the number of headshots for the weapon stats
         *
         * @param headshots Weapon headshots
         * @return Builder
         */
        public StandardWeaponStatsBuilder setHeadshots(int headshots) {
            this.headshots = headshots;
            return this;
        }

        /**
         * Set the weapon that the player stats pertain to
         *
         * @param weapon Weapon that player stats pertain to
         * @return Builder
         */
        public StandardWeaponStatsBuilder setWeapon(Weapon weapon) {
            this.weapon = weapon;
            return this;
        }

        /**
         * Build the player weapon stats
         *
         * @return Weapon stats
         */
        public StandardWeaponStats build() {
            return new StandardWeaponStats(this);
        }
    }

    /**
     * Get the headshots of the weapon
     *
     * @return Headshots
     */
    public int getHeadshots() {
        return headshots;
    }

    /**
     * Get the percentage formatted accuracy ratio of the weapon
     *
     * @return Accuracy ratio
     */
    public String getAccuracy() {
        return accuracy.getRatioPercentage();
    }

    /**
     * Get the shots fired
     *
     * @return Shots fired
     */
    public String getShotsFired() {
        return accuracy.formatDenominator();
    }

    /**
     * Get the K/D ratio formatted to 2 decimal places
     *
     * @return K/D ratio
     */
    public String getKd() {
        return kd.formatRatio(kd.getRatio());
    }

    /**
     * Get number of shots hit
     *
     * @return Shots hit
     */
    public String getShotsHit() {
        return accuracy.formatNumerator();
    }

    /**
     * Get number of deaths
     *
     * @return Deaths
     */
    public int getDeaths() {
        return kd.getDenominator();
    }

    /**
     * Get number of kills
     *
     * @return Kills
     */
    public int getKills() {
        return kd.getNumerator();
    }

    /**
     * Get the kills in a formatted String
     *
     * @return Formatted String kills
     */
    public String formatKills() {
        return kd.formatNumerator();
    }

    /**
     * Get the deaths in a formatted String
     *
     * @return Formatted String deaths
     */
    public String formatDeaths() {
        return kd.formatDenominator();
    }

    @Override
    public int getSortValue() {
        return getKills();
    }
}
