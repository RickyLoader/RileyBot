package Command.Commands.Passive;

import Bot.DiscordCommandManager;
import COD.Gunfight;
import Command.Structure.*;
import COD.Session;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

/**
 * Show the gunfight leaderboard
 */
public class LeaderboardCommand extends PageableEmbedCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!", "leaderboard!\nleaderboard! [position]");
    }

    @Override
    public void execute(CommandContext context) {
        String query = context.getLowerCaseMessage();
        if(query.equals(getTrigger())) {
            super.execute(context);
            return;
        }
        MessageChannel channel = context.getMessageChannel();
        int position = DiscordCommandManager.getQuantity(query.replace(getTrigger() + " ", ""));
        if(position > 0) {
            ArrayList<Session> sessions = Session.getHistory();
            if(sessions == null) {
                channel.sendMessage("Something went wrong fetching the leaderboard!").queue();
                return;
            }
            else if(sessions.isEmpty() || position > sessions.size()) {
                channel.sendMessage("There are only " + sessions.size() + " scores in the leaderboard!").queue();
                return;
            }
            channel.sendMessage(buildSessionMessage(sessions.get(position - 1), position)).queue();
            return;
        }
        channel.sendMessage(getHelpNameCoded()).queue();
    }


    /**
     * Build a message embed with more in depth information about the Gunfight Session
     *
     * @param session Session to build embed for
     * @param rank    Rank of the session
     * @return Message embed detailing Session
     */
    private MessageEmbed buildSessionMessage(Session session, int rank) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(EmbedHelper.getGreen());
        builder.setThumbnail(Gunfight.getThumb());
        builder.setTitle("GUNFIGHT RANK #" + rank);
        builder.setDescription("Here's the break down!");
        builder.addField("DATE", session.getFormattedDate(), true);
        builder.addBlankField(true);
        builder.addField("DURATION", session.getDuration(), true);
        builder.addField("W/L", session.getFormattedWinLoss(), true);
        builder.addBlankField(true);
        builder.addField("RATIO", session.getFormattedRatio(), true);
        builder.addField("LONGEST STREAK", String.valueOf(session.getLongestStreak()), false);
        builder.setFooter("Check out the leaderboard!", "https://i.imgur.com/yaLMta5.png");
        return builder.build();
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

    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger());
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
            return new String[]{String.valueOf(rank), session.getWinLossSummary(), session.formatStreak()};
        }
    }
}
