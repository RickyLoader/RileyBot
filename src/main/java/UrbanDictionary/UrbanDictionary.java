package UrbanDictionary;

import Network.NetworkRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import static UrbanDictionary.Definition.parseSubmissionDate;

/**
 * Wrap the urban dictionary API
 */
public class UrbanDictionary {
    /**
     * Strip the embedded links from given String
     *
     * @return String stripped of embedded links
     */
    private String stripFormatting(String string) {
        return string
                .replace("[", "")
                .replace("]", "")
                .replace("*", "");
    }

    /**
     * Search for a definition on the Urban Dictionary
     *
     * @param term Term to search for
     * @return List of definitions for the given search term
     */
    public ArrayList<Definition> searchDefinition(String term) {
        ArrayList<Definition> definitions = definitionRequest(
                "https://api.urbandictionary.com/v0/define?term=" + term
        );
        definitions = definitions
                .stream()
                .filter(definition -> definition.getTerm().equalsIgnoreCase(term))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(definitions);
        return definitions;
    }

    /**
     * Get a random definition from the Urban Dictionary
     *
     * @return Random definition
     */
    public Definition getRandomDefinition() {
        ArrayList<Definition> definitions = definitionRequest("https://api.urbandictionary.com/v0/random");
        return definitions.get(new Random().nextInt(definitions.size()));
    }

    /**
     * Retrieve a list of definitions from the given URL
     *
     * @param url URL to urban dictionary API endpoint
     * @return List of definitions
     */
    private ArrayList<Definition> definitionRequest(String url) {
        ArrayList<Definition> definitions = new ArrayList<>();
        String json = new NetworkRequest(url, false).get().body;
        if(json == null) {
            return definitions;
        }
        JSONArray results = new JSONObject(json).getJSONArray("list");
        for(int i = 0; i < results.length(); i++) {
            definitions.add(parseDefinition(results.getJSONObject(i)));
        }
        return definitions;
    }

    /**
     * Parse a definition from a JSON response
     *
     * @param definition JSON response of definition
     * @return Definition
     */
    private Definition parseDefinition(JSONObject definition) {
        String explanation = stripFormatting(definition.getString("definition"));
        return new Definition(
                definition.getString("word"),
                explanation.length() > 1000 ? explanation.substring(0, 1000) + "..." : explanation,
                stripFormatting(definition.getString("example")),
                definition.getInt("thumbs_up"),
                definition.getInt("thumbs_down"),
                definition.getString("permalink"),
                parseSubmissionDate(definition.getString("written_on")),
                definition.getString("author")
        );
    }
}
