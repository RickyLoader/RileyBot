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
    private boolean league = false;

    public OSRSLookupCommand() {
        super(
                "osrslookup",
                "Check out someone's stats on OSRS!",
                "league " + getDefaultLookupArgs("osrslookup"),
                12
        );
    }

    @Override
    public void processName(String name, CommandContext context) {
        Hiscores hiscores = new Hiscores(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                "/Runescape/OSRS/",
                "osrs.ttf",
                league
        );
        hiscores.buildImage(name, getHelpName());
    }

    @Override
    public String stripArguments(String query) {
        if(query.startsWith("league")) {
            query = query.replaceFirst("league", "").trim();
            league = true;
        }
        else {
            league = false;
        }
        return query;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.OSRS);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.OSRS, channel, user);
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger()) || query.startsWith("league");
    }
}
