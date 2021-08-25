package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.EmoteHelper;
import Command.Structure.LookupCommand;
import Runescape.Stats.RS3Hiscores;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashSet;

/**
 * Look up a RS3 player and build an image with their stats
 */
public class RS3LookupCommand extends LookupCommand {
    private static final String TRIGGER = "rs3lookup";
    private final HashSet<ARGUMENT> arguments;
    private RS3Hiscores hiscores;

    public enum ARGUMENT {
        VIRTUAL
    }

    public RS3LookupCommand() {
        super(
                TRIGGER,
                "Check out someone's stats on RS3!",
                "[" + ARGUMENT.VIRTUAL.name().toLowerCase() + "] " + TRIGGER + DEFAULT_LOOKUP_ARGS,
                12
        );
        this.arguments = new HashSet<>();
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.hiscores = new RS3Hiscores(emoteHelper, "Type: " + getTrigger() + " for help");
    }

    @Override
    public void processName(String name, CommandContext context) {
        hiscores.buildImage(name, context.getMessageChannel(), arguments);
    }

    @Override
    public String stripArguments(String query) {
        arguments.clear();

        if(query.startsWith(ARGUMENT.VIRTUAL.name().toLowerCase())) {
            query = query.replaceFirst(ARGUMENT.VIRTUAL.name().toLowerCase(), "").trim();
            arguments.add(ARGUMENT.VIRTUAL);
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
        return super.matches(query, message)
                || query.startsWith(ARGUMENT.VIRTUAL.name().toLowerCase()) && query.contains(getTrigger());
    }
}
