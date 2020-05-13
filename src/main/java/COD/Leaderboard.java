package COD;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.ArrayList;

/**
 * Gunfight history message, shows leaderboard of past gunfight sessions
 */
public class Leaderboard {
    private ArrayList<Session> history;
    private MessageChannel channel;

    public Leaderboard(MessageChannel channel) {
        this.history = Session.getHistory();
        this.channel = channel;
        if(history.size() > 0) {
            showLeaderboard();
        }
        else {
            channel.sendMessage("There are gunfight matches on the leaderboard!").queue();
        }
    }

    /**
     * Builds an embedded message showing the gunfight leaderboard and sends it to the channel
     */
    private void showLeaderboard() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("GUNFIGHT LEADERBOARD");
        builder.setThumbnail(Gunfight.getThumb());

        // Max of 5 matches
        int max = Math.min(history.size(), 5);
        builder.setDescription("Here are the top " + max + " gunfight performances!");
        for(int i = 0; i < max; i++) {
            Session session = history.get(i);
            builder.addField("**RANK**", String.valueOf(i + 1), true);
            builder.addField("**W/L**", session.formatRatio(), true);
            builder.addField("**STREAK**", session.formatStreak(), true);
        }
        channel.sendMessage(builder.build()).queue();
    }
}
