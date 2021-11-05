package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Text to speech command to play in the voice channel of the user
 */
public class TTSCommand extends DiscordCommand {
    private static final String
            BASE_URL = "http://192.168.1.18/api/tts/",
            STANDARD_TRIGGER = "tts",
            GOOGLE_TRIGGER = "g" + STANDARD_TRIGGER,
            DECTALK_TRIGGER = "d" + STANDARD_TRIGGER,
            SURVIVOR_TRIGGER = "survivor",
            RANDOM = "random",
            DECTALK_TEXT_HELP = "[text/song name/" + RANDOM + "]",
            DECTALK_HELP = DECTALK_TRIGGER + " - Dectalk TTS"
                    + "\n\n" + DECTALK_TRIGGER + " " + DECTALK_TEXT_HELP
                    + "\n" + DECTALK_TRIGGER + " " + SURVIVOR_TRIGGER + " " + DECTALK_TEXT_HELP,
            GOOGLE_HELP = GOOGLE_TRIGGER + " - Google TTS"
                    + "\n\n" + GOOGLE_TRIGGER + " [text]"
                    + "\n" + GOOGLE_TRIGGER + " " + SURVIVOR_TRIGGER + " [text]";
    private final HashMap<String, String> phonemeSongs;
    private final String[] songList;

    /**
     * Initialise a map of phoneme songs
     */
    public TTSCommand() {
        super(
                STANDARD_TRIGGER,
                "Make RileyBot speak!",
                "Text To Speech:"
                        + "\n\n" + STANDARD_TRIGGER + " - Send this message"
                        + "\n\n" + DECTALK_HELP
                        + "\n\n" + GOOGLE_HELP
        );
        this.phonemeSongs = initialisePhonemeSongs();
        this.songList = phonemeSongs.keySet().toArray(new String[0]);
    }

    /**
     * Create a map of phoneme song name -> phoneme songs.
     * These can be fed to Dectalk TTS to make cool noises.
     *
     * @return Map of phoneme songs
     */
    private HashMap<String, String> initialisePhonemeSongs() {
        final ResourceHandler resourceHandler = new ResourceHandler();
        final JSONArray songs = new JSONArray(resourceHandler.getResourceFileAsString("/Audio/TTS/phoneme_songs.json"));

        // Map from song name -> song text
        HashMap<String, String> phonemeSongs = new HashMap<>();

        for(int i = 0; i < songs.length(); i++) {
            JSONObject song = songs.getJSONObject(i);
            phonemeSongs.put(song.getString("name").toLowerCase(), song.getString("text"));
        }

        return phonemeSongs;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        final String message = context.getLowerCaseMessage();
        final String[] args = message.split(" ");

        // "dtts"
        final String trigger = args[0];
        final boolean useGoogleTTS = trigger.equals(GOOGLE_TRIGGER);

        // Send TTS help message
        if(trigger.equals(STANDARD_TRIGGER)) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // "dtts hello" -> "hello"
        String textToSay = message.replaceFirst(trigger, "").trim();

        // Nothing provided to say - send the help message
        if(textToSay.isEmpty()) {
            final String helpMessage = useGoogleTTS
                    ? GOOGLE_HELP
                    : DECTALK_HELP + "\n\nSongs:\n\n" + StringUtils.join(songList, "\n");

            channel.sendMessage("```" + helpMessage + "```").queue();
            return;
        }

        // URL to send to lavaplayer to play in the voice channel
        String audioUrl = BASE_URL + trigger + "/";

        // Getting survivor audio
        if(args[1].equals(SURVIVOR_TRIGGER)) {
            textToSay = textToSay.replaceFirst(SURVIVOR_TRIGGER, "").trim();

            // No text provided
            if(textToSay.isEmpty()) {
                channel.sendMessage(member.getAsMention() + " give me something to say!").queue();
                return;
            }

            audioUrl += SURVIVOR_TRIGGER + "/";
        }

        // Check for phoneme song name if it is a dectalk request
        if(!useGoogleTTS) {
            String songText;

            // Replace text with random phoneme song
            if(textToSay.equals(RANDOM)) {
                songText = phonemeSongs.get(songList[new Random().nextInt(songList.length)]);
            }
            else {
                songText = phonemeSongs.get(textToSay);
            }

            // Replace the given text with the text required to sing the song
            if(songText != null) {
                textToSay = songText;
            }
        }

        audioUrl += EmbedHelper.urlEncode(textToSay);

        // Log TTS regardless of success
        logTTS(trigger + " " + textToSay, member.getUser(), context.getGuild());

        // Attempt to play the text to speech audio
        DiscordAudioPlayer.getInstance(context.getGuild()).play(
                audioUrl,
                member,
                channel
        );
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

        // No log channel exists
        if(channels.isEmpty()) {
            return;
        }

        try {
            final MessageEmbed logMessage = new EmbedBuilder()
                    .setColor(EmbedHelper.YELLOW)
                    .setDescription("**Author**: " + author.getAsMention() + "\n**Submitted At**: " + new Date())
                    .setTitle("**TTS Log**")
                    .addField("Contents:", msg, false)
                    .build();
            channels.get(0).sendMessage(logMessage).queue();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(STANDARD_TRIGGER)
                || query.startsWith(GOOGLE_TRIGGER)
                || query.startsWith(DECTALK_TRIGGER);
    }
}
