package Hangman;

/**
 * Dictionary word
 */
public class DictWord {
    private final String word, definition;

    /**
     * Create a dictionary word with a definition
     *
     * @param word       Word
     * @param definition Dictionary definition
     */
    public DictWord(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    /**
     * Create a dictionary word with no definition
     *
     * @param word Word
     */
    public DictWord(String word) {
        this(word, null);
    }

    /**
     * Get the word
     *
     * @return Word
     */
    public String getWord() {
        return word;
    }

    /**
     * Get the dictionary definition of the word
     *
     * @return Dictionary definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Check the presence of a dictionary definition for the word
     *
     * @return Word has definition
     */
    public boolean hasDefinition() {
        return definition != null;
    }
}
