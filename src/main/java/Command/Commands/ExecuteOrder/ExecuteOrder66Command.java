package Command.Commands.ExecuteOrder;

import Audio.TrackEndListener;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.EmbedLoadingMessage.Status;
import ExecuteOrder.ExecutorHandler;
import ExecuteOrder.ExecutorHandler.Executor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;

import static Command.Structure.EmbedHelper.getTitleField;
import static Command.Structure.EmbedHelper.getValueField;

/**
 * Kick all members with the 'target' role
 */
public class ExecuteOrder66Command extends DiscordCommand {

    private HashMap<Member, String> targetStatus;
    private List<Member> targets;
    private long id;
    private Executor executor;
    private String image;
    private boolean finished;
    private Status status;

    public ExecuteOrder66Command() {
        super("execute order 66", "Execute targets on the kill list!");
    }


    /**
     * Kick all members with the 'target' role, apologise in a private message and give them an invite
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().complete();
        Member instigator = context.getMember();
        targets = context.getTargets();
        MessageChannel channel = context.getMessageChannel();

        if(!instigator.hasPermission(Permission.KICK_MEMBERS) && context.getSelfMember().canInteract(instigator)) {
            context.getGuild().addRoleToMember(instigator, context.getTargetRole()).queue(aVoid -> context.getMessageChannel().sendMessage(instigator.getAsMention() + " big mistake cunt, now you're on the kill list.").queue());
            return;
        }

        if(status == null) {
            status = new Status(context.getEmoteHelper());
        }

        if(executor != null) {
            channel.sendMessage("I'm already doing that!").queue();
            return;
        }

        if(targets.isEmpty()) {
            context.getMessageChannel().sendMessage("Target sectors are already clear sir.").queue();
            return;
        }

        executor = new ExecutorHandler().getRandomExecutor();

        image = executor.getImage();
        targetStatus = new HashMap<>();

        for(Member target : targets) {
            targetStatus.put(target, status.getNeutral());
        }

        channel.sendMessage(
                buildStatusMessage()
        ).queue(message -> {
            id = message.getIdLong();
            context.playAudio(
                    executor.getTrack(),
                    () -> new Thread(() -> {
                        purgeTargets(context);
                        executor = null;
                    }).start()
            );
        });
    }

    /**
     * Build the status embed displaying the targets and the current status of their kick
     *
     * @return Status embed
     */
    private MessageEmbed buildStatusMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("EXECUTING ORDER 66")
                .setColor(finished ? EmbedHelper.getGreen() : EmbedHelper.getRed())
                .setThumbnail(image)
                .setImage(EmbedHelper.getSpacerImage());

        long executed = targetStatus.entrySet().stream().filter(x -> x.getValue().equals(status.getComplete())).count();
        builder.setDescription(executed + "/" + targets.size() + " targets exterminated.");

        for(int i = 0; i < targets.size(); i++) {
            Member target = targets.get(i);
            String number = String.valueOf(i + 1);
            String name = getName(target);
            String status = targetStatus.get(target);
            if(i == 0) {
                builder
                        .addField(getTitleField("#", number))
                        .addField(getTitleField("TARGET", name))
                        .addField(getTitleField("EXECUTED", status));
                continue;
            }
            builder.addField(getValueField(number))
                    .addField(getValueField(name))
                    .addField(getValueField(status));
        }
        return builder.build();
    }

    /**
     * Edit the status message
     *
     * @param channel Channel to find status message
     */
    private void updateStatusMessage(MessageChannel channel) {
        channel.editMessageById(id, buildStatusMessage()).queue();
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
                targetStatus.put(target, status.getComplete());
            }
            catch(Exception e) {
                targetStatus.put(target, status.getFail());
            }
            if(targets.indexOf(target) == targets.size() - 1) {
                this.finished = true;
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
            MessageEmbed apology = new EmbedBuilder()
                    .setColor(EmbedHelper.getRed())
                    .setTitle("I AM SORRY FOR KICKING YOU")
                    .setDescription(
                            "Sorry about that bro, i'm actually a really friendly bot when you get to know me but I have to do what i'm told."
                    )
                    .addField("Feel free to join back though!", invite, true)
                    .setThumbnail(image)
                    .build();
            loser.openPrivateChannel().complete().sendMessage(apology).complete();
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
