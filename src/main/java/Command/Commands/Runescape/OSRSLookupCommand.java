package Command.Commands.Runescape;

import Bot.DiscordUser;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilderLookupCommand;
import Command.Structure.ImageBuilder;
import Runescape.OSRS.Stats.Hiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends ImageBuilderLookupCommand {

    public OSRSLookupCommand() {
        super("osrslookup", "Check out someone's stats on OSRS!", 12);
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, EmoteHelper emoteHelper) {
        return new Hiscores(channel, emoteHelper, "src/main/resources/Runescape/OSRS/", "osrs.ttf");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getOSRSName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveOSRSName(name, channel, user);
    }
}
