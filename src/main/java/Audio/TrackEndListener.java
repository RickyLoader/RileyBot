package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;

public class TrackEndListener extends AudioEventAdapter {

    private final Guild guild;
    private Response method;
    private final boolean leave;

    /**
     * Create a listener
     *
     * @param guild  Guild where audio is playing
     * @param method Method to execute once audio has finished
     */
    public TrackEndListener(Guild guild, Response method) {
        this.guild = guild;
        this.method = method;
        this.leave = true;
    }

    /**
     * Create a listener
     *
     * @param guild Guild where audio is playing
     */
    public TrackEndListener(Guild guild) {
        this.guild = guild;
        this.leave = false;
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        System.out.println("TRACK EXCEPTION:\n");
        exception.printStackTrace();
        stopSession(player, track);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
        System.out.println("TRACK STUCK");
    }

    /**
     * Listener fires when the track being played has ended. Call the processFinish method of the given implementation
     * of Response interface.
     *
     * @param player    The audio player
     * @param track     The track being played
     * @param endReason The reason the track ended
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println("Track ended: " + track.getInfo().title);
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
            if(leave) {
                new Thread(() -> guild.getAudioManager().closeAudioConnection()).start();
            }
            if(method != null) {
                new Thread(method::processFinish).start();
            }
            player.removeListener(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface containing single method signature. Methods wanting to do something after the track ends implement
     * this interface and override the method.
     */
    public interface Response {
        void processFinish();
    }
}
