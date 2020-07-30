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
import net.dv8tion.jda.api.entities.VoiceChannel;


public class DiscordAudioPlayer {
    AudioPlayer player;
    AudioPlayerManager manager;
    Member member;
    Guild guild;

    public DiscordAudioPlayer(Member member, Guild guild, TrackEndListener listener) {
        this.member = member;
        this.guild = guild;
        this.manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        this.player = manager.createPlayer();
        this.player.addListener(listener);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
    }

    public DiscordAudioPlayer(Member member, Guild guild) {
        this(member, guild, new TrackEndListener(null, guild));
    }

    public void join(VoiceChannel vc) {
        guild.getAudioManager().openAudioConnection(vc);
    }

    public void leave() {
        guild.getAudioManager().closeAudioConnection();
    }

    public VoiceChannel getMemberVoiceChannel() {
        if(member.getVoiceState() == null) {
            return null;
        }
        return member.getVoiceState().getChannel();
    }

    public void play(String audio) {
        VoiceChannel vc = getMemberVoiceChannel();
        join(vc);
        try {
            manager.loadItem(audio, new AudioLoadResultHandler() {

                /**
                 * Play the audio once loaded
                 *
                 * @param audioTrack Track loaded in to player
                 */
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    player.playTrack(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {}

                @Override
                public void noMatches() {}

                @Override
                public void loadFailed(FriendlyException e) {
                    e.printStackTrace();
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
