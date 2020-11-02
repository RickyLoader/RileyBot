package Command.Commands.Runescape;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.LookupCommand;
import Runescape.OSRS.Stats.OSRSHiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends LookupCommand {
    private boolean league = false, virtual = false;

    public OSRSLookupCommand() {
        super(
                "osrslookup",
                "Check out someone's stats on OSRS!",
                "[league] [virtual] " + getDefaultLookupArgs("osrslookup"),
                12
        );
    }

    @Override
    public void processName(String name, CommandContext context) {
        OSRSHiscores hiscores = new OSRSHiscores(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                league,
                virtual
        );
        hiscores.buildImage(name, "Type " + getTrigger() + " for help");
    }

    @Override
    public String stripArguments(String query) {
        league = false;
        virtual = false;

        String[] args = query
                .split(getTrigger())[0]
                .trim()
                .split(" ");

        for(String arg : args) {
            switch(arg) {
                case "league":
                    league = true;
                    break;
                case "virtual":
                    virtual = true;
                    break;
            }
            query = query.replaceFirst(arg, "").trim();
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
        return query.startsWith(getTrigger()) ||
                query.startsWith("league") && query.contains(getTrigger()) ||
                query.startsWith("virtual") && query.contains(getTrigger());
    }
}
