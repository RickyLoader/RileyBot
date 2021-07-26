package Olympics.Athlete;

import Olympics.Medal.MedalCount;

/**
 * Pairing of Olympic athlete and a medal count
 */
public class AthleteMedalCount {
    private final Athlete athlete;
    private final MedalCount medalCount;

    /**
     * Create an athlete medal count
     *
     * @param athlete    Athlete which medals belong to
     * @param medalCount Count of each type of medal for the athlete
     */
    public AthleteMedalCount(Athlete athlete, MedalCount medalCount) {
        this.athlete = athlete;
        this.medalCount = medalCount;
    }

    /**
     * Get the count of each type of medal for the athlete
     *
     * @return Medal count
     */
    public MedalCount getMedalCount() {
        return medalCount;
    }

    /**
     * Get the athlete which the medal count belong to
     *
     * @return Athlete
     */
    public Athlete getAthlete() {
        return athlete;
    }
}
