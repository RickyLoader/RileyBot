package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Collections;
import java.util.List;

public class PageableImageEmbed extends PageableEmbed {

    /**
     * Embedded message that can be paged through with emotes and displays a single image
     *
     * @param jda         JDA for listener
     * @param channel     Channel to send embed to
     * @param emoteHelper Emote helper
     * @param items       List of items to be displayed
     * @param thumb       Thumbnail to use for embed
     * @param title       Title to use for embed
     * @param colour      Optional colour to use for embed
     */
    public PageableImageEmbed(JDA jda, MessageChannel channel, EmoteHelper emoteHelper, List<String> items, String thumb, String title, int... colour) {
        super(jda, channel, emoteHelper, items, thumb, title, null, 1, colour);
    }

    @Override
    public void addFields(EmbedBuilder builder, int currentIndex) {
        builder.setImage((String) (getItems().get(currentIndex)));
    }

    @Override
    public void sortItems(List<?> items, boolean defaultSort) {
        Collections.reverse(items);
    }
}
