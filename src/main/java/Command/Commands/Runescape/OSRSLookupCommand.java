package Command.Commands.Runescape;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.LookupCommand;
import Runescape.OSRS.Stats.Hiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends LookupCommand {

    public OSRSLookupCommand() {
        super("osrslookup", "Check out someone's stats on OSRS!", 12);
    }

    @Override
    public void processName(String name, CommandContext context) {
        Hiscores hiscores = new Hiscores(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                "/Runescape/OSRS/",
                "osrs.ttf"
        );
        hiscores.buildImage(name, getHelpName());
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
