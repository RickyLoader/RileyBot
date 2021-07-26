package Olympics.Medal;

/**
 * Country/athlete medal count
 */
public class MedalCount {
    private int bronzeMedals, silverMedals, goldMedals, totalMedals;

    /**
     * Create a medal count for a country/athlete.
     *
     * @param bronzeMedals Total number of bronze medals awarded to the country/athlete
     * @param silverMedals Total number of silver medals awarded to the country/athlete
     * @param goldMedals   Total number of gold medals awarded to the country/athlete
     */
    public MedalCount(int bronzeMedals, int silverMedals, int goldMedals) {
        this.bronzeMedals = bronzeMedals;
        this.silverMedals = silverMedals;
        this.goldMedals = goldMedals;
        this.totalMedals = bronzeMedals + silverMedals + goldMedals;
    }

    /**
     * Create an empty medal count for a country/athlete.
     */
    public MedalCount() {
        this.bronzeMedals = 0;
        this.silverMedals = 0;
        this.goldMedals = 0;
        this.totalMedals = 0;
    }

    /**
     * Increment the count of a type of medal.
     *
     * @param type Type of medal to increment
     */
    public void addMedalType(Medal.TYPE type) {
        switch(type) {
            case GOLD:
                goldMedals++;
                break;
            case SILVER:
                silverMedals++;
                break;

            // BRONZE
            default:
                bronzeMedals++;
                break;
        }
        totalMedals++;
    }

    /**
     * Get the total number of bronze medals the country has been awarded
     *
     * @return Total awarded bronze medals
     */
    public int getBronzeMedals() {
        return bronzeMedals;
    }

    /**
     * Get the total number of gold medals the country has been awarded
     *
     * @return Total awarded gold medals
     */
    public int getGoldMedals() {
        return goldMedals;
    }

    /**
     * Get the total number of silver medals the country has been awarded
     *
     * @return Total awarded silver medals
     */
    public int getSilverMedals() {
        return silverMedals;
    }

    /**
     * Get the total number of medals the country has been awarded
     *
     * @return Total awarded medals
     */
    public int getTotalMedals() {
        return totalMedals;
    }
}
