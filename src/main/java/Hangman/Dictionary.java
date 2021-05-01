package Hangman;

import Bot.ResourceHandler;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Hold dictionary words
 */
public class Dictionary {
    private final ArrayList<DictWord> wordList = new ArrayList<>();
    private final HashMap<String, DictWord> wordMap = new HashMap<>();
    private final Random random = new Random();

    /**
     * Add a word to the dictionary
     *
     * @param word Dictionary word
     */
    public void addWord(DictWord word) {
        wordList.add(word);
        wordMap.put(word.getWord().toLowerCase(), word);
    }

    /**
     * Parse Webster's English dictionary in to a Dictionary object
     *
     * @return English Dictionary
     */
    public static Dictionary createDictionary() {
        System.out.println("Parsing Webster's English dictionary...");
        JSONObject data = new JSONObject(
                new ResourceHandler().getResourceFileAsString("/Dictionary/dictionary.json")
        );
        Dictionary dictionary = new Dictionary();
        for(String word : data.keySet()) {
            dictionary.addWord(new DictWord(word, data.getString(word)));
        }
        return dictionary;
    }

    /**
     * Attempt to get the DictWord from the dictionary for the given word
     *
     * @param query Word to look for
     * @return DictWord of the given query
     */
    public DictWord getWord(String query) {
        return wordMap.get(query.toLowerCase());
    }

    /**
     * Get a random word from the dictionary
     *
     * @return Random word
     */
    public DictWord getRandomWord() {
        return wordList.get(random.nextInt(wordList.size()));
    }

    /**
     * Get a list of all words in the dictionary
     *
     * @return List of words in the dictionary
     */
    public ArrayList<DictWord> getWords() {
        return wordList;
    }

    /**
     * Search the dictionary for a list of words containing the given query.
     *
     * @param query Query to search for
     * @return List of words containing query
     */
    public ArrayList<DictWord> searchWords(String query) {
        return wordList
                .stream()
                .filter(dictWord -> dictWord.getWord().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
