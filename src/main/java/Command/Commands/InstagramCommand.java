package Command.Commands;

import Command.Structure.*;
import Facebook.SocialResponse;
import Instagram.*;
import Instagram.Post.MEDIA_TYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Embed Instagram URLs
 */
public class InstagramCommand extends OnReadyDiscordCommand {
    private final Instagram instagram;
    private long parseFailId;

    /**
     * Set to secret to prevent showing in help command
     */
    public InstagramCommand() {
        super("[instagram url]", "Embed Instagram posts/users");
        setSecret(true);
        this.instagram = new Instagram();
    }

    @Override
    public void execute(CommandContext context) {
        new Thread(() -> {
            Message message = context.getMessage();
            JDA jda = context.getJDA();

            final String url = context.getMessageContent();
            AuditableRestAction<Void> deleteMessage = message.delete();
            context.getMessageChannel().sendTyping().queue();

            if(Instagram.isPostUrl(url)) {
                Post post = instagram.getPostByUrl(url);

                // Issue retrieving post
                if(post == null) {
                    failRequest(message, jda);
                    return;
                }

                deleteMessage.queue(deleted -> {
                    switch(post.getType()) {
                        case CAROUSEL:
                            showCarouselPost(context, post);
                            return;
                        case IMAGE:
                            showImagePost(context, post);
                            return;

                        // Video
                        default:
                            showVideoPost(context, post);
                    }
                });
            }

            else if(Instagram.isUserUrl(url)) {
                User user = instagram.getUserFromUrl(url);

                // Issue retrieving user
                if(user == null) {
                    failRequest(message, jda);
                    return;
                }

                deleteMessage.queue(deleted -> showUserPosts(context, user, instagram.getRecentUserPosts(user)));
            }

            // Location
            else {
                Location location = instagram.getLocationByUrl(url);

                // Issue retrieving location
                if(location == null) {
                    failRequest(message, jda);
                    return;
                }

                ArrayList<Post> recentPosts = instagram.getRecentLocationPosts(location);
                deleteMessage.queue(deleted -> showLocationPosts(context, location, recentPosts));
            }
        }).start();
    }

    /**
     * Display the given video post in the channel.
     * Attempt to download and send the video alongside the message,
     * if this fails, use a thumbnail image from the video instead.
     *
     * @param context   Command context
     * @param videoPost Instagram video post
     */
    private void showVideoPost(CommandContext context, Post videoPost) {
        MessageChannel channel = context.getMessageChannel();
        VideoMedia videoDetails = (VideoMedia) videoPost.getMedia().get(0);

        EmbedBuilder videoEmbedBuilder = getDefaultEmbedBuilder(videoPost, context.getEmoteHelper());

        channel.sendMessage(videoEmbedBuilder.build()).queue(message -> {
            channel.sendTyping().queue();
            byte[] video = EmbedHelper.downloadVideo(videoDetails.getVideoUrl());

            // Failed to download
            if(video == null) {
                videoEmbedBuilder
                        .setImage(videoDetails.getThumbnailUrl())
                        .setDescription(
                                videoEmbedBuilder.getDescriptionBuilder()
                                        .append("\n\n**Error**: Failed to download video!")
                                        .toString()
                        );
                message.editMessage(videoEmbedBuilder.build()).queue();
            }
            else {
                channel.sendFile(video, "video.mp4").queue();
            }
        });
    }

    /**
     * Display the given image post in the channel.
     *
     * @param context   Command context
     * @param imagePost Instagram image post
     */
    private void showImagePost(CommandContext context, Post imagePost) {
        ImageMedia image = (ImageMedia) imagePost.getMedia().get(0);
        context.getMessageChannel().sendMessage(
                getDefaultEmbedBuilder(imagePost, context.getEmoteHelper()).setImage(image.getImageUrl()).build()
        ).queue();
    }

    /**
     * Display the given carousel post in the channel as a pageable message.
     * Video media within the carousel will have their thumbnails displayed instead of the video.
     *
     * @param context      Command context
     * @param carouselPost Instagram carousel post
     */
    private void showCarouselPost(CommandContext context, Post carouselPost) {
        new CyclicalPageableEmbed<Media>(
                context,
                carouselPost.getMedia(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder(carouselPost, context.getEmoteHelper()).setFooter(pageDetails);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                EmbedBuilder builder = getEmbedBuilder("No media");
                return builder.setDescription(
                        builder.getDescriptionBuilder()
                                .append("\n\n**Error**: No media found in post!")
                                .toString()
                ).build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Media media) {
                if(media.getType() == MEDIA_TYPE.IMAGE) {
                    builder.setImage(((ImageMedia) media).getImageUrl());
                }

                // Video
                else {
                    VideoMedia videoMedia = (VideoMedia) media;
                    builder
                            .setImage(videoMedia.getThumbnailUrl())
                            .setDescription(
                                    builder.getDescriptionBuilder()
                                            .append("\n\n**Note**: This is the thumbnail of a video.")
                                            .toString()
                            );
                }
            }

            @Override
            public String getPageDetails() {
                return "Media: " + getPage() + "/" + getPages();
            }
        }.showMessage();
    }

