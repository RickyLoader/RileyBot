package Command.Commands.Lookup;

import Command.Structure.CommandContext;
import Command.Structure.ImageLoadingMessage;
import LOL.SummonerOverview;
import LOL.TFTRankedQueue;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

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
        TFTRankedQueue queue = parseQueue(leagues.getJSONObject(0));
        String message = "```" + summonerOverview.getName() + " TFT Stats:```"
                + "```Tier: " + queue.getRankSummary()
                + "\nWins: " + queue.getWins()
                + "\nLosses: " + queue.getLosses()
                + "\nRatio: " + queue.getRatio()
                + "\nLP: " + queue.getPoints()
                + "\nVeteran: " + queue.isVeteran()
                + "\nInactive: " + queue.isInactive()
                + "\nFresh Blood: " + queue.isFreshBlood()
                + "\nHot Streak: " + queue.onHotStreak()
                + "```";
        channel.sendMessage(message)
                .addFile(ImageLoadingMessage.imageToByteArray(queue.getHelmet()), "helmet.png")
                .addFile(ImageLoadingMessage.imageToByteArray(queue.getBanner()), "banner.png")
                .addFile(ImageLoadingMessage.imageToByteArray(summonerOverview.getLevelBorder()), "border.png")
                .addFile(ImageLoadingMessage.imageToByteArray(summonerOverview.getProfileIcon()), "profile.png")
                .queue();
    }

    /**
     * Parse a JSONObject representing a summoner's stats in a TFT queue
     * in to a TFTRankedQueue object
     *
     * @param queue TFT queue stats
     * @return TFTRankedQueue object
     */
    private TFTRankedQueue parseQueue(JSONObject queue) {
        return new TFTRankedQueue.TFTRankedQueueBuilder()
                .setWinLoss(queue.getInt("wins"), queue.getInt("losses"))
                .setTierRank(queue.getString("tier"), queue.getString("rank"))
                .setLeaguePoints(queue.getInt("leaguePoints"))
                .setVeteran(queue.getBoolean("veteran"))
                .setHotStreak(queue.getBoolean("hotStreak"))
                .setFreshBlood(queue.getBoolean("freshBlood"))
                .setInactive(queue.getBoolean("inactive"))
                .build();
    }
}
