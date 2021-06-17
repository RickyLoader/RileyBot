package Command.Structure;

import Facebook.FacebookPost;
import Facebook.PostDetails;
import Facebook.SocialResponse;
import Facebook.UserDetails;
import net.dv8tion.jda.api.EmbedBuilder;

import java.text.DecimalFormat;

/**
 * Page through images in a facebook post
 */
public class PageableFacebookPost extends CyclicalPageableEmbed {
    private final FacebookPost facebookPost;

    /**
     * Initialise the values
     *
     * @param context      Command context
     * @param facebookPost Facebook post to display
     */
    public PageableFacebookPost(CommandContext context, FacebookPost facebookPost) {
        super(context, facebookPost.getAttachments().getImages(), 1);
        this.facebookPost = facebookPost;
    }

    /**
     * Get a default embed builder to use with a given facebook post
     *
     * @param facebookPost Facebook post to get embed builder for
     * @param emoteHelper  Emote helper to display reactions/comments
     * @return Embed builder
     */
    public static EmbedBuilder getDefaultFacebookEmbed(FacebookPost facebookPost, EmoteHelper emoteHelper) {
        UserDetails userDetails = facebookPost.getAuthor();
        PostDetails postDetails = facebookPost.getPostDetails();
        SocialResponse socialResponse = facebookPost.getSocialResponse();
        DecimalFormat df = new DecimalFormat("#,###");

        String text = postDetails.getText();
        if(text.length() > 50) {
            text = text.substring(0, 50) + "...";
        }

        return new EmbedBuilder()
                .setColor(EmbedHelper.BLUE)
                .setTitle("View on Facebook", postDetails.getUrl())
                .setThumbnail("https://i.imgur.com/L4F1dEk.png")
                .setFooter("Posted: " + postDetails.getDatePublished())
                .setAuthor(
                        userDetails.getName(),
                        userDetails.getUrl(),
                        userDetails.getThumbnailUrl()
                )
                .setDescription(
                        text + "\n\n"
                                + emoteHelper.getFacebookReactions().getAsMention()
                                + " " + df.format(socialResponse.getReactions())
                                + emoteHelper.getBlankGap().getAsMention()
                                + emoteHelper.getFacebookComments().getAsMention()
                                + " " + df.format(socialResponse.getComments())
                );
    }

    @Override
    public EmbedBuilder getEmbedBuilder(String pageDetails) {
        return getDefaultFacebookEmbed(facebookPost, getEmoteHelper()).setFooter(pageDetails);
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex) {
        String image = (String) getItems().get(currentIndex);
        builder.setImage(image);
    }

    @Override
    public boolean nonPagingButtonPressed(String buttonId) {
        return false;
    }
}
