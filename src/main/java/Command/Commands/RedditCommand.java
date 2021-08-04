package Command.Commands;

import COD.Assets.Ratio;
import Command.Structure.*;
import Countdown.Countdown;
import Reddit.*;
import Reddit.PollContent.RedditPoll;
import Reddit.PollContent.RedditPoll.Option;
import Reddit.Reddit.URL_TYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Take Reddit post URLs and replace with an embed detailing the post
 */
public class RedditCommand extends OnReadyDiscordCommand {
    private final Reddit reddit;
    private String upvote, downvote, comment, winner, blankGap;
    private Emote parseFailEmote;

    /**
     * Initialise Reddit instance
     */
    public RedditCommand() {
        super("[reddit url]", "Embed Reddit posts/videos!");
        setSecret(true);
        this.reddit = new Reddit();
    }

    @Override
    public void execute(CommandContext context) {
        new Thread(() -> {
            final String url = context.getMessageContent();
            URL_TYPE type = Reddit.getUrlType(url);

            if(type == URL_TYPE.SUBREDDIT) {
                handleSubredditUrl(context, url);
            }
            else {
                handlePostUrl(context, url);
            }
        }).start();
    }

    /**
     * Take a subreddit URL and embed the current top posts.
     * Gallery/video posts will only display the first image/video thumbnail with a message to try embedding
     * the post URL.
     *
     * @param context Command context
     * @param url     Subreddit URL
     */
    private void handleSubredditUrl(CommandContext context, String url) {
        Message message = context.getMessage();

        Subreddit subreddit = reddit.getSubredditByUrl(url);

        // Issue getting subreddit data
        if(subreddit == null) {
            message.addReaction(parseFailEmote).queue();
            return;
        }

        ArrayList<RedditPost> topPosts = reddit.getPostsBySubreddit(subreddit);

        PageableEmbed<RedditPost> pageablePosts = new PageableEmbed<RedditPost>(context, topPosts, 1) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder(subreddit).setFooter(pageDetails);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No posts to display").build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, RedditPost post) {
                PostContent content = post.getContent();

                addPostTitleToEmbedBuilder(builder, post)
                        .setFooter(getPageDetails() + " | " + buildFooter(post), getContentTypeImage(content));

                String description = buildPostDescription(post);

                // Show image
                if(content instanceof ImageContent) {
                    builder.setImage(((ImageContent) content).getImageUrl());
                }

                // Show first image in gallery
                else if(content instanceof GalleryContent) {
                    ArrayList<String> gallery = ((GalleryContent) content).getGallery();

                    if(!gallery.isEmpty()) {
                        builder.setImage(gallery.get(0));
                    }
                    description = "**Note**: This is a gallery post with "
                            + gallery.size() + " images, here's the first one (send the post URL to view them all).\n\n"
                            + description;
                }

                // Show video thumbnail
                else if(content instanceof VideoPostContent) {
                    builder.setImage(((VideoPostContent) content).getThumbnailUrl());
                    description = "**Note**: This is a video post, send the post URL to view the video.\n\n"
                            + description;
                }
                builder.setDescription(description);
            }

