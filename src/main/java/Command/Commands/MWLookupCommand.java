package Command.Commands;

import Bot.DiscordUser;
import COD.CombatRecord;
import Command.Structure.CodImageBuilderCommand;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWLookupCommand extends CodImageBuilderCommand {

    public MWLookupCommand() {
        super("mwlookup", "Have a gander at a player's Modern Warfare stats!");
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, EmoteHelper emoteHelper) {
        return new CombatRecord(channel, emoteHelper, "MW", "ModernWarfare.otf");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getMWName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveMWName(name, channel, user);
    }
}
