package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * Text to speech command to play in the voice channel of the user
 */
public class TTSCommand extends DiscordCommand {

    public TTSCommand() {
        super(".[TTS]", "Make the bot speak!");
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String location = NetworkInfo.getAddress() + "/DiscordBotApi/api/dectalk/";
            String content = URLEncoder.encode(context.getLowerCaseMessage().replaceFirst(".", ""), "UTF-8");
            new DiscordAudioPlayer(
                    context.getMember(),
                    context.getGuild()
            ).play(location + content);
            logTTS(context.getMessageContent(), context.getUser(), context.getGuild());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If the server has a tts-log channel, send an embed containing what was requested to say
     *
     * @param msg    String message from user
     * @param author User who requested TTS
     * @param guild  Guild to search for tts-log channel
     */
    private void logTTS(String msg, User author, Guild guild) {
        List<TextChannel> channels = guild.getTextChannelsByName("tts-log", true);
        if(!channels.isEmpty()) {
            MessageChannel log = channels.get(0);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(15655767);
            builder.setDescription("**Author**: " + author.getAsMention() + " **Submitted At**: " + new Date());
            builder.setTitle("**TTS Log**");
            builder.addField("Contents:", msg, false);
            log.sendMessage(builder.build()).queue();
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith(".");
    }
}
