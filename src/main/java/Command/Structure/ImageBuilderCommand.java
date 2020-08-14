package Command.Structure;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Command to look up a player and build an image
 */
public abstract class ImageBuilderCommand extends LookupCommand {

    /**
     * Initialise the command
     *
     * @param trigger   Trigger of command
     * @param desc      Description of command
     * @param maxLength Max length of name
     */
    public ImageBuilderCommand(String trigger, String desc, int maxLength) {
        super(trigger, desc, maxLength);
    }

    public ImageBuilderCommand(String trigger, String desc, String prefix, int maxLength) {
        super(trigger, desc, prefix, maxLength);
    }

    @Override
    public void processName(String name, MessageChannel channel, Guild guild) {
        buildImage(name, getImageBuilder(channel, guild));
    }

    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name);
    }

    /**
     * Get the image builder for building the player stat image
     *
     * @param channel Channel to send image to
     * @param guild   Guild to find emotes
     * @return Image builder
     */
    public abstract ImageBuilder getImageBuilder(MessageChannel channel, Guild guild);
}
