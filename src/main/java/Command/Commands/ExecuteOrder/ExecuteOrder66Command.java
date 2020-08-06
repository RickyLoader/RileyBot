package Command.Commands.ExecuteOrder;

import Audio.DiscordAudioPlayer;
import Audio.TrackEndListener;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;

import static Command.Structure.EmbedHelper.getTitleField;
import static Command.Structure.EmbedHelper.getValueField;

/**
 * Kick all members with the 'target' role
 */
public class ExecuteOrder66Command extends DiscordCommand {

    // Map enum name to emoji for displaying on embed
    enum STATUS {
        EXECUTED("\uD83D\uDDF9"),
        FAILED("☒"),
        PENDING("☐");
        public final String emoji;

        STATUS(String emoji) {
            this.emoji = emoji;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    private HashMap<Member, STATUS> targetStatus;
    private List<Member> targets;
    private long id;
    private ExecutorHandler.Executor executor;
    private String image;

    public ExecuteOrder66Command() {
        super("execute order 66", "Execute targets on the kill list!");
    }

    /**
     * Map the targets to the pending status
     *
     * @param targets Members with 'target' role
     * @return Map of member->status
     */
    private HashMap<Member, STATUS> getTargetStatus(List<Member> targets) {
        HashMap<Member, STATUS> targetStatus = new HashMap<>();
        for(Member target : targets) {
            targetStatus.put(target, STATUS.PENDING);
        }
        return targetStatus;
    }

    /**
     * Kick all members with the 'target' role, apologise in a private message and give them an invite
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().complete();

        // Impatient, command is currently in progress
        if(executor != null) {
            return;
        }
        executor = new ExecutorHandler().getRandomExecutor();

        // Image is randomly selected, save to variable to use the same image in private message
        image = executor.getImage();
        targets = context.getTargets();

        if(targets.isEmpty()) {
            context.getMessageChannel().sendMessage("Target sectors are already clear sir.").queue();
            return;
        }

        targetStatus = getTargetStatus(targets);

        context.getMessageChannel().sendMessage(buildStatusMessage()).queue(message -> {
            id = message.getIdLong();
            TrackEndListener.Response method = new Thread(() -> {
                purgeTargets(context);
                executor = null;
            })::start;
            TrackEndListener listener = new TrackEndListener(method, context.getGuild());

            // Play the track
            new DiscordAudioPlayer(context.getMember(), context.getGuild(), listener).play(executor.getTrack());
        });
    }

    /**
     * Build the status embed displaying the targets and the current status of their kick
     *
     * @return Status embed
     */
    private MessageEmbed buildStatusMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("EXECUTING ORDER 66");
        builder.setColor(16711680);
        builder.setThumbnail(image);

        long executed = targetStatus.entrySet().stream().filter(x -> x.getValue() == STATUS.EXECUTED).count();
        builder.setDescription(executed + "/" + targets.size() + " targets exterminated.");

        for(int i = 0; i < targets.size(); i++) {
            Member target = targets.get(i);
            String number = String.valueOf(i + 1);
            String name = getName(target);
            STATUS status = targetStatus.get(target);
            if(i == 0) {
                builder.addField(getTitleField("#", number));
                builder.addField(getTitleField("TARGET", name));
                builder.addField(getTitleField("EXECUTED", status.getEmoji()));
                continue;
            }
            builder.addField(getValueField(number));
            builder.addField(getValueField(name));
            builder.addField(getValueField(status.getEmoji()));

        }
        return builder.build();
    }

    /**
     * Edit the status message
     *
     * @param channel Channel to find status message
     */
    private void updateStatusMessage(MessageChannel channel) {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(buildStatusMessage()).queue());
    }

    /**
     * Kick all members of the 'target' role
     *
     * @param context Context of command
     */
    private void purgeTargets(CommandContext context) {
        String invite = context.getInvite();
        for(Member target : targets) {
            try {
                apologise(target.getUser(), invite);
                context.getGuild().kick(target).complete();
                targetStatus.put(target, STATUS.EXECUTED);
            }
            catch(Exception e) {
                targetStatus.put(target, STATUS.FAILED);
            }
            updateStatusMessage(context.getMessageChannel());
        }
    }

    /**
     * Attempt to private message the user before they are kicked
     *
     * @param loser  User about to be kicked
     * @param invite An invite back to the server
     */
    private void apologise(User loser, String invite) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(15655767);
            builder.setTitle("I AM SORRY FOR KICKING YOU");
            builder.setDescription("Sorry about that bro, i'm actually a really friendly bot when you get to know me but I have to do what i'm told.");
            builder.addField("Feel free to join back though!", invite, true);
            builder.setThumbnail(image);
            loser.openPrivateChannel().complete().sendMessage(builder.build()).complete();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the name used in the status message Either name or nickname/name
     *
     * @param m Member to get name for
     * @return Formatted name for status message
     */
    static String getName(Member m) {
        StringBuilder builder = new StringBuilder();
        if(m.getNickname() != null) {
            builder.append(m.getNickname());
            builder.append("/");
        }
        builder.append(m.getUser().getName());
        return builder.toString();
    }
}
