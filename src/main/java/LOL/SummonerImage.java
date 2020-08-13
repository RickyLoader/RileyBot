package LOL;

import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import Network.ImgurManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class SummonerImage extends ImageBuilder {
    private Summoner summoner;

    public SummonerImage(MessageChannel channel, Guild guild, String resourcePath, String fontName) {
        super(channel, guild, resourcePath, fontName);
    }

    @Override
    public void buildImage(String nameQuery) {
        ImageLoadingMessage loading = new ImageLoadingMessage(
                getChannel(),
                getGuild(),
                "LOL Summoner lookup: " + nameQuery.toUpperCase(),
                "One moment please.",
                "https://img.pngio.com/league-of-legends-needs-a-new-game-icon-league-of-legends-icon-png-256_256.png",
                new String[]{
                        "Fetching summoner data...",
                        "Building image...",
                        "Uploading image..."
                }
        );
        loading.showLoading();
        this.summoner = new Summoner(nameQuery, getResourcePath());
        if(!summoner.exists()) {
            loading.failLoading("That summoner doesn't exist  on the OCE server cunt");
            return;
        }
        loading.completeStage();
        try {
            BufferedImage bg = ImageIO.read(new File(getResourcePath() + "map.png"));
            Graphics g = bg.getGraphics();
            BufferedImage profileBanner = buildProfileBanner();
            g.drawImage(profileBanner, getCenterX(bg, profileBanner), 0, null);

            int padding = (bg.getWidth() - (5 * 265)) / 6;
            int x = padding;
            int y = profileBanner.getHeight() + 50;
            ArrayList<Summoner.Champion> champions = summoner.getChampions();
            for(int i = 0; i < 5; i++) {
                Summoner.Champion c = champions.get(i);
                BufferedImage championImage = buildChampionImage(c);
                g.drawImage(championImage, x, y, null);
                x += championImage.getWidth() + padding;
            }

            BufferedImage soloQueue = buildRankedImage(summoner.getSoloQueue());

            x = (int) ((bg.getWidth() / 2) - (soloQueue.getWidth() * 1.25));
            y += 800;

            g.drawImage(soloQueue, x, y, null);

            x = (bg.getWidth() / 2) + (soloQueue.getWidth() / 4);
            BufferedImage flexQueue = buildRankedImage(summoner.getFlexQueue());
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
            g.drawImage(helmet, getCenterX(bg, helmet), getCenterY(bg, helmet), null);
            g.setFont(getGameFont().deriveFont(30f));
            FontMetrics fm = g.getFontMetrics();
            String rank = queue.getTier() + " " + queue.getRank();
            String title = queue.getQueue();

            g.drawString(title, (bg.getWidth() - fm.stringWidth(title)) / 2, fm.getHeight() + 10);

            //g.drawString(rank, (bg.getWidth() - fm.stringWidth(rank)) / 2, ((bg.getHeight() - fm.getHeight()) / 2) + fm.getMaxAscent());


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
        try {
            BufferedImage championImage = ImageIO.read(champion.getImage());
            BufferedImage masteryIcon = ImageIO.read(champion.getMasteryIcon());
            Graphics g = championImage.getGraphics();
            g.setFont(getGameFont().deriveFont(40f));
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
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build the profile icon from the summoner's chosen icon. Add a border around the icon based on summoner level
     * and add summoner level to the icon.
     *
     * @return Summoner profile icon
     */
    private BufferedImage buildProfileIcon() {
        try {
            BufferedImage profileIcon = ImageIO.read(summoner.getProfileIcon());
            BufferedImage borderOutline = ImageIO.read(summoner.getProfileBorder());
            Graphics g = profileIcon.getGraphics();
            g.drawImage(borderOutline, getCenterX(profileIcon, borderOutline), getCenterY(profileIcon, borderOutline), null);
            return profileIcon;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build the profile banner displaying the summoner name, icon, level, and appropriate border based on rank
     *
     * @return Summoner profile banner
     */
    private BufferedImage buildProfileBanner() {
        try {
            BufferedImage banner = ImageIO.read(summoner.getProfileBanner());
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
            g.drawImage(profileIcon, profileIcon.getWidth(), getCenterY(banner, profileIcon), null);
            String name = summoner.getName();
            g.drawString(name, (banner.getWidth() - fm.stringWidth(name)) / 2, ((banner.getHeight() - fm.getHeight()) / 2) + fm.getMaxAscent());
            return banner;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
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
