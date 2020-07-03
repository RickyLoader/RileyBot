package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkInfo;

import java.net.URLEncoder;

public class SurvivorCommand extends DiscordCommand {

    public SurvivorCommand() {
        super("survivor [NAME]", "Put out your torch!");
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String location = NetworkInfo.getAddress() + "/DiscordBotApi/api/survivor/";
            String name = URLEncoder.encode(context.getMessageContent().replace(".survivor ", ""), "UTF-8");
            new DiscordAudioPlayer(context.getMember(), context.getGuild()).play(location+name);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("survivor ");
    }
}
