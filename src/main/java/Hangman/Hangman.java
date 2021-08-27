package Hangman;

import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Play Hangman!
 */
public class Hangman {
    private Gallows gallows;
    private final int MAX_HINTS;
    private final String helpMessage;
    private long gameID;
    private int currentHints;
    private String currentGuess;
    private DictWord secretWord;
    private MessageChannel channel;
    private Member owner;
    private HashMap<Character, ArrayList<Integer>> secretWordMap;
    private LinkedHashSet<String> guesses;
    private ArrayList<String> hints;
    private boolean running, victory, stopped, paused;

    /**
     * Create the hangman instance. Games can be started from this instance once created.
     *
     * @param helpMessage Help message to display
     */
    public Hangman(String helpMessage) {
        this.helpMessage = helpMessage;
        this.MAX_HINTS = 3;
    }

    /**
     * Start the Hangman game and reset variables
     *
     * @param gallows Gallows to play on
     * @param channel Channel for game to take place
     * @param word    Word to guess
     * @param owner   Creator of hangman game
     */
    public void startGame(Gallows gallows, MessageChannel channel, DictWord word, Member owner) {
        this.gallows = gallows;
        this.running = true;
        this.paused = true;
        this.victory = false;
        this.stopped = false;

        this.channel = channel;
        this.owner = owner;

        this.secretWord = word;

        // "word" -> "____" or "two words" -> "___ ____" (spaces not converted to underscores)
        this.currentGuess = word.getWord().replaceAll("[a-zA-Z]", "_");

        this.currentHints = 0;

        this.secretWordMap = new HashMap<>();
        this.guesses = new LinkedHashSet<>();

        // List of possible hints (unique characters in the word)
        this.hints = new ArrayList<>();

        /*
         * Create a map of unique characters in the word to a list of
         * indexes within the word where this character appears.
         */
        final char[] characters = secretWord.getWord().toLowerCase().toCharArray();

        for(int i = 0; i < characters.length; i++) {
            final char c = characters[i];

            // Don't map spaces as there is no need to guess them (or get them as hints).
            if(c == ' ') {
                continue;
            }

            ArrayList<Integer> indexes = secretWordMap.get(c);

            /*
             * First time seeing character, create an empty list of indexes for the character.
             * Add to the list of possible hints.
             */
            if(indexes == null) {
                hints.add(String.valueOf(c));
                indexes = new ArrayList<>();
            }

            indexes.add(i);
            secretWordMap.put(c, indexes);
        }
        sendGameMessage();
    }

    /**
     * Check if game is running
     *
     * @return Game running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stop the hangman game
     */
    public void stopGame() {
        running = false;
        stopped = true;
        updateGame();
    }

    /**
     * Update the game message and check the current status of the game
     */
    private void updateGame() {

        // The current guess is now equal to the word, the player has won the game
        if(currentGuess.equalsIgnoreCase(secretWord.getWord())) {
            running = false;
            victory = true;
        }

        // The player has guessed incorrectly too many times, they have lost
        else if(gallows.MAX_STAGES - gallows.getStage() == 0) {
            running = false;
        }

        // Delete the current game message and send the new one
        channel.deleteMessageById(gameID).queue(delete -> {
            sendGameMessage();
            if(!running) {
                gallows.resetStages();
            }
        });
    }

    /**
     * Build and send the game message
     */
    private void sendGameMessage() {
        paused = true;
        channel.sendMessage(buildGameMessage()).addFile(buildImage(), "image.png").queue(message -> {
            gameID = message.getIdLong();
            paused = false;
        });
    }

    /**
     * Check whether the game is accepting updates
     *
     * @return Allowed to update game
     */
    public boolean canUpdate() {
        return !paused;
    }

