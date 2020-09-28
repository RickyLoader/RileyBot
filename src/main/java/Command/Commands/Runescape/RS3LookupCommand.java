package Command.Commands.Runescape;

import Bot.DiscordUser;
import Runescape.Stats.Hiscores;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageBuilderLookupCommand;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class RS3LookupCommand extends ImageBuilderLookupCommand {

    public RS3LookupCommand() {
        super("rs3lookup", "Check out someone's stats on RS3!", 12);
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, EmoteHelper emoteHelper) {
        return new Hiscores(channel, emoteHelper, "src/main/resources/Runescape/RS3/", "rs3.ttf");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getRS3Name(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveRS3Name(name, channel, user);
    }
}
