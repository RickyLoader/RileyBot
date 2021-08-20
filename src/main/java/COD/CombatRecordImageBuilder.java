package COD;

import Bot.ResourceHandler;
import COD.API.CODStatsManager.PLATFORM;
import COD.API.MWStatsManager;
import COD.API.PlayerStatsResponse;
import COD.Assets.*;
import COD.PlayerStats.*;
import COD.API.MWManager;
import Command.Structure.*;
import TrackerGG.CODTrackerAPI;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static COD.API.CODAPI.API_FAILURE_MESSAGE;

/**
 * Build an image containing the user's Modern Warfare stats
 */
public class CombatRecordImageBuilder extends ImageBuilder {
    private final String helpMessage;
    private static final float
            COMMENDATION_NAME_SIZE = 32,
            COMMENDATION_DESCRIPTION_SIZE = 25,
            COMMENDATION_QUANTITY_SIZE = 50;
    private static final int BORDER = 5;

    private final BufferedImage
            lethalSection,
            tacticalSection,
            fieldUpgradeSection,
            winLossSection,
            commendationsSection,
            commendationSection,
            killDeathSection,
            killstreaksSection,
            killstreakSection,
            weaponSection,
            background;

    /**
     * Create the combat record image builder
     *
     * @param helpMessage  Help message to use in image embed
     * @param emoteHelper  Emote helper
     * @param resourcePath Base resource path
     * @param font         Font to use in image
     */
    public CombatRecordImageBuilder(String helpMessage, EmoteHelper emoteHelper, String resourcePath, Font font) {
        super(emoteHelper, "/COD/" + resourcePath + "/Templates/", font);
        this.helpMessage = helpMessage;

        ResourceHandler handler = getResourceHandler();
        String templates = getResourcePath();
        this.lethalSection = handler.getImageResource(templates + "lethal_section.png");
        this.tacticalSection = handler.getImageResource(templates + "tactical_section.png");
        this.weaponSection = handler.getImageResource(templates + "weapon_section.png");
        this.fieldUpgradeSection = handler.getImageResource(templates + "field_upgrade_section.png");
        this.winLossSection = handler.getImageResource(templates + "wl_section.png");
        this.commendationsSection = handler.getImageResource(templates + "commendations_section.png");
        this.commendationSection = handler.getImageResource(templates + "commendation_section.png");
        this.killDeathSection = handler.getImageResource(templates + "kd_section.png");
        this.killstreaksSection = handler.getImageResource(templates + "killstreaks_section.png");
        this.killstreakSection = handler.getImageResource(templates + "killstreak_section.png");
        this.background = handler.getImageResource(templates + "template.png");
    }

    /**
     * Get the appropriate background image for the given weapon type
     *
     * @param type Weapon type
     * @return Background image for weapon type
     */
    private BufferedImage getWeaponImage(Weapon.TYPE type) {
        if(type == Weapon.TYPE.LETHAL) {
            return copyImage(lethalSection);
        }
        if(type == Weapon.TYPE.TACTICAL) {
            return copyImage(tacticalSection);
        }
        return copyImage(weaponSection);
    }

