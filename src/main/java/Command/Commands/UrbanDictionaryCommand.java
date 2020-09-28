package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import UrbanDictionary.*;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Search or get random definitions from the Urban Dictionary
 */
public class UrbanDictionaryCommand extends DiscordCommand {

    private UrbanDictionary urbanDictionary;

    public UrbanDictionaryCommand() {
        super("urbandict\nurbandict [search term]", "Get cool definitions from Urban Dictionary");
    }

    @Override
    public void execute(CommandContext context) {
        String msg = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();

        if(!msg.equals("urbandict") && !msg.startsWith("urbandict ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(urbanDictionary == null) {
            urbanDictionary = new UrbanDictionary(context.getEmoteHelper(), getHelpName().replaceAll("\n", " | "));
        }

        new Thread(() -> {
            Definition definition;
            if(msg.equals("urbandict")) {
                definition = urbanDictionary.getRandomDefinition();
            }
            else {
                definition = urbanDictionary.searchDefinition(msg.replace("urbandict ", ""));
            }

            if(definition == null) {
                channel.sendMessage("No definitions were found!").queue();
                return;
            }
            channel.sendMessage(urbanDictionary.getDefinitionEmbed(definition)).queue();
        }).start();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("urbandict");
    }
}
