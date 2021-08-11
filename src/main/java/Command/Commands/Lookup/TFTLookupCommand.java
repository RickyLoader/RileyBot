package Command.Commands.Lookup;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.ImageLoadingMessage;
import Riot.LOL.SummonerOverview;
import Riot.LOL.TFTRankedQueue;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

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
        BufferedImage man = buildRankedImage(queue, summonerOverview);
        channel.sendFile(ImageLoadingMessage.imageToByteArray(man), "man.png").queue();
    }

    /**
     * Build an image displaying the summoner's TFT stats
     *
     * @param queue TFT ranked queue stats
     * @return Image displaying summoner TFT stats
     */
    private BufferedImage buildStatsImage(TFTRankedQueue queue) {
        BufferedImage man = new ResourceHandler().getImageResource(ResourceHandler.LEAGUE_BASE_PATH + "man.png");
        BufferedImage helmet = new BufferedImage(
                200,
                260,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = helmet.getGraphics();
        g.drawImage(queue.getHelmet(), 0, 0, helmet.getWidth(), helmet.getHeight(), null);
        g = man.getGraphics();
        g.drawImage(
                helmet,
                (man.getWidth() / 2) - (helmet.getWidth() / 2),
                0,
                null
        );

        g.setFont(FontManager.LEAGUE_FONT.deriveFont(40f));
        g.drawString(
                queue.getRankSummary(),
                235,
                320
        );
        g.drawString(
                queue.getPoints(),
                265,
                370
        );
        g.setColor(Color.BLACK);
        g.drawString(queue.getWins(), 40, 430);
        g.setColor(Color.ORANGE);
        g.drawString(queue.getLosses(), 40, 480);
        g.setColor(Color.WHITE);
        g.drawString(queue.getRatio(), 40, 530);

        g.setFont(g.getFont().deriveFont(25f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.GREEN);
        g.drawString("Hot Streak? - " + yessify(queue.onHotStreak()), 230, 550);
        g.setColor(Color.YELLOW);
        g.drawString("Fresh Blood? - " + yessify(queue.isFreshBlood()), 175, 270);
        g.setColor(Color.BLUE);
        String inactive = "Inactive? - " + yessify(queue.isInactive());
        g.drawString(inactive, 560 - fm.stringWidth(inactive), 430);
        g.setColor(Color.PINK);
        String veteran = "Veteran? - " + yessify(queue.isVeteran());
        g.drawString(veteran, 525 - fm.stringWidth(veteran), 485);
        g.dispose();
        return man;
    }

    /**
     * Build an image displaying the summoner's name and profile icon
     *
     * @param overview Summoner overview
     * @return Image displaying summoner details
     */
    private BufferedImage buildTitleImage(SummonerOverview overview) {
        BufferedImage title = new ResourceHandler().getImageResource(ResourceHandler.LEAGUE_BASE_PATH + "name.png");
        Graphics g = title.getGraphics();
        g.drawImage(
                overview.getProfileIcon(),
                10,
                (title.getHeight() / 2) - (overview.getProfileIcon().getHeight() / 2),
                null
        );

        g.drawImage(
                overview.getLevelIcon(),
                title.getWidth() - overview.getLevelIcon().getWidth() - 10,
                (title.getHeight() / 2) - (overview.getLevelIcon().getHeight() / 2),
                null
        );

        g.setFont(FontManager.LEAGUE_FONT.deriveFont(30f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                overview.getName(),
                (title.getWidth() / 2) - (fm.stringWidth(overview.getName()) / 2),
                (title.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return title;
    }

    /**
     * Build a sick ranked image display the summoner's TFT ranked stats
     *
     * @param queue    Queue to draw stats from
     * @param overview Summoner overview
     * @return Sick image
     */
    private BufferedImage buildRankedImage(TFTRankedQueue queue, SummonerOverview overview) {
        BufferedImage title = buildTitleImage(overview);
        BufferedImage stats = buildStatsImage(queue);
        BufferedImage image = new BufferedImage(
                title.getWidth(),
                stats.getHeight() + title.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = image.getGraphics();
        g.drawImage(title, 0, 0, null);
        g.drawImage(stats, 0, title.getHeight(), null);
        g.dispose();
        return image;
    }

    /**
     * Yessify the given boolean to either yes or no
     *
     * @param value Value to yessify
     * @return Boolean value yessifed to a yes or no
     */
    private String yessify(boolean value) {
        String[] yes = new String[]{"YES!", "YOU KNOW IT!", "YEAH!", "FUCK YEAH!"};
        String[] no = new String[]{"NO!", "NO WAY!", "FUCK NO!", "NA BRO!"};
        String[] toUse = value ? yes : no;
        return toUse[new Random().nextInt(toUse.length)];
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
