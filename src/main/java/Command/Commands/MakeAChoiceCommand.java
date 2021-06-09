package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Random;

public class MakeAChoiceCommand extends DiscordCommand {

    public MakeAChoiceCommand() {
        super("or", "Choose between multiple things!", "[option] or [option] or [option]...");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String[] options = context.getLowerCaseMessage().split(" " + getTrigger() + " ");
        if(options.length < 2) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        channel.sendMessage(options[new Random().nextInt(options.length)]).queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.contains(getTrigger());
    }
}
