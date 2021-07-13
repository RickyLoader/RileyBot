package Command.Commands;

import COD.Assets.Ratio;
import Command.Commands.GIFCommand.GIF;
import Command.Structure.*;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Reddit.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

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
            if(redditPost == null) {
                return;
            }
            PostContent postContent = redditPost.getContent();
            if(postContent instanceof GalleryContent) {
                showGalleryPost(context, redditPost);
                return;
            }
            AuditableRestAction<Void> deleteUrl = context.getMessage().delete();
            MessageEmbed redditEmbed = buildRedditPostEmbed(redditPost);
            MessageAction sendRedditEmbed = channel.sendMessage(redditEmbed);

            if(postContent instanceof VideoPostContent) {
                VideoPostContent videoPostContent = (VideoPostContent) postContent;

                // Send post content
                deleteUrl.queue(deleted -> sendRedditEmbed.queue(message -> channel.sendTyping().queue()));

                // Attempt to download video
                byte[] video = EmbedHelper.downloadVideo(
                        videoPostContent.hasDownloadUrl()
                                ? videoPostContent.getDownloadUrl()
                                : videoPostContent.getNoAudioUrl()
                );

                // Send URL to video if download fails
                if(video == null) {
                    channel.sendMessage(videoPostContent.getNoAudioUrl()).queue();
                    return;
                }

                // Send video file
                channel.sendFile(video, "video.mp4").queue();
            }
            else {
                deleteUrl.queue(delete -> sendRedditEmbed.queue());
            }
        }).start();
    }

    /**
     * Display a gallery Reddit post in a pageable message embed
     *
     * @param context     Command context
     * @param galleryPost Gallery post to display
     */
    private void showGalleryPost(CommandContext context, RedditPost galleryPost) {
        new CyclicalPageableEmbed<String>(
                context,
                ((GalleryContent) galleryPost.getContent()).getGallery(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder(galleryPost)
                        .setFooter(buildFooter(galleryPost) + " | " + pageDetails)
                        .setDescription(buildDescription(galleryPost));
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String item) {
                builder.setImage(item);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No images to display").build();
            }

            @Override
            public String getPageDetails() {
                return "Image: " + getPage() + "/" + getPages();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Get a message embed detailing the given Reddit post
     *
     * @param post Reddit post
     * @return Message embed detailing Reddit post
     */
    private MessageEmbed buildRedditPostEmbed(RedditPost post) {
        EmbedBuilder builder = getDefaultEmbedBuilder(post);
        String description = buildDescription(post);
        PostContent content = post.getContent();

        if(content instanceof TextPostContent) {
            String text = ((TextPostContent) content).getText();
            int limit = 300;
            if(text.length() > limit) {
                text = text.substring(0, limit) + "...";
            }
            description = text + "\n\n" + description;
        }
        else if(content instanceof LinkContent) {
            description = "**Post link**: " + EmbedHelper.embedURL("View", ((LinkContent) content).getUrl())
                    + "\n\n" + description;
        }
        else if(content instanceof ImageContent) {
            builder.setImage(((ImageContent) content).getImageUrl());
        }
        else {
            description = "**Note**: Videos can take a a bit to show up and *may* not have sound!\n\n"
                    + description;
        }
        return builder
                .setFooter(buildFooter(post))
                .setDescription(description).build();
    }

    /**
     * Get the default embed builder to use when displaying a Reddit post
     *
     * @param post Post to initialise embed builder values with
     * @return Embed builder initialised with post values
     */
    private EmbedBuilder getDefaultEmbedBuilder(RedditPost post) {
        Subreddit subreddit = post.getSubreddit();
        String title = post.getTitle();
        if(title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            title = title.substring(0, MessageEmbed.TITLE_MAX_LENGTH);
        }
        return new EmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setAuthor(
                        subreddit.getName(),
                        subreddit.getUrl(),
                        subreddit.getImageUrl()
                )
                .setTitle(title, post.getUrl())
                .setThumbnail(thumbnail);
    }

    /**
     * Build the description to use in the message embed
     *
     * @param post Reddit post
     * @return Embed footer
     */
    private String buildFooter(RedditPost post) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
        return "Posted by: " + post.getAuthor() + " on " + dateFormat.format(post.getDatePosted());
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
        NetworkResponse response = new NetworkRequest(url + ".json?raw_json=1", false).get();
        if(response.code != 200) {
            return null;
        }

        JSONObject info = new JSONArray(response.body)
                .getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data");

        return new RedditPost(
                info.getString("title"),
                info.getString("author"),
                url,
                getPostContent(info, url),
                calculateRatio(info.getInt("ups"), info.getDouble("upvote_ratio")),
                info.getInt("num_comments"),
                getSubredditInfo(info.getString("subreddit")),
                new Date(info.getLong("created_utc") * 1000)
        );
    }

    /**
     * Reddit videos have the video & audio tracks served separately, attempt to
     * use the https://viddit.red/ website to get a download URL for the combined tracks.
     *
     * @param postUrl URL to the reddit video post
     * @return Video download URL or null
     */
    @Nullable
    private String getVideoDownloadUrl(String postUrl) {
        String url = "https://viddit.red/?url=" + postUrl;
        try {
            return Jsoup
                    .connect(url)
                    .get()
                    .getElementById("dlbutton")
                    .absUrl("href");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the content of the post - text/URL/video etc
     *
     * @param post    Post JSON object
     * @param postUrl URL to the reddit post
     * @return Post content
     */
    private PostContent getPostContent(JSONObject post, String postUrl) {
        String crosspost = "crosspost_parent_list";

        // Media is stored in the original post
        if(post.has(crosspost)) {
            post = post.getJSONArray(crosspost).getJSONObject(0);
        }

        String text = post.getString("selftext");
        if(post.getBoolean("is_video")) {
            String noAudioUrl = post.getJSONObject("media").getJSONObject("reddit_video").getString("fallback_url");
            return new VideoPostContent(
                    getVideoDownloadUrl(postUrl),
                    noAudioUrl
            );
        }
        else if(text.isEmpty()) {
            String url = post.getString("url");
            if(post.has("post_hint") && post.getString("post_hint").equals("link")) {
                return new LinkContent(url);
            }
            if(url.matches("https://www.reddit.com/gallery/.+")) {
                GalleryContent gallery = new GalleryContent();
                JSONObject mediaList = post.getJSONObject("media_metadata");
                for(String key : mediaList.keySet()) {
                    JSONArray mediaUrls = mediaList
                            .getJSONObject(key)
                            .getJSONArray("p");

                    JSONObject targetMedia = mediaUrls.getJSONObject(mediaUrls.length() - 1);
                    gallery.addImageToGallery(targetMedia.getString("u"));
                }
                return gallery;
            }
            return new ImageContent(processImageUrl(url));
        }
        return new TextPostContent(text);
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
        upvote = emoteHelper.getRedditUpvote().getAsMention();
        downvote = emoteHelper.getRedditDownvote().getAsMention();
        blankGap = emoteHelper.getBlankGap().getAsMention();
        comment = emoteHelper.getFacebookComments().getAsMention();
    }
}
