package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkRequest;
import Riddle.Riddle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
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

    public RiddleCommand() {
        super("riddle play\nriddle guess [guess]\nriddle hint\nriddle stop", "Try solve a riddle!");
        this.riddles = parseRiddles();
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

        switch(arg) {
            case "play":
                startRiddle(channel, player);
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
     * Reset the riddle guesses as it may have been used before
     *
     * @param channel Channel to map/send riddle to
     * @param player  Player to inform of existing riddle
     */
    private void startRiddle(MessageChannel channel, Member player) {
        Riddle riddle = games.get(channel);
        if(riddle != null) {
            channel.sendMessage(
                    player.getAsMention() + " There's already a riddle running, solve it or give up!\n\n"
                            + getRiddleQuestion(riddle)
            ).queue();
            return;
        }
        riddle = getRandomRiddle();
        riddle.resetGuesses();
        games.put(channel, riddle);
        channel.sendMessage(getRiddleQuestion(riddle)).queue();
    }

    /**
     * Get a random riddle from the list
     *
     * @return Random riddle
     */
    private Riddle getRandomRiddle() {
        return riddles.get(random.nextInt(riddles.size()));
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

        channel.sendMessage(
                player.getAsMention() + "\n\n" + getRiddleSummary(riddle)

        ).queue();
        games.remove(channel);
    }

    /**
     * Get the summary of the given riddle, displaying the question and answer
     *
     * @param riddle Riddle to get summary for
     * @return Riddle summary
     */
    private String getRiddleSummary(Riddle riddle) {
        return getRiddleQuestion(riddle)
                + "\n" + "Answer: **" + riddle.getAnswer() + "**"
                + "\n" + "Guesses: **" + riddle.getGuesses() + "**";
    }

    /**
     * Get the riddle question formatted in bold
     *
     * @param riddle Riddle to format question
     * @return Formatted riddle question
     */
    private String getRiddleQuestion(Riddle riddle) {
        return "Riddle: **" + riddle.getQuestion() + "**";
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

        if(!riddle.guessRiddle(guess)) {
            channel.sendMessage(
                    player.getAsMention() + " " + getIncorrectResponse() + "\n\n" + getRiddleQuestion(riddle)
            ).queue();
            return;
        }
        channel.sendMessage(
                player.getAsMention() + " " + getCorrectResponse() + "\n\n" + getRiddleSummary(riddle)
        ).queue();
        games.remove(channel);
    }

    /**
     * Get a random response to send for an incorrect guess
     *
     * @return Response for incorrect guess
     */
    private String getIncorrectResponse() {
        String[] responses = new String[]{
                "Incorrect!",
                "Wrong!",
                "Not even close!",
                "Guess again",
                "The real riddle is how are you so shit at this?",
                "I figured this one out ages ago",
                "NO",
                "Are you even trying?"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * Get a random response to send for a correct guess
     *
     * @return Response for correct guess
     */
    private String getCorrectResponse() {
        String[] responses = new String[]{
                "Correct!",
                "You did it!",
                "Nice one!",
                "Is that the Riddler?",
                "What a guess!",
                "YES",
                "Wow!"
        };
        return responses[random.nextInt(responses.length)];
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
