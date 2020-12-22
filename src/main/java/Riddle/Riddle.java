package Riddle;


public class Riddle {
    private final String question, answer;
    private int guesses = 0;

    /**
     * Create a riddle
     *
     * @param question Riddle question
     * @param answer   Answer to riddle question
     */
    public Riddle(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    /**
     * Guess the answer to the riddle
     *
     * @param guess Answer guess
     * @return Guess is correct
     */
    public boolean guessRiddle(String guess) {
        this.guesses++;
        return guess.equalsIgnoreCase(answer);
    }

    /**
     * Get the number of guesses for the riddle
     *
     * @return Guesses
     */
    public int getGuesses() {
        return guesses;
    }

    /**
     * Reset the guesses to 0
     */
    public void resetGuesses() {
        this.guesses = 0;
    }

    /**
     * Get the riddle question
     *
     * @return Riddle question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Get the answer to the riddle question
     *
     * @return Answer to riddle
     */
    public String getAnswer() {
        return answer;
    }
}
