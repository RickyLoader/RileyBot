package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class InviteCommand extends DiscordCommand {

    public InviteCommand() {
        super("invite!", "Create an invite to the server!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(context.getInvite()).queue();
    }
}
