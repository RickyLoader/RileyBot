package LOL;

import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SummonerImage extends ImageBuilder {
    private SummonerStats summonerStats;

    /**
     * Initialise the class for building a summoner image
     *
     * @param channel     Channel to send built image to
     * @param emoteHelper Emote helper
     */
    public SummonerImage(MessageChannel channel, EmoteHelper emoteHelper, Font font) {
        super(channel, emoteHelper, "/LOL/", font);
    }

    /**
     * Build the various sections of the image and draw them each on to the background image
     *
     * @param summonerOverview Basic summoner overview
     * @param helpMessage      Help message to display in loading message
     */
    public void buildImage(SummonerOverview summonerOverview, String helpMessage) {
        ImageLoadingMessage loading = new ImageLoadingMessage(
                getChannel(),
                getEmoteHelper(),
                "Summoner lookup: " + summonerOverview.getName(),
                "One moment please - Slobbering up the summoner's stats from the "
                        + summonerOverview.getRegion().getDisplayName().toUpperCase() + " region.",
                "https://img.pngio.com/league-of-legends-needs-a-new-game-icon-league-of-legends-icon-png-256_256.png",
                helpMessage,
                new String[]{
                        "Fetching summoner data...",
                        "Building image..."
                }
        );
        loading.showLoading();
        this.summonerStats = new SummonerStats(summonerOverview);
        loading.completeStage();
        try {
            BufferedImage bg = getResourceHandler().getImageResource(getResourcePath() + "map.png");
            Graphics g = bg.getGraphics();
            BufferedImage profileBanner = buildProfileBanner();
            if(profileBanner == null) {
                loading.failLoading("Something went wrong!");
                return;
            }
            g.drawImage(profileBanner, getCenterX(bg, profileBanner), 0, null);

            int padding = (bg.getWidth() - (5 * 265)) / 6;
            int x = padding;
            int y = profileBanner.getHeight() + 50;
            ArrayList<SummonerStats.Champion> champions = summonerStats.getChampions();
            int bound = Math.min(champions.size(), 5);
            for(int i = 0; i < bound; i++) {
                SummonerStats.Champion c = champions.get(i);
                BufferedImage championImage = buildChampionImage(c);
                if(championImage == null) {
                    if((bound + 1) < champions.size()) {
                        bound++;
                    }
                    continue;
                }
                g.drawImage(championImage, x, y, null);
                x += championImage.getWidth() + padding;
            }

            BufferedImage soloQueue = buildRankedImage(summonerStats.getSoloQueue());
            BufferedImage flexQueue = buildRankedImage(summonerStats.getFlexQueue());
            if(soloQueue == null || flexQueue == null) {
                loading.failLoading("Something went wrong!");
                return;
            }
            x = (int) ((bg.getWidth() / 2) - (soloQueue.getWidth() * 1.25));
            y += 800;

            g.drawImage(soloQueue, x, y, null);

            x = (bg.getWidth() / 2) + (soloQueue.getWidth() / 4);
            g.drawImage(flexQueue, x, y, null);

            loading.completeStage();
            loading.completeLoading(bg);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the ranked queue image displaying rank, tier, and stats
     *
     * @param queue Ranked queue to build image for
     * @return Ranked queue summary image
     */
    private BufferedImage buildRankedImage(RankedQueue queue) {
        try {
            BufferedImage bg = queue.getBanner();
            BufferedImage helmet = queue.getHelmet();
            Graphics g = bg.getGraphics();
            g.setFont(getGameFont().deriveFont(30f));
            FontMetrics fm = g.getFontMetrics();

            String rank = queue.getRankSummary();
            String title = queue.getQueue();
            String points = queue.getPoints();
            String winLossPercent = String.valueOf(queue.getRatio());
            String wins = queue.getWins();
            String losses = queue.getLosses();

            g.drawImage(helmet, getCenterX(bg, helmet), getCenterY(bg, helmet), null);

            int padding = (int) (fm.getHeight() * 1.5);
            int y = fm.getHeight() + 20;

            g.drawString("Ranked", (bg.getWidth() - fm.stringWidth("Ranked")) / 2, y);
            g.drawString(title, (bg.getWidth() - fm.stringWidth(title)) / 2, y + padding);

            y = (bg.getHeight() / 2) - helmet.getHeight();
            g.drawString(rank, (bg.getWidth() - fm.stringWidth(rank)) / 2, y);
            g.drawString(points, (bg.getWidth() - fm.stringWidth(points)) / 2, y + padding);

            y = (bg.getHeight() / 2) + helmet.getHeight() - fm.getHeight();
            g.setFont(getGameFont().deriveFont(24f));
            fm = g.getFontMetrics();
            padding = (int) (fm.getHeight() * 1.5);

            g.drawString(wins, (bg.getWidth() - fm.stringWidth(wins)) / 2, y);
            g.drawString(losses, (bg.getWidth() - fm.stringWidth(losses)) / 2, y + padding);
            g.drawString(winLossPercent, (bg.getWidth() - fm.stringWidth(winLossPercent)) / 2, y + (padding * 2));
            return bg;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build the champion image displaying mastery icon, level, and points
     *
     * @param champion Champion to build image for
     * @return Champion summary image
     */
    private BufferedImage buildChampionImage(SummonerStats.Champion champion) {
        BufferedImage championImage = null;
        try {
            championImage = getResourceHandler().getImageResource(champion.getImagePath());
            BufferedImage masteryIcon = getResourceHandler().getImageResource(champion.getMasteryIconPath());
            Graphics g = championImage.getGraphics();
            g.setFont(getGameFont().deriveFont(35f));
            FontMetrics fm = g.getFontMetrics();
            String name = champion.getName();
            int y = 500 + fm.getHeight();
            g.drawString(name, (championImage.getWidth() - fm.stringWidth(name)) / 2, y);
            y += 30;
            g.drawImage(masteryIcon, getCenterX(championImage, masteryIcon), y, null);
            g.setFont(getGameFont().deriveFont(25f));
            fm = g.getFontMetrics();
            String level = "Mastery Level " + champion.getLevel();
            y += masteryIcon.getHeight() + 30;
            g.drawString(level, (championImage.getWidth() - fm.stringWidth(level)) / 2, y + fm.getHeight());
            y += 50;
            String points = champion.getFormattedPoints() + " points";
            g.drawString(points, (championImage.getWidth() - fm.stringWidth(points)) / 2, y + fm.getHeight());
            return championImage;
        }
        catch(Exception e) {
            System.out.println("Missing info for " + champion.getName() + ":" + champion.getId());
        }
        return championImage;
    }

    /**
     * Build the profile banner displaying the summoner name, icon, level, and appropriate border based on rank
     *
     * @return Summoner profile banner
     */
    private BufferedImage buildProfileBanner() {
        BufferedImage banner = null;
        try {
            banner = summonerStats.getProfileBanner();
            BufferedImage profileIcon = summonerStats.getSummonerOverview().getProfileIcon();
            BufferedImage levelIcon = summonerStats.getSummonerOverview().getLevelIcon();

            Graphics g = banner.getGraphics();
            g.setFont(getGameFont().deriveFont(80f));
            FontMetrics fm = g.getFontMetrics();
            g.drawImage(
                    levelIcon,
                    banner.getWidth() - (levelIcon.getWidth() * 2),
                    getCenterY(banner, levelIcon),
                    null
            );
            String name = summonerStats.getSummonerOverview().getName();
            g.drawString(
                    name,
                    (banner.getWidth() - fm.stringWidth(name)) / 2,
                    (banner.getHeight() / 2) + (fm.getMaxAscent() / 2)
            );
            if(profileIcon != null) {
                g.drawImage(profileIcon, profileIcon.getWidth(), getCenterY(banner, profileIcon), null);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return banner;
    }

    /**
     * Get the x coordinate required to center image b within image a horizontally
     *
     * @param a Destination image
     * @param b Source image
     * @return X coordinate required to center b within a
     */
    private int getCenterX(BufferedImage a, BufferedImage b) {
        return (a.getWidth() / 2) - (b.getWidth() / 2);
    }

    /**
     * Get the y coordinate required to center image b within image a vertically
     *
     * @param a Destination image
     * @param b Source image
     * @return Y coordinate required to center b within a
     */
    private int getCenterY(BufferedImage a, BufferedImage b) {
        return (a.getHeight() / 2) - (b.getHeight() / 2);
    }
}
