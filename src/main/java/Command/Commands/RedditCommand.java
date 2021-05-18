package Command.Commands;

import COD.Assets.Ratio;
import Command.Commands.GIFCommand.GIF;
import Command.Structure.*;
import Network.NetworkRequest;
import Reddit.PostContent;
import Reddit.RedditPost;
import Reddit.Subreddit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Take Reddit post URLs and replace with an embed detailing the post
 */
public class RedditCommand extends OnReadyDiscordCommand {
    private final String thumbnail = "https://i.imgur.com/zSNgbNA.png";
    private String upvote, downvote, comment, blankGap;

    public RedditCommand() {
        super("[reddit url]", "Embed Reddit posts/videos!");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        new Thread(() -> {
            RedditPost redditPost = getPostInfo(context.getMessageContent());
            MessageEmbed redditEmbed = buildRedditPostEmbed(redditPost);
            PostContent postContent = redditPost.getContent();
            AuditableRestAction<Void> deleteUrl = context.getMessage().delete();
            MessageAction sendRedditEmbed = channel.sendMessage(redditEmbed);

            if(postContent.getType() == PostContent.TYPE.VIDEO) {
                byte[] video = EmbedHelper.downloadVideo(postContent.getContent());
                deleteUrl.queue(delete -> {
                    if(video == null) {
                        sendRedditEmbed.queue(message -> channel.sendMessage(postContent.getContent()).queue());
                        return;
                    }
                    sendRedditEmbed.queue(message -> channel.sendFile(video, "video.mp4").queue());
                });
            }
            else {
                deleteUrl.queue(delete -> sendRedditEmbed.queue());
            }
        }).start();
    }

    /**
     * Get a message embed detailing the given Reddit post
     *
     * @param post Reddit post
     * @return Message embed detailing Reddit post
     */
    private MessageEmbed buildRedditPostEmbed(RedditPost post) {
        Subreddit subreddit = post.getSubreddit();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
        String description = buildDescription(post);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setAuthor(
                        subreddit.getName(),
                        subreddit.getUrl(),
                        subreddit.getImageUrl()
                )
                .setTitle(post.getTitle(), post.getUrl())
                .setFooter("Posted by: " + post.getAuthor() + " on " + dateFormat.format(post.getDatePosted()))
                .setThumbnail(thumbnail);

        PostContent content = post.getContent();
        switch(content.getType()) {
            case TEXT:
                String text = content.getContent();
                int limit = 300;
                if(text.length() > limit) {
                    text = text.substring(0, 300) + "...";
                }
                description = text + "\n\n" + description;
                break;
            case LINK:
                description = "**Post link**: " + EmbedHelper.embedURL("View", content.getContent())
                        + "\n\n" + description;
                break;
            case IMAGE:
                builder.setImage(content.getContent());
                break;
            case VIDEO:
                description = "**Note**: Videos will not have any sound!\n\n" + description;
        }
        return builder.setDescription(description).build();
    }

    /**
     * Build the description to use in the message embed
     *
     * @param post Reddit post
     * @return Embed description
     */
    private String buildDescription(RedditPost post) {
        DecimalFormat commaFormat = new DecimalFormat("#,###");
        Ratio votes = post.getVotes();
        return upvote + " " + commaFormat.format(votes.getNumerator())
                + " (" + votes.getNumeratorPercentage() + ")"
                + blankGap
                + downvote + " " + commaFormat.format(votes.getDenominator())
                + blankGap
                + comment + " " + commaFormat.format(post.getComments());
    }

    /**
     * Get the info for a Reddit post from the given URL to the post
     *
     * @param url URL to reddit post
     * @return Reddit post info
     */
    private RedditPost getPostInfo(String url) {
        url = url.split("\\?")[0];
        String json = new NetworkRequest(url + ".json", false).get().body;

        JSONObject info = new JSONArray(json)
                .getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data");

        return new RedditPost(
                info.getString("title"),
                info.getString("author"),
                url,
                getPostContent(info),
                calculateRatio(info.getInt("ups"), info.getDouble("upvote_ratio")),
                info.getInt("num_comments"),
                getSubredditInfo(info.getString("subreddit")),
                new Date(info.getLong("created_utc") * 1000)
        );
    }

