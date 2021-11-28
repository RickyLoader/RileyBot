package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Runescape.Hiscores.OSRSHiscores;
import Runescape.ImageBuilding.OSRSHiscoresImageBuilder;
import Runescape.Stats.OSRSPlayerStats;
import Runescape.Stats.PlayerStats;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

import static Command.Commands.Lookup.RunescapeLookupCommand.ARGUMENT.*;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends RunescapeLookupCommand<OSRSPlayerStats, OSRSHiscores, OSRSHiscoresImageBuilder> {

    public OSRSLookupCommand() {
        super(
                "osrslookup",
                "Check out someone's stats on OSRS!",
                Arrays.asList(
                        BOSSES,
                        VIRTUAL,
                        ACHIEVEMENTS,
                        XP_TRACKER,
                        SKILL_XP,
                        MAX,
                        BOSS_BACKGROUNDS,
                        SHOW_UNRANKED_BOSSES
                ),
                Arrays.asList(PlayerStats.ACCOUNT.values()),
                new OSRSHiscores()
        );
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
    protected OSRSHiscoresImageBuilder initialiseImageBuilder(OSRSHiscores hiscores, JDA jda, EmoteHelper emoteHelper) {
        return new OSRSHiscoresImageBuilder(
                hiscores,
                emoteHelper,
                "Type: " + getTrigger() + " for help"
        );
    }
}
