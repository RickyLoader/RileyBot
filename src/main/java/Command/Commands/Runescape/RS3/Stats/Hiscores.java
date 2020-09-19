package Command.Commands.Runescape.RS3.Stats;

import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Build an image displaying a player's RS3 stats
 */
public class Hiscores extends ImageBuilder {
    public Hiscores(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName) {
        super(channel, emoteHelper, resourcePath, fontName);
    }

    @Override
    public void buildImage(String nameQuery, String helpMessage, String... args) {

    }
}
