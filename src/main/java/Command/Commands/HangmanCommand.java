package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Hangman.Hangman;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Play Hangman! Fun!
 */
public class HangmanCommand extends DiscordCommand {
    private final HashMap<MessageChannel, Hangman> hangmanGames;
    private final HashSet<String> dictionary;
    private final int MIN_LENGTH = 5, MAX_LENGTH = 25;

    public HangmanCommand() {
        super("hm start [word]\nhm guess [word/letter]\nhm hint\nhm stop\nhm ai", "Play hangman!");
        this.hangmanGames = new HashMap<MessageChannel, Hangman>();
        this.dictionary = parseDictionary();
    }

    /**
     * Parse Webster's English dictionary in to a HashSet
     *
     * @return English Dictionary in HashSet
     */
    private HashSet<String> parseDictionary() {
        System.out.println("Parsing Webster's English dictionary...");
        JSONObject data = new JSONObject(
                new ResourceHandler().getResourceFileAsString("/Hangman/dictionary.json")
        );
        HashSet<String> dictionary = new HashSet<>();
        for(String word : data.keySet()) {
            if(invalidInput(word)) {
                continue;
            }
            dictionary.add(word.toLowerCase());
        }
        return dictionary;
    }


    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member player = context.getMember();
        String content = context.getLowerCaseMessage();

        if(content.startsWith("hangman")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        String message = content.replaceFirst("hm", "").trim();
        String op = message.split(" ")[0];

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        Hangman game = hangmanGames.get(channel);

        if(game == null) {
            game = new Hangman(getHelpName().replace("\n", " | "));
            hangmanGames.put(channel, game);
        }

        if(!game.canUpdate()) {
            channel.sendMessage("Slow down NOW").queue();
            return;
        }

        Hangman finalGame = game;
        new Thread(() -> {
            switch(op) {
                case "start":
                    context.getMessage().delete().queue();
                    startGame(finalGame, message, channel, player);
                    break;
                case "guess":
                    playerGuess(finalGame, player, channel, message);
                    break;
                case "stop":
                    if(!finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " There isn't a game to stop!").queue();
                        return;
                    }
                    finalGame.stopGame();
                    break;
                case "hint":
                    if(!finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " Here's a hint: You can't get a hint for a Hangman game if there are no Hangman games!").queue();
                        return;
                    }
                    finalGame.getHint(player);
                    break;
                case "ai":
                    if(finalGame.isRunning()) {
                        channel.sendMessage(player.getAsMention() + " Stop the current game if you want to play Hangman with me cunt").queue();
                        return;
                    }
                    finalGame.startGame(channel, getRandomWord(), context.getSelfMember());
                    break;
            }
        }).start();
    }

    /**
     * Get a random word from the dictionary for playing
     * Hangman with RileyBot
     *
     * @return Random word within MIN_LENGTH & MAX_LENGTH
     */
    private String getRandomWord() {
        ArrayList<String> possibilities = dictionary
                .stream()
                .filter(word -> word.length() >= MIN_LENGTH && word.length() <= MAX_LENGTH)
                .collect(Collectors.toCollection(ArrayList::new));
        return possibilities.get(new Random().nextInt(possibilities.size()));
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
        if(!game.isRunning()) {
            channel.sendMessage(player.getAsMention() + " There isn't a game to guess the word for!").queue();
            return;
        }
        if(player == game.getOwner()) {
            channel.sendMessage(player.getAsMention() + " The word picker can't make a fucking guess cunt").queue();
            return;
        }

        String guess = message.replaceFirst("guess", "").trim();
        if(invalidInput(guess)) {
            channel.sendMessage(
                    player.getAsMention() + " Give me a proper fucking word/letter cunt"
            ).queue();
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
        if(game.isRunning()) {
            channel.sendMessage(player.getAsMention() + " There's already a game running in this channel, you have to stop it first!").queue();
            return;
        }
        String word = message.replace("start", "").trim();

        if(!word.startsWith("||") || !word.endsWith("||")) {
            channel.sendMessage(player.getAsMention() + " Place your word inside a spoiler tag! e.g hangman start ||word||").queue();
            return;
        }
        word = word.replaceAll("\\|", "");

        if(invalidInput(word)) {
            channel.sendMessage(
                    player.getAsMention() + " Input must be a one word, using only the alphabet."
            ).queue();
            return;
        }

        if(word.length() < MIN_LENGTH || word.length() > MAX_LENGTH) {
            channel.sendMessage(player.getAsMention() + " Minimum of " + MIN_LENGTH + " / Maximum of " + MAX_LENGTH + " characters").queue();
            return;
        }

        game.startGame(channel, word, player);
    }

    /**
     * Check the input is only alphabetical & is not empty
     *
     * @param input Input string
     * @return Alphabetical input
     */
    private boolean invalidInput(String input) {
        return !input.matches("[a-z]+");
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("hm") || query.startsWith("hangman");
    }
}
