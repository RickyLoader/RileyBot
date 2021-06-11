package Command.Structure;

import Bot.ResourceHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Get a cool selfie with someone
 */
public abstract class SelfieCommand extends DiscordCommand {
    private final SelfieManager selfieManager;

    /**
     * Create a selfie command
     *
     * @param trigger       Command trigger
     * @param desc          Command description
     * @param selfieManager Selfie manager to use
     */
    public SelfieCommand(String trigger, String desc, SelfieManager selfieManager) {
        super(trigger, desc);
        this.selfieManager = selfieManager;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        new Thread(() -> {
            try {
                channel.sendTyping().queue();
                byte[] image = selfieManager.getSelfie(context.getMember().getUser());
                EmbedBuilder builder = getEmbedBuilder(context).setImage("attachment://" + SelfieManager.IMAGE_NAME);

                channel.sendMessage(builder.build()).addFile(image, SelfieManager.IMAGE_NAME).queue();
            }
            catch(Exception e) {
                channel.sendMessage("They were too busy to take a selfie with you, sorry!").queue();
            }
        }).start();
    }

    /**
     * Get the embed builder to use when displaying the selfie image.
     * (The selfie image will be set as the embed image before sending)
     *
     * @param context Command context
     * @return Embed builder to use when displaying selfie image
     */
    public abstract EmbedBuilder getEmbedBuilder(CommandContext context);

    /**
     * Get the help text to use in the embed footer
     *
     * @return Embed footer help text
     */
    public String getFooterHelpText() {
        return "Try: " + getTrigger();
    }

    /**
     * Get the selfie manager
     *
     * @return Selfie manager
     */
    public SelfieManager getSelfieManager() {
        return selfieManager;
    }

    /**
     * Draw a user's avatar image on to a background image in a specific size and position
     */
    public static class SelfieManager {
        private final BufferedImage background;
        private final ProfileDimensions profileDimensions;
        private final String thumbnailUrl;
        private static final String IMAGE_NAME = "selfie.jpg";

        /**
         * Create the selfie manager
         *
         * @param backgroundPath    Path to background image
         * @param thumbnailUrl      URL to thumbnail image
         * @param profileDimensions Profile dimensions/co-ordinates
         */
        public SelfieManager(String backgroundPath, String thumbnailUrl, ProfileDimensions profileDimensions) {
            this.background = new ResourceHandler().getImageResource(backgroundPath);
            this.thumbnailUrl = thumbnailUrl;
            this.profileDimensions = profileDimensions;
        }

        /**
         * Get the thumbnail URL
         *
         * @return Thumbnail URL
         */
        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        /**
         * Build the selfie image. Make a copy of the background image and resize the given user's avatar
         * to the provided dimensions before drawing it on to the background.
         *
         * @param user User to get avatar image for
         * @return Byte array of background image with user avatar drawn on
         * @throws Exception When unable to retrieve user avatar image
         */
        private byte[] getSelfie(User user) throws Exception {
            BufferedImage userImage = getUserAvatar(user);
            BufferedImage background = ImageBuilder.copyImage(this.background);

            Graphics g = background.getGraphics();
            g.drawImage(userImage, profileDimensions.getProfileX(), profileDimensions.getProfileY(), null);
            g.dispose();

            return ImageLoadingMessage.imageToByteArray(background);
        }

        /**
         * Get and resize the user's avatar to the given dimensions
         *
         * @param user User to retrieve avatar image for
         * @return Resized user avatar image
         * @throws Exception When unable to retrieve user avatar image
         */
        private BufferedImage getUserAvatar(User user) throws Exception {
            BufferedImage avatar = EmbedHelper.downloadImage(user.getEffectiveAvatarUrl());
            if(avatar == null) {
                throw new Exception("Unable to retrieve user avatar");
            }
            return resizeAvatar(avatar);
        }

        /**
         * Resize the user avatar image
         *
         * @param avatar User avatar image
         * @return Resized image
         */
        private BufferedImage resizeAvatar(BufferedImage avatar) {
            BufferedImage resized = new BufferedImage(
                    profileDimensions.getProfileWidth(),
                    profileDimensions.getProfileHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics g = resized.getGraphics();
            g.drawImage(
                    avatar,
                    0,
                    0,
                    profileDimensions.getProfileWidth(),
                    profileDimensions.getProfileHeight(),
                    null
            );
            g.dispose();
            return resized;
        }

        /**
         * Dimensions/co-ordinates used to resize/draw the profile image on to the background
         */
        public static class ProfileDimensions {
            private final int profileX, profileY, profileWidth, profileHeight;

            /**
             * Initialise the profile dimensions
             *
             * @param profileX      x co-ordinate to begin drawing profile image
             * @param profileY      y co-ordinate to begin drawing profile image
             * @param profileWidth  Width to resize profile image to
             * @param profileHeight Height to resize profile image to
             */
            public ProfileDimensions(int profileX, int profileY, int profileWidth, int profileHeight) {
                this.profileX = profileX;
                this.profileY = profileY;
                this.profileWidth = profileWidth;
                this.profileHeight = profileHeight;
            }

            /**
             * Get the height to resize the profile image to
             *
             * @return Profile image height
             */
            public int getProfileHeight() {
                return profileHeight;
            }

            /**
             * Get the x co-ordinate to begin drawing profile image
             *
             * @return Profile image x co-ordinate
             */
            public int getProfileX() {
                return profileX;
            }

            /**
             * Get the y co-ordinate to begin drawing profile image
             *
             * @return Profile image y co-ordinate
             */
            public int getProfileY() {
                return profileY;
            }

            /**
             * Get the width to resize the profile image to
             *
             * @return Profile image width
             */
            public int getProfileWidth() {
                return profileWidth;
            }
        }
    }
}
