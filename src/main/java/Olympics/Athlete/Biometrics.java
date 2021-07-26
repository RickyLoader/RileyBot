package Olympics.Athlete;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Period;

/**
 * Athlete biometrics - height, weight, age, etc
 */
public class Biometrics {
    private final LocalDate birthDate;
    private final Integer height, weight;
    private final GENDER_CODE genderCode;

    public enum GENDER_CODE {
        M,
        F,
        X;

        /**
         * Get the name from the gender code - e.g M -> Male
         *
         * @return Gender code name
         */
        public String getName() {
            switch(this) {
                case M:
                    return "Male";
                case F:
                    return "Female";
                default:
                    return "Unspecified";
            }
        }
    }

    /**
     * Create the biometrics data for an athlete.
     *
     * @param genderCode Gender code - e.g W
     * @param birthDate  Date of birth for the athlete
     * @param height     Height (in cm) e.g 160
     * @param weight     Weight (in kg) e.g 60
     */
    public Biometrics(GENDER_CODE genderCode, LocalDate birthDate, @Nullable Integer height, @Nullable Integer weight) {
        this.genderCode = genderCode;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
    }

    /**
     * Check if the athlete has a known weight.
     * This is typically false if the athlete does not compete in a sport where weight is relevant.
     *
     * @return Weight data available
     */
    public boolean hasWeight() {
        return weight != null;
    }

    /**
     * Get the weight of the athlete (in kg)
     *
     * @return Athlete weight
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * Get the height of the athlete (in cm)
     *
     * @return Athlete height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Check if the athlete has a known height.
     * This is typically false if the athlete does not compete in a sport where height is relevant.
     *
     * @return Height data available
     */
    public boolean hasHeight() {
        return height != null;
    }

    /**
     * Get the gender code of the athlete e.g W
     *
     * @return Athlete gender code
     */
    public GENDER_CODE getGenderCode() {
        return genderCode;
    }

    /**
     * Get the age of the athlete, calculated in real time.
     *
     * @return Athlete age
     */
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Get the birth date of the athlete
     *
     * @return Date of birth
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Check if the biometrics has any height/weight measurements
     *
     * @return Biometrics has measurements
     */
    public boolean hasMeasurements() {
        return hasHeight() || hasWeight();
    }
}
