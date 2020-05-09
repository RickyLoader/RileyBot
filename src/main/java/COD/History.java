package COD;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;

/**
 * Gunfight history message, shows leaderboard of past gunfight sessions
 */
public class History {
    private ArrayList<Session> history;
    private MessageChannel channel;

    public History(MessageChannel channel) {
        this.history = Session.getHistory();
        this.channel = channel;
        if(history.size() > 0) {
            showHistory();
        }
        else {
            channel.sendMessage("There are gunfight matches in the history!").queue();
        }
    }

    /**
     * Builds an embedded message showing the gunfight leaderboards and sends it to the channel
     */
    private void showHistory() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("GUNFIGHT LEADERBOARD");
        builder.setThumbnail(Gunfight.getThumb());

        // Max of 5 matches
        int max = Math.min(history.size(), 5);
        builder.setDescription("Here are the top " + max + " gunfight performances out of " + Session.getTotalMatches() + "!");
        for(int i = 0; i < max; i++) {
            Session session = history.get(i);
            builder.addField("**RANK/DATE**", "[" + (i + 1) + "] " + session.formatDate(), true);
            builder.addField("**W/L**", session.getWins() + "/" + session.getLosses() + " (" + session.getRatio() + ")", true);
            builder.addField("**STREAK**", session.getStreak() + "", true);
        }
        channel.sendMessage(builder.build()).queue();
    }
}
