package Olympics.Athlete;

import Olympics.Medal.Medal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Olympic athlete bio - medals, social media, etc
 */
public class AthleteBio {
    private final Biometrics biometrics;
    private final String philosophyQuote;
    private final ResidenceInfo currentResidence, birthResidence;
    private final SocialMedia[] socialMedia;
    private ArrayList<Medal> medals;

    /**
     * Create an athlete bio
     *
     * @param biometrics       Height, weight, and age info
     * @param philosophyQuote  Quote about the philosophy of the athlete
     * @param currentResidence Current residence of the athlete
     * @param birthResidence   Birth residence of the athlete
     * @param socialMedia      Array of social media connections - platform names and profile URLs
     * @param medals           List of medals awarded to the athlete
     */
    public AthleteBio(Biometrics biometrics, @Nullable String philosophyQuote, @Nullable ResidenceInfo currentResidence, @Nullable ResidenceInfo birthResidence, SocialMedia[] socialMedia, ArrayList<Medal> medals) {
        this.biometrics = biometrics;
        this.philosophyQuote = philosophyQuote;
        this.currentResidence = currentResidence;
        this.birthResidence = birthResidence;
        this.socialMedia = socialMedia;
        this.medals = medals;
    }

    /**
     * Check if the athlete has a quote about their philosophy
     *
     * @return Athlete has philosophy quote
     */
    public boolean hasPhilosophyQuote() {
        return philosophyQuote != null;
    }

    /**
     * Get the biometrics of the athlete.
     * This includes height, age, weight, etc.
     *
     * @return Athlete biometrics
     */
    public Biometrics getBiometrics() {
        return biometrics;
    }

    /**
     * Get a quote from the athlete about their philosophy.
     *
     * @return Philosophy quote
     */
    public String getPhilosophyQuote() {
        return philosophyQuote;
    }

    /**
     * Get the list of medals that the athlete has won
     *
     * @return List of medals
     */
    public ArrayList<Medal> getMedals() {
        return medals;
    }

    /**
     * Check if the athlete has any medals
     *
     * @return Athlete has medals
     */
    public boolean hasMedals() {
        return !medals.isEmpty();
    }

    /**
     * Set the athlete's medals
     *
     * @param medals List of medals the athlete has been awarded
     */
    public void setMedals(ArrayList<Medal> medals) {
        this.medals = medals;
    }

    /**
     * Check if the athlete has any social media
     *
     * @return Athlete has social media
     */
    public boolean hasSocialMedia() {
        return socialMedia.length > 0;
    }

    /**
     * Get an array of social media connections for the athlete
     *
     * @return Social media connections
     */
    public SocialMedia[] getSocialMedia() {
        return socialMedia;
    }

    /**
     * Get the current residence of the athlete.
     * This is the country and place within the country that the athlete currently resides in.
     *
     * @return Current residence
     */
    public ResidenceInfo getCurrentResidence() {
        return currentResidence;
    }

    /**
     * Get the birth residence of the athlete.
     * This is the country and place within the country that the athlete was born.
     *
     * @return Birth residence
     */
    public ResidenceInfo getBirthResidence() {
        return birthResidence;
    }

    /**
     * Check if the birth residence of the athlete is available
     *
     * @return Birth residence is available
     */
    public boolean hasBirthResidence() {
        return birthResidence != null;
    }

    /**
     * Check if the current residence of the athlete is available
     *
     * @return Current residence is available
     */
    public boolean hasCurrentResidence() {
        return currentResidence != null;
    }
}
