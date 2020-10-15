package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Hangman.Hangman;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;

/**
 * Play Hangman! Fun!
 */
public class HangmanCommand extends DiscordCommand {
    private final HashMap<MessageChannel, Hangman> hangmanGames;

    public HangmanCommand() {
        super("hangman start [word]\nhangman guess [word/letter]\nhangman hint\nhangman stop", "Play hangman!");
        this.hangmanGames = new HashMap<MessageChannel, Hangman>();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member player = context.getMember();

        String message = context.getLowerCaseMessage().replaceFirst("hangman", "").trim();
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
            }
        }).start();
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
                    player.getAsMention() + " Input must be one word, using only the alphabet."
            ).queue();
            return;
        }

        if(word.length() < 5 || word.length() > 25) {
            channel.sendMessage(player.getAsMention() + " Minimum of 5 / Maximum of 25 characters").queue();
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
        return query.startsWith("hangman");
    }
}
