package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.EmoteHelper;
import Command.Structure.LookupCommand;
import Runescape.OSRS.Stats.OSRSHiscores;
import Runescape.OSRSHiscoresArgs;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends LookupCommand {
    private static final String
            LEAGUE = "league",
            VIRTUAL = "virtual",
            XP = "xp",
            BOSSES = "bosses",
            TRIGGER = "osrslookup";
    private boolean league, virtual, xp, bosses, achievements;
    private OSRSHiscores hiscores;

    public OSRSLookupCommand() {
        super(
                TRIGGER,
                "Check out someone's stats on OSRS!",
                "[" + LEAGUE + "] [" + VIRTUAL + "] [" + XP + "] " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS + "\n"
                        + BOSSES + " " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS,
                12
        );
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.hiscores = new OSRSHiscores(
                emoteHelper,
                "Type: " + getTrigger() + " for help"
        );
    }

    @Override
    public void processName(String name, CommandContext context) {
        OSRSHiscoresArgs args = new OSRSHiscoresArgs(virtual, league, xp, achievements);
        hiscores.buildImage(
                name,
                context.getMessageChannel(),
                args
        );
    }

    @Override
    public String stripArguments(String query) {
        league = false;
        virtual = false;
        xp = false;
        bosses = false;
        achievements = true; // Default fetch achievements

        if(query.equals(getTrigger())) {
            return query;
        }
        String[] args = query
                .split(getTrigger())[0] // xp virtual osrslookup me -> xp virtual
                .trim()
                .split(" "); // ["xp", "virtual"]

        for(String arg : args) {
            switch(arg) {
                case LEAGUE:
                    league = true;
                    break;
                case VIRTUAL:
                    virtual = true;
                    break;
                case XP:
                    xp = true;
                    break;
                // Don't fetch achievements or xp tracker when doing boss message
                case BOSSES:
                    bosses = true;
                    achievements = false;
                    xp = false;
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

    /**
     * Check if the given query is one of the lookup arguments
     *
     * @param query Query to check
     * @return Query is a lookup arg
     */
    private boolean isArg(String query) {
        return query.equals(LEAGUE) || query.equals(XP) || query.equals(VIRTUAL) || query.equals(BOSSES);
    }

    @Override
    public boolean matches(String query, Message message) {
        String firstArg = query.split(" ")[0];
        return super.matches(query, message) || query.contains(getTrigger()) && isArg(firstArg);
    }
}
