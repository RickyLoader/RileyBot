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
        this.definition = formatDefinition(definition);
    }

    /**
     * Add new line characters & bold markdown formatting to separate multiple definitions.
     *
     * @param definition Definition to format
     * @return Formatted definition
     */
    private String formatDefinition(String definition) {
        if(definition == null) {
            return null;
        }
        String replacement = "\n\n**$0**";
        return definition
                .replaceAll("(\\d+\\.)", replacement)
                .replaceAll("(\\([a-z]\\))", "\t" + replacement);
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
     * Get the word definition truncated to the given number of characters
     * If the bound is larger than or equal to the length of the definition (or <=0),
     * the full definition will be returned.
     *
     * @param bound Number of characters to truncate definition to
     * @return Truncated definition
     */
    public String getTruncatedDescription(int bound) {
        if(bound >= definition.length() || bound <= 0) {
            return definition;
        }
        return definition.substring(0, bound) + "...";
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
