package LOL;

import com.objectplanet.image.PngEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SummonerImage {
    private final Summoner summoner;
    private final Font leagueFont;
    private final String res;

    public SummonerImage(Summoner summoner) {
        this.summoner = summoner;
        this.leagueFont = registerFont();
        this.res = summoner.getRes();
    }

    /**
     * Register league font with the graphics environment
     *
     * @return league font
     */
    private Font registerFont() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(summoner.getRes() + "font.otf")));
            return new Font("Friz Quadrata", Font.PLAIN, 12);

        }
        catch(Exception e) {
            return null;
        }
    }

    private BufferedImage buildProfileImage() {
        try {
            // Level circle
            BufferedImage circle = ImageIO.read(new File(res + "level_circle.png"));
            Graphics g = circle.getGraphics();
            g.setColor(Color.decode("#e3ddc6"));
            String level = String.valueOf(summoner.getLevel());
            int levelWidth = g.getFontMetrics().stringWidth(level);
            g.drawString(level, (circle.getWidth() / 2) - (levelWidth / 2), circle.getHeight() / 2 + g.getFont().getSize() / 2);

            // Draw level circle and border on icon
            BufferedImage profileIcon = ImageIO.read(summoner.getProfileIcon());
            BufferedImage borderOutline = ImageIO.read(summoner.getProfileBorder());

            g = profileIcon.getGraphics();
            g.drawImage(borderOutline, getCenterX(profileIcon, borderOutline), getCenterY(profileIcon, borderOutline), null);
            g.drawImage(circle, getCenterX(borderOutline, circle), profileIcon.getHeight() - circle.getHeight(), null);

            BufferedImage banner = ImageIO.read(new File(res + "banner.png"));
            g = banner.getGraphics();
            g.drawImage(profileIcon, getCenterX(banner, profileIcon), getCenterY(banner, profileIcon), null);

            g.setFont(leagueFont.deriveFont(22f));
            g.setColor(Color.decode("#e3ddc6"));

            String name = summoner.getName();
            int nameWidth = g.getFontMetrics().stringWidth(name) / 2;

            g.drawString(name, (banner.getWidth() / 2) - nameWidth, 62);
            g.dispose();
            return banner;
        }
        catch(Exception e) {
            return null;
        }
    }

    private int getCenterX(BufferedImage a, BufferedImage b) {
        return (a.getWidth() / 2) - (b.getWidth() / 2);
    }

    private int getCenterY(BufferedImage a, BufferedImage b) {
        return (a.getHeight() / 2) - (b.getHeight() / 2);
    }

    public File buildImage() {
        try {
            BufferedImage background = ImageIO.read(new File(res + "map.jpg"));
            Graphics g = background.getGraphics();
            BufferedImage profileIcon = buildProfileImage();
            int padding = (background.getWidth() - (6 * 222)) / 7;
            int x = padding;
            int y = background.getHeight() / 2 - 240;
            g.drawImage(profileIcon, x, y, null);
            ArrayList<Summoner.Champion> champions = summoner.getChampions();
            for(int i = 0; i < 5; i++) {
                Summoner.Champion c = champions.get(i);
                BufferedImage champImage = ImageIO.read(c.getImage());
                y = background.getHeight() / 2 - 202;
                x += padding + champImage.getWidth();
                g.drawImage(champImage, x, y, null);
            }
            g.dispose();
            return saveImage(background);
        }
        catch(Exception e) {
            return null;
        }
    }

    private File saveImage(BufferedImage image) {
        try {
            File save = new File(res + summoner.getName() + ".png");
            FileOutputStream output = new FileOutputStream(save);
            new PngEncoder().encode(image, output);
            output.close();
            return save;
        }
        catch(Exception e) {
            return null;
        }
    }

}
