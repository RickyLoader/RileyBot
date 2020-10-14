package Command.Commands.Lookup;

import Bot.DiscordUser;
import COD.CombatRecord;
import Command.Structure.CODImageBuilderCommand;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class CWLookupCommand extends CODImageBuilderCommand {

    public CWLookupCommand() {
        super("cwlookup", "Have a gander at a player's Cold War stats!");
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, EmoteHelper emoteHelper) {
        return new CombatRecord(channel, emoteHelper, "CW", "ModernWarfare.otf");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getCWName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveCWName(name, channel, user);
    }
}
