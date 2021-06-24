package Command.Commands;

import Command.Commands.Lookup.TTVLookupCommand;
import Command.Structure.CommandContext;
import Command.Structure.CyclicalPageableEmbed;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Twitch.Streamer;
import Twitch.TwitchTV;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

/**
 * Cool hot tub streamers
 */
public class HotTubCommand extends DiscordCommand {
    private static final long HOT_TUB_CATEGORY = 116747788;

    public HotTubCommand() {
        super("hot tub", "Take a look at some hot tub streamers!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        ArrayList<Streamer> hotTubStreamers = TwitchTV.getInstance().getStreamersByCategoryId(HOT_TUB_CATEGORY);
        displayHotTubbers(context, hotTubStreamers);
    }

    /**
     * Display the hot tub streams in a pageable message embed
     *
     * @param context   Command context
     * @param streamers List of streamers to display
     */
    private void displayHotTubbers(CommandContext context, ArrayList<Streamer> streamers) {
        new CyclicalPageableEmbed<Streamer>(
                context,
                streamers,
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Streamer streamer) {
                if(!streamer.hasThumbnail()) {
                    streamer.updateThumbnail(TwitchTV.getInstance().getStreamerProfilePicture(streamer.getId()));
                }
                TTVLookupCommand.addStreamerToEmbed(
                        builder,
                        streamer,
                        context.getMember(),
                        "Try: " + getTrigger() + " | " + getPageDetails()
                );
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return new EmbedBuilder()
                        .setThumbnail(TwitchTV.TWITCH_LOGO)
                        .setColor(EmbedHelper.BLUE)
                        .setTitle("No Hot Tubbers!")
                        .setDescription("I didn't find any tubbies bro")
                        .build();
            }

            @Override
            public String getPageDetails() {
                return "Tubber: " + getPage() + "/" + getPages();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }
}
