package Command.Structure;

import Bot.DiscordUser;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public abstract class MWLookupCommand extends CODLookupCommand {

    public MWLookupCommand(String trigger, String desc, String helpText) {
        super(trigger, desc, helpText);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.MW);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.MW, channel, user);
    }
}
