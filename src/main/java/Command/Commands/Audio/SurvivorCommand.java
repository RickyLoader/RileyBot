package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.Secret;
import net.dv8tion.jda.api.entities.Message;

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
            String location = "http://" + Secret.LOCAL_IP + Secret.LOCAL_API_PATH + "survivor/";
            String name = EmbedHelper.urlEncode(context.getLowerCaseMessage().replaceFirst("survivor ", ""));
            context.playAudio(
                    location + name
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("survivor ");
    }
}
