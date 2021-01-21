package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Poll.Poll;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;

/**
 * Create a poll of up to 4 items and use emotes to vote
 */
public class PollCommand extends DiscordCommand {
    private final HashMap<Long, Poll> polls = new HashMap<>();

    public PollCommand() {
        super("poll [item] | [item]\npoll resend\npoll stop", "Poll up to 4 items!");
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
            current.refreshPollMessage();
            return;
        }

        if(message.equals("stop")) {
            if(pollMissing(current, channel, pollMaster)) {
                return;
            }
            current.stop(jda);
            return;
        }

        String[] questions = message.split("\\|");
        if(questions.length == 1) {
            channel.sendMessage(pollMaster.getAsMention() + " What kind of poll has 1 item?").queue();
            return;
        }

        if(questions.length > 4) {
            channel.sendMessage(pollMaster.getAsMention() + " Let's keep it to 4 items bro").queue();
            return;
        }

        if(current != null && current.isRunning()) {
            channel.sendMessage(
                    pollMaster.getAsMention() + " There's already a poll running in this channel, it will end in "
                            + current.getTimeRemaining()
            ).queue();
            return;
        }

        Poll poll = new Poll(channel, questions, jda, context.getEmoteHelper());
        polls.put(channelId, poll);
        poll.start();
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
    public boolean matches(String query) {
        return query.startsWith("poll");
    }
}
