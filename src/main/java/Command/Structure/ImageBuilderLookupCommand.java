package Command.Structure;

import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Command to look up a player and build an image
 */
public abstract class ImageBuilderLookupCommand extends LookupCommand {

    /**
     * Initialise the command
     *
     * @param trigger   Trigger of command
     * @param desc      Description of command
     * @param maxLength Max length of name
     */
    public ImageBuilderLookupCommand(String trigger, String desc, int maxLength) {
        super(trigger, desc, maxLength);
    }

    public ImageBuilderLookupCommand(String trigger, String desc, String prefix, int maxLength) {
        super(trigger, desc, prefix, maxLength);
    }

    @Override
    public void processName(String name, MessageChannel channel, EmoteHelper emoteHelper) {
        buildImage(name, getImageBuilder(channel, emoteHelper));
    }

    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name, getHelpName());
    }

    /**
     * Get the image builder for building the player stat image
     *
     * @param channel     Channel to send image to
     * @param emoteHelper Emote helper
     * @return Image builder
     */
    public abstract ImageBuilder getImageBuilder(MessageChannel channel, EmoteHelper emoteHelper);
}