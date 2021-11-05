package Dinosaurs;

import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Yoink fun facts about dinosaurs from https://dinosaurpictures.org/
 */
public class DinosaurDatabase {
    private static final String
            BASE_WEB_URL = "https://dinosaurpictures.org/",
            BASE_API_URL = BASE_WEB_URL + "api/";
    private final ArrayList<String> dinosaurNames;
    private final HashMap<String, DinosaurFacts> dinosaurFacts;

    /**
     * Initialise a list of the dinosaur names in the database
     */
    public DinosaurDatabase() {
        this.dinosaurNames = fetchDinosaurNames();
        this.dinosaurFacts = new HashMap<>();
    }

    /**
     * Fetch a list dinosaur names available in the database.
     *
     * @return List of dinosaur names
     */
    private ArrayList<String> fetchDinosaurNames() {
        final ArrayList<String> dinosaurNames = new ArrayList<>();
        try {
            final NetworkResponse response = new NetworkRequest(BASE_API_URL + "category/all", false).get();

            // Returns an array of all dinosaur names in the database
            final JSONArray names = new JSONArray(response.body);
            for(int i = 0; i < names.length(); i++) {
                dinosaurNames.add(names.getString(i));
            }
        }
        catch(Exception e) {
            System.out.println("Failed to parse dinosaur names!");
        }
        return dinosaurNames;
    }

    /**
     * Search the list of dinosaur names in the database.
     * If a single matching name is found the list will contain only that name,
     * otherwise the list will contain all names found for the query.
     *
     * @param query Name query - e.g "Brachiosaurus"
     * @return List of matching dinosaur names
     */
    public ArrayList<String> searchDinosaurNames(String query) {
        final ArrayList<String> fuzzyMatches = dinosaurNames
                .stream()
                .filter(n -> n.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        final ArrayList<String> exactMatches = fuzzyMatches
                .stream()
                .filter(n -> n.equalsIgnoreCase(query))
                .collect(Collectors.toCollection(ArrayList::new));

        // Return single match or any names containing the query
        return exactMatches.size() == 1 ? exactMatches : fuzzyMatches;
    }

    /**
     * Fetch dinosaur facts for a dinosaur of the given name.
     * Valid names may be found at {@link DinosaurDatabase#searchDinosaurNames(String)}.
     * Invalid names will return null.
     *
     * @param name Dinosaur name - e.g "Brachiosaurus"
     * @return Details about the dinosaur of the given name
     */
    @Nullable
    private DinosaurFacts fetchDinosaurFacts(String name) {
        try {

            // https://dinosaurpictures.org/api/dinosaur/brachiosaurus
            final String url = BASE_API_URL + "dinosaur/" + name.toLowerCase();
            final JSONObject data = new JSONObject(new NetworkRequest(url, false).get().body);

            // Parse images
            final ArrayList<String> images = new ArrayList<>();
            final JSONArray imageArr = data.getJSONArray("pics");
            for(int i = 0; i < imageArr.length(); i++) {
                images.add(imageArr.getJSONObject(i).getString("url"));
            }

            // Database name should be the same as the provided but maybe not so use it (correct capitalisation etc)
            final String actualName = data.getString("name");
            return new DinosaurFacts(
                    actualName,
                    data.getString("creatureType"),
                    getOptionalValue(data, "period"),
                    getOptionalValue(data, "eats"),
                    getDatabaseUrl(actualName),
                    images,
                    scrapeTrivia(actualName)
            );
        }

        // Invalid name/API down
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get an optional String value from the given dinosaur JSON data.
     * If the key is not found, return "N/A".
     *
     * @param dinosaurFactsData JSON data from the API
     * @param key               Key for value - e.g "eats"
     * @return Value at key or "N/A"
     */
    private String getOptionalValue(JSONObject dinosaurFactsData, String key) {
        return dinosaurFactsData.has(key) ? dinosaurFactsData.getString(key) : "N/A";
    }

    /**
     * Attempt to scrape a list of trivia for the dinosaur of the given name.
     * This is not returned by the API, but can potentially be scraped (if available) from the webpage
     * of the dinosaur.
     * This will be empty if no trivia is found, or null if an error occurs while scraping.
     * Each item in the resulting list is a piece of trivia about the dinosaur.
     *
     * @param dinosaurName Dinosaur name - e.g "Brachiosaurus"
     * @return List of dinosaur trivia
     */
    @Nullable
    private ArrayList<String> scrapeTrivia(String dinosaurName) {
        final ArrayList<String> trivia = new ArrayList<>();
        try {
            Document dinosaurPage = Jsoup.connect(getDatabaseUrl(dinosaurName)).get();

            // Trivia list is preceded by a <p>Quick facts about [dinosaur name]</p> element
            Elements triviaTitles = dinosaurPage.select("p:contains(Quick facts)");

            // No title element found, there is probably no trivia for this dinosaur
            if(triviaTitles.isEmpty()) {
                return trivia;
            }

            // The trivia is in an unordered list right below the title
            Element triviaContainer = triviaTitles.get(0).nextElementSibling();

            // Add the trivia to the list
            Elements triviaList = triviaContainer.select("li");
            for(Element triviaItem : triviaList) {
                trivia.add(triviaItem.text());
            }
        }
        catch(Exception e) {
            return null;
        }
        return trivia;
    }

    /**
     * Get the URL to the database webpage for the dinosaur of the given name.
     *
     * @param dinosaurName Dinosaur name - e.g "Brachiosaurus" (capitalisation doesn't matter)
     * @return URL to dinosaur webpage e.g "https://dinosaurpictures.org/Brachiosaurus-pictures"
     */
    public String getDatabaseUrl(String dinosaurName) {
        return BASE_WEB_URL + dinosaurName + "-pictures";
    }

    /**
     * Fetch dinosaur facts for a random dinosaur.
     *
     * @return Random dinosaur facts
     */
    @Nullable
    public DinosaurFacts getRandomDinosaurFacts() {
        return getDinosaurFactsByName(dinosaurNames.get(new Random().nextInt(dinosaurNames.size())));
    }

    /**
     * Get dinosaur facts for a dinosaur of the given name.
     * Check if the facts for this dinosaur have already been mapped before attempting to fetch from the API.
     * If fetched from the API, map it by name as fetching requires multiple requests and the data does not change.
     *
     * @param name Dinosaur name - e.g "Brachiosaurus"
     * @return Details about the dinosaur of the given name
     */
    @Nullable
    public DinosaurFacts getDinosaurFactsByName(String name) {
        final String key = name.toLowerCase();

        DinosaurFacts dinosaurFacts;

        // Already have fetched the facts for this dinosaur
        if(this.dinosaurFacts.containsKey(key)) {
            dinosaurFacts = this.dinosaurFacts.get(key);

            /*
             * Failed to parse trivia when cached, try again.
             * This is only when trivia failed to scrape, not when there was none.
             */
            if(!dinosaurFacts.wasTriviaProvided()) {
                dinosaurFacts.setTrivia(scrapeTrivia(dinosaurFacts.getName()));
            }

            return dinosaurFacts;
        }

        // Fetch info for this dinosaur from the API
        dinosaurFacts = fetchDinosaurFacts(name);

        // Failed to retrieve facts, may be invalid name or API down
        if(dinosaurFacts == null) {
            return null;
        }

        // Map as to not fetch again (this data does not change)
        this.dinosaurFacts.put(key, dinosaurFacts);
        return dinosaurFacts;
    }
}
