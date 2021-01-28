package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkRequest;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONObject;

import java.util.Random;

import static Network.Secret.BROCK_ID;

/**
 * Get a random dad joke if brock ain't around
 */
public class JokeCommand extends DiscordCommand {
    private final Random random = new Random();
    private final String[] brockMessages;

    public JokeCommand() {
        super("joke", "Get a cool joke!");
        this.brockMessages = createBrockMessages();
    }

    @Override
    public void execute(CommandContext context) {
        Member brock = context.getGuild().getMemberById(BROCK_ID);

        if(brock != null && brock.getOnlineStatus() != OnlineStatus.OFFLINE) {
            return;
        }

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
        String mention = "<@" + BROCK_ID + ">";
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
