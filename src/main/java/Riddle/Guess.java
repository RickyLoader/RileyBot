package Riddle;

import net.dv8tion.jda.api.entities.Member;

/**
 * Riddle guess
 */
public class Guess {
    private final Member guesser;
    private final String guess;
    private final boolean correct;

    /**
     * Create a guess
     *
     * @param guesser Member who guessed
     * @param guess   Guessed String
     * @param correct Guess is correct
     */
    public Guess(Member guesser, String guess, boolean correct) {
        this.guesser = guesser;
        this.guess = guess;
        this.correct = correct;
    }

    /**
     * Check if the guess is correct
     *
     * @return Correct guess
     */
    public boolean isCorrect() {
        return correct;
    }

    /**
     * Get the member's guess formatted in either bold (correct) or with a strikethrough
     *
     * @return Member's guess
     */
    public String formatGuess() {
        String format = correct ? "**" : "~~";
        return format + guess + format;
    }

    /**
     * Get the member who guessed
     *
     * @return Get guesser
     */
    public Member getGuesser() {
        return guesser;
    }
}
