package Command.Commands;

import Bot.DiscordUser;
import COD.CombatRecord;
import Command.Structure.ImageBuilderCommand;
import Command.Structure.ImageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.List;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWLookupCommand extends ImageBuilderCommand {
    private String platform;
    private final List<String> platforms;

    public MWLookupCommand() {
        super("mwlookup", "Have a gander at a player's Modern Warfare stats!", "acti/bnet", 30);
        platforms = Arrays.asList("bnet", "acti");
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, Guild guild) {
        return new CombatRecord(channel, guild, "src/main/resources/COD/", "ModernWarfare.otf");
    }

    @Override
    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name, platform);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getMWName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveMWName(name, channel, user);
    }

    @Override
    public String stripArguments(String query) {
        String platform = query.split(" ")[0];
        if(platform.equals(getTrigger())) {
            this.platform = "bnet";
            return query;
        }
        this.platform = platform;
        return query.replaceFirst(platform, "").trim();
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || platforms.contains(args[0]) && args[1].matches(getTrigger());
    }
}
