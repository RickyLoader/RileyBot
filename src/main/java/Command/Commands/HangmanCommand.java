package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Hangman.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static Command.Structure.PageableTableEmbed.*;

/**
 * Play Hangman! Fun!
 */
public class HangmanCommand extends DiscordCommand {
    private final HashMap<MessageChannel, Hangman> hangmanGames;
    private final Dictionary dictionary;
    private static final int
            MIN_LENGTH = 1,
            MAX_LENGTH = 25;
    private static final String
            TRIGGER = "hm",
            ALT_TRIGGER = "hangman",
            TRIGGER_HELP = "[trigger]",
            GUESS = "guess",
            START = "start",
            HINT = "hint",
            STOP = "stop",
            AI = "ai",
            SPOILER_TAG = "||",
            WORD_HELP = SPOILER_TAG + "word" + SPOILER_TAG;
    private final ArrayList<Gallows> gallows;

    /**
     * Initialise dictionary for AI, gallows, and map of running games
     */
    public HangmanCommand() {
        super(
                TRIGGER,
                "Play hangman!",
                TRIGGER_HELP + " = " + TRIGGER + "/" + ALT_TRIGGER
                        + "\n\n" + TRIGGER_HELP + " " + START + " " + WORD_HELP
                        + "\n" + TRIGGER_HELP + " " + GUESS + " [word/letter]"
                        + "\n" + TRIGGER_HELP + " " + HINT
                        + "\n" + TRIGGER_HELP + " " + STOP
                        + "\n" + TRIGGER_HELP + " " + AI
        );
        this.hangmanGames = new HashMap<>();
        this.dictionary = filterDictionary();
        this.gallows = createGallows();
    }

    /**
     * Create a filtered copy of the Webster's English dictionary containing only words that
     * meet the criteria to be used in hangman.
     *
     * @return Filtered english Dictionary
     */
    private Dictionary filterDictionary() {
        Dictionary dictionary = new Dictionary();
        ArrayList<DictWord> allWords = Dictionary.getInstance().getWords();

        for(DictWord dictWord : allWords) {
            String word = dictWord.getWord();

            // Doesn't meet criteria for hangman game
            if(invalidInput(word)) {
                continue;
            }

            dictionary.addWord(dictWord);
        }
        return dictionary;
    }

