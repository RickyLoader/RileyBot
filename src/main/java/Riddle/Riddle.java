package Riddle;


import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Riddle {
    private final String question, answer;
    private String title;
    private final ArrayList<Guess> guesses;
    private final HashSet<String> guessedWords;
    private boolean running, solved;
    private long id;
    private Guess correct;

    /**
     * Create a riddle
     *
     * @param question Riddle question
     * @param answer   Answer to riddle question
     */
    public Riddle(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.guesses = new ArrayList<>();
        this.guessedWords = new HashSet<>();
        this.running = true;
        this.solved = false;
    }

    /**
     * Get the title of the riddle
     *
     * @return Riddle title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the riddle
     * Title will be displayed in the riddle message
     *
     * @param title Riddle title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the message id
     *
     * @param id Message id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the message id
     *
     * @return Message id
     */
    public long getId() {
        return id;
    }

    /**
     * Check if the riddle has a message sent
     *
     * @return Message has been sent
     */
    public boolean hasMessage() {
        return id != 0;
    }

    /**
     * Guess the answer to the riddle
     *
     * @param guesser       Member who guessed
     * @param guessedAnswer Answer guess
     * @return Guess is correct
     */
    public boolean guess(Member guesser, String guessedAnswer) {
        boolean correct = guessedAnswer.equalsIgnoreCase(answer);

        Guess guess = new Guess(
                guesser,
                guessedAnswer,
                correct
        );

        this.guesses.add(guess);
        this.guessedWords.add(guessedAnswer.toLowerCase());

        if(correct) {
            this.running = false;
            this.solved = true;
            this.correct = guess;
        }
        return correct;
    }

    /**
     * Check if a word has been guessed
     *
     * @param guess Guessed word
     * @return Word has been guessed
     */
    public boolean hasGuessedWord(String guess) {
        return guessedWords.contains(guess.toLowerCase());
    }

    /**
     * Get the correct guess
     *
     * @return Correct guess
     */
    public Guess getCorrectGuess() {
        return correct;
    }

    /**
     * Set the status of the riddle to false
     */
    public void stopRiddle() {
        this.running = false;
        this.solved = false;
    }

    /**
     * Check if the riddle is running
     *
     * @return Riddle is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Check if the riddle has been solved
     *
     * @return Riddle has been solved
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * Get a String showing a comma separated list of guesses and total number of guesses
     *
     * @return Guesses
     */
    public String formatGuesses() {
        if(guesses.isEmpty()) {
            return "None!";
        }
        return guesses
                .stream()
                .map(g -> g.formatGuess())
                .collect(Collectors.joining(", "))
                + " (" + guesses.size() + ")";
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
