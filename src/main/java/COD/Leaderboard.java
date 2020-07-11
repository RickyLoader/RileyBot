package COD;

import Command.Structure.PageableEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;

/**
 * Gunfight history message, shows leaderboard of past gunfight sessions
 */
public class Leaderboard extends PageableEmbed {

    public Leaderboard(MessageChannel channel, Guild guild, ArrayList<?> items, String thumb, String title, String desc) {
        super(channel, guild, items, thumb, title, desc);
    }

    @Override
    public MessageEmbed.Field[] getField(int index, ArrayList<?> items, boolean header, boolean ascending) {
        int rank = ascending ? (index + 1) : (items.size() - index);
        Session session = (Session) items.get(index);
        if(header) {
            return new MessageEmbed.Field[]{
                    getTitleField("**RANK**", String.valueOf(rank), true),
                    getTitleField("**W/L**", session.formatRatio(), true),
                    getTitleField("**STREAK**", session.formatStreak(), true)
            };
        }
        return new MessageEmbed.Field[]{
                new MessageEmbed.Field(getBlankChar(), String.valueOf(rank), true),
                new MessageEmbed.Field(getBlankChar(), session.formatRatio(), true),
                new MessageEmbed.Field(getBlankChar(), session.formatStreak(), true)
        };
    }

    @Override
    public void sortItems(ArrayList<?> items, boolean defaultSort) {
        Session.sortSessions((ArrayList<Session>) items, defaultSort);
    }
}
