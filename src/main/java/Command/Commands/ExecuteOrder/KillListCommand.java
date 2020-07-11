package Command.Commands.ExecuteOrder;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class KillListCommand extends DiscordCommand {
    private ExecutorHandler executorHandler;

    public KillListCommand() {
        super("kill list", "Check out who is on the kill list!");
        this.executorHandler = new ExecutorHandler();
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue();
        List<Member> targets = context.getTargets();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("KILL LIST");
        builder.setColor(16711680);
        builder.setThumbnail(executorHandler.getRandomExecutor().getImage());

        if(targets.isEmpty()) {
            builder.setDescription("There are no targets sir.");
            builder.setFooter("Add the 'target' role to anyone you need taken care of.");
            privateMessage(context.getUser(), builder.build());
            return;
        }

        String desc = "There are " + targets.size() + " targets waiting for your order sir.";
        if(targets.size() == 1) {
            desc = "There is 1 target waiting for your order sir.";
        }

        builder.setDescription(desc);
        builder.addField("#", "1", true);
        builder.addBlankField(true);
        builder.addField("TARGET", getName(targets.get(0)), true);

        for(int i = 1; i < targets.size(); i++) {
            Member target = targets.get(i);
            builder.addField("\u200B", String.valueOf(i + 1), true);
            builder.addBlankField(true);
            builder.addField("\u200B", getName(target), true);
        }

        builder.setFooter("Execute order 66 when you're ready.");
        privateMessage(context.getUser(), builder.build());
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

    private void privateMessage(User user, MessageEmbed embed) {
        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(embed).queue();
            privateChannel.close().queue();
        });
    }
}