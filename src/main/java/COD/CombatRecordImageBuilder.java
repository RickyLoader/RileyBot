package COD;

import COD.Assets.*;
import COD.PlayerStats.*;
import Command.Structure.CODLookupCommand.PLATFORM;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Build an image containing the user's Modern Warfare stats
 */
public class CombatRecordImageBuilder extends ImageBuilder {
    private final String helpMessage;

    /**
     * Create the combat record image builder
     *
     * @param helpMessage  Help message to use in image embed
     * @param emoteHelper  Emote helper
     * @param resourcePath Base resource path
     * @param font         Font to use in image
     */
    public CombatRecordImageBuilder(String helpMessage, EmoteHelper emoteHelper, String resourcePath, Font font) {
        super(emoteHelper, "/COD/" + resourcePath + "/", font);
        this.helpMessage = helpMessage;
    }

    /**
     * Get the appropriate background image for the given weapon type
     *
     * @param type Weapon type
     * @return Background image for weapon type
     */
    private BufferedImage getWeaponImage(Weapon.TYPE type) {
        if(type == Weapon.TYPE.LETHAL) {
            return getResourceHandler().getImageResource(getResourcePath() + "Templates/lethal_section.png");
        }
        if(type == Weapon.TYPE.TACTICAL) {
            return getResourceHandler().getImageResource(getResourcePath() + "Templates/tactical_section.png");
        }
        return getResourceHandler().getImageResource(getResourcePath() + "Templates/weapon_section.png");
    }