    /**
     * Get an embed builder with the Instagram logo & colour set
     *
     * @return Instagram embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder() {
        return new EmbedBuilder()
                .setThumbnail(Instagram.LOGO)
                .setColor(EmbedHelper.PURPLE);
    }

    /**
     * Get an embed builder initialised with details about the given post (excluding media).
     *
     * @param post        Post to display
     * @param emoteHelper Emote helper
     * @return Post embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(Post post, EmoteHelper emoteHelper) {
        EmbedBuilder builder = addUserToEmbedBuilder(getDefaultEmbedBuilder(), post.getUser());
        return addPostToEmbedBuilder(builder, post, emoteHelper);
    }

    /**
     * Add the details of the given Instagram post to the embed builder (excluding media).
     *
     * @param builder     Embed builder to add post details to
     * @param post        Post to add details from
     * @param emoteHelper Emote helper
     * @return Embed builder
     */
    public static EmbedBuilder addPostToEmbedBuilder(EmbedBuilder builder, Post post, EmoteHelper emoteHelper) {
        builder.setTitle(StringUtils.capitalize(post.getType().name().toLowerCase()) + " Post", post.getUrl());

        String description = "";

        // Post may have a caption
        if(post.hasCaption()) {
            String caption = post.getCaption().replaceAll("\\*", "").trim();

            final int maxLength = 100;

            // Truncate caption, they can be massive
            if(caption.length() > maxLength) {
                caption = caption.substring(0, maxLength) + "...";
            }

            description = caption + "\n\n";
        }

        description += "**Posted**: " + new SimpleDateFormat("dd/MM/yyyy").format(post.getDatePosted()) + "\n";

        // Post may have a tagged location - link to the explore page
        if(post.hasLocation()) {
            Location location = post.getLocation();
            description += "**Location**: "
                    + EmbedHelper.embedURL(location.getName(), location.getExploreUrl()) + "\n";
        }

        // Show mentioned people
        if(post.hasMentionedUsers()) {
            StringBuilder mentioned = new StringBuilder("**Mentioned**: ");
            ArrayList<String> mentionedUsernames = post.getMentionedUserNames();

            for(int i = 0; i < mentionedUsernames.size(); i++) {
                final String username = mentionedUsernames.get(i);
                final String url = User.getProfileUrlFromUsername(username);

                mentioned.append(EmbedHelper.embedURL("@" + username, url));

                // Don't add comma to last mention
                if(i < mentionedUsernames.size() - 1) {
                    mentioned.append(", ");
                }
            }

            description += mentioned.toString() + "\n";
        }

        // Add likes & comments
        SocialResponse socialResponse = post.getSocialResponse();
        DecimalFormat format = new DecimalFormat("#,###");

        description += "\n"
                + emoteHelper.getTikTokLikes().getAsMention() + " " + format.format(socialResponse.getReactions())
                + emoteHelper.getBlankGap().getAsMention()
                + emoteHelper.getFacebookComments().getAsMention() + " " + format.format(socialResponse.getComments());

        return builder.setDescription(description);
    }

    /**
     * Add the given Instagram user as the author of the embed builder
     *
     * @param builder Embed builder to add user to
     * @param user    Instagram user to add as author of embed builder
     * @return Embed builder
     */
    private EmbedBuilder addUserToEmbedBuilder(EmbedBuilder builder, User user) {
        return builder.setAuthor(
                user.getFullName() + " (@" + user.getUsername() + ")",
                user.getProfileUrl(), user.getImageUrl()
        );
    }

    /**
     * Display the given user's recent Instagram posts as a pageable message
     *
     * @param context Command context
     * @param user    User to display
     * @param posts   List of recent posts by the user to display
     */
    private void showUserPosts(CommandContext context, User user, ArrayList<Post> posts) {
        new PageableInstagramPosts(
                context,
                posts
        ) {

            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return addUserToEmbedBuilder(getDefaultEmbedBuilder(), user).setFooter(pageDetails);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No posts")
                        .setTitle("No recent posts!")
                        .build();
            }

            @Override
            public String getPageDetails() {
                return "@" + user.getUsername() + " recent post: " + getPage() + "/" + getPages();
            }
        }.showMessage();
    }

    /**
     * Display recent posts from the given location as a pageable message
     *
     * @param context  Command context
     * @param location Location to display
     * @param posts    List of recent posts from the location to display
     */
    private void showLocationPosts(CommandContext context, Location location, ArrayList<Post> posts) {
        new PageableInstagramPosts(
                context,
                posts
        ) {

            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder().setFooter(pageDetails);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No posts")
                        .setTitle("No recent posts in " + location.getName())
                        .build();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Post post) {
                addUserToEmbedBuilder(builder, post.getUser());
                super.displayItem(builder, currentIndex, post);
            }

            @Override
            public String getPageDetails() {
                return location.getName() + " popular post: " + getPage() + "/" + getPages();
            }
        }.showMessage();
    }

    /**
     * Add a reaction to the given message indicating the Instagram request failed
     *
     * @param message Message to add fail reaction to
     * @param jda     JDA for retrieving emote
     */
    private void failRequest(Message message, JDA jda) {
        Emote emote = jda.getEmoteById(parseFailId);

        // Unable to retrieve emote
        if(emote == null) {
            return;
        }

        message.addReaction(emote).queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return Instagram.isInstagramUrl(message.getContentDisplay());
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.parseFailId = emoteHelper.getFail().getIdLong();
    }
}
