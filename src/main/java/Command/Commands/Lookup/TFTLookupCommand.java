package Command.Commands.Lookup;

import Command.Structure.CommandContext;
import LOL.SummonerOverview;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Look up a LOL summoner and create an image displaying their TFT stats
 */
public class TFTLookupCommand extends SummonerLookupCommand {

    public TFTLookupCommand() {
        super("tftlookup", "Look up a summoner's TFT stats!", true);
    }

    @Override
    protected void onSummonerFound(SummonerOverview summonerOverview, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        channel.sendTyping().queue();

        String url = summonerOverview.getApiURL()
                + "league/v1/entries/by-summoner/"
                + summonerOverview.getId()
                + "?api_key=" + Secret.TFT_KEY;

        JSONArray leagues = new JSONArray(new NetworkRequest(url, false).get().body);
        if(leagues.isEmpty()) {
            channel.sendMessage(
                    member.getAsMention() + " " + summonerOverview.getName()
                            + " exists but they do not have any TFT data!"
            ).queue();
            return;
        }
        StringBuilder builder = new StringBuilder("```" + summonerOverview.getName() + " TFT Stats:```");
        for(int i = 0; i < leagues.length(); i++) {
            builder.append(parseQueue(leagues.getJSONObject(i)));
            if(i < leagues.length() - 1) {
                builder.append("\n\n");
            }
        }
        channel.sendMessage(builder.toString()).queue();
    }

    /**
     * Parse a JSONObject representing a summoner's stats in a TFT queue in to a String
     *
     * @param queue TFT queue stats
     * @return String summary of queue
     */
    private String parseQueue(JSONObject queue) {
        int wins = queue.getInt("wins");
        int losses = queue.getInt("losses");
        return "```" + queue.getString("queueType")
                + ":"
                + "\n\nTier: " + queue.getString("tier") + " " + queue.getString("rank")
                + "\nWin/Loss/Ratio: " + wins
                + "/" + losses
                + "/" + new DecimalFormat("#.##").format((double) wins / losses)
                + "\nLP: " + queue.getInt("leaguePoints")
                + "\nVeteran: " + queue.getBoolean("veteran")
                + "\nInactive: " + queue.getBoolean("inactive")
                + "\nFresh Blood: " + queue.getBoolean("freshBlood")
                + "\nHot Streak: " + queue.getBoolean("hotStreak")
                + "```";
    }
}
