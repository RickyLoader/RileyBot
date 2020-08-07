package Command.Commands;

import Bot.DiscordUser;
import COD.CombatRecord;
import Command.Structure.ImageBuilderCommand;
import Command.Structure.ImageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWLookupCommand extends ImageBuilderCommand {

    public MWLookupCommand() {
        super("mwlookup", "Have a gander at a player's Modern Warfare stats!", 18);
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, Guild guild) {
        return new CombatRecord(channel, guild, "src/main/resources/COD/", "ModernWarfare.otf");
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
