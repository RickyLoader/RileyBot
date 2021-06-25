package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.EmoteHelper;
import Command.Structure.LookupCommand;
import Runescape.HiscoresArgs;
import Runescape.Stats.RS3Hiscores;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a RS3 player and build an image with their stats
 */
public class RS3LookupCommand extends LookupCommand {
    private static final String TRIGGER = "rs3lookup", VIRTUAL = "virtual";
    private boolean virtual;
    private RS3Hiscores hiscores;

    public RS3LookupCommand() {
        super(
                TRIGGER,
                "Check out someone's stats on RS3!",
                "[" + VIRTUAL + "] " + TRIGGER + DEFAULT_LOOKUP_ARGS,
                12
        );
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.hiscores = new RS3Hiscores(emoteHelper, "Type: " + getTrigger() + " for help");
    }

    @Override
    public void processName(String name, CommandContext context) {
        hiscores.buildImage(name, context.getMessageChannel(), new HiscoresArgs(virtual));
    }

    @Override
    public String stripArguments(String query) {
        if(query.startsWith(VIRTUAL)) {
            query = query.replaceFirst(VIRTUAL, "").trim();
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
        return super.matches(query, message) || query.startsWith(VIRTUAL) && query.contains(getTrigger());
    }
}
