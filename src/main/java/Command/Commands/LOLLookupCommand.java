package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageBuilderCommand;
import LOL.SummonerImage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;


public class LOLLookupCommand extends ImageBuilderCommand {


    public LOLLookupCommand() {
        super("lollookup", "Look up a summoner", 16);
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, Guild guild) {
        return new SummonerImage(channel, guild, "src/main/resources/LOL/", "font.otf");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getLOLName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveLOLName(name, channel, user);
    }
}
