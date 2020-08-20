package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkInfo;

import java.net.URLEncoder;

/**
 * Play a hyper realistic custom Survivor voice clip in the voice channel of the user.
 * 'Dave, put your torch out..'
 */
public class SurvivorCommand extends DiscordCommand {

    public SurvivorCommand() {
        super("survivor [NAME]", "Put out your torch!");
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String location = NetworkInfo.getAddress() + "/DiscordBotApi/api/survivor/";
            String name = URLEncoder.encode(context.getLowerCaseMessage().replaceFirst("survivor ", ""), "UTF-8");
            context.getAudioPlayer().play(
                    location + name,
                    context.getMember(),
                    context.getMessageChannel(),
                    context.getGuild(),
                    true
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("survivor ");
    }
}
