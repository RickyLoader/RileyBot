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

    /**
     * Create the audio player
     */
    public DiscordAudioPlayer() {
        this.manager = new DefaultAudioPlayerManager();
        manager.setHttpRequestConfigurator((config) -> RequestConfig.copy(config).setConnectTimeout(10000).build());
        AudioSourceManagers.registerRemoteSources(manager);
        this.player = manager.createPlayer();
    }

    /**
     * Join the given voice channel
     *
     * @param vc    Voice channel to join
     * @param guild Guild for audio manager
     */
    public void join(VoiceChannel vc, Guild guild) {
        guild.getAudioManager().openAudioConnection(vc);
    }

    /**
     * Stop the audio & leave the voice channel
     *
     * @param guild Guild for audio manager
     */
    public void stop(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
        player.stopTrack();
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
     * @param guild   Guild for audio manager
     * @param doAfter Method to execute once the audio is complete/cancelled
     */
    public void play(String audio, Member member, MessageChannel channel, Guild guild, TrackEndListener.Response... doAfter) {
        if(guild.getAudioManager().getSendingHandler() == null) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        }

        player.addListener(doAfter.length > 0 ? new TrackEndListener(guild, doAfter[0]) : new TrackEndListener(guild));
        VoiceChannel vc = getMemberVoiceChannel(member);

        if(vc == null) {
            channel.sendMessage("You're not in a voice channel").queue();
            return;
        }

        join(vc, guild);

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
                System.out.println(e.getMessage());
                guild.getAudioManager().closeAudioConnection();
                channel.sendMessage(
                        "I couldn't get "
                                + (audio.toLowerCase().contains("youtube") ? "YouTube" : "anyone")
                                + " on the phone"
                ).queue();
                if(doAfter.length > 0) {
                    doAfter[0].processFinish();
                }
            }
        });
    }
}
