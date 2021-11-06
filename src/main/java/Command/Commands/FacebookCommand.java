package Command.Commands;

import Command.Structure.*;
import Facebook.*;
import Facebook.Attachments.VideoAttachment;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

/**
 * Take Facebook post URLs and replace with an embed detailing the post
 */
public class FacebookCommand extends DiscordCommand {
    private final FacebookScraper scraper = new FacebookScraper();

    public FacebookCommand() {
        super("[facebook url]", "Embed facebook posts/videos!");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        String url = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();
        EmoteHelper emoteHelper = context.getEmoteHelper();

        new Thread(() -> {
            FacebookPost facebookPost = scraper.fetchFacebookPost(url, context.getGuild());

            if(facebookPost == null) {
                return;
            }

            Attachments attachments = facebookPost.getAttachments();
            EmbedBuilder builder = PageableFacebookPost.getDefaultFacebookEmbed(facebookPost, emoteHelper);
            context.getMessage().delete().queue();

            if(attachments.hasImages()) {
                new PageableFacebookPost(context, facebookPost).showMessage();
            }
            else if(attachments.hasVideos()) {
                VideoAttachment videoAttachment = attachments.getVideos().get(0);
                if(!videoAttachment.hasVideo()) {
                    channel.sendMessage(builder.setImage(videoAttachment.getThumbnail()).build()).queue();
                }
                else {
                    MessageAction sendFile = channel.sendFile(
                            videoAttachment.getVideo(),
                            facebookPost.getAuthor().getName() + "_video.mp4"
                    );
                    channel.sendMessage(builder.build()).queue(message -> sendFile.queue());
                }
            }
            else {
                channel.sendMessage(builder.build()).queue();
            }
        }).start();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(FacebookScraper.BASE_URL);
    }
}
