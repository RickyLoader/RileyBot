package Command.Commands.Passive;

import COD.Gunfight;
import Command.Structure.*;
import COD.Session;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

public class LeaderboardCommand extends PageableEmbedCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!");
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new Leaderboard(
                context.getMessageChannel(),
                context.getGuild(),
                Session.getHistory(),
                Gunfight.getThumb(),
                "GUNFIGHT LEADERBOARD!",
                "Here are the top gunfight performances!"
        );
    }


    /**
     * Gunfight history message, shows leaderboard of past gunfight sessions
     */
    public static class Leaderboard extends PageableEmbed {

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
}
