package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.http.client.config.RequestConfig;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordAudioPlayer {
    private static final HashMap<Guild, DiscordAudioPlayer> AUDIO_PLAYERS = new HashMap<>();
    private final AudioPlayer player;
    private final AudioPlayerManager manager;
    private final Guild guild;
    private Timer timer;
    private boolean isTimerRunning;

    /**
     * Create the audio player
     *
     * @param guild Guild where audio will be played
     */
    private DiscordAudioPlayer(Guild guild) {
        this.guild = guild;
        this.manager = new DefaultAudioPlayerManager();
        manager.setHttpRequestConfigurator((config) -> RequestConfig.copy(config).setConnectTimeout(10000).build());
        AudioSourceManagers.registerRemoteSources(manager);
        this.player = manager.createPlayer();
        this.timer = new Timer();
        if(guild.getAudioManager().getSendingHandler() == null) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        }
    }

    /**
     * Get/create an instance of the audio player for the given guild.
     *
     * @param guild Guild to get audio player for
     * @return Audio player
     */
    public static DiscordAudioPlayer getInstance(Guild guild) {
        DiscordAudioPlayer player = AUDIO_PLAYERS.get(guild);

        // Create an audio player for the guild and map for later retrieval
        if(player == null) {
            player = new DiscordAudioPlayer(guild);
            AUDIO_PLAYERS.put(guild, player);
        }

        return player;
    }

    /**
     * Join the given voice channel
     *
     * @param vc Voice channel to join
     */
    public void join(VoiceChannel vc) {
        guild.getAudioManager().openAudioConnection(vc);
    }

    /**
     * Stop the audio & leave the voice channel
     *
     * @return Audio was stopped
     */
    public boolean stop() {
        AudioManager manager = guild.getAudioManager();

        // Not connected/playing
        if(!manager.isConnected() && !isPlaying()) {
            return false;
        }

        manager.closeAudioConnection();
        player.stopTrack();

        // Reset timer task that is set to leave the voice channel
        resetTimer();
        return true;
    }

    /**
     * Reset the timer that is set to leave the voice channel
     */
    private void resetTimer() {

        // Cancel scheduled task
        if(isTimerRunning) {
            timer.cancel();
        }

        // Reset timer
        timer = new Timer();
        isTimerRunning = false;
    }


    /**
     * Get the voice channel of the given member
     *
     * @param member Member to get voice channel for
     * @return Voice channel or null if not in one
     */
    public VoiceChannel getMemberVoiceChannel(Member member) {
        if(member.getVoiceState() == null) {
            return null;
        }
        return member.getVoiceState().getChannel();
    }

    /**
     * Check if the audio player is currently playing
     *
     * @return Audio player playing
     */
    public boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    /**
     * Play audio in the given member's voice channel
     *
     * @param audio   URL to audio
     * @param member  Member to join voice channel of
     * @param channel Channel to send status updates to
     * @param doAfter Method to execute once the audio is complete/cancelled
     */
    public void play(String audio, Member member, MessageChannel channel, TrackEndListener.Response... doAfter) {
        final TrackEndListener.Response onTrackFinish = doAfter.length > 0 ? doAfter[0] : null;
        final TrackEndListener trackEndListener = new TrackEndListener(audio, guild, onTrackFinish);
        player.addListener(trackEndListener);

        VoiceChannel vc = getMemberVoiceChannel(member);

        if(vc == null) {
            channel.sendMessage(member.getAsMention() + " You're not in a voice channel!").queue();
            return;
        }

        join(vc);

        // Initialise a timer task to leave the voice channel
        TimerTask leaveTask = new TimerTask() {
            @Override
            public void run() {

                // Mark task as executed prior to resetting timer, so a cancel isn't attempted (would throw exception)
                isTimerRunning = false;

                // Only send message if audio was stopped/channel was left
                if(stop()) {
                    channel.sendMessage("goodbye").queue();
                }
            }
        };


        // Reset the current timer & schedule a leave for an hour from now
        resetTimer();
        timer.schedule(leaveTask, 1000 * 60 * 60);

        // Mark task as pending
        isTimerRunning = true;

        manager.loadItem(audio, new AudioLoadResultHandler() {

            /**
             * Play the audio once loaded
             *
             * @param audioTrack Track loaded in to player
             */
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                System.out.println("Track loaded: " + audioTrack.getInfo().uri);
                player.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException e) {
                System.out.println("Load failed: " + e.getMessage());
                stop();
                channel.sendMessage(
                        "I couldn't get "
                                + (audio.toLowerCase().contains("youtube") ? "YouTube" : "anyone")
                                + " on the phone"
                ).queue();
                if(doAfter.length > 0) {
                    doAfter[0].processFinish();
                }
                player.removeListener(trackEndListener);
            }
        });
    }
}
