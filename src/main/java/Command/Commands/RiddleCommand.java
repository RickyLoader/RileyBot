package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Riddle.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Guess the answers to some riddles!
 */
public class RiddleCommand extends DiscordCommand {
    private final Random random = new Random();
    private final HashMap<MessageChannel, Riddle> games = new HashMap<>();
    private final ArrayList<Riddle> riddles;
    private final String[] titles, winMessages;

    /**
     * Parse the riddles from a json file and initialise title/win message possibilities
     */
    public RiddleCommand() {
        super("riddle play\nriddle guess [guess]\nriddle hint\nriddle stop", "Try solve a riddle!");
        this.riddles = parseRiddles();
        this.titles = getTitles();
        this.winMessages = getWinMessages();
    }

    /**
     * Get an array of riddle titles to be used when displaying a riddle
     *
     * @return Array of titles
     */
    private String[] getTitles() {
        return new String[]{
                "Can you solve the riddle?",
                "Riddle time!",
                "Riddle Riddle Riddle!",
                "Riddle - Let's do this!",
                "Time for a riddle!",
                "The Riddler has arrived!"
        };
    }

    /**
     * Get an array of win messages to be used when a riddle has been solved
     *
     * @return Array of win messages
     */
    private String[] getWinMessages() {
        return new String[]{
                "Correct!",
                "You did it!",
                "Nice one!",
                "Is that the Riddler?",
                "What a guess!",
                "YES",
                "Wow!"
        };
    }

    /**
     * Parse the list of riddles from a json file
     *
     * @return List of riddles
     */
    private ArrayList<Riddle> parseRiddles() {
        JSONArray riddleData = readJSONFile("/Riddle/riddles.json").getJSONArray("riddles");
        ArrayList<Riddle> riddles = new ArrayList<>();

        for(int i = 0; i < riddleData.length(); i++) {
            JSONObject riddle = riddleData.getJSONObject(i);
            riddles.add(
                    new Riddle(
                            riddle.getString("question"),
                            riddle.getString("answer")
                    )
            );
        }
        return riddles;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member player = context.getMember();

        String message = context.getLowerCaseMessage().replace("riddle", "").trim();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        String arg = message.split(" ")[0];
        message = message.replaceFirst(arg, "").trim();
        context.getMessage().delete().queue();

        switch(arg) {
            case "play":
            case "start":
            case "commence":
            case "begin":
            case "go":
                startRiddle(channel);
                break;
            case "guess":
                guessRiddle(channel, player, message);
                break;
            case "hint":
                getHint(channel, player);
                break;
            case "stop":
                stopRiddle(channel, player);
                break;
        }
    }

    /**
     * Get a hint for the current riddle
     *
     * @param channel Channel to get riddle for
     * @param player  Player requesting hint
     */
    private void getHint(MessageChannel channel, Member player) {
        Riddle riddle = games.get(channel);
        if(riddle == null) {
            channel.sendMessage(
                    player.getAsMention() + " Here's a hint: There are no fucking riddles at the moment"
            ).queue();
            return;
        }
        channel.sendMessage(player.getAsMention() + " " + fetchHint()).queue();
    }

    /**
     * Start a riddle session, map a random riddle to the channel and send the question
     *
     * @param channel Channel to map/send riddle to
     */
    private void startRiddle(MessageChannel channel) {
        Riddle riddle = games.get(channel);
        if(riddle == null) {
            riddle = getRandomRiddle();
            games.put(channel, riddle);
        }
        sendRiddleMessage(channel, riddle);
    }

    /**
     * Build and send the riddle message
     *
     * @param channel Channel to send message to
     * @param riddle  Riddle to send message for
     */
    private void sendRiddleMessage(MessageChannel channel, Riddle riddle) {
        MessageEmbed riddleMessage = buildRiddleMessage(riddle);
        if(riddle.hasMessage()) {
            channel.deleteMessageById(riddle.getId()).queue();
        }
        channel.sendMessage(riddleMessage).queue(message -> riddle.setId(message.getIdLong()));
    }

