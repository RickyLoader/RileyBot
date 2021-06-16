package Command.Commands.Runescape;

import Command.Structure.*;
import Runescape.OSRS.Polling.PageableMessage.PollMessage;
import Runescape.OSRS.Polling.PageableMessage.PollSearchResultsMessage;
import Runescape.OSRS.Polling.Poll;
import Runescape.OSRS.Polling.PollManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

/**
 * View OSRS past and previous polls in a pageable message embed
 */
public class OSRSPollCommand extends DiscordCommand {
    private final PollManager pollManager;
    private static final String
            LATEST = "latest",
            TRIGGER = "osrspoll";

    public OSRSPollCommand() {
        super(
                TRIGGER,
                "Look at poll results!",
                TRIGGER + " " + LATEST + "\n" + TRIGGER + " [poll number/poll name]"
        );
        this.pollManager = new PollManager();
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace(getTrigger(), "").trim();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        channel.sendTyping().queue();

        int number = 0;

        // Not asking for latest
        if(!message.equals(LATEST)) {
            number = toInteger(message);

            // Message was NaN, Searching by title
            if(number == 0) {
                ArrayList<Poll> results = pollManager.getPollsByTitle(message);
                if(results.isEmpty()) {
                    channel.sendMessage(
                            member.getAsMention()
                                    + " I didn't find any polls with **" + message + "** in the **title**"
                    ).queue();
                    return;
                }
                if(results.size() > 1) {
                    new PollSearchResultsMessage(
                            results,
                            message,
                            "Try: " + getHelpName(),
                            context
                    ).showMessage();
                    return;
                }

                // Assign poll number
                number = results.get(0).getNumber();
            }
        }

        Poll poll = pollManager.getPollByNumber(number);
        if(poll == null) {
            channel.sendMessage(
                    member.getAsMention()
                            + " Poll #" + number + " doesn't exist (or I couldn't parse it)"
            ).queue();
            return;
        }
        new PollMessage(context, poll, "Type: " + getTrigger() + " for help").showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
