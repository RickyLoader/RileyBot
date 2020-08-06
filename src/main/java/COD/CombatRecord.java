package COD;

import Command.Structure.ImageLoadingMessage;
import Network.ImgurManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Build an image containing the user's Modern Warfare stats
 */
public class CombatRecord {

    private Player player;
    private final String resources = "src/main/resources/COD/";
    private Font codFont;
    private final MessageChannel channel;
    private final Guild guild;

    public CombatRecord(MessageChannel channel, Guild guild) {
        registerFont();
        this.channel = channel;
        this.guild = guild;
    }

    /**
     * Get the appropriate background image for the given weapon type
     *
     * @param type Weapon type
     * @return Background image for weapon type
     */
    private File getWeaponImage(Player.Weapon.TYPE type) {
        if(type == Player.Weapon.TYPE.PRIMARY || type == Player.Weapon.TYPE.SECONDARY) {
            return new File((resources + "Templates/wep_quadrant.png"));
        }
        return new File((resources + "Templates/lethal_quadrant.png"));
    }

    /**
     * Draw the weapon stats on to the weapon section
     *
     * @param weapon Weapon to draw
     * @return Weapon section with weapon stats drawn on
     */
    private BufferedImage drawWeapon(Player.Weapon weapon) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(getWeaponImage(weapon.getType()));
            Graphics g = image.getGraphics();
            g.setFont(codFont.deriveFont(50f));

            BufferedImage weaponImage = ImageIO.read(weapon.getImage());

            // Draw the weapon image on to the background and write the name and kills
            g.drawImage(weaponImage, (image.getWidth() / 2) - (weaponImage.getWidth() / 2), 250, null);
            g.drawString(weapon.getName(), (image.getWidth() / 2) - (g.getFontMetrics().stringWidth(weapon.getName()) / 2), 175);
            g.drawString(String.valueOf(weapon.getKills()), 277, 490);

            // Draw the other weapon stats (lethals only have kills)
            if(weapon.getType() != Player.Weapon.TYPE.LETHAL) {
                g.drawString(String.valueOf(weapon.getDeaths()), 277, 590);
                g.drawString(String.valueOf(weapon.getKd()), 277, 690);

                g.drawString(String.valueOf(weapon.getShotsHit()), 277, 841);
                g.drawString(String.valueOf(weapon.getShotsFired()), 277, 941);
                g.drawString(String.valueOf(weapon.getAccuracy()), 277, 1041);
            }

            g.setFont(codFont.deriveFont(28f));
            g.drawString(weapon.getImageTitle(), (image.getWidth()) / 2 - (g.getFontMetrics().stringWidth(weapon.getImageTitle())) / 2, 38);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the win loss ratio on to the win loss section
     *
     * @return Win loss section with stats drawn on
     */
    private BufferedImage drawWinLoss() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File((resources + "Templates/wl_quadrant.png")));
            Graphics g = image.getGraphics();
            g.setFont(codFont);
            int x = 288;
            int y = 175;
            g.drawString(String.valueOf(player.getWins()), x, y);
            y += 165;
            g.drawString(String.valueOf(player.getLosses()), x, y);
            y += 165;
            g.drawString(String.valueOf(player.getWinLoss()), x, y);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the commendations on to the commendation section
     *
     * @return Commendation section with commendations drawn on
     */
    private BufferedImage drawCommendations() {
        ArrayList<Player.Commendation> commendations = player.getCommendations();
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File((resources + "Templates/commendations_section.png")));
            Graphics g = image.getGraphics();
            g.setFont(codFont);
            int x = 100;
            for(int i = 0; i < 5; i++) {
                g.setFont(codFont.deriveFont(32f));
                Player.Commendation c = commendations.get(i);
                BufferedImage icon = ImageIO.read(c.getImage());
                g.drawImage(icon, x - (icon.getWidth() / 2), 250 - (icon.getHeight() / 2), null);
                int titleWidth = g.getFontMetrics().stringWidth(c.getTitle()) / 2;
                g.drawString(c.getTitle(), x - titleWidth, 155);

                String[] descSplit = c.getDesc().split(" ");
                String desc = "";
                int y = 350;
                g.setFont(codFont.deriveFont(25f));
                for(String word : descSplit) {
                    String attempt = desc + " " + word;
                    if(g.getFontMetrics().stringWidth(attempt) > 180) {
                        g.drawString(desc, x - (g.getFontMetrics().stringWidth(desc)) / 2, y);
                        y += 35;
                        desc = word;
                        continue;
                    }
                    desc = attempt;
                }
                g.drawString(desc, x - (g.getFontMetrics().stringWidth(desc)) / 2, y);
                g.setFont(codFont.deriveFont(50f));
                g.drawString(c.formatQuantity(), x - (g.getFontMetrics().stringWidth(c.formatQuantity())) / 2, 550);
                x += 210;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the kill death ratio on to the kill death section
     *
     * @return Kill death section with stats drawn on
     */
    private BufferedImage drawKillDeath() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File((resources + "Templates/kd_section.png")));
            Graphics g = image.getGraphics();
            g.setFont(codFont);
            g.drawString(String.valueOf(player.getKD()), 282, 200);
            g.drawString(String.valueOf(player.getLongestKillStreak()), 282, 482);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Build the various sections of the image and draw them each on to the background image
     *
     * @param nameQuery Player name
     */
    public void buildImage(String nameQuery) {
        ImageLoadingMessage loading = new ImageLoadingMessage(
                channel,
                guild,
                "MW Player lookup: " + nameQuery.toUpperCase(),
                "One moment please.",
                Gunfight.getThumb(),
                new String[]{
                        "Fetching player data...",
                        "Building image...",
                        "Uploading image..."
                }
        );
        loading.showLoading();
        this.player = new Player(nameQuery, "bnet");
        if(!player.exists()) {
            loading.failLoading("That player doesn't exist cunt");
            return;
        }
        loading.completeStage();
        try {
            codFont = new Font("Eurostile Next LT Pro Semibold", Font.PLAIN, 75);

            BufferedImage main = ImageIO.read(new File((resources + "Templates/template.png")));
            Graphics g = main.getGraphics();
            g.drawImage(drawWeapon(player.getPrimary()), 17, 119, null);
            g.drawImage(drawWeapon(player.getSecondary()), 542, 119, null);
            g.drawImage(drawWeapon(player.getLethal()), 1067, 119, null);
            g.drawImage(drawWinLoss(), 1067, 700, null);
            g.drawImage(drawKillDeath(), 1067, 1290, null);
            g.drawImage(drawCommendations(), 17, 1290, null);

            g.setFont(codFont.deriveFont(100f));
            g.setColor(Color.black);
            String name = player.getName().toUpperCase();
            g.drawString(player.getName().toUpperCase(), (main.getWidth() / 2) - (g.getFontMetrics().stringWidth(name) / 2), 100);
            g.dispose();
            loading.completeStage();
            String url = ImgurManager.uploadImage(main);
            loading.completeStage();
            loading.completeLoading(url);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Register the COD font with the graphics environment
     */
    private void registerFont() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(resources + "ModernWarfare.otf")));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