    /**
     * Build a message embed detailing the riddle
     *
     * @param riddle Riddle to build message for
     * @return Message embed showing riddle status, guesses, etc
     */
    private MessageEmbed buildRiddleMessage(Riddle riddle) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(riddle.getTitle())
                .setColor(getRiddleColour(riddle))
                .setThumbnail("https://i.imgur.com/BGdvfJd.png")
                .setFooter("Type riddle for help", "https://i.imgur.com/wKYK3c7.png")
                .setImage(EmbedHelper.SPACER_IMAGE);

        String desc = "**Riddle**: " + riddle.getQuestion() + "\n\n" + "**Guesses**: " + riddle.formatGuesses();

        if(!riddle.isRunning()) {
            desc += "\n**Answer**: " + riddle.getAnswer();
            if(riddle.isSolved()) {
                Guess correct = riddle.getCorrectGuess();
                desc += "\n\n**Winner**: " + correct.getGuesser().getAsMention() + " " + getWinMessage();
            }
        }

        return builder
                .setDescription(desc)
                .build();
    }

    /**
     * Get the colour to use when displaying the given riddle
     * Use green for a solved riddle, yellow for an in progress riddle, and red for a forfeited riddle
     *
     * @param riddle Riddle to get colour for
     * @return Colour to use when displaying riddle
     */
    private int getRiddleColour(Riddle riddle) {
        return riddle.isSolved() ? EmbedHelper.GREEN : (riddle.isRunning() ? EmbedHelper.YELLOW : EmbedHelper.RED);
    }

    /**
     * Get a random riddle from the list and return a deep copy
     * Assign a random title to be used for the messages
     *
     * @return Random riddle
     */
    private Riddle getRandomRiddle() {
        Riddle riddle = riddles.get(random.nextInt(riddles.size()));
        Riddle copy = new Riddle(riddle.getQuestion(), riddle.getAnswer());
        copy.setTitle(getRiddleTitle());
        return copy;
    }

    /**
     * Stop the current channel's riddle if there is one
     *
     * @param channel Channel to check for riddle
     * @param player  Member attempting to stop riddle
     */
    private void stopRiddle(MessageChannel channel, Member player) {
        Riddle riddle = games.get(channel);
        if(riddle == null) {
            channel.sendMessage(player.getAsMention() + " stop what?").queue();
            return;
        }
        riddle.stopRiddle();
        games.remove(channel);
        sendRiddleMessage(channel, riddle);
    }

    /**
     * Guess the answer to the current riddle
     *
     * @param channel Channel to check for riddle
     * @param player  Player guessing the answer
     * @param guess   Answer guess
     */
    private void guessRiddle(MessageChannel channel, Member player, String guess) {
        Riddle riddle = games.get(channel);
        if(riddle == null) {
            channel.sendMessage(player.getAsMention() + " There isn't a riddle to guess against!").queue();
            return;
        }

        if(guess.isEmpty()) {
            channel.sendMessage(player.getAsMention() + " what?").queue();
            return;
        }

        if(riddle.hasGuessedWord(guess)) {
            channel.sendMessage(player.getAsMention() + " That's already been guessed!").queue();
            return;
        }
        if(riddle.guess(player, guess)) {
            games.remove(channel);
        }
        sendRiddleMessage(channel, riddle);
    }

    /**
     * Get a random response to send for a correct guess
     *
     * @return Response for correct guess
     */
    private String getWinMessage() {
        return winMessages[random.nextInt(winMessages.length)];
    }

    /**
     * Get a random response to use as the riddle title
     *
     * @return Response for correct guess
     */
    private String getRiddleTitle() {
        return titles[random.nextInt(titles.length)];
    }

    /**
     * Get a random useless fact to send as a hint (super funny)
     *
     * @return Random hint
     */
    private String fetchHint() {
        JSONObject hint = new JSONObject(
                new NetworkRequest(
                        "https://uselessfacts.jsph.pl/random.json?language=en",
                        false
                ).get().body
        );
        return hint.getString("text");
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("riddle");
    }
}
