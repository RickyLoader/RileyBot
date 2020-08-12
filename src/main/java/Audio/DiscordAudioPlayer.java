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


public class DiscordAudioPlayer {
    private final AudioPlayer player;
    private final AudioPlayerManager manager;
    private final Member member;
    private final Guild guild;
    private final MessageChannel channel;

    public DiscordAudioPlayer(Member member, Guild guild, TrackEndListener listener, MessageChannel channel) {
        this.member = member;
        this.guild = guild;
        this.channel = channel;
        this.manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        this.player = manager.createPlayer();
        this.player.addListener(listener);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
    }

    public DiscordAudioPlayer(Member member, Guild guild, MessageChannel channel) {
        this(member, guild, new TrackEndListener(null, guild), channel);
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

    public boolean play(String audio) {
        VoiceChannel vc = getMemberVoiceChannel();
        if(vc == null) {
            channel.sendMessage("You're not in a voice channel").queue();
            return false;
        }
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
            return false;
        }
        return true;
    }
}
