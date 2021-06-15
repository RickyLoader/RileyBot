package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.LookupCommand;
import Runescape.HiscoresArgs;
import Runescape.Stats.RS3Hiscores;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a RS3 player and build an image with their stats
 */
public class RS3LookupCommand extends LookupCommand {
    private boolean virtual;

    public RS3LookupCommand() {
        super(
                "rs3lookup",
                "Check out someone's stats on RS3!",
                "virtual rs3lookup " + DEFAULT_LOOKUP_ARGS,
                12
        );
    }

    @Override
    public void processName(String name, CommandContext context) {
        RS3Hiscores hiscores = new RS3Hiscores(
                context.getEmoteHelper(),
                "Type: " + getTrigger() + " for help"
        );
        hiscores.buildImage(name, context.getMessageChannel(), new HiscoresArgs(virtual));
    }

    @Override
    public String stripArguments(String query) {
        if(query.startsWith("virtual")) {
            query = query.replaceFirst("virtual", "").trim();
            virtual = true;
        }
        else {
            virtual = false;
        }
        return query;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.RS3);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.RS3, channel, user);
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || query.startsWith("virtual") && query.contains(getTrigger());
    }
}
