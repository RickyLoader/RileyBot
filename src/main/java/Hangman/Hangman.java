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
     * Create the hangman instance
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
        this.currentGuess = word.getWord().replaceAll("[a-zA-Z]", "_");

        this.currentHints = 0;

        this.secretWordMap = new HashMap<>();
        this.guesses = new LinkedHashSet<>();
        this.hints = new ArrayList<>();
        char[] characters = secretWord.getWord().toCharArray();

        for(int i = 0; i < characters.length; i++) {
            char c = characters[i];
            ArrayList<Integer> indexes = secretWordMap.get(c);
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
        if(currentGuess.equals(secretWord.getWord())) {
            running = false;
            victory = true;
        }
        else if(gallows.MAX_STAGES - gallows.getStage() == 0) {
            running = false;
        }
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
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(owner.getEffectiveName().toUpperCase() + " | Hangman - " + getGameStatus())
                .setFooter(helpMessage, running ? EmbedHelper.CLOCK_GIF : EmbedHelper.CLOCK_STOPPED)
                .setThumbnail(gallows.getImagePreview())
                .setImage("attachment://image.png")
                .setColor(getColour());

        String desc = "\n**Guesses**: " + getGuessSummary() + "\n**Hints**: " + getHintSummary();
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
     * Get the current game status message
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
     * Get the hint summary for the game embed
     * Either message informing that no hints are available or x/y hints where x = used hints & y = total hints
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
     * Get the guess summary for the game embed
     * Return guessed characters in the order they were guessed
     * Display in bold if the guess was correct or struck through if not
     * Show total guessed characters in parenthesis
     *
     * @return Guess summary
     */
    private String getGuessSummary() {
        ArrayList<String> guesses = this.guesses
                .stream()
                .map(e -> {
                    String guess = e.toUpperCase();
                    if((e.length() == 1 && secretWordMap.containsKey(e.charAt(0))) || e.equals(secretWord.getWord())) {
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

            String guess = StringUtils.join(
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
        if(guesses.contains(guess)) {
            channel.sendMessage(
                    player.getAsMention() + " " + guess + " has already been guessed, should've gone to Specsavers"
            ).queue();
            return;
        }

        if(guess.length() == 1) {
            guesses.add(guess);
            guessCharacter(guess.charAt(0));
            updateGame();
        }
        else if(guessWord(guess, player)) {
            guesses.add(guess);
            updateGame();
        }
    }

    /**
     * Guess a character
     *
     * @param guess Guess character
     */
    private void guessCharacter(Character guess) {
        if(!secretWordMap.containsKey(guess)) {
            gallows.incrementStage();
            return;
        }
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
        int guessLength = guess.length();
        String secretWord = this.secretWord.getWord();
        int secretLength = secretWord.length();
        if(guessLength != secretLength) {
            channel.sendMessage(
                    player.getAsMention()
                            + " I'm going to pretend I didn't just see you guess a "
                            + guessLength
                            + " letter word against a "
                            + secretLength
                            + " letter word..."
            ).queue();
            return false;
        }

        if(!secretWord.equals(guess)) {
            gallows.incrementStage();
        }
        else {
            currentGuess = guess;
            hints = new ArrayList<>();
        }
        return true;
    }

    /**
     * Add a correct character to the current guess
     *
     * @param guess Character guess
     */
    private void addCharacterToGuess(Character guess) {
        char[] currentGuess = this.currentGuess.toCharArray();
        ArrayList<Integer> indexes = secretWordMap.get(guess);
        for(int index : indexes) {
            currentGuess[index] = guess;
        }
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
        if(currentHints == MAX_HINTS) {
            channel.sendMessage(
                    player.getAsMention()
                            + " You've already exceeded the maximum of " + MAX_HINTS + " hints, time to use your brain"
            ).queue();
            return;
        }

        if(hints.size() == 1) {
            channel.sendMessage(player.getAsMention() + " NO, Use your brain for the last letter").queue();
            return;
        }

        String hint = hints.get(new Random().nextInt(hints.size()));
        guesses.add(hint);
        hints.remove(hint);
        this.currentHints++;
        addCharacterToGuess(hint.charAt(0));
        updateGame();
    }
}
