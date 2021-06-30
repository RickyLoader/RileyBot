package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.Date;
import java.util.List;

/**
 * Text to speech command to play in the voice channel of the user
 */
public class TTSCommand extends DiscordCommand {

    public TTSCommand() {
        super("dtts [What to say with Dectalk TTS]\ngtts [What to say with Google TTS]", "Make RileyBot speak!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue();
        String content = context.getLowerCaseMessage();
        String url;
        if(content.startsWith("gtts ")) {
            url = getGoogleTTS(content.replaceFirst("gtts ", ""));
        }
        else if(content.startsWith("dtts ")) {
            url = getDectalkTTS(content.replaceFirst("dtts ", ""));
        }
        else {
            context.getMessageChannel().sendMessage(getHelpNameCoded()).queue();
            return;
        }
        Guild guild = context.getGuild();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        if(url == null) {
            channel.sendMessage("Something went wrong when I tried to say that, sorry bro").queue();
            return;
        }
        context.playAudio(
                url
        );
        logTTS(content, member.getUser(), guild);
    }

    /**
     * Get the URL required for Google TTS
     *
     * @param content Content to speak
     * @return URL to speak given content using Google
     */
    private String getGoogleTTS(String content) {
        try {
            return "http://" + Secret.LOCAL_IP + "DiscordBotAPI/api/google/" + EmbedHelper.urlEncode(content);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the URL required for Dectalk TTS
     *
     * @param content Content to speak
     * @return URL to speak given content using Dectalk
     */
    private String getDectalkTTS(String content) {
        try {
            return "http://" + Secret.LOCAL_IP + "DiscordBotAPI/api/dectalk/" + EmbedHelper.urlEncode(content);
        }
        catch(Exception e) {
            return null;
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
            try {
                MessageChannel log = channels.get(0);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(EmbedHelper.YELLOW);
                builder.setDescription("**Author**: " + author.getAsMention() + " **Submitted At**: " + new Date());
                builder.setTitle("**TTS Log**");
                builder.addField("Contents:", msg, false);
                log.sendMessage(builder.build()).queue();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("gtts ") || query.startsWith("dtts ");
    }
}