    /**
     * Build the game message
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage() {
        final EmbedBuilder builder = new EmbedBuilder()
                .setTitle(owner.getEffectiveName().toUpperCase() + " | Hangman - " + getGameStatus())
                .setFooter(helpMessage, running ? EmbedHelper.CLOCK_GIF : EmbedHelper.CLOCK_STOPPED)
                .setThumbnail(gallows.getImagePreview())
                .setImage("attachment://image.png")
                .setColor(getColour());

        String desc = "\n**Guesses**: " + getGuessSummary()
                + "\n**Hints**: " + getHintSummary();

        // Add the dictionary definition of the secret word if the game has ended
        if(!running) {
            desc += "\n\n**Definition**: " + ((secretWord.hasDefinition()) ? secretWord.getDefinition() : "Fuck knows");
        }

        return builder
                .setDescription(desc)
                .build();
    }

    /**
     * Get the colour to use based on the current game status
     *
     * @return Colour
     */
    private int getColour() {
        return running ? EmbedHelper.YELLOW : (victory ? EmbedHelper.GREEN : EmbedHelper.RED);
    }

    /**
     * Get the current game status message.
     * Used in the title of the game message.
     *
     * @return Game status message
     */
    private String getGameStatus() {
        if(running) {
            int chances = gallows.MAX_STAGES - gallows.getStage();
            return chances + (chances == 1 ? " Chance" : " Chances") + " remaining!";
        }
        return victory ? "Victory!" : (stopped ? "Forfeited!" : "Defeat!");
    }

    /**
     * Get the hint summary for the game message.
     * This is either a message informing that no hints are available or
     * x/y hints where x = used hints & y = total hints e.g "0/3", "1/3", etc.
     *
     * @return Hint summary
     */
    private String getHintSummary() {
        if(hints.size() == 1 && running) {
            return "No hints available for the final character.";
        }
        return currentHints + "/" + MAX_HINTS;
    }

    /**
     * Get the guess summary for the game message.
     * Return guessed characters in the order they were guessed.
     * Display the guess in bold if it was correct or struck through if not.
     * Show total guessed characters in parenthesis.
     *
     * @return Guess summary
     */
    private String getGuessSummary() {
        ArrayList<String> guesses = this.guesses
                .stream()
                .map(e -> {
                    String guess = e.toUpperCase();
                    if((e.length() == 1 && secretWordMap.containsKey(e.charAt(0))) || e.equalsIgnoreCase(secretWord.getWord())) {
                        return "**" + guess + "**";
                    }
                    return "~~" + guess + "~~";
                })
                .collect(Collectors.toCollection(ArrayList::new));
        return guesses.isEmpty() ? "None!" : StringUtils.join(guesses, ", ") + " (" + guesses.size() + ")";
    }

    /**
     * Build the Hangman image
     *
     * @return Hangman image
     */
    private byte[] buildImage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BufferedImage gallows = this.gallows.getCurrentImage();
            Graphics g = gallows.getGraphics();
            Font font = g.getFontMetrics().getFont().deriveFont(50f);
            FontMetrics fm = g.getFontMetrics(font);

            /*
             * Display the word with the characters separated by a space, this makes a more obvious distinction
             * between characters (and prevents underscores merging in to a line e.g "___" vs "_ _ _").
             */
            final String guess = StringUtils.join(
                    (running ? currentGuess : secretWord.getWord())
                            .toUpperCase().split(""), " "
            );

            BufferedImage playerGuess = new BufferedImage(
                    fm.stringWidth(guess), fm.getHeight(), BufferedImage.TYPE_INT_ARGB
            );
            g = playerGuess.getGraphics();
            g.setFont(font);
            g.drawString(guess, 0, playerGuess.getHeight() - fm.getMaxDescent());

