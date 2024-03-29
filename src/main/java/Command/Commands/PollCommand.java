package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Poll.Poll;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;

import static Command.Structure.PageableTableEmbed.*;

/**
 * Create a poll of up to 4 items and use emotes to vote
 */
public class PollCommand extends DiscordCommand {
    private final HashMap<Long, Poll> polls = new HashMap<>();

    public PollCommand() {
        super("poll [title] | [item] | [item]\npoll resend\npoll stop", "Poll up to 4 items!");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getMessageContent().substring(4).trim();
        MessageChannel channel = context.getMessageChannel();
        Member pollMaster = context.getMember();
        long channelId = channel.getIdLong();
        JDA jda = context.getJDA();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        Poll current = polls.get(channelId);

        if(message.equals("resend")) {
            if(pollMissing(current, channel, pollMaster)) {
                return;
            }
            current.relocateMessage();
            return;
        }

        if(message.equals("stop")) {
            if(pollMissing(current, channel, pollMaster)) {
                return;
            }
            current.stop();
            return;
        }

        String title = message.substring(0, message.indexOf('|')).trim();
        if(title.isEmpty()) {
            channel.sendMessage(pollMaster.getAsMention() + "You forgot the title bro").queue();
            return;
        }

        String delim = "\\|";
        String[] questions = message
                .replace(title, "")
                .replaceFirst(delim, "")
                .trim()
                .split(delim);

        if(current != null && current.isRunning()) {
            channel.sendMessage(
                    pollMaster.getAsMention() + " There's already a poll running in this channel, it will end in "
                            + current.getTimeRemaining()
            ).queue();
            return;
        }
        try {
            Poll poll = new Poll(channel, questions, title, jda, context.getEmoteHelper());
            polls.put(channelId, poll);
            poll.start();
        }
        catch(IncorrectQuantityException e) {
            channel.sendMessage(pollMaster.getAsMention() + " " + e.getMessage()).queue();
        }
    }

    /**
     * Check if the given poll is ended or doesn't exist
     *
     * @param poll    Poll to check
     * @param channel Channel to send message to if the poll does not exist
     * @param member  Member to mention
     * @return Poll missing
     */
    private boolean pollMissing(Poll poll, MessageChannel channel, Member member) {
        if(poll == null || !poll.isRunning()) {
            channel.sendMessage(member.getAsMention() + " There are no polls running in this channel!").queue();
            return true;
        }
        return false;
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("poll");
    }
}