    /**
     * Get the content of the post - text/URL
     *
     * @param post Post JSON object
     * @return Post content
     */
    private PostContent getPostContent(JSONObject post) {
        String crosspost = "crosspost_parent_list";

        // Media is stored in the original post
        if(post.has(crosspost)) {
            post = post.getJSONArray(crosspost).getJSONObject(0);
        }

        String text = post.getString("selftext");
        if(post.getBoolean("is_video")) {
            return new PostContent(
                    post.getJSONObject("media").getJSONObject("reddit_video").getString("fallback_url"),
                    PostContent.TYPE.VIDEO
            );
        }
        else if(text.isEmpty()) {
            String url = post.getString("url");
            if(post.getString("post_hint").equals("link")) {
                return new PostContent(
                        url,
                        PostContent.TYPE.LINK
                );
            }
            return new PostContent(
                    processImageUrl(url),
                    PostContent.TYPE.IMAGE
            );
        }
        return new PostContent(
                text,
                PostContent.TYPE.TEXT
        );
    }

    /**
     * Refactor the image/gif URL if it will not embed properly in discord
     *
     * @param url Image/gif URL
     * @return Refactored URL
     */
    private String processImageUrl(String url) {
        if(url.matches("https?://i.imgur.com/.+.gifv/?")) {
            url = url.substring(0, url.length() - 1);
            return url;
        }
        String redGifs = "https?://(www\\.)?redgifs.com/watch/.+";
        String gfyCat = "https://(www\\.)?gfycat.com/.+";
        if(url.matches(redGifs) || url.matches(gfyCat)) {
            String[] urlArgs = url.split("/");
            GIF gif = GIFCommand.getGifById(urlArgs[urlArgs.length - 1], url.matches(redGifs));
            if(gif != null) {
                url = gif.getUrl();
            }
        }
        return url;
    }

    /**
     * Calculate the ratio of upvotes to downvotes.
     * Reddit API doesn't provide the number downvotes but provides the number of upvotes and the upvote percentage.
     *
     * @param upvotes     Number of upvotes
     * @param upvoteRatio Ratio of upvotes - e.g 0.97
     * @return Ratio of upvotes/downvotes
     */
    private Ratio calculateRatio(int upvotes, double upvoteRatio) {
        upvoteRatio = upvoteRatio * 100;
        double remaining = 100 - upvoteRatio;

        return new Ratio(
                upvotes,
                (int) (remaining * (upvotes / upvoteRatio))
        );
    }

    /**
     * Get the subreddit info for the given subreddit name
     *
     * @param name Name of subreddit
     * @return Subreddit info
     */
    private Subreddit getSubredditInfo(String name) {
        String json = new NetworkRequest("https://www.reddit.com/r/" + name + "/about.json", false).get().body;

        JSONObject info = new JSONObject(json)
                .getJSONObject("data");

        String icon = info.getString("icon_img");
        String communityIcon = info.getString("community_icon").split("\\?")[0];

        return new Subreddit(
                info.getString("display_name_prefixed"),
                "https://www.reddit.com" + info.getString("url"),
                icon.isEmpty() ? (communityIcon.isEmpty() ? thumbnail : communityIcon) : icon
        );
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.matches("https://(www.)?reddit.com/r/.+/comments/.+/.+/?");
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        upvote = EmoteHelper.formatEmote(emoteHelper.getRedditUpvote());
        downvote = EmoteHelper.formatEmote(emoteHelper.getRedditDownvote());
        blankGap = EmoteHelper.formatEmote(emoteHelper.getBlankGap());
        comment = EmoteHelper.formatEmote(emoteHelper.getFacebookComments());
    }
}
