package Command.Commands.Passive;

import COD.Gunfight;
import Command.Structure.*;
import COD.Session;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Show the gunfight leaderboard
 */
public class LeaderboardCommand extends PageableEmbedCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!");
    }

    /**
     * Create the leaderboard embed
     *
     * @param context Context of command
     * @return Leaderboard embed
     */
    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new Leaderboard(
                context.getMessageChannel(),
                context.getGuild(),
                Session.getHistory(),
                Gunfight.getThumb(),
                "GUNFIGHT LEADERBOARD!",
                "Here are the top gunfight performances!",
                new String[]{"RANK", "W/L", "STREAK"}
        );
    }

    /**
     * Gunfight history message, shows leaderboard of past gunfight sessions
     */
    public static class Leaderboard extends PageableEmbed {

        public Leaderboard(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
            super(channel, guild, items, thumb, title, desc, columns);
        }

        @Override
        public void sortItems(List<?> items, boolean defaultSort) {
            Session.sortSessions((ArrayList<Session>) items, defaultSort);
        }

        @Override
        public String[] getValues(int index, List<?> items, boolean defaultSort) {
            int rank = defaultSort ? (index + 1) : (items.size() - index);
            Session session = (Session) items.get(index);
            return new String[]{String.valueOf(rank), session.formatRatio(), session.formatStreak()};
        }
    }
}
