package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.EmoteHelper;
import Runescape.Hiscores.RS3Hiscores;
import Runescape.Stats.PlayerStats;
import Runescape.ImageBuilding.RS3HiscoresImageBuilder;
import Runescape.Stats.RS3PlayerStats;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;

/**
 * Look up a RS3 player and build an image with their stats
 */
public class RS3LookupCommand extends RunescapeLookupCommand<RS3PlayerStats, RS3Hiscores, RS3HiscoresImageBuilder> {

    public RS3LookupCommand() {
        super(
                "rs3lookup",
                "Check out someone's stats on RS3!",
                Arrays.asList(
                        ARGUMENT.VIRTUAL,
                        ARGUMENT.RUNEMETRICS,
                        ARGUMENT.CLAN
                ),
                Arrays.asList(
                        PlayerStats.ACCOUNT.NORMAL,
                        PlayerStats.ACCOUNT.IRON,
                        PlayerStats.ACCOUNT.HARDCORE,
                        PlayerStats.ACCOUNT.LOCATE
                ),
                new RS3Hiscores()
        );
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
    protected RS3HiscoresImageBuilder initialiseImageBuilder(RS3Hiscores hiscores, JDA jda, EmoteHelper emoteHelper) {
        return new RS3HiscoresImageBuilder(hiscores, emoteHelper, "Type: " + getTrigger() + " for help");
    }
}
