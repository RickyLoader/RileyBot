package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

import java.util.Random;

public class MakeAChoiceCommand extends DiscordCommand {

    public MakeAChoiceCommand() {
        super("[option] or [option] or [option]...", "Choose between multiple things!");
    }

    @Override
    public void execute(CommandContext context) {
        String[] options = context.getLowerCaseMessage().split(" or ");
        context.getMessageChannel().sendMessage(options[new Random().nextInt(options.length)]).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.split(" or ").length >= 2;
    }
}
