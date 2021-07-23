package Command.Commands;

import COD.Assets.Ratio;
import Command.Commands.GIFCommand.GIF;
import Command.Structure.*;
import Countdown.Countdown;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Reddit.*;
import Reddit.PollContent.RedditPoll;
import Reddit.PollContent.RedditPoll.Option;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Take Reddit post URLs and replace with an embed detailing the post
 */
public class RedditCommand extends OnReadyDiscordCommand {
    private final String thumbnail = "https://i.imgur.com/zSNgbNA.png";
    private static final String
            POLL_KEY = "poll_data",
            VOTE_COUNT_KEY = "vote_count",
            CROSSPOST_KEY = "crosspost_parent_list";

    private String upvote, downvote, comment, winner, blankGap;

    public RedditCommand() {
        super("[reddit url]", "Embed Reddit posts/videos!");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        new Thread(() -> {
            RedditPost redditPost = getPostInfo(context.getMessageContent());
            AuditableRestAction<Void> deleteUrl = context.getMessage().delete();

            // Issue parsing
            if(redditPost == null) {
                return;
            }

            PostContent postContent = redditPost.getContent();
            if(postContent instanceof GalleryContent) {
                deleteUrl.queue(deleted -> showGalleryPost(context, redditPost));
                return;
            }

            MessageEmbed redditEmbed = buildRedditPostEmbed(redditPost);
            MessageAction sendRedditEmbed = channel.sendMessage(redditEmbed);

            if(postContent instanceof VideoPostContent) {
                VideoPostContent videoPostContent = (VideoPostContent) postContent;

                // Send post content
                deleteUrl.queue(deleted -> sendRedditEmbed.queue(message -> channel.sendTyping().queue(typing -> {

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
                })));
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
        else if(content instanceof PollContent) {
            description = buildPollPostDescription((PollContent) content);
        }
        else {
            VideoPostContent videoPostContent = (VideoPostContent) content;
            Boolean audioStatus = videoPostContent.getAudioStatus();
            String videoNote;

            // Wasn't able to determine if the post had an audio track (shouldn't really happen but just in case)
            if(audioStatus == null) {
                videoNote = "I wasn't able to determine if this post had audio!";
            }
            else if(audioStatus) {
                videoNote = "This post **did** have audio, ";

                // The download URL for video+audio may not have been retrieved for whatever reason (viddit may be down)
                videoNote += videoPostContent.hasDownloadUrl()
                        ? "if the video below does not, it was too big to download!"
                        : "but I was unable to get a download URL for it, blame "
                        + EmbedHelper.embedURL("viddet", "https://viddit.red/") + "!";
            }
            else {
                videoNote = "This post **did not** have any audio!";
            }
            description = "**Note**: " + videoNote + "\n\n" + description;
        }
        return builder
                .setFooter(buildFooter(post))
                .setDescription(description).build();
    }

    /**
     * Build a String detailing the given poll post content to display in a message embed.
     * Display the post text (usually describing the poll), as well as the poll options and their votes (if available).
     *
     * @param content Poll post content
     * @return String detailing poll
     */
    private String buildPollPostDescription(PollContent content) {
        final DecimalFormat voteFormat = new DecimalFormat("#,###");
        final String dateFormat = "dd/MM/yyyy", timeFormat = "HH:mm:ss";
        RedditPoll poll = content.getPoll();
        final Date closingDate = poll.getClosingDate();

        String description = content.getText()
                + "\n\n**Total Votes**: " + voteFormat.format(poll.getTotalVotes());

        Option[] options = poll.getOptions();

        // Can display option votes
        if(poll.isClosed()) {

            // Sort in descending order of votes
            Arrays.sort(options, Comparator.comparingInt(Option::getVotes).reversed());
            final int winningVotes = options[0].getVotes();

            StringBuilder optionsBuilder = new StringBuilder();

            for(int i = 0; i < options.length; i++) {
                Option option = options[i];

                // "1. Yes `(12)` <checkmark emote>
                optionsBuilder
                        .append(i + 1).append(". ")
                        .append(option.getText())
                        .append(" `(").append(voteFormat.format(option.getVotes())).append(")`");

                // Either winning option or an option with equal votes to the highest (draw)
                if(option.getVotes() == winningVotes) {
                    optionsBuilder.append(" ").append(winner);
                }

                if(i != options.length - 1) {
                    optionsBuilder.append("\n");
                }
            }

            // Exact closing time isn't relevant if the poll is closed
            description += "\n**Closed**: " + new SimpleDateFormat(dateFormat).format(closingDate)
                    + "\n\n**Options**:\n\n" + optionsBuilder.toString();
        }

        // Option votes unavailable until poll closes, display without votes
        else {
            final long daysRemaining = Countdown.from(System.currentTimeMillis(), closingDate.getTime()).getDays();

            // Display only time if the closing date is in under a day, otherwise display full date
            final String dateString = new SimpleDateFormat(
                    daysRemaining > 0 ? dateFormat + " " + timeFormat : timeFormat
            ).format(closingDate);

            final String codeBlock = "```";
            StringBuilder optionsBlock = new StringBuilder(codeBlock);

            for(int i = 0; i < options.length; i++) {
                optionsBlock.append(i + 1).append(". ").append(options[i].getText());
                if(i != options.length - 1) {
                    optionsBlock.append("\n");
                }
            }

            optionsBlock.append(codeBlock);

            description += " (option votes aren't disclosed until closing)\n**Closing**: " + dateString
                    + "\n\n**Options**: " + optionsBlock.toString();
        }
        return description;
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
     * @return Video download URL or null (if there is an error acquiring the download URL)
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

        // Media is stored in the original post
        if(post.has(CROSSPOST_KEY)) {
            post = post.getJSONArray(CROSSPOST_KEY).getJSONObject(0);
        }

        String text = post.getString("selftext");
        if(post.has(POLL_KEY)) {
            return parsePollDetails(post.getJSONObject(POLL_KEY), text);
        }
        else if(post.getBoolean("is_video")) {
            return parseVideoDetails(postUrl, post.getJSONObject("media").getJSONObject("reddit_video"));
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
     * Parse poll post content from the given JSON data for a poll.
     *
     * @param pollData Poll JSON data
     * @param postText Text from the poll post - usually describes poll
     * @return Poll post content
     */
    private PollContent parsePollDetails(JSONObject pollData, String postText) {
        JSONArray optionsData = pollData.getJSONArray("options");

        Option[] options = new Option[optionsData.length()];

        // Parse poll options
        for(int i = 0; i < optionsData.length(); i++) {
            JSONObject optionData = optionsData.getJSONObject(i);
            final String text = optionData.getString("text");

            // Option votes are not available until poll closes
            options[i] = optionData.has(VOTE_COUNT_KEY)
                    ? new Option(text, optionData.getInt(VOTE_COUNT_KEY))
                    : new Option(text);
        }

        return new PollContent(
                postText,
                new RedditPoll(
                        pollData.getInt("total_vote_count"),
                        new Date(pollData.getLong("voting_end_timestamp")),
                        options
                )
        );
    }

    /**
     * Parse video post content from the given video post JSON data.
     * Attempt to get a download URL for the video with sound (if it has sound),
     * otherwise provide a URL to the video without sound.
     *
     * @param postUrl   URL to video post
     * @param videoData Video JSON data
     * @return Video post content
     */
    private VideoPostContent parseVideoDetails(String postUrl, JSONObject videoData) {
        Boolean audioStatus = getAudioStatus(videoData);
        return new VideoPostContent(

                /*
                 * Only attempt to get a download URL for the video + audio if the post indicates an audio track is
                 * present or this is unable to be checked (just in case).
                 */
                audioStatus == null || audioStatus ? getVideoDownloadUrl(postUrl) : null,

                videoData.getString("fallback_url"), // No audio URL
                audioStatus
        );
    }

    /**
     * Check if the video post associated with the given video JSON data has an audio track.
     * This can be done by querying the {@code dash_url} value and checking for the presence of an audio track in
     * the given XML. If this fails, null will be returned meaning the post may or may not have audio.
     *
     * @param videoData JSON data of video from a video post
     * @return Video has audio or null (if unable to determine)
     */
    @Nullable
    private Boolean getAudioStatus(JSONObject videoData) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            NodeList tracks = docBuilder.parse(videoData.getString("dash_url"))
                    .getDocumentElement()
                    .getElementsByTagName("Period")
                    .item(0)
                    .getChildNodes();

            // Iterate through tracks and return true if an audio track is found
            for(int i = 0; i < tracks.getLength(); i++) {
                Node trackNode = tracks.item(i);

                // Not an element
                if(trackNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                // Audio track found
                if(((Element) trackNode).getAttribute("contentType").equals("audio")) {
                    return true;
                }
            }
            return false;
        }
        catch(Exception e) {
            return null;
        }
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
        return query.matches("https://(www.|old.)?reddit.com/r/.+/comments/.+/.+/?");
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        upvote = emoteHelper.getRedditUpvote().getAsMention();
        downvote = emoteHelper.getRedditDownvote().getAsMention();
        blankGap = emoteHelper.getBlankGap().getAsMention();
        comment = emoteHelper.getFacebookComments().getAsMention();
        winner = emoteHelper.getComplete().getAsMention();
    }
}
