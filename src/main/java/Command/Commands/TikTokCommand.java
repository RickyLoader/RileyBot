package Command.Commands;

import Command.Commands.OSRSLendingCommand.QUANTITY_ABBREVIATION;
import Command.Structure.*;
import TikTokAPI.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.text.SimpleDateFormat;

import static Command.Commands.OSRSLendingCommand.QUANTITY_ABBREVIATION.*;

/**
 * Replace TikTok URLs with the video & details about the creator
 */
public class TikTokCommand extends OnReadyDiscordCommand {
    private static final String
            ATTACHMENT_PREFIX = "attachment://",
            VIDEO_FILENAME = "post.mp4",
            VIDEO_PREVIEW_FILENAME = "preview.png",
            MUSIC_FILENAME = "album.png",
            CREATOR_FILENAME = "creator.png";

    private final TikTok tikTok;
    private String likes, comments, shares, plays, blank;

    /**
     * Set to secret to prevent showing in help command.
     * Initialise TikTok instance.
     */
    public TikTokCommand() {
        super("[TikTok URL]", "View some TikToks");
        setSecret(true);
        this.tikTok = new TikTok();
    }

    @Override
    public void execute(CommandContext context) {
        final MessageChannel channel = context.getMessageChannel();

        new Thread(() -> {
            TikTokPost post = tikTok.getVideoByUrl(context.getMessageContent());

            // API error
            if(post == null) {
                return;
            }

            context.getMessage().delete().queue();

            // Send the message and add the video (to ensure it appears below the message)
            getMessageAction(channel, post).queue(message -> {
                if(post.hasVideo()) {
                    channel.sendFile(post.getVideo(), VIDEO_FILENAME).queue();
                }
            });
        }).start();
    }

    /**
     * Get the message action required to send a TikTok embed to the given channel
     *
     * @param channel Channel to send to
     * @param post    Post to create message action for
     * @return TikTok post message action
     */
    private MessageAction getMessageAction(MessageChannel channel, TikTokPost post) {
        Creator creator = post.getCreator();
        Music music = post.getMusic();

        EmbedBuilder builder = new EmbedBuilder()
                .setThumbnail(TikTok.LOGO)
                .setColor(EmbedHelper.PURPLE)
                .setTitle(post.hasDescription() ? post.getDescription() : "No title provided!", post.getUrl())

                // Author image may fail to download - use a default image URL when this happens
                .setAuthor(
                        "@" + creator.getId() + " | " + creator.getName(),
                        creator.getProfileUrl(),
                        creator.hasThumbnailImage()
                                ? ATTACHMENT_PREFIX + CREATOR_FILENAME
                                : Creator.DEFAULT_THUMBNAIL_URL
                )
                // Video may fail to download - use an image preview if available
                .setImage(
                        shouldDisplayPreviewImage(post)
                                ? ATTACHMENT_PREFIX + VIDEO_PREVIEW_FILENAME : EmbedHelper.SPACER_IMAGE
                )
                .setFooter(
                        music.getTitle() + " - " + music.getAuthors(3),
                        music.hasThumbnailImage()
                                ? ATTACHMENT_PREFIX + MUSIC_FILENAME
                                : Music.DEFAULT_THUMBNAIL_URL
                )
                .setDescription(
                        "**Posted**: " + new SimpleDateFormat("dd/MM/yyyy").format(post.getDate())
                                + "\n\n" + buildSocialEmoteDescription(post.getSocialResponse())
                );

        MessageAction action = channel.sendMessage(builder.build());

        // Only attach preview image if no video is available and an image is available
        if(shouldDisplayPreviewImage(post)) {
            action = action.addFile(post.getPreviewImage(), VIDEO_PREVIEW_FILENAME);
        }

        // Attach thumbnail image for the creator
        if(creator.hasThumbnailImage()) {
            action = action.addFile(creator.getThumbnailImage(), CREATOR_FILENAME);
        }

        // Attach album thumbnail image
        if(music.hasThumbnailImage()) {
            action = action.addFile(music.getThumbnailImage(), MUSIC_FILENAME);
        }

        return action;
    }

    /**
     * Build the description of the message embed. Display the social response to the post (likes/comments/etc)
     * with their accompanying emotes.
     *
     * @param socialResponse Social response to the TikTok post
     * @return String detailing social response
     */
    private String buildSocialEmoteDescription(SocialResponse socialResponse) {
        return likes + " " + formatSocialCount(socialResponse.getLikes())
                + blank
                + comments + " " + formatSocialCount(socialResponse.getComments())
                + blank
                + shares + " " + formatSocialCount(socialResponse.getShares())
                + blank
                + plays + " " + formatSocialCount(socialResponse.getPlays());
    }

    /**
     * Format the given social count in to a String.
     * E.g 2000 -> 2k or 2500000 -> 2.5m
     *
     * @param count Social count to format (likes etc)
     * @return Formatted count
     */
    private String formatSocialCount(int count) {
        QUANTITY_ABBREVIATION abbreviation = forQuantity(count);

        // Don't need a symbol for singular
        if(abbreviation == SINGULAR) {
            return String.valueOf(count);
        }
        else {
            return ((double) count / abbreviation.getMultiplier()) + abbreviation.getSymbol("");
        }
    }

    /**
     * Check if a preview image for the video should be displayed in the message embed.
     * This should only occur when the video has failed to download and an image is available, otherwise
     * a default spacer image will be used.
     *
     * @param post TikTok post to check
     * @return Should display preview image
     */
    private boolean shouldDisplayPreviewImage(TikTokPost post) {
        return !post.hasVideo() && post.hasPreviewImage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return TikTok.isVideoUrl(message.getContentDisplay());
    }

    /**
     * Initialise social emotes
     */
    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.likes = emoteHelper.getTikTokLikes().getAsMention();
        this.comments = emoteHelper.getTikTokComments().getAsMention();
        this.plays = emoteHelper.getTikTokPlays().getAsMention();
        this.shares = emoteHelper.getTikTokShares().getAsMention();
        this.blank = emoteHelper.getBlankGap().getAsMention();
    }
}