            BufferedImage canvas = new BufferedImage(
                    Math.max(
                            gallows.getWidth(),
                            playerGuess.getWidth()
                    ),
                    gallows.getHeight() + playerGuess.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            g = canvas.getGraphics();
            g.drawImage(gallows, 0, 0, null);
            g.drawImage(playerGuess, 0, gallows.getHeight(), null);
            g.dispose();

            ImageIO.write(canvas, "png", outputStream);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    /**
     * Guess a letter/word
     *
     * @param guess  Player's guess
     * @param player Player who guessed
     */
    public void guess(String guess, Member player) {

        // This guess has already been made, inform the player
        if(guesses.contains(guess)) {
            channel.sendMessage(
                    player.getAsMention() + " " + guess + " has already been guessed, should've gone to Specsavers"
            ).queue();
            return;
        }

        // The guess is a single character, check if it is in the secret word
        if(guess.length() == 1) {
            guesses.add(guess);
            guessCharacter(guess.charAt(0));
            updateGame();
        }

        // The guess is a String, check if it matches the entire secret word
        else if(guessWord(guess, player)) {
            guesses.add(guess);

            // Only update the game if it was a valid guess
            updateGame();
        }
    }

    /**
     * Guess a character
     *
     * @param guess Guess character
     */
    private void guessCharacter(Character guess) {

        // Character is not in the word, add a fail
        if(!secretWordMap.containsKey(guess)) {
            gallows.incrementStage();
            return;
        }

        // Remove the character from the possible hints
        hints.remove(String.valueOf(guess));
        addCharacterToGuess(guess);
    }

    /**
     * Guess a word
     *
     * @param guess  Guess word
     * @param player Player making guess
     * @return Valid guess
     */
    private boolean guessWord(String guess, Member player) {
        final String secretWord = this.secretWord.getWord();
        final int secretLength = secretWord.length();
        final int guessLength = guess.length();

        // Don't count an invalid length guess as a fail, e.g guessing "appl" against "apple"
        if(guessLength != secretLength) {
            channel.sendMessage(
                    player.getAsMention()
                            + " I'm going to pretend I didn't just see you guess a "
                            + guessLength
                            + " letter word against a "
                            + secretLength
                            + " letter word..."
            ).queue();

            // Guess not counted
            return false;
        }

        // Player guessed correctly, set the current guess to the guessed word
        if(secretWord.equals(guess)) {
            currentGuess = guess;
            hints = new ArrayList<>();
        }

        // Incorrect guess, add a fail
        else {
            gallows.incrementStage();
        }

        // Valid guess
        return true;
    }

    /**
     * Add a correct character to the current guess
     *
     * @param guess Character guess e.g 'a'
     */
    private void addCharacterToGuess(Character guess) {

        // "_____" -> [_,_,_,_,_]
        final char[] currentGuess = this.currentGuess.toCharArray();

        // List of indexes where the character is seen within the word
        final ArrayList<Integer> indexes = secretWordMap.get(guess);

        /*
         * Iterate through the indexes changing the underscore (hidden) characters within the current guess
         * to the guessed character. E.g [_,_,_,_,_] -> [_,a,_,a,_]
         */
        for(int index : indexes) {
            currentGuess[index] = guess;
        }

        // The current guess is now "_a_a_"
        this.currentGuess = String.valueOf(currentGuess);
    }

    /**
     * Get the game owner
     *
     * @return Game owner
     */
    public Member getOwner() {
        return owner;
    }

    /**
     * Get a hint for the current game
     *
     * @param player Player who asked for hint
     */
    public void getHint(Member player) {

        // The player has used all of their hints
        if(currentHints == MAX_HINTS) {
            channel.sendMessage(
                    player.getAsMention()
                            + " You've already exceeded the maximum of " + MAX_HINTS + " hints, time to use your brain"
            ).queue();
            return;
        }

        /*
         * Hints are a list of the unique characters in the secret word, and are removed when used/guessed.
         * If only one is left, there is only one letter in the secret word left to guess, don't allow the player to
         * use a hint for it.
         */
        if(hints.size() == 1) {
            channel.sendMessage(player.getAsMention() + " NO, Use your brain for the last letter").queue();
            return;
        }

        // Roll a random character in the possible hints
        final String hint = hints.get(new Random().nextInt(hints.size()));

        // Add it as a guess (so the player cannot waste a guess on it later)
        guesses.add(hint);

        // Remove it as a possible hint (so it cannot be rolled again and unlock nothing)
        hints.remove(hint);

        this.currentHints++;
        addCharacterToGuess(hint.charAt(0));
        updateGame();
    }
}
