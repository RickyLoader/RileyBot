package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;

public class TrackEndListener extends AudioEventAdapter {

    private final Guild guild;
    private Response method;
    private final boolean leave;

    public TrackEndListener(Guild guild, Response method) {
        this.guild = guild;
        this.method = method;
        this.leave = true;
    }

    public TrackEndListener(Guild guild) {
        this.guild = guild;
        this.leave = false;
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
        if(leave) {
            new Thread(() -> guild.getAudioManager().closeAudioConnection()).start();
        }
        if(method != null) {
            new Thread(method::processFinish).start();
        }
        player.removeListener(this);
    }

    /**
     * Interface containing single method signature. Methods wanting to do something after the track ends implement
     * this interface and override the method.
     */
    public interface Response {
        void processFinish();
    }
}