    /**
     * Draw the weapon stats on to the weapon section
     *
     * @param weaponStats Weapon stats to draw
     * @return Weapon section
     */
    private BufferedImage drawWeapon(WeaponStats weaponStats) {
        BufferedImage image = null;
        Weapon weapon = weaponStats.getAsset();
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
        FieldUpgrade fieldUpgrade = fieldUpgradeStats.getAsset();
        try {
            image = copyImage(fieldUpgradeSection);
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
     * @param basicStats Basic player stats
     * @return Win loss section
     */
    private BufferedImage drawWinLoss(PlayerBasicStats basicStats) {
        BufferedImage image = null;
        try {
            image = copyImage(winLossSection);
            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            int x = 288;
            int y = 160;
            g.drawString(basicStats.getFormattedWins(), x, y);
            y += 165;
            g.drawString(basicStats.getFormattedLosses(), x, y);
            y += 165;
            g.drawString(basicStats.getFormattedWinLoss(), x, y);
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
     * @param playerCommendationStats Player commendation stats
     * @return Commendation section
     */
    private BufferedImage drawCommendations(ArrayList<CommendationStats> playerCommendationStats) {

        // Sort in descending order of uses
        Collections.sort(playerCommendationStats);

        BufferedImage image = null;
        try {
            image = copyImage(commendationsSection);
            Graphics g = image.getGraphics();
            int x = 100;
            for(int i = 0; i < Math.min(5, playerCommendationStats.size()); i++) {
                g.setFont(getGameFont().deriveFont(COMMENDATION_NAME_SIZE));
                CommendationStats commendationStats = playerCommendationStats.get(i);
                Commendation commendation = commendationStats.getAsset();

                BufferedImage icon = commendation.getImage();

                g.drawImage(icon, x - (icon.getWidth() / 2), 250 - (icon.getHeight() / 2), null);
                int titleWidth = g.getFontMetrics().stringWidth(commendation.getName()) / 2;
                g.drawString(commendation.getName(), x - titleWidth, 155);

                String[] descSplit = commendation.getDesc().split(" ");
                String desc = "";
                int y = 350;
                g.setFont(getGameFont().deriveFont(COMMENDATION_DESCRIPTION_SIZE));
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
                g.setFont(getGameFont().deriveFont(COMMENDATION_QUANTITY_SIZE));
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
     * Build an image displaying the given commendation stats
     *
     * @param stats Commendation stats to draw
     * @return Image displaying commendation stats
     */
    private BufferedImage drawCommendation(CommendationStats stats) {
        final BufferedImage image = copyImage(commendationSection);
        final Commendation commendation = stats.getAsset();
        final int centreHorizontal = image.getWidth() / 2;
        final int centreVertical = image.getHeight() / 2;

        Graphics g = image.getGraphics();

        // Draw the commendation name at the top of the image
        g.setFont(getGameFont().deriveFont(COMMENDATION_NAME_SIZE));
        g.drawString(
                commendation.getName(),
                centreHorizontal - (g.getFontMetrics().stringWidth(commendation.getName()) / 2),
                88
        );

        // Draw the commendation image in the centre
        BufferedImage commendationImage = commendation.getImage();
        final int imageY = centreVertical - (commendationImage.getHeight() / 2);
        g.drawImage(
                commendationImage,
                centreHorizontal - (commendationImage.getWidth() / 2),
                imageY,
                null
        );

        // Draw the commendation description right below the image
        g.setFont(getGameFont().deriveFont(COMMENDATION_DESCRIPTION_SIZE));
        final int descY = imageY + commendationImage.getHeight() + g.getFontMetrics().getHeight();
        g.drawString(
                commendation.getDesc(),
                centreHorizontal - (g.getFontMetrics().stringWidth(commendation.getDesc()) / 2),
                descY
        );

        // Draw the commendation quantity in the centre of the area below the description
        g.setFont(getGameFont().deriveFont(COMMENDATION_QUANTITY_SIZE));
        g.drawString(
                stats.formatQuantity(),
                centreHorizontal - (g.getFontMetrics().stringWidth(stats.formatQuantity()) / 2),
                descY + ((image.getHeight() - descY) / 2) + (g.getFontMetrics().getMaxAscent() / 2)
        );
        g.dispose();
        return image;
    }

    /**
     * Draw the kill death ratio on to the kill death section
     *
     * @param basicStats Basic player stats
     * @return Kill death section
     */
    private BufferedImage drawKillDeath(PlayerBasicStats basicStats) {
        BufferedImage image = null;
        try {
            image = copyImage(killDeathSection);
            Graphics g = image.getGraphics();
            g.setFont(getGameFont().deriveFont(50f));
            g.drawString(String.valueOf(basicStats.getFormattedKillDeath()), 282, 180);
            g.drawString(String.valueOf(basicStats.getLongestKillStreak()), 282, 462);
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
     * @param playerKillstreakStats Player killstreak stats
     * @return Killstreak section
     */
    private BufferedImage drawKillstreaks(ArrayList<KillstreakStats> playerKillstreakStats) {

        // Sort in descending order of uses
        Collections.sort(playerKillstreakStats);

        ArrayList<KillstreakStats> topKillstreakStats = new ArrayList<>(playerKillstreakStats.subList(0, 5));
        BufferedImage image = null;
        try {
            image = copyImage(killstreaksSection);
            Graphics g = image.getGraphics();
            KillstreakStats largest = topKillstreakStats
                    .stream()
                    .max(Comparator.comparing(stats -> stats.getAsset().getImage().getHeight()))
                    .orElse(null);

            if(largest == null) {
                throw new Exception("Unable to determine largest height for killstreaks");
            }

            int maxHeight = largest.getAsset().getImage().getHeight();
            int padding = (image.getWidth() - (280 * 5)) / 6;
            int x = padding;

            for(KillstreakStats killstreakStats : topKillstreakStats) {
                Killstreak killstreak = killstreakStats.getAsset();
                g.setFont(getGameFont().deriveFont(35f));
                BufferedImage icon = killstreakStats.getAsset().getImage();
                int y = (200 + (maxHeight / 2) - (icon.getHeight() / 2));

                g.drawImage(icon, x, y, null);
                g.drawString(
                        killstreak.getName(),
                        x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(killstreak.getName()) / 2),
                        155
                );

                g.setFont(getGameFont().deriveFont(32f));
                String quantity = killstreakStats.formatUses();
                int space = ((image.getHeight() - (maxHeight + 200))) / 4;

                y = (200 + maxHeight + space);
                g.drawString(
                        quantity,
                        x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(quantity) / 2),
                        y
                );

                if(killstreak.hasExtraStat()) {
                    String extra = killstreakStats.formatStatQuantity();
                    y += space;
                    g.drawString(extra, x + (icon.getWidth() / 2) - (g.getFontMetrics().stringWidth(extra) / 2), y);
                    String average = killstreakStats.formatAverageStatQuantity();
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
     * Build an image displaying the given killstreak stats
     *
     * @param stats Killstreak stats to draw
     * @return Image displaying killstreak stats
     */
    private BufferedImage drawKillstreak(KillstreakStats stats) {
        BufferedImage image = copyImage(killstreakSection);
        Graphics g = image.getGraphics();
        g.setFont(getGameFont().deriveFont(50f));

        final Killstreak killstreak = stats.getAsset();
        final int centreHorizontal = image.getWidth() / 2;
        final int centreVertical = image.getHeight() / 2;

        // Draw the killstreak name at the top of the image
        g.drawString(
                killstreak.getName(),
                centreHorizontal - (g.getFontMetrics().stringWidth(killstreak.getName()) / 2),
                155
        );

        // Draw the killstreak image in the centre
        final BufferedImage killstreakImage = killstreak.getImage();
        final int imageY = centreVertical - (killstreakImage.getHeight() / 2);
        g.drawImage(
                killstreakImage,
                centreHorizontal - (killstreakImage.getWidth() / 2),
                imageY,
                null

        );

        // Draw the killstreak stats below the image
        g.setFont(getGameFont().deriveFont(40f));
        final FontMetrics fm = g.getFontMetrics();

        ArrayList<String> statsSummary = new ArrayList<>(Collections.singletonList(stats.formatUses()));

        // Not all streaks have an extra stat e.g "Assists"
        if(killstreak.hasExtraStat()) {
            statsSummary.add(stats.formatStatQuantity());
            statsSummary.add(stats.formatAverageStatQuantity());
        }

        // Add font height to add gap below image
        final int statsBeginY = imageY + killstreakImage.getHeight() + (fm.getMaxAscent());
        final int remainingArea = image.getHeight() - statsBeginY - BORDER;

        // Height of area to use per stat, centre text within
        final int perStatHeight = remainingArea / statsSummary.size();

        // Begin here and add perStatHeight to have stats evenly spaced
        int statsY = statsBeginY + (perStatHeight / 2) + (fm.getMaxAscent() / 2);

        for(String stat : statsSummary) {
            g.drawString(
                    stat,
                    centreHorizontal - (fm.stringWidth(stat) / 2),
                    statsY
            );
            statsY += perStatHeight;
        }

        g.dispose();
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
     * @return Player stats or null (if unable to retrieve)
     */
    @Nullable
    private MWPlayerStats initialisePlayerStats(String nameQuery, PLATFORM platform, ImageLoadingMessage loading) {
        loading.showLoading();
        MWStatsManager statsManager = new MWStatsManager();
        PlayerStatsResponse<MWPlayerAssetStats, MWPlayerStats> response = statsManager.fetchPlayerStats(nameQuery, platform);

        // No response, try tracker.gg
        if(!response.statsRetrieved()) {
            final String failMessage = response.getMessage();

            // Player not found etc
            if(!failMessage.equals(API_FAILURE_MESSAGE)) {
                loading.failLoading(failMessage);
                return null;
            }

            // API down, attempt to fall back on COD tracker website
            loading.updateStage("Failed to contact API, giving " + CODTrackerAPI.DOMAIN + " a ring...");

            response = statsManager.fetchPlayerStatsFallback(nameQuery, platform);

            // Failed to contact tracker also, fail loading
            if(!response.statsRetrieved()) {
                loading.failLoading(response.getMessage());
                return null;
            }
        }

        // Stats retrieved
        if(response.hasMessage()) {
            loading.completeStage(response.getMessage());
        }
        else {
            loading.completeStage();
        }
        return response.getStats();
    }

    /**
     * Build an image displaying the player's stats for the weapon/equipment/etc of
     * the given name.
     *
     * @param nameQuery Player name to search for
     * @param platform  Player platform
     * @param assetName Name of asset to view stats for
     * @param context   Command context
     */
    public void buildQueryImage(String nameQuery, PLATFORM platform, String assetName, CommandContext context) {
        ImageLoadingMessage loadingMessage = buildImageLoadingMessage(
                "MW " + assetName.toUpperCase() + " Stats: " + nameQuery.toUpperCase(),
                context.getMessageChannel()
        );

        MWPlayerStats playerStats = initialisePlayerStats(nameQuery, platform, loadingMessage);

        if(playerStats == null) {
            return;
        }

        ArrayList<AssetStats<? extends CODAsset>> statsList = playerStats.getAssetStats().getAssetStatsByName(assetName);

        // More than one result - display as pageable embed
        if(statsList.size() != 1) {
            loadingMessage.failLoading("You'll have to narrow that query down bro!");
            new PageableCODAssetStatsEmbed(
                    context,
                    statsList,
                    "MW Stats Search: " + nameQuery.toUpperCase(),
                    MWManager.THUMBNAIL,
                    helpMessage,
                    "This player has **" + statsList.size()
                            + "** stats available for: **" + assetName + "**\n\nTry using the **Codename**!"
            ) {
                @Override
                public String getNoItemsDescription() {
                    return "I didn't find any stats for: **" + assetName + "**";
                }

                @Override
                public void sortItems(List<AssetStats<? extends CODAsset>> items, boolean defaultSort) {
                    items.sort(new LevenshteinDistance<AssetStats<? extends CODAsset>>(assetName, true) {
                        @Override
                        public String getString(AssetStats<? extends CODAsset> o) {
                            return o.getAsset().getName();
                        }
                    });
                }
            }.showMessage();
            return;
        }

        AssetStats<? extends CODAsset> stats = statsList.get(0);
        BufferedImage image;

        if(stats instanceof FieldUpgradeStats) {
            image = drawSuper((FieldUpgradeStats) stats);
        }
        else if(stats instanceof CommendationStats) {
            image = drawCommendation((CommendationStats) stats);
        }
        else if(stats instanceof KillstreakStats) {
            image = drawKillstreak((KillstreakStats) stats);
        }
        else if(stats instanceof WeaponStats) {
            image = drawWeapon((WeaponStats) stats);
        }
        else {
            loadingMessage.failLoading(
                    "Something went wrong when displaying the **" + stats.getAsset().getName() + "** stats!"
            );
            return;
        }
        loadingMessage.completeStage();
        loadingMessage.completeLoading(image);
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
                MWManager.THUMBNAIL,
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

        if(playerStats == null) {
            return;
        }

        try {
            BufferedImage main = copyImage(background);
            Graphics g = main.getGraphics();

            MWPlayerAssetStats assetStats = playerStats.getAssetStats();
            PlayerBasicStats basicStats = playerStats.getBasicStats();
            PlayerWeaponStats weaponStats = assetStats.getWeaponStats();
            PlayerEquipmentStats equipmentStats = assetStats.getEquipmentStats();

            g.drawImage(
                    drawWeapon(
                            (WeaponStats) PlayerAssetStats.getFavouriteAsset(weaponStats.getPrimaryWeaponStats())
                    ),
                    17, 119, null
            );
            g.drawImage(
                    drawWeapon(
                            (WeaponStats) PlayerAssetStats.getFavouriteAsset(weaponStats.getSecondaryWeaponStats())
                    ), 542, 119, null
            );
            g.drawImage(
                    drawWeapon(
                            (WeaponStats) PlayerAssetStats.getFavouriteAsset(equipmentStats.getLethalStats())
                    ), 1067, 119, null
            );

            // Tracker doesn't provided tactical stats
            ArrayList<TacticalStats> tacticalStats = equipmentStats.getTacticalStats();
            if(!tacticalStats.isEmpty()) {
                g.drawImage(
                        drawWeapon(
                                (WeaponStats) PlayerAssetStats.getFavouriteAsset(tacticalStats)
                        ), 1592, 119, null
                );
            }

            g.drawImage(
                    drawSuper(
                            (FieldUpgradeStats) PlayerAssetStats.getFavouriteAsset(assetStats.getFieldUpgradeStats())
                    ), 1067, 1030, null
            );

            g.drawImage(drawKillDeath(basicStats), 1592, 1030, null);
            g.drawImage(drawWinLoss(basicStats), 1592, 1609, null);
            g.drawImage(drawCommendations(assetStats.getCommendationStats()), 17, 1290, null);
            g.drawImage(drawKillstreaks(assetStats.getKillstreakStats()), 17, 1869, null);
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
