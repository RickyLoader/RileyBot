package Hangman;

import Bot.ResourceHandler;
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
    private final BufferedImage[] images;
    private final int MAX_FAILS, MAX_HINTS;
    private final String helpMessage;
    private long gameID;
    private int currentFails, currentHints;
    private String currentGuess, secretWord;
    private MessageChannel channel;
    private Member owner;
    private HashMap<Character, ArrayList<Integer>> secretWordMap;
    private LinkedHashSet<String> guesses;
    private ArrayList<String> hints;
    private boolean running, victory, stopped, paused;

    /**
     * Initialise images to be used
     *
     * @param helpMessage Help message to display
     */
    public Hangman(String helpMessage) {
        this.helpMessage = helpMessage;
        this.images = getImages(new ResourceHandler());
        this.MAX_FAILS = 6;
        this.MAX_HINTS = 3;
    }

    /**
     * Get the images required to play Hangman
     *
     * @param handler Resource handler
     * @return Array of images displaying each stage of the game
     */
    private BufferedImage[] getImages(ResourceHandler handler) {
        String path = "/Hangman/";
        return new BufferedImage[]{
                handler.getImageResource(path + "board.png"),
                handler.getImageResource(path + "head.png"),
                handler.getImageResource(path + "head_body.png"),
                handler.getImageResource(path + "head_body_leg_1.png"),
                handler.getImageResource(path + "head_body_leg_2.png"),
                handler.getImageResource(path + "head_body_leg_2_arm_1.png"),
                handler.getImageResource(path + "head_body_leg_2_arm_2.png")
        };
    }

    /**
     * Start the Hangman game and reset variables
     *
     * @param channel Channel for game to take place
     * @param word    Word to guess
     * @param owner   Creator of hangman game
     */
    public void startGame(MessageChannel channel, String word, Member owner) {
        this.running = true;
        this.paused = true;
        this.victory = false;
        this.stopped = false;

        this.channel = channel;
        this.owner = owner;

        this.secretWord = word;
        this.currentGuess = word.replaceAll("[a-z]", "_");

        this.currentFails = 0;
        this.currentHints = 0;

        this.secretWordMap = new HashMap<>();
        this.guesses = new LinkedHashSet<>();
        this.hints = new ArrayList<>();
        char[] characters = secretWord.toCharArray();

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
        if(currentGuess.equals(secretWord)) {
            running = false;
            victory = true;
        }
        else if(MAX_FAILS - currentFails == 0) {
            running = false;
        }
        channel.deleteMessageById(gameID).queue(aVoid -> sendGameMessage());
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
        return new EmbedBuilder()
                .setTitle(owner.getEffectiveName().toUpperCase() + " | Hangman - " + getGameStatus())
                .setFooter("Try: " + helpMessage, EmbedHelper.getClock())
                .setThumbnail("https://i.imgur.com/5kyZ42Q.png")
                .setImage("attachment://image.png")
                .setColor(getColour())
                .setDescription(
                        "\n**Guesses**: " + formatGuesses() + "\n**Hints**: "
                                + (hints.size() == 1 ? "No hints available for the final character " : currentHints + "/" + MAX_HINTS)
                )
                .build();
    }

    /**
     * Get the colour to use based on the current game status
     *
     * @return Colour
     */
    private int getColour() {
        return running ? EmbedHelper.getYellow() : (victory ? EmbedHelper.getGreen() : EmbedHelper.getRed());
    }

    /**
     * Get the current game status message
     *
     * @return Game status message
     */
    private String getGameStatus() {
        if(running) {
            int chances = MAX_FAILS - currentFails;
            return chances + (chances == 1 ? " Chance" : " Chances") + " remaining!";
        }
        return victory ? "Victory!" : (stopped ? "Forfeited!" : "Defeat!");
    }

    /**
     * Format the guesses for the message embed
     *
     * @return Formatted guesses
     */
    private String formatGuesses() {
        ArrayList<String> guesses = this.guesses
                .stream()
                .map(e -> {
                    String guess = e.toUpperCase();
                    if((e.length() == 1 && secretWordMap.containsKey(e.charAt(0))) || e.equals(secretWord)) {
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
            BufferedImage gallows = images[currentFails];
            Graphics g = gallows.getGraphics();
            Font font = g.getFontMetrics().getFont().deriveFont(50f);
            FontMetrics fm = g.getFontMetrics(font);

            String guess = StringUtils.join(
                    (running ? currentGuess : secretWord)
                            .toUpperCase().split(""), " "
            );

            BufferedImage playerGuess = new BufferedImage(
                    fm.stringWidth(guess), fm.getHeight(), BufferedImage.TYPE_INT_ARGB
            );
            g = playerGuess.getGraphics();
            g.setFont(font);
            g.drawString(guess, 0, playerGuess.getHeight() - fm.getMaxDescent());

            BufferedImage canvas = new BufferedImage(
                    Math.max(gallows.getWidth(), playerGuess.getWidth()), gallows.getHeight() + playerGuess.getHeight(), BufferedImage.TYPE_INT_ARGB
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
            channel.sendMessage(player.getAsMention() + " " + guess + " has already been guessed, should've gone to Specsavers").queue();
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
            currentFails++;
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
            currentFails++;
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
                    player.getAsMention() + " You've already exceeded the maximum of " + MAX_HINTS + " hints, time to use your brain"
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
