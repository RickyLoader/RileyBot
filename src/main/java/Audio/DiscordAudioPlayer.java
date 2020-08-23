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
import org.apache.http.client.config.RequestConfig;


public class DiscordAudioPlayer {
    private final AudioPlayer player;
    private final AudioPlayerManager manager;
    private boolean cancelable = true;

    public DiscordAudioPlayer() {
        this.manager = new DefaultAudioPlayerManager();
        manager.setHttpRequestConfigurator((config) -> RequestConfig.copy(config).setConnectTimeout(10000).build());
        AudioSourceManagers.registerRemoteSources(manager);
        this.player = manager.createPlayer();
    }

    public void join(VoiceChannel vc, Guild guild) {
        guild.getAudioManager().openAudioConnection(vc);
    }

    public boolean stop(Guild guild) {
        if(cancelable) {
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    public VoiceChannel getMemberVoiceChannel(Member member) {
        if(member.getVoiceState() == null) {
            return null;
        }
        return member.getVoiceState().getChannel();
    }

    public boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    public boolean play(String audio, Member member, MessageChannel channel, Guild guild, boolean cancelable, TrackEndListener.Response... doAfter) {
        try {
            if(guild.getAudioManager().getSendingHandler() == null) {
                guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
            }

            if(isPlaying() && !this.cancelable) {
                channel.sendMessage("I'm busy right now").queue();
                return false;
            }

            this.cancelable = cancelable;
            player.addListener(doAfter.length > 0 ? new TrackEndListener(guild, doAfter[0]) : new TrackEndListener(guild));
            VoiceChannel vc = getMemberVoiceChannel(member);

            if(vc == null) {
                channel.sendMessage("You're not in a voice channel").queue();
                return false;
            }

            join(vc, guild);


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
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    }

                    @Override
                    public void noMatches() {
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        e.printStackTrace();
                    }
                });
            }
            catch(Exception e) {
                e.printStackTrace();
                this.cancelable = true;
                return false;
            }
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
