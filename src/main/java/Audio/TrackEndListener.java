package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Extends the AudioEventAdapter class of Lava Player, overrides only the onTrackEnd listener.
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class TrackEndListener extends AudioEventAdapter{

    // Implementation of the Response interface
    private Response method;

    // The server where the audio is being played
    private Guild guild;

    public TrackEndListener(Response method, Guild guild){
        this.method = method;
        this.guild = guild;
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
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        System.out.println("\nTrack ended\n");

        // Leave the voice channel
        new Thread(() -> guild.getAudioManager().closeAudioConnection()).start();

        /*
         * Methods may want to do something after the track ends. In that case, an implementation of the Response
         * interface is given which contains an implementation of the processFinish() method.
         */
        if(method != null){
            method.processFinish();
        }
    }

    /**
     * Interface containing single method signature. Methods wanting to do something after the track ends implement
     * this interface and override the method.
     */
    public interface Response{
        void processFinish();
    }
}
