package Olympics.Athlete;

import Olympics.Country.Country;
import Olympics.OlympicData;
import Olympics.Sport.Sport;

/**
 * Olympic athlete details
 */
public class Athlete extends OlympicData {
    private final String olympicName, imageUrl;
    private final Sport[] sports;
    private final Country country;
    private AthleteBio bio;

    /**
     * Create an Olympic athlete
     *
     * @param id           Unique ID of athlete e.g 1467441
     * @param readableName Name in first name last name format - e.g "Rien van der Schaft"
     * @param olympicName  Name in format given by Olympic data - e.g "van der SCHAFT Rien"
     * @param imageUrl     URL to an image of the athlete
     * @param country      Country athlete is competing for
     * @param sports       Array of sports the athlete competes in
     */
    public Athlete(String id, String readableName, String olympicName, String imageUrl, Country country, Sport[] sports) {
        super(id, readableName);
        this.olympicName = olympicName;
        this.imageUrl = imageUrl;
        this.country = country;
        this.sports = sports;
    }

    /**
     * Get the athlete bio.
     * This contains their medals, age, etc
     *
     * @return Athlete bio
     */
    public AthleteBio getBio() {
        return bio;
    }

    /**
     * Set the athlete bio
     *
     * @param bio Athlete bio
     */
    public void setBio(AthleteBio bio) {
        this.bio = bio;
    }

    /**
     * Check if the athlete has a bio
     *
     * @return Athlete has a bio
     */
    public boolean hasBio() {
        return bio != null;
    }

    /**
     * Get the URL to an image of the athlete
     *
     * @return Athlete image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get the country that the athlete is competing for
     *
     * @return Athlete country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Get the sports that the athlete competes in
     *
     * @return Athlete sports
     */
    public Sport[] getSports() {
        return sports;
    }

    /**
     * Get the name of the athlete as given in Olympic data.
     * This is in the format last name first name.
     *
     * @return Athlete Olympic name - e.g "HURDLES Frank"
     */
    public String getOlympicName() {
        return olympicName;
    }
}
