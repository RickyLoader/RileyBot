package Command.Structure;

import Bot.DiscordUser;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public abstract class MWLookupCommand extends CODLookupCommand {

    public MWLookupCommand(String trigger, String desc) {
        super(trigger, desc);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getMWName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveMWName(name, channel, user);
    }
}