    @Override
    public void execute(CommandContext context) {
        final MessageChannel channel = context.getMessageChannel();
        final Member player = context.getMember();
        final String content = context.getLowerCaseMessage();
        final String[] args = content.split(" ");

        // "hm" or "hangman"
        if(args.length == 1) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        final String trigger = args[0];

        // "guess", "stop", etc
        final String op = args[1];

        final String message = content.replaceFirst(trigger, "").trim();
        Hangman game = hangmanGames.get(channel);

        // Create a game
        if(game == null) {
            game = new Hangman("Type: hm for help");
            hangmanGames.put(channel, game);
        }

        // Message is not currently visible
        if(!game.canUpdate()) {
            channel.sendMessage("Slow down NOW").queue();
            return;
        }

        final Hangman finalGame = game;
        context.getMessage().delete().queue();

        new Thread(() -> {
            switch(op) {
                case START:
                    startGame(finalGame, message, channel, player);
                    break;
                case GUESS:
                    playerGuess(finalGame, player, channel, message);
                    break;
                case STOP:
                    if(!finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " There isn't a game to stop!").queue();
                        return;
                    }
                    finalGame.stopGame();
                    break;
                case HINT:
                    if(!finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " Here's a hint: You can't get a hint for a Hangman game if there are no Hangman games!").queue();
                        return;
                    }
                    finalGame.getHint(player);
                    break;
                case AI:
                    if(finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " Stop the current game if you want to play Hangman with me cunt").queue();
                        return;
                    }
                    finalGame.startGame(
                            getRandomGallows(),
                            channel,
                            dictionary.getRandomWord(),
                            context.getSelfMember()
                    );
                    break;
            }
        }).start();
    }

    /**
     * Get a random gallows
     *
     * @return Random gallows
     */
    private Gallows getRandomGallows() {
        return gallows.get(new Random().nextInt(gallows.size()));
    }

    /**
     * Make a guess for the given Hangman game
     *
     * @param game    Hangman game to guess for
     * @param player  Player guessing
     * @param channel Channel to send reply
     * @param message Player message - "guess [word]"
     */
    private void playerGuess(Hangman game, Member player, MessageChannel channel, String message) {

        // Game is no longer running and guesses can't be made
        if(!game.isRunning()) {
            channel.sendMessage(player.getAsMention() + " There isn't a game to guess the word for!").queue();
            return;
        }

        // Owner cannot guess on their own game
        if(player == game.getOwner()) {
            channel.sendMessage(player.getAsMention() + " The word picker can't make a fucking guess cunt").queue();
            return;
        }

        final String guess = message.replaceFirst(GUESS, "").trim();

        // The guess does not meet the same criteria that was used to check the hangman word (it cannot be correct)
        if(invalidInput(guess)) {
            channel.sendMessage(player.getAsMention() + " Give me a proper fucking word/letter cunt").queue();
            return;
        }
        game.guess(guess, player);
    }

    /**
     * Start the Hangman game
     *
     * @param game    Hangman game
     * @param message Player message - "start [word]"
     * @param channel Channel to play in
     * @param player  Player starting game
     */
    private void startGame(Hangman game, String message, MessageChannel channel, Member player) {

        // There is already a game running in the channel
        if(game.isRunning()) {
            channel.sendMessage(
                    player.getAsMention()
                            + " There's already a game running in this channel, you have to stop it first!"
            ).queue();
            return;
        }

        String word = message.replace(START, "").trim();

        /*
         * Word must be wrapped in spoiler tags. Not necessary, but there's a chance other players may see the word
         * before the bot has a chance to delete it otherwise.
         */
        if(!word.startsWith(SPOILER_TAG) || !word.endsWith(SPOILER_TAG)) {
            channel.sendMessage(
                    player.getAsMention() + " Place your word inside a spoiler tag! e.g: " + WORD_HELP
            ).queue();
            return;
        }

        word = word
                .replace(SPOILER_TAG, "")
                .replaceAll("\\s+", " ")
                .trim();

        // Not a valid word to use, inform the player
        if(invalidInput(word)) {
            channel.sendMessage(
                    player.getAsMention()
                            + " Minimum of " + MIN_LENGTH + " / Maximum of " + MAX_LENGTH + " characters "
                            + "(using only the alphabet)"
            ).queue();
            return;
        }

        // Attempt to retrieve the word from the dictionary (allows showing the definition once the game has ended)
        final DictWord dictWord = dictionary.getWord(word);

        game.startGame(
                getRandomGallows(),
                channel,

                // Not in the dictionary, create a dictionary word with no description
                dictWord == null ? new DictWord(word) : dictWord,
                player
        );
    }

    /**
     * Check the input is only alphabetical & is not empty
     *
     * @param input Input string
     * @return Alphabetical input
     */
    private boolean invalidInput(String input) {
        return !input.matches("[a-zA-Z ]+") || input.length() < MIN_LENGTH || input.length() > MAX_LENGTH;
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(TRIGGER) || query.startsWith(ALT_TRIGGER);
    }

    /**
     * Create the list of gallows instances that may be used for the game of Hangman
     *
     * @return List of gallows instances
     */
    private ArrayList<Gallows> createGallows() {
        ResourceHandler handler = new ResourceHandler();
        ArrayList<Gallows> gallows = new ArrayList<>();
        String path = "/Hangman/";
        JSONObject gallowsData = readJSONFile(path + "gallows.json");

        for(String gallowsName : gallowsData.keySet()) {
            JSONObject info = gallowsData.getJSONObject(gallowsName);
            String imagePath = path + info.getString("folder") + "/";
            JSONArray imageNames = info.getJSONArray("images");
            BufferedImage[] images = new BufferedImage[imageNames.length()];

            for(int i = 0; i < imageNames.length(); i++) {
                images[i] = handler.getImageResource(imagePath + imageNames.getString(i));
            }
            try {
                gallows.add(
                        new Gallows(
                                images,
                                info.getString("thumbnail")
                        )
                );
            }
            catch(IncorrectQuantityException e) {
                e.printStackTrace();
            }
        }
        return gallows;
    }
}
