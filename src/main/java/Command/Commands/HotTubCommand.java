package Command.Commands;

import Command.Commands.Lookup.TTVLookupCommand;
import Command.Structure.CommandContext;
import Command.Structure.CyclicalPageableEmbed;
import Command.Structure.DiscordCommand;
import Twitch.Streamer;
import Twitch.TwitchTV;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;

/**
 * Cool hot tub streamers
 */
public class HotTubCommand extends DiscordCommand {

    public HotTubCommand() {
        super("hot tub", "Take a look at some hot tub streamers!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        ArrayList<Streamer> hotTubStreamers = TwitchTV.getInstance().searchStreamersByStreamTitle(
                "hot tub",
                "Just Chatting"
        );
        if(hotTubStreamers.size() == 0) {
            channel.sendMessage("I didn't see anyone in a hot tub right now!").queue();
            return;
        }
        displayHotTubbers(context, hotTubStreamers);
    }

    /**
     * Display the hot tub streams in a pageable message embed
     *
     * @param context   Command context
     * @param streamers List of streamers to display
     */
    private void displayHotTubbers(CommandContext context, ArrayList<Streamer> streamers) {
        new CyclicalPageableEmbed(
                context,
                streamers,
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex) {
                Streamer streamer = (Streamer) getItems().get(currentIndex);
                if(!streamer.hasFollowers()) {
                    streamer.updateFollowers(TwitchTV.getInstance().fetchFollowers(streamer.getId()));
                }

                TTVLookupCommand.addStreamerToEmbed(
                        builder,
                        streamer,
                        context.getMember(),
                        "Try: " + getTrigger() + " | " + getPageDetails()
                );
            }

            @Override
            public String getPageDetails() {
                return "Tubber: " + getPage() + "/" + getPages();
            }

            @Override
            public boolean nonPagingEmoteAdded(Emote e) {
                return false;
            }
        }.showMessage();
    }
}
