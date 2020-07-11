package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Audio.TrackEndListener;
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
