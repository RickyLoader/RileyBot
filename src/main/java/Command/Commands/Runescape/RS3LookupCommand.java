package Command.Commands.Runescape;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.LookupCommand;
import Runescape.Stats.Hiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class RS3LookupCommand extends LookupCommand {

    public RS3LookupCommand() {
        super("rs3lookup", "Check out someone's stats on RS3!", 12);
    }

    @Override
    public void processName(String name, CommandContext context) {
        Hiscores hiscores = new Hiscores(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                "/Runescape/RS3/",
                "rs3.ttf"
        );
        hiscores.buildImage(name, getHelpName());
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
