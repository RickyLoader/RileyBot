package Hangman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
}
