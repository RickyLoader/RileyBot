package Command.Structure;

import Instagram.ImageMedia;
import Instagram.Media;
import Instagram.Post;
import Instagram.VideoMedia;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

import static Command.Commands.InstagramCommand.addPostToEmbedBuilder;

/**
 * Pageable message for Instagram posts
 */
public abstract class PageableInstagramPosts extends CyclicalPageableEmbed<Post> {

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param items   List of posts to be displayed
     */
    public PageableInstagramPosts(CommandContext context, List<Post> items) {
        super(context, items, 1);
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex, Post post) {
        addPostToEmbedBuilder(builder, post, getEmoteHelper());

        ArrayList<Media> postMediaList = post.getMedia();

        // Nothing to display
        if(postMediaList.isEmpty()) {
            builder.setDescription(
                    builder.getDescriptionBuilder().append("\n\n**Error**: No media").toString()
            );
            return;
        }

        // Main item
        Media postMedia = postMediaList.get(0);

        builder.setImage(
                postMedia.getType() == Post.MEDIA_TYPE.IMAGE
                        ? ((ImageMedia) postMedia).getImageUrl()
                        : ((VideoMedia) postMedia).getThumbnailUrl()
        );
    }

    @Override
    public boolean nonPagingButtonPressed(String buttonId) {
        return false;
    }
}
