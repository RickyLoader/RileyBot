package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

public class TrackEndListener extends AudioEventAdapter {
    private final Guild guild;
    private final Response method;
    private final boolean leave;
    private final String audio;

    /**
     * Create a listener
     *
     * @param audio  Audio to listen for events
     * @param guild  Guild where audio is playing
     * @param method Optional method to execute once audio has finished
     */
    public TrackEndListener(String audio, Guild guild, @Nullable Response method) {
        this.audio = audio;
        this.guild = guild;
        this.method = method;
        this.leave = method != null;
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        System.out.println("TRACK EXCEPTION:\n");
        exception.printStackTrace();
        stopSession(player, track);
    }

    /**
     * Listener fires when the track being played has ended. Call the {@link Response#processFinish()}
     * method of the given Response interface implementation.
     *
     * @param player    The audio player
     * @param track     The track being played
     * @param endReason The reason the track ended
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        stopSession(player, track);
    }

    /**
     * Stop the audio playing session
     *
     * @param player Audio player
     * @param track  Track which was played
     */
    private void stopSession(AudioPlayer player, AudioTrack track) {
        try {

            // Not the track this listener is monitoring
            if(!track.getInfo().uri.equals(audio)) {
                return;
            }

            System.out.println("Track ended: " + track.getInfo().title);
            player.removeListener(this);

            if(leave) {
                new Thread(() -> guild.getAudioManager().closeAudioConnection()).start();
            }
            if(method != null) {
                new Thread(method::processFinish).start();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface for executing methods
     */
    public interface Response {
        void processFinish();
    }
}
