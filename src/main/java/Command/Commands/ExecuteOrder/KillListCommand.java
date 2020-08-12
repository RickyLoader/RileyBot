package Command.Commands.ExecuteOrder;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.time.chrono.MinguoDate;
import java.util.List;

import static Command.Commands.ExecuteOrder.ExecuteOrder66Command.getName;
import static Command.Structure.EmbedHelper.getTitleField;
import static Command.Structure.EmbedHelper.getValueField;

/**
 * Get a message displaying everyone on the kill list
 */
public class KillListCommand extends DiscordCommand {
    private final ExecutorHandler executorHandler;

    public KillListCommand() {
        super("kill list", "Check out who is on the kill list!");
        this.executorHandler = new ExecutorHandler();
    }

    /**
     * Send a message to the user displaying all members with the 'target' role
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue();
        List<Member> targets = context.getTargets();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("KILL LIST");
        builder.setColor(EmbedHelper.getRed());
        builder.setThumbnail(executorHandler.getRandomExecutor().getImage());

        if(targets.isEmpty()) {
            builder.setDescription("There are no targets sir.");
            builder.setFooter("Add the 'target' role to anyone you need taken care of.");
            privateMessage(context.getUser(), builder.build());
            return;
        }

        String desc = targets.size() == 1 ? "There is 1 target waiting for your order sir." : "There are " + targets.size() + " targets waiting for your order sir.";
        builder.setDescription(desc);


        for(int i = 0; i < targets.size(); i++) {
            Member target = targets.get(i);
            String name = getName(target);
            String number = String.valueOf(i + 1);

            if(i == 0) {
                builder.addField(getTitleField("#", number));
                builder.addBlankField(true);
                builder.addField(getTitleField("TARGET", name));
                continue;
            }
            builder.addField(getValueField(number));
            builder.addBlankField(true);
            builder.addField(getValueField(name));
        }

        builder.setFooter("Execute order 66 when you're ready.");
        privateMessage(context.getUser(), builder.build());
    }

    /**
     * Send the embed to the user
     *
     * @param user  User who called command
     * @param embed Embed displaying the kill list
     */
    private void privateMessage(User user, MessageEmbed embed) {
        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(embed).queue();
            privateChannel.close().queue();
        });
    }
}