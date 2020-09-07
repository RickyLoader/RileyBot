package LOL;

import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import Network.ImgurManager;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class SummonerImage extends ImageBuilder {
    private Summoner summoner;

    /**
     * Initialise the class for building a summoner image
     *
     * @param channel      Channel to send built image to
     * @param emoteHelper  Emote helper
     * @param resourcePath Path to resources used by class
     * @param fontName     Name of font
     */
    public SummonerImage(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName) {
        super(channel, emoteHelper, resourcePath, fontName);
    }

    /**
     * Build the various sections of the image and draw them each on to the background image
     *
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     * @param args        Region
     */
    @Override
    public void buildImage(String nameQuery, String helpMessage, String... args) {
        String displayRegion = args[0].toUpperCase();
        String apiRegion = args[1];
        ImageLoadingMessage loading = new ImageLoadingMessage(
                getChannel(),
                getEmoteHelper(),
                "Summoner lookup: " + nameQuery.toUpperCase(),
                "One moment please, checking the " + displayRegion + " region.",
                "https://img.pngio.com/league-of-legends-needs-a-new-game-icon-league-of-legends-icon-png-256_256.png",
                helpMessage,
                new String[]{
                        "Fetching summoner data...",
                        "Building image...",
                        "Uploading image..."
                }
        );
        loading.showLoading();
        this.summoner = new Summoner(nameQuery, apiRegion, getResourcePath());
        if(!summoner.exists()) {
            loading.failLoading("That summoner doesn't exist  on the " + displayRegion + " server cunt");
            return;
        }
        loading.completeStage();
        try {
            BufferedImage bg = ImageIO.read(new File(getResourcePath() + "map.png"));
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
            ArrayList<Summoner.Champion> champions = summoner.getChampions();
            int bound = Math.min(champions.size(), 5);
            for(int i = 0; i < bound; i++) {
                Summoner.Champion c = champions.get(i);
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

            BufferedImage soloQueue = buildRankedImage(summoner.getSoloQueue());
            BufferedImage flexQueue = buildRankedImage(summoner.getFlexQueue());
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
            String url = ImgurManager.uploadImage(bg);
            loading.completeStage();
            loading.completeLoading(url);
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
    private BufferedImage buildRankedImage(Summoner.RankedQueue queue) {
        try {
            BufferedImage bg = ImageIO.read(queue.getBanner());
            BufferedImage helmet = ImageIO.read(queue.getHelmet());
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
    private BufferedImage buildChampionImage(Summoner.Champion champion) {
        BufferedImage championImage = null;
        try {
            championImage = ImageIO.read(champion.getImage());
            BufferedImage masteryIcon = ImageIO.read(champion.getMasteryIcon());
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
     * Build the profile icon from the summoner's chosen icon. Add a border around the icon based on summoner level
     * and add summoner level to the icon.
     *
     * @return Summoner profile icon
     */
    private BufferedImage buildProfileIcon() {
        BufferedImage profileIcon = null;
        try {
            profileIcon = ImageIO.read(summoner.getProfileIcon());
            BufferedImage borderOutline = ImageIO.read(summoner.getProfileBorder());
            Graphics g = profileIcon.getGraphics();
            g.drawImage(borderOutline, getCenterX(profileIcon, borderOutline), getCenterY(profileIcon, borderOutline), null);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return profileIcon;
    }

    /**
     * Build the profile banner displaying the summoner name, icon, level, and appropriate border based on rank
     *
     * @return Summoner profile banner
     */
    private BufferedImage buildProfileBanner() {
        BufferedImage banner = null;
        try {
            banner = ImageIO.read(summoner.getProfileBanner());
            BufferedImage profileIcon = buildProfileIcon();
            BufferedImage levelCircle = ImageIO.read(new File(getResourcePath() + "Summoner/Banners/level_circle.png"));
            BufferedImage borderOutline = ImageIO.read(summoner.getProfileBorder());

            Graphics g = levelCircle.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            FontMetrics fm = g.getFontMetrics();
            String level = String.valueOf(summoner.getLevel());
            g.drawString(level, (levelCircle.getWidth() - fm.stringWidth(level)) / 2, ((levelCircle.getHeight() - fm.getHeight()) / 2) + fm.getMaxAscent());
            g.drawImage(borderOutline, getCenterX(levelCircle, borderOutline), getCenterY(levelCircle, borderOutline), null);

            g = banner.getGraphics();
            g.setFont(getGameFont().deriveFont(80f));
            fm = g.getFontMetrics();
            g.drawImage(levelCircle, banner.getWidth() - (levelCircle.getWidth() * 2), getCenterY(banner, levelCircle), null);
            String name = summoner.getName();
            g.drawString(name, (banner.getWidth() - fm.stringWidth(name)) / 2, ((banner.getHeight() - fm.getHeight()) / 2) + fm.getMaxAscent());
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