            @Override
            public String getPageDetails() {
                return "Post: " + getPage() + "/" + getPages();
            }
        };

        message.delete().queue(deleted -> pageablePosts.showMessage());
    }

    /**
     * Take a Reddit post URL and display it in the channel.
     * Display the various types of posts differently e.g pageable gallery posts.
     *
     * @param context Command context
     * @param url     Reddit post URL
     */
    private void handlePostUrl(CommandContext context, String url) {
        Message message = context.getMessage();
        MessageChannel channel = context.getMessageChannel();

        RedditPost redditPost = reddit.getPostByUrl(url);
        AuditableRestAction<Void> deleteUrl = message.delete();

        // Issue parsing post data
        if(redditPost == null) {
            message.addReaction(parseFailEmote).queue();
            return;
        }

        // Pageable gallery post
        PostContent postContent = redditPost.getContent();
        if(postContent instanceof GalleryContent) {
            deleteUrl.queue(deleted -> showGalleryPost(context, redditPost));
            return;
        }

        MessageEmbed redditEmbed = buildRedditPostEmbed(redditPost);
        MessageAction sendRedditEmbed = channel.sendMessage(redditEmbed);

        // Send video after the post details
        if(postContent instanceof VideoPostContent) {
            VideoPostContent videoPostContent = (VideoPostContent) postContent;

            // Send post content
            deleteUrl.queue(deleted -> sendRedditEmbed.queue(postSent -> channel.sendTyping().queue(typing -> {

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

        // Send link after post details
        else if(postContent instanceof LinkContent) {
            deleteUrl.queue(delete -> sendRedditEmbed.queue(sent -> channel.sendMessage(((LinkContent) postContent).getUrl()).queue()));
        }

        // All info is in embed
        else {
            deleteUrl.queue(delete -> sendRedditEmbed.queue());
        }
    }

    /**
     * Display a gallery Reddit post in a pageable message embed
     *
     * @param context     Command context
     * @param galleryPost Gallery post to display
     */
    private void showGalleryPost(CommandContext context, RedditPost galleryPost) {
        GalleryContent content = (GalleryContent) galleryPost.getContent();

        new CyclicalPageableEmbed<String>(
                context,
                ((GalleryContent) galleryPost.getContent()).getGallery(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder(galleryPost.getSubreddit());
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String image) {
                addPostTitleToEmbedBuilder(builder, galleryPost)
                        .setImage(image)
                        .setFooter(
                                buildFooter(galleryPost) + " | " + getPageDetails(),
                                getContentTypeImage(content)
                        )
                        .setDescription(buildPostDescription(galleryPost));
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
        String description = buildPostDescription(post);
        PostContent content = post.getContent();

        if(content instanceof ImageContent) {
            builder.setImage(((ImageContent) content).getImageUrl());
        }
        else if(content instanceof VideoPostContent) {
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
                .setFooter(buildFooter(post), getContentTypeImage(content))
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
     * Get the default embed builder to use in Reddit messages.
     * Display the subreddit as the author.
     *
     * @param subreddit Subreddit to display
     * @return Reddit embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(@Nullable Subreddit subreddit) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setThumbnail(Reddit.LOGO);

        if(subreddit != null) {
            builder.setAuthor(
                    subreddit.getName(),
                    subreddit.getUrl(),
                    subreddit.getImageUrl()
            );
        }
        return builder;
    }

    /**
     * Get the default embed builder to use when displaying a Reddit post
     * Display the subreddit as the author and the title as the title of the post (linking to the post).
     *
     * @param post Post to display
     * @return Embed builder displaying basic post details
     */
    private EmbedBuilder getDefaultEmbedBuilder(RedditPost post) {
        return addPostTitleToEmbedBuilder(getDefaultEmbedBuilder(post.getSubreddit()), post);
    }

    /**
     * Add the post title (linking to the post) to the given embed builder.
     *
     * @param builder Builder to add details to
     * @param post    Post to add title from
     * @return Embed builder
     */
    private EmbedBuilder addPostTitleToEmbedBuilder(EmbedBuilder builder, RedditPost post) {
        String title = post.getTitle();

        if(title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            title = title.substring(0, MessageEmbed.TITLE_MAX_LENGTH);
        }

        return builder.setTitle(title, post.getUrl());
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
     * Get the URL to an image representing the post content type - e.g a camera for video type etc
     *
     * @param content Reddit post content
     * @return Content type image URL
     */
    private String getContentTypeImage(PostContent content) {
        if(content instanceof ImageContent || content instanceof GalleryContent) {
            return "https://i.imgur.com/BMwz4yQ.png";
        }
        else if(content instanceof LinkContent) {
            return "https://i.imgur.com/YmjQSma.png";
        }
        else if(content instanceof PollContent) {
            return "https://i.imgur.com/ZSJC0fg.png";
        }
        else if(content instanceof TextPostContent) {
            return "https://i.imgur.com/omlxx9r.png";
        }

        // Video
        else {
            return "https://i.imgur.com/U33u1PB.png";
        }
    }

    /**
     * Build the description to use in a message embed for the given Reddit post.
     * This is the details of a poll in a poll post, the URL in a link post, etc.
     *
     * @param post Reddit post
     * @return Embed description
     */
    private String buildPostDescription(RedditPost post) {
        DecimalFormat commaFormat = new DecimalFormat("#,###");
        Ratio votes = post.getVotes();
        String description = upvote + " " + commaFormat.format(votes.getNumerator())
                + " (" + votes.getNumeratorPercentage() + ")"
                + blankGap
                + downvote + " " + commaFormat.format(votes.getDenominator())
                + blankGap
                + comment + " " + commaFormat.format(post.getComments());

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
        else if(content instanceof PollContent) {
            description = buildPollPostDescription((PollContent) content) + "\n\n" + description;
        }
        return description;
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        upvote = emoteHelper.getRedditUpvote().getAsMention();
        downvote = emoteHelper.getRedditDownvote().getAsMention();
        blankGap = emoteHelper.getBlankGap().getAsMention();
        comment = emoteHelper.getFacebookComments().getAsMention();
        winner = emoteHelper.getComplete().getAsMention();
        parseFailEmote = emoteHelper.getFail();
    }

    @Override
    public boolean matches(String query, Message message) {
        return Reddit.isRedditUrl(query);
    }
}
