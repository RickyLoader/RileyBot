package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Random;

public class MakeAChoiceCommand extends DiscordCommand {
    private static final int MIN_OPTIONS = 2;

    public MakeAChoiceCommand() {
        super("or", "Choose between multiple things!", "[option] or [option] or [option]...");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage();
        if(query.equals(getTrigger())) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        String[] options = getOptions(query);
        channel.sendMessage(options[new Random().nextInt(options.length)]).queue();
    }

    /**
     * Split the given String on occurrence of the trigger in to an array of options
     *
     * @param query Query to split
     * @return Array of options
     */
    public String[] getOptions(String query) {
        return query.split(" " + getTrigger() + " ");
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.equals(getTrigger()) || getOptions(query).length >= MIN_OPTIONS;
    }
}
