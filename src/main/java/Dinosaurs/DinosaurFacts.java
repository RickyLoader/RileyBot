package Dinosaurs;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Details about a dinosaur
 */
public class DinosaurFacts {
    private final String name, type, period, diet, databaseUrl;
    private final ArrayList<String> images;
    private ArrayList<String> trivia;

    /**
     * Create the dinosaur facts.
     *
     * @param name        Dinosaur name - e.g "Brachiosaurus"
     * @param type        Creature type - e.g "dinosaur"
     * @param period      Period in which this dinosaur lived - e.g "Jurassic"
     * @param diet        Diet - e.g "herbivore"
     * @param databaseUrl URL to the webpage for the dinosaur
     * @param images      List of images of the dinosaur
     * @param trivia      Optional list of trivia
     */
    public DinosaurFacts(String name, String type, String period, String diet, String databaseUrl, ArrayList<String> images, @Nullable ArrayList<String> trivia) {
        this.name = name;
        this.type = type;
        this.period = period;
        this.diet = diet;
        this.databaseUrl = databaseUrl;
        this.images = images;
        this.trivia = trivia;
    }

    /**
     * Get a list of trivia for the dinosaur (may be null if there is none).
     * Each item in the list is a piece of trivia.
     *
     * @return Dinosaur trivia
     */
    @Nullable
    public ArrayList<String> getTrivia() {
        return trivia;
    }

    /**
     * Set the dinosaur trivia
     *
     * @param trivia List of dinosaur trivia - each item in the list is a piece of trivia
     */
    public void setTrivia(ArrayList<String> trivia) {
        this.trivia = trivia;
    }

    /**
     * Check if any trivia was provided when creating these dinosaur facts.
     *
     * @return Trivia was provided
     */
    public boolean wasTriviaProvided() {
        return trivia != null;
    }

    /**
     * Get a list of images of the dinosaur
     *
     * @return List of dinosaur images
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get the URL to the database webpage for the dinosaur.
     *
     * @return Database webpage URL
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * Get the dinosaur name - e.g "Brachiosaurus"
     *
     * @return Dinosaur name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of creature - e.g "dinosaur"
     *
     * @return Creature type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the type of diet that the dinosaur had - e.g "herbivore"
     *
     * @return Dinosaur diet type
     */
    public String getDiet() {
        return diet;
    }

    /**
     * Get the period in which this dinosaur lived - e.g "Jurassic".
     *
     * @return Dinosaur period
     */
    public String getPeriod() {
        return period;
    }
}
