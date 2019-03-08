package Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;

public class TrackEndListener extends AudioEventAdapter{
    private Response method;
    private Guild guild;
    public TrackEndListener(Response method, Guild guild){
        this.method = method;
        this.guild = guild;
    }
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        new Thread(() -> guild.getAudioManager().closeAudioConnection()).start();
        if(method!=null){
            method.processFinish();
        }
    }
    public interface Response{
        void processFinish();
    }
}
