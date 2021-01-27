package Command.Commands.Runescape;

import Command.Structure.*;
import Runescape.OSRS.Polling.PageableMessage.PollMessage;
import Runescape.OSRS.Polling.PageableMessage.PollSearchResultsMessage;
import Runescape.OSRS.Polling.Poll;
import Runescape.OSRS.Polling.PollManager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

/**
 * View OSRS past and previous polls in a pageable message embed
 */
public class OSRSPollCommand extends DiscordCommand {
    private final PollManager pollManager;

    public OSRSPollCommand() {
        super("osrspoll\nosrspoll [poll number]", "Look at poll results!");
        this.pollManager = new PollManager();
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace("osrspoll", "").trim();
        MessageChannel channel = context.getMessageChannel();

        int number = 0;
        if(!message.isEmpty()) {
            number = getQuantity(message);
            if(number == 0) {
                ArrayList<Poll> results = pollManager.getPollsByTitle(message);
                if(results.isEmpty()) {
                    context.getMessageChannel().sendMessage(
                            context.getMember().getAsMention()
                                    + " I didn't find any polls with **" + message + "** in the title"
                    ).queue();
                    return;
                }
                if(results.size() > 1) {
                    new PollSearchResultsMessage(results, message, context).showMessage();
                    return;
                }
                number = results.get(0).getNumber();
            }
        }

        Poll poll = pollManager.getPollByNumber(number);
        if(poll == null) {
            channel.sendMessage(
                    context.getMember().getAsMention()
                            + " Poll #" + number + " doesn't exist (or I couldn't parse it)"
            ).queue();
            return;
        }
        new PollMessage(context, poll).showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("osrspoll");
    }
}