    /**
     * Draw the weapon stats on to the weapon section
     *
     * @param weaponStats Weapon stats to draw
     * @return Weapon section
     */
    private BufferedImage drawWeapon(WeaponStats weaponStats) {
        BufferedImage image = null;
        Weapon weapon = weaponStats.getWeapon();
        try {
            int x = 277;
            image = getWeaponImage(weapon.getType());
            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            FontMetrics fm = g.getFontMetrics();

            BufferedImage weaponImage = weapon.getImage();

            g.drawImage(weaponImage, (image.getWidth() / 2) - (weaponImage.getWidth() / 2), 250, null);
            g.drawString(weapon.getName(), (image.getWidth() / 2) - (fm.stringWidth(weapon.getName()) / 2), 175);

            if(weapon.getType() != Weapon.TYPE.TACTICAL) {
                String kills;
                if(weapon.getType() == Weapon.TYPE.LETHAL) {
                    kills = ((LethalStats) weaponStats).formatKills();
                }
                else {
                    kills = ((StandardWeaponStats) weaponStats).formatKills();
                }
                g.drawString(kills, x, 490);
            }

            if(weapon.getType() == Weapon.TYPE.PRIMARY || weapon.getType() == Weapon.TYPE.SECONDARY) {
                StandardWeaponStats standardWeaponStats = (StandardWeaponStats) weaponStats;
                g.drawString(standardWeaponStats.formatDeaths(), x, 590);
                g.drawString(String.valueOf(standardWeaponStats.getKd()), x, 690);
                g.drawString(standardWeaponStats.getShotsHit(), x, 841);
                g.drawString(standardWeaponStats.getShotsFired(), x, 941);
                g.drawString(standardWeaponStats.getAccuracy(), x, 1041);
            }

            if(weapon.getType() == Weapon.TYPE.LETHAL) {
                LethalStats lethalStats = (LethalStats) weaponStats;
                g.drawString(lethalStats.formatUses(), x, 590);
                g.drawString(lethalStats.getKillUse(), x, 690);
            }

            if(weapon.getType() == Weapon.TYPE.TACTICAL) {
                TacticalStats tacticalStats = (TacticalStats) weaponStats;
                TacticalWeapon tacticalWeapon = (TacticalWeapon) weapon;
                g.drawString(tacticalStats.formatUses(), x, 490);
                if(tacticalWeapon.hasExtraProperty()) {
                    g.drawString(tacticalStats.formatStat(), x, 590);
                    g.drawString(tacticalStats.getStatUse(), x, 690);

                    int titleX = 267;

                    String name = tacticalWeapon.getProperty().toUpperCase();
                    g.setFont(getGameFont().deriveFont(28f));
                    fm = g.getFontMetrics();
                    g.drawString(name, titleX - fm.stringWidth(name), 580);
                    name += "/USE";
                    g.drawString(name, titleX - fm.stringWidth(name), 680);

                    g.setFont(getGameFont().deriveFont(20f));
                    fm = g.getFontMetrics();
                    String sub = "TOTAL NUMBER";
                    g.drawString(sub, titleX - fm.stringWidth(sub), 605);
                    sub = "RATIO";
                    g.drawString(sub, titleX - fm.stringWidth(sub), 705);
                }
            }

            g.setFont(getGameFont().deriveFont(28f));
            g.setColor(Color.WHITE);
            g.drawString(
                    weapon.getType().getTitle(),
                    (image.getWidth()) / 2 - (g.getFontMetrics().stringWidth(weapon.getType().getTitle())) / 2,
                    38
            );
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the field upgrade stats on to the field upgrade section
     *
     * @param fieldUpgradeStats Field upgrade stats to draw
     * @return Field upgrade section
     */
    private BufferedImage drawSuper(FieldUpgradeStats fieldUpgradeStats) {
        BufferedImage image = null;
        FieldUpgrade fieldUpgrade = fieldUpgradeStats.getFieldUpgrade();
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/field_upgrade_section.png");
            BufferedImage superImage = fieldUpgrade.getImage();

            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(40f));
            FontMetrics fm = g.getFontMetrics();

            g.drawImage(
                    superImage,
                    (image.getWidth() / 2) - (superImage.getWidth() / 2),
                    220,
                    null
            );

            g.drawString(
                    fieldUpgrade.getName(),
                    (image.getWidth() / 2) - (fm.stringWidth(fieldUpgrade.getName()) / 2),
                    175
            );

            int x = 277;
            g.setFont(getGameFont().deriveFont(50f));
            g.drawString(fieldUpgradeStats.formatUses(), x, 500);

            int y = 570;
            int titleX = 267;

            if(fieldUpgradeStats.hasKills()) {
                g.setFont(getGameFont().deriveFont(28f));
                fm = g.getFontMetrics();
                String name = "KILLS";
                g.drawString(name, titleX - fm.stringWidth(name), y);
                name = "KILL/USE";
                g.drawString(name, titleX - fm.stringWidth(name), y + 80);

                g.setFont(getGameFont().deriveFont(20f));
                fm = g.getFontMetrics();
                String sub = "TOTAL NUMBER";
                g.drawString(sub, titleX - fm.stringWidth(sub), y + 25);
                sub = "RATIO";
                g.drawString(sub, titleX - fm.stringWidth(sub), y + 105);

                g.setFont(getGameFont().deriveFont(50f));
                g.setColor(Color.WHITE);
                g.drawString(fieldUpgradeStats.formatKills(), x, y + 15);
                g.drawString(fieldUpgradeStats.getKillUseRatio(), x, y + 95);
                y += 160;
            }

            if(fieldUpgrade.hasProperty()) {
                g.setFont(getGameFont().deriveFont(28f));
                fm = g.getFontMetrics();
                String name = fieldUpgrade.getPropertyName().toUpperCase();
                g.drawString(name, titleX - fm.stringWidth(name), y);

                g.setFont(getGameFont().deriveFont(20f));
                fm = g.getFontMetrics();
                String sub = "TOTAL NUMBER";
                g.drawString(sub, titleX - fm.stringWidth(sub), y + 25);

                g.setFont(getGameFont().deriveFont(50f));
                g.drawString(String.valueOf(fieldUpgradeStats.getPropertyStat()), x, y + 15);
            }

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
     * @param playerStats Player stats
     * @return Win loss section
     */
    private BufferedImage drawWinLoss(MWPlayerStats playerStats) {
        BufferedImage image = null;
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/wl_section.png");
            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            int x = 288;
            int y = 160;
            g.drawString(playerStats.getWins(), x, y);
            y += 165;
            g.drawString(playerStats.getLosses(), x, y);
            y += 165;
            g.drawString(playerStats.getWinLoss(), x, y);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the player commendation stats on to the commendation section
     *
     * @param playerStats Player stats
     * @return Commendation section
     */
    private BufferedImage drawCommendations(MWPlayerStats playerStats) {
        ArrayList<CommendationStats> allCommendationStats = playerStats.getCommendationStats();
        BufferedImage image = null;
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/commendation_section.png");
            Graphics g = image.getGraphics();
            g.setFont(getGameFont());
            int x = 100;
            for(int i = 0; i < Math.min(5, allCommendationStats.size()); i++) {
                g.setFont(getGameFont().deriveFont(32f));
                CommendationStats commendationStats = allCommendationStats.get(i);
                Commendation commendation = commendationStats.getCommendation();

                BufferedImage icon = commendation.getImage();

                g.drawImage(icon, x - (icon.getWidth() / 2), 250 - (icon.getHeight() / 2), null);
                int titleWidth = g.getFontMetrics().stringWidth(commendation.getName()) / 2;
                g.drawString(commendation.getName(), x - titleWidth, 155);

                String[] descSplit = commendation.getDesc().split(" ");
                String desc = "";
                int y = 350;
                g.setFont(getGameFont().deriveFont(25f));
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
                g.setFont(getGameFont().deriveFont(50f));
                g.drawString(
                        commendationStats.formatQuantity(),
                        x - (g.getFontMetrics().stringWidth(commendationStats.formatQuantity())) / 2,
                        550
                );
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
     * @param playerStats Player stats
     * @return Kill death section
     */
    private BufferedImage drawKillDeath(MWPlayerStats playerStats) {
        BufferedImage image = null;
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/kd_section.png");
            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            g.drawString(String.valueOf(playerStats.getKD()), 282, 180);
            g.drawString(String.valueOf(playerStats.getLongestKillStreak()), 282, 462);
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Draw the player killstreak stats on to the killstreak section
     *
     * @param playerStats Player stats
     * @return Killstreak section
     */
    private BufferedImage drawKillstreaks(MWPlayerStats playerStats) {
        ArrayList<KillstreakStats> topKillstreakStats = new ArrayList<>(playerStats.getKillstreakStats().subList(0, 5));
        BufferedImage image = null;
        try {
            image = getResourceHandler().getImageResource(getResourcePath() + "Templates/killstreak_section.png");
            Graphics g = image.getGraphics();
            g.setFont(getGameFont());
            KillstreakStats largest = topKillstreakStats
                    .stream()
                    .max(Comparator.comparing(stats -> stats.getKillstreak().getImage().getHeight()))
                    .orElse(null);

            if(largest == null) {
                throw new Exception("Unable to determine largest height for killstreaks");
            }

            int maxHeight = largest.getKillstreak().getImage().getHeight();
            int padding = (image.getWidth() - (280 * 5)) / 6;
            int x = padding;
            for(KillstreakStats killstreakStats : topKillstreakStats) {
                Killstreak killstreak = killstreakStats.getKillstreak();
                g.setFont(getGameFont().deriveFont(32f));
                BufferedImage icon = killstreakStats.getKillstreak().getImage();
                int y = (200 + (maxHeight / 2) - (icon.getHeight() / 2));

                g.drawImage(icon, x, y, null);
                g.drawString(
                        killstreak.getName(),
                        x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(killstreak.getName()) / 2),
                        155
                );

                g.setFont(getGameFont().deriveFont(40f));
                String quantity = "Quantity: " + killstreakStats.formatUses();
                int space = ((image.getHeight() - (maxHeight + 200))) / 4;

                y = (200 + maxHeight + space);
                g.drawString(
                        quantity,
                        x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(quantity) / 2),
                        y
                );

                if(killstreak.hasExtraStat()) {
                    String extra = killstreak.getStatName() + ": " + killstreakStats.formatStatQuantity();
                    y += space;
                    g.drawString(extra, x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(extra) / 2), y);
                    String average = "Avg: " + killstreakStats.getAverage();
                    y += space;
                    g.drawString(
                            average,
                            x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(average) / 2),
                            y
                    );
                }
                x += padding + icon.getWidth();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Attempt to find the desired player's stats.
     * Display the given loading message and complete the fetching stats stage
     * based on whether the player stats are found.
     *
     * @param nameQuery Player name to search for
     * @param platform  Player platform
     * @param loading   Loading message
     * @return Player stats found
     */
    private MWPlayerStats initialisePlayerStats(String nameQuery, PLATFORM platform, ImageLoadingMessage loading) {
        loading.showLoading();
        MWPlayerStats playerStats = new MWPlayerStats(nameQuery, platform);
        if(playerStats.success()) {
            loading.completeStage();
        }
        else {
            loading.failLoading(playerStats.getStatus());
        }
        return playerStats;
    }

    /**
     * Build an image displaying the player's stats for the weapon/equipment of
     * the given name.
     *
     * @param nameQuery  Player name to search for
     * @param platform   Player platform
     * @param weaponName Name of weapon to display stats for
     * @param channel    Channel to send image to
     */
    public void buildWeaponRecordImage(String nameQuery, PLATFORM platform, String weaponName, MessageChannel channel) {
        ImageLoadingMessage loadingMessage = buildImageLoadingMessage(
                "MW " + weaponName.toUpperCase() + " Stats: " + nameQuery.toUpperCase(),
                channel
        );
        MWPlayerStats playerStats = initialisePlayerStats(nameQuery, platform, loadingMessage);
        if(!playerStats.success()) {
            return;
        }
        WeaponStats weaponStats = playerStats.getWeaponStatsByName(weaponName);
        if(weaponStats == null) {
            loadingMessage.failLoading("I didn't any weapon/equipment for: **" + weaponName + "**");
            return;
        }
        BufferedImage weaponImage = drawWeapon(weaponStats);
        loadingMessage.completeStage();
        loadingMessage.completeLoading(weaponImage);
    }

    /**
     * Create the loading message to display while building an image
     *
     * @param title   Title to use in loading message
     * @param channel Channel to send loading message to
     * @return Loading message
     */
    private ImageLoadingMessage buildImageLoadingMessage(String title, MessageChannel channel) {
        return new ImageLoadingMessage(
                channel,
                getEmoteHelper(),
                title,
                "One moment please.",
                Gunfight.THUMBNAIL,
                helpMessage,
                new String[]{
                        "Fetching player data...",
                        "Building image...",
                }
        );
    }

    /**
     * Build an image displaying the player's combat record.
     * Display favourite weapons, killstreaks, etc.
     *
     * @param nameQuery Player name to search for
     * @param platform  Player platform
     * @param channel   Channel to send loading message to
     */
    public void buildCombatRecordImage(String nameQuery, PLATFORM platform, MessageChannel channel) {
        ImageLoadingMessage loadingMessage = buildImageLoadingMessage(
                "MW Player Stats: " + nameQuery.toUpperCase(),
                channel
        );
        MWPlayerStats playerStats = initialisePlayerStats(nameQuery, platform, loadingMessage);
        if(!playerStats.success()) {
            return;
        }
        try {
            BufferedImage main = getResourceHandler().getImageResource(getResourcePath() + "Templates/template.png");
            Graphics g = main.getGraphics();

            g.drawImage(drawWeapon(playerStats.getPrimaryStats()), 17, 119, null);
            g.drawImage(drawWeapon(playerStats.getSecondaryStats()), 542, 119, null);
            g.drawImage(drawWeapon(playerStats.getLethalStats()), 1067, 119, null);
            g.drawImage(drawWeapon(playerStats.getTacticalStats()), 1592, 119, null);
            g.drawImage(drawSuper(playerStats.getSuperStats()), 1067, 1030, null);
            g.drawImage(drawKillDeath(playerStats), 1592, 1030, null);
            g.drawImage(drawWinLoss(playerStats), 1592, 1609, null);
            g.drawImage(drawCommendations(playerStats), 17, 1290, null);
            g.drawImage(drawKillstreaks(playerStats), 17, 1869, null);
            g.setFont(getGameFont().deriveFont(100f));
            g.setColor(Color.BLACK);
            String name = playerStats.getName().toUpperCase();
            g.drawString(name, (main.getWidth() / 2) - (g.getFontMetrics().stringWidth(name) / 2), 100);
            g.dispose();
            loadingMessage.completeStage();
            loadingMessage.completeLoading(main);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
