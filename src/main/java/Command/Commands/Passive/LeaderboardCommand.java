package Command.Commands.Passive;

import COD.Gunfight;
import Command.Structure.*;
import COD.Session;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

/**
 * Show the gunfight leaderboard
 */
public class LeaderboardCommand extends DiscordCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!", "leaderboard!\nleaderboard! [position]");
    }

    @Override
    public void execute(CommandContext context) {
        String query = context.getLowerCaseMessage();
        if(query.equals(getTrigger())) {
            PageableTableEmbed leaderboard = getLeaderboardEmbed(context);
            leaderboard.showMessage();
            return;
        }
        MessageChannel channel = context.getMessageChannel();
        int position = getQuantity(query.replace(getTrigger() + " ", ""));
        if(position > 0) {
            ArrayList<Session> sessions = Session.getHistory();
            if(sessions == null) {
                channel.sendMessage("Something went wrong fetching the leaderboard!").queue();
                return;
            }
            else if(sessions.isEmpty() || position > sessions.size()) {
                channel.sendMessage("There are only " + sessions.size() + " scores on the leaderboard!").queue();
                return;
            }
            channel.sendMessage(buildSessionMessage(sessions.get(position - 1), position)).queue();
            return;
        }
        channel.sendMessage(getHelpNameCoded()).queue();
    }


    /**
     * Build a message embed with in depth information about the given Gunfight Session
     *
     * @param session Session to build embed for
     * @param rank    Rank of the session
     * @return Message embed detailing Session
     */
    private MessageEmbed buildSessionMessage(Session session, int rank) {
        return new EmbedBuilder()
                .setColor(EmbedHelper.GREEN)
                .setThumbnail(Gunfight.thumbnail)
                .setTitle("GUNFIGHT RANK #" + rank)
                .setDescription("Here's the break down!")
                .addField("DATE", session.getFormattedDate(), true)
                .addBlankField(true)
                .addField("DURATION", session.getDuration(), true)
                .addField("W/L", session.getFormattedWinLoss(), true)
                .addBlankField(true)
                .addField("RATIO", session.getFormattedRatio(), true)
                .addField("LONGEST STREAK", String.valueOf(session.getLongestStreak()), false)
                .setFooter("Check out the leaderboard!", "https://i.imgur.com/yaLMta5.png")
                .build();
    }

    /**
     * Create the leaderboard embed
     *
     * @param context Context of command
     * @return Leaderboard embed
     */
    public PageableTableEmbed getLeaderboardEmbed(CommandContext context) {
        return new PageableTableEmbed(
                context,
                Session.getHistory(),
                Gunfight.thumbnail,
                "GUNFIGHT LEADERBOARD!",
                "Here are the top gunfight performances!",
                new String[]{"RANK", "W/L", "STREAK"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                int rank = defaultSort ? (index + 1) : (items.size() - index);
                Session session = (Session) items.get(index);
                return new String[]{String.valueOf(rank), session.getWinLossSummary(), session.formatStreak()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                ArrayList<Session> sessions = new ArrayList<>();
                for(Object session : items) {
                    sessions.add((Session) session);
                }
                Session.sortSessions(sessions, defaultSort);
                updateItems(sessions);
            }
        };
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
