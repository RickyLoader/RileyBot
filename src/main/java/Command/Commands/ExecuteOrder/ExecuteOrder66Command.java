package Command.Commands.ExecuteOrder;

import Audio.DiscordAudioPlayer;
import Audio.TrackEndListener;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;

public class ExecuteOrder66Command extends DiscordCommand {

    enum STATUS {
        EXECUTED,
        FAILED,
        PENDING
    }

    private final HashMap<STATUS, String> status = new HashMap<>();
    private HashMap<Member, STATUS> targetStatus;
    private List<Member> targets;
    private long id;
    private ExecutorHandler.Executor executor;
    private String image;

    public ExecuteOrder66Command() {
        super("execute order 66", "Execute targets on the kill list!");
        status.put(STATUS.EXECUTED, "\uD83D\uDDF9");
        status.put(STATUS.FAILED, "☒");
        status.put(STATUS.PENDING, "☐");
    }

    private HashMap<Member, STATUS> getTargetStatus(List<Member> targets) {
        HashMap<Member, STATUS> targetStatus = new HashMap<>();
        for(Member target : targets) {
            targetStatus.put(target, STATUS.PENDING);
        }
        return targetStatus;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().complete();
        if(executor != null) {
            return;
        }
        executor = new ExecutorHandler().getRandomExecutor();
        image = executor.getImage();
        targets = context.getTargets();

        if(targets.isEmpty()) {
            context.getMessageChannel().sendMessage("Target sectors are already clear sir.").queue();
            return;
        }

        targetStatus = getTargetStatus(targets);

        context.getMessageChannel().sendMessage(buildStatusMessage()).queue(message -> {
            id = message.getIdLong();

            // Implement the Response interface method to purge the kill list after the track finishes
            TrackEndListener.Response method = new Thread(() -> {
                purgeTargets(context);
                executor = null;
            })::start;
            TrackEndListener listener = new TrackEndListener(method, context.getGuild());

            // Play the track
            new DiscordAudioPlayer(context.getMember(), context.getGuild(), listener).play(executor.getTrack());
        });
    }

    private MessageEmbed buildStatusMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("EXECUTING ORDER 66");
        builder.setColor(16711680);
        builder.setThumbnail(image);
        builder.addField("#", "1", true);
        builder.addField("TARGET", getName(targets.get(0)), true);
        builder.addField("EXECUTED", status.get(targetStatus.get(targets.get(0))), true);

        long executed = targetStatus.entrySet().stream().filter(x -> x.getValue() == STATUS.EXECUTED).count();
        builder.setDescription(executed + "/" + targets.size() + " targets exterminated.");
        for(int i = 1; i < targets.size(); i++) {
            Member target = targets.get(i);
            builder.addField("\u200B", String.valueOf(i + 1), true);
            builder.addField("\u200B", getName(target), true);
            builder.addField("\u200B", status.get(targetStatus.get(target)), true);
        }
        return builder.build();
    }

    private void updateStatusMessage(MessageChannel channel) {
        channel.retrieveMessageById(id).queue(message -> message.editMessage(buildStatusMessage()).queue());
    }

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

    private String getName(Member m) {
        StringBuilder builder = new StringBuilder();
        if(m.getNickname() != null) {
            builder.append(m.getNickname());
            builder.append("/");
        }
        builder.append(m.getUser().getName());
        return builder.toString();
    }
}
