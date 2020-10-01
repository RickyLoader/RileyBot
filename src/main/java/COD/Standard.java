package COD;

import COD.CODPlayer.Ratio;

/**
 * Standard weapon
 */
public class Standard extends Weapon {
    private final int headshots;
    private final Ratio kd, accuracy;

    /**
     * Create a weapon
     *
     * @param iwName    Infinity Ward name of weapon e.g "iw8_me_akimboblunt"
     * @param name      Real name of weapon e.g "Kali Sticks"
     * @param category  Infinity Ward name of weapon category e.g "weapon_melee"
     * @param type      Type of weapon
     * @param res       Resource location
     * @param kd        Kill/Death ratio of weapon
     * @param accuracy  Shots Hit/Shots Fired ratio of weapon
     * @param headshots Number of headshots with weapon
     */
    public Standard(String iwName, String name, String category, TYPE type, String res, Ratio kd, Ratio accuracy, int headshots) {
        super(iwName, name, category, type, res);
        this.kd = kd;
        this.accuracy = accuracy;
        this.headshots = headshots;
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
