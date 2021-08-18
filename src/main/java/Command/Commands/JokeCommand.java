package Command.Commands;

import Command.Structure.BrockCommand;
import Command.Structure.CommandContext;
import Network.NetworkRequest;
import org.json.JSONObject;

import java.util.Random;

/**
 * Get a random dad joke if brock ain't around
 */
public class JokeCommand extends BrockCommand {
    private final Random random = new Random();
    private final String[] brockMessages;

    public JokeCommand() {
        super("joke", "Get a cool joke!");
        this.brockMessages = createBrockMessages();
    }

    @Override
    protected void onAbsent(CommandContext context) {
        String joke = new JSONObject(
                new NetworkRequest("https://icanhazdadjoke.com", false).get().body
        ).getString("joke");

        context.getMessageChannel().sendMessage(
                getBrockMessage() + ":\n\n" + joke
        ).queue();
    }

    /**
     * Get a message to Brock for prepending to the joke
     *
     * @return Random Brock message
     */
    private String getBrockMessage() {
        return brockMessages[random.nextInt(brockMessages.length)];
    }

    /**
     * Get a list of messages insulting Brock
     *
     * @return Brock messages
     */
    private String[] createBrockMessages() {
        final String mention = getUserMention();
        return new String[]{
                "For fuck's sake " + mention,
                "Fine, I'll do it myself",
                "Where are you " + mention + "?",
                "I miss you " + mention,
                mention + " is OFFLINE",
                "This is a really funny one " + mention + " wish you were still with us to see it",
                mention + " where have you gone?"
        };
    }
}
