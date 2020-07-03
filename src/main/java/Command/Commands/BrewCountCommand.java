package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class BrewCountCommand extends DiscordCommand {

    public BrewCountCommand() {
        super("# brews", "Let everyone know how many brews you've had!");
    }

    @Override
    public void execute(CommandContext context) {
        String quantity = context.getMessageContent().split(" ")[0];
        context.getMessageChannel().sendMessage(
                "Hey @everyone my name is " + context.getUser().getAsMention() + " and I have had " + quantity + " brews!"
        ).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.split(" ")[1].equalsIgnoreCase(" brews");
    }
}