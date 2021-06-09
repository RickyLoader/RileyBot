package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public abstract class VariableLinkCommand extends DiscordCommand {
    private final HashMap<String, String> versions;

    /**
     * Create a variable link command
     *
     * @param helpTrigger Trigger to use to see the triggers
     * @param desc        Description of the command
     */
    public VariableLinkCommand(String helpTrigger, String desc) {
        super(helpTrigger, desc);
        this.versions = new HashMap<>();
        insertVersions(versions);
    }

    /**
     * Add to the given map of trigger -> link
     *
     * @param versions Map of trigger -> link to add to
     */
    public abstract void insertVersions(HashMap<String, String> versions);

    @Override
    public String getHelpName() {
        return StringUtils.join(versions.keySet(), " | ");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();

        if(message.equals(getTrigger())) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        channel.sendMessage(versions.get(message)).queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.equals(getTrigger()) || versions.containsKey(query);
    }
}
