package Runescape.ImageBuilding;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import Command.Structure.ImageLoadingMessage;
import Command.Structure.PieChart;
import Runescape.Hiscores.OSRSHiscores;
import Runescape.OSRS.Boss.Boss;
import Runescape.OSRS.Boss.BossStats;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.Relic;
import Runescape.OSRS.League.RelicTier;
import Runescape.Stats.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Command.Commands.Lookup.RunescapeLookupCommand.*;
import static Runescape.Stats.Skill.SKILL_NAME.*;

/**
 * Build an image displaying a player's OSRS stats
 */
public class OSRSHiscoresImageBuilder extends HiscoresImageBuilder<OSRSPlayerStats, OSRSHiscores> {
    private final Font trackerFont;
    private final Color redOverlay, greenOverlay, blackOverlay, dark, light, highestXpColour, closestToLevelColour;
    private final DecimalFormat percentageFormat = new DecimalFormat("0.00%");
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");

    private static final float STANDARD_FONT_SIZE = 65;

    public static final int
            MAX_BOSSES = 5,
            UPPER_SKILL_TEXT_Y = 70, // Y for upper skill number in a skill box
            UPPER_SKILL_TEXT_X = 160, // X for upper skill numbers in a skill box
            SKILL_TEXT_OFFSET = 60, // Upper/Lower skill numbers are directly diagonal
            LOWER_SKILL_TEXT_Y = UPPER_SKILL_TEXT_Y + SKILL_TEXT_OFFSET, // Y for lower skill number in a skill box
            LOWER_SKILL_TEXT_X = UPPER_SKILL_TEXT_X + SKILL_TEXT_OFFSET, // X for lower skill number in a skill box
            SHADOW_OFFSET = 5, // X & Y offset for drawing skill text shadow
            SKILL_NOTCH_INSET = 15, // Horizontal & vertical inset of skill box corners
            SKILL_BORDER = 10, // Horizontal & vertical borders of skill boxes
            SKILL_NOTCH_SIZE = SKILL_NOTCH_INSET + SKILL_BORDER, // Width/height of notches in the corners of skill boxes
            BORDER = 25; // Horizontal & vertical borders on template images

    private final BufferedImage
            titleContainer,
            achievementsContainer,
            achievementsTitleContainer,
            bossContainer,
            cluesContainer,
            skillsContainer,
            xpContainer,
            xpHeader,
            mapContainer,
            relicContainer,
            skillBox,
            xpSkillBox,
            totalLevelBox,
            totalXpBox,
            leagueInfoContainer,
            skull;

    /**
     * Create the OSRS Hiscores image builder
     *
     * @param hiscores    Hiscores to use
     * @param emoteHelper Emote helper
     * @param helpMessage Help message to display in loading message
     */
    public OSRSHiscoresImageBuilder(OSRSHiscores hiscores, EmoteHelper emoteHelper, String helpMessage) {
        super(
                hiscores,
                emoteHelper,
                ResourceHandler.OSRS_BASE_PATH + "Templates/",
                FontManager.OSRS_FONT,
                helpMessage
        );

        this.trackerFont = FontManager.WISE_OLD_MAN_FONT.deriveFont(40f);

        final int opacity = 127; // 50% opacity
        this.redOverlay = new Color(255, 0, 0, opacity);
        this.greenOverlay = new Color(0, 255, 0, opacity);
        this.blackOverlay = new Color(0, 0, 0, opacity);

        this.dark = new Color(EmbedHelper.ROW_DARK);
        this.light = new Color(EmbedHelper.ROW_LIGHT);

        this.highestXpColour = Color.BLACK;
        this.closestToLevelColour = new Color(EmbedHelper.PURPLE);

        ResourceHandler handler = getResourceHandler();
        String templates = getResourcePath();
        this.titleContainer = handler.getImageResource(templates + "title_container.png");
        this.achievementsContainer = handler.getImageResource(templates + "achievements_container.png");
        this.achievementsTitleContainer = handler.getImageResource(templates + "achievements_title_container.png");
        this.bossContainer = handler.getImageResource(templates + "boss_container.png");
        this.cluesContainer = handler.getImageResource(templates + "clues_container.png");
        this.skillsContainer = handler.getImageResource(templates + "skills_container.png");
        this.xpContainer = handler.getImageResource(templates + "xp_tracker_container.png");
        this.xpHeader = handler.getImageResource(templates + "xp_tracker_header.png");
        this.mapContainer = handler.getImageResource(templates + "map_container.png");
        this.relicContainer = handler.getImageResource(templates + "relic_container.png");
        this.leagueInfoContainer = handler.getImageResource(templates + "league_info_container.png");
        this.totalXpBox = handler.getImageResource(templates + "total_xp_box.png");
        this.totalLevelBox = handler.getImageResource(templates + "total_level_box.png");
        this.skillBox = handler.getImageResource(templates + "normal_skill_box.png");
        this.xpSkillBox = handler.getImageResource(templates + "xp_skill_box.png");
        this.skull = handler.getImageResource(PlayerStats.ACCOUNT.SKULL_IMAGE_PATH);
    }

    /**
     * Build an image displaying the given list of bosses.
     * This image consists of vertical rows, where each row contains an image of the boss, and a summary detailing the
     * rank and total kills that the player has for that boss.
     * If the list is empty, the image will have a red overlay indicating no boss kills.
     *
     * @param bossStats List of bosses to display
     * @param args      Hiscores arguments
     * @return Image displaying player boss kills
     */
    public BufferedImage buildBossSection(List<BossStats> bossStats, HashSet<ARGUMENT> args) {
        BufferedImage container = copyImage(bossContainer);
        Graphics g = container.getGraphics();

        // Adjusted dimensions after factoring in border that surrounds boss container image
        final int adjustedHeight = container.getHeight() - (BORDER * 2);
        final int adjustedWidth = container.getWidth() - (BORDER * 2);

        // Draw a red overlay over the empty boss container
        if(bossStats.isEmpty()) {
            g.setColor(redOverlay);
            g.fillRect(BORDER, BORDER, adjustedWidth, adjustedHeight);
            g.dispose();
            return container;
        }

        // Calculate the height to use for each row (always calculate for max bosses)
        final int bossRowHeight = adjustedHeight / MAX_BOSSES;

        final int bossDisplayCount = Math.min(MAX_BOSSES, bossStats.size());
        int y = BORDER;

        for(int i = 0; i < bossDisplayCount; i++) {
            final BufferedImage bossImage = buildBossRowImage(
                    bossStats.get(i),
                    adjustedWidth,
                    bossRowHeight,
                    args
            );
            g.drawImage(bossImage, BORDER, y, null);
            y += bossRowHeight;
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given boss with the player's total kills & rank.
     * This image has a background depicting the bosses environment.
     *
     * @param bossStats Boss to display
     * @param width     Width of boss image to build
     * @param height    Height of clue image to build
     * @param args      Hiscores arguments
     * @return Image displaying a boss and the player's kills/rank for that boss
     */
    private BufferedImage buildBossRowImage(BossStats bossStats, int width, int height, HashSet<ARGUMENT> args) {
        final int boxWidth = (width - BORDER) / 2;
        final int centreVertical = height / 2;
        final int centreHorizontal = boxWidth / 2;
        final Boss boss = bossStats.getBoss();
        final BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics g;

        // Draw boss background (if requested & available)
        final BufferedImage bossBackground = boss.getBackgroundImage();
        if(bossBackground != null && args.contains(ARGUMENT.BOSS_BACKGROUNDS)) {
            g = row.getGraphics();
            g.drawImage(bossBackground, 0, 0, null);
        }

        // Boss image side of the boss row
        BufferedImage bossBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        // Boss kill count/rank side of the boss row
        final BufferedImage textBox = new BufferedImage(
                boxWidth,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        // Fill the background of the boss row sections with random colours
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(bossBox);
            fillImage(textBox);
        }

        final BufferedImage bossImage = boss.getFullImage();

        // Image may be missing, skip drawing boss but still draw rank/kill count
        if(bossImage != null) {
            g = bossBox.getGraphics();

            // Draw boss image centred in boss box
            g.drawImage(
                    bossImage,
                    centreHorizontal - (bossImage.getWidth() / 2),
                    centreVertical - (bossImage.getHeight() / 2),
                    null
            );
        }

        // Draw rank & kill count
        g = textBox.getGraphics();
        g.setFont(getGameFont().deriveFont(70f));
        g.setColor(Color.YELLOW);

        final FontMetrics fm = g.getFontMetrics();

        // Where bottom of kill text will be
        final int midTextBottom = centreVertical + (fm.getMaxAscent() / 2);
        final int gap = 5;

        // Draw kills centred on centre line
        final String kills = bossStats.getFormattedKills();
        g.drawString(
                kills,
                centreHorizontal - (fm.stringWidth(kills) / 2),
                midTextBottom
        );

        // Rank title will be different colour so must be drawn as two separate Strings
        final String rankTitle = "Rank: ";
        final String rankValue = bossStats.getFormattedRank(); // 1,234
        final int rankTitleWidth = fm.stringWidth(rankTitle);

        /*
         * Calculate where to begin drawing the rank title such that when both Strings are drawn beside
         * each other, the full String is centred.
         */
        final int x = centreHorizontal - ((rankTitleWidth + fm.stringWidth(rankValue)) / 2);

        // Draw rank below kills
        final int y = midTextBottom + fm.getMaxAscent() + gap;

        // Add rank title width to leave room for rank title to be drawn
        g.drawString(rankValue, x + rankTitleWidth, y);

        g.setColor(Color.WHITE);
        g.drawString(rankTitle, x, y);

        // Draw boss name above kills
        final String bossName = boss.getShortName();
        g.drawString(
                bossName,
                centreHorizontal - (fm.stringWidth(bossName) / 2),
                midTextBottom - fm.getMaxAscent() - gap
        );

        // Combine in to row with border-sized gap in centre for mid border
        g = row.getGraphics();
        g.drawImage(bossBox, 0, 0, null);
        g.drawImage(textBox, boxWidth + BORDER, 0, null);
        g.dispose();
        return row;
    }

    /**
     * Fill the given image with a random colour
     *
     * @param image Image to fill
     */
    private void fillImage(BufferedImage image) {
        Graphics g = image.getGraphics();
        g.setColor(EmbedHelper.getRandomColour());
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * Build an image displaying the given player's skills.
     * Optionally, the virtual skill levels may be drawn instead of the normal skill levels.
     *
     * @param stats Player stats
     * @param args  Hiscores arguments
     * @return Image displaying player skills
     */
    private BufferedImage buildSkillsSection(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {
        BufferedImage container = copyImage(skillsContainer);

        // Adjusted dimensions after factoring in border that surrounds skills container image
        final int adjustedHeight = container.getHeight() - (BORDER * 2);
        final int adjustedWidth = container.getWidth() - (BORDER * 2);

        final Skill[] skills = stats.getSkills();

        // Skills placed horizontally
        final int columns = 3;

        // Equal gap between columns (including before first column and after last)
        final int horizontalGap = (adjustedWidth - (columns * skillBox.getWidth())) / (columns + 1);

        // +1 to skills for total level, +1 to rows for total xp row
        final int totalRows = ((skills.length + 1) / columns) + 1;

        // Equal gap at the top and bottom
        final int verticalGap = (adjustedHeight - (totalRows * skillBox.getHeight())) / 2;

        Graphics g = container.getGraphics();
        final int startX = BORDER + horizontalGap;
        int x = startX, y = BORDER + verticalGap;

        // Draw skills
        for(int i = 0; i < skills.length; i++) {
            BufferedImage skillImage = buildSkillImage(stats, skills[i], args);
            g.drawImage(skillImage, x, y, null);

            // Drawing final column, move to next row
            if((i + 1) % columns == 0) {
                x = startX;
                y += skillImage.getHeight();
            }

            // Move to next column
            else {
                x += skillImage.getWidth() + horizontalGap;
            }
        }

        // Draw total level
        final BufferedImage totalLevelImage = buildSkillImage(stats, stats.getTotalLevel(), args);
        g.drawImage(totalLevelImage, x, y, null);

        // Draw total XP
        g.drawImage(buildTotalXpImage(stats, args), startX, y + totalLevelImage.getHeight(), null);
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given player's total experience.
     * Depending on arguments, progress towards max may also be displayed.
     *
     * @param stats Player stats to draw total experience from
     * @param args  Hiscores arguments
     * @return Total experience image
     */
    private BufferedImage buildTotalXpImage(OSRSPlayerStats stats, HashSet<ARGUMENT> args) {
        BufferedImage totalXpImage = copyImage(totalXpBox);

        // Fill the background of the total XP box with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(totalXpImage);
        }

        Graphics g = totalXpImage.getGraphics();
        g.setColor(Color.YELLOW);

        final int centreX = totalXpImage.getWidth() / 2;
        final Skill total = stats.getTotalLevel();

        String title = "Total Experience:";
        String value;

        // Show progress towards max XP
        if(args.contains(ARGUMENT.MAX)) {
            final boolean virtual = args.contains(ARGUMENT.VIRTUAL);

            // "Total Experience: 235,422,567"
            title = title + " " + total.getFormattedXp();

            // When not showing virtual max, only count the XP required to max each skill
            final long xpProgress = virtual ? total.getXp() : stats.getXpTowardsMax();

            final long goalXp = virtual ? total.getMaxXp() : total.getXpAtMaxLevel();
            final double progressPercentage = xpProgress / (double) goalXp;

            // "182,598,913/299,791,913 (60.91%)"
            value = Skill.formatNumber(xpProgress)
                    + "/" + Skill.formatNumber(goalXp)
                    + " (" + percentageFormat.format(progressPercentage) + ")";

            g.setFont(getGameFont().deriveFont(40f));
        }

        // Show total XP
        else {
            value = total.getFormattedXp();
            g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        }

        final FontMetrics fm = g.getFontMetrics();

        // Match title alignment with the top number of a skill box
        g.drawString(
                title,
                centreX - (fm.stringWidth(title) / 2),
                UPPER_SKILL_TEXT_Y
        );

        // Match value alignment with the bottom number of a skill box
        g.drawString(
                value,
                centreX - (fm.stringWidth(value) / 2),
                LOWER_SKILL_TEXT_Y
        );

        final BufferedImage icon = total.getImage(true);

        // Skip total icon if missing
        if(icon != null) {
            g.drawImage(
                    icon,
                    (skillBox.getWidth() / 4) - (icon.getWidth() / 2), // Align with skill boxes above it
                    (totalXpImage.getHeight() / 2) - (icon.getHeight() / 2),
                    null
            );
        }

        g.dispose();
        return totalXpImage;
    }

    /**
     * Build an image displaying the given skill level and icon.
     *
     * @param stats Player stats
     * @param skill Skill to draw
     * @param args  Hiscores arguments
     * @return Skill image
     */
    private BufferedImage buildSkillImage(OSRSPlayerStats stats, Skill skill, HashSet<ARGUMENT> args) {
        final boolean totalLevel = skill.getName() == OVERALL;

        final BufferedImage skillImage = totalLevel
                ? copyImage(totalLevelBox)
                : args.contains(ARGUMENT.SKILL_XP) ? copyImage(xpSkillBox) : copyImage(skillBox);

        Graphics g = skillImage.getGraphics();
        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));

        // Fill the background of the skill box with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(skillImage);
        }

        final boolean virtual = args.contains(ARGUMENT.VIRTUAL);

        // TODO Don't display virtual level when it is a group skill
        final int displayLevel = virtual ? skill.getVirtualLevel() : skill.getLevel();

        final String level = skill.isRanked() ? String.valueOf(displayLevel) : "-";
        final int centreX = skillImage.getWidth() / 2;

        // Drawing total level
        if(totalLevel) {
            g.setColor(Color.YELLOW);
            String title = "Total Level:";
            String value;

            // Show number of maxed skills
            if(args.contains(ARGUMENT.MAX)) {
                title = title + " " + level;
                value = "Maxed: " + stats.getTotalMaxedSkills(virtual) + "/" + stats.getSkills().length;
                g.setFont(getGameFont().deriveFont(45f));
            }

            // Only show total level
            else {
                value = level;
            }

            final FontMetrics fm = g.getFontMetrics();

            // Match alignment with the top number of a skill box
            g.drawString(
                    title,
                    centreX - (fm.stringWidth(title) / 2),
                    UPPER_SKILL_TEXT_Y
            );


            // Match alignment with the bottom number of a skill box
            g.drawString(
                    value,
                    (skillImage.getWidth() / 2) - (fm.stringWidth(value) / 2),
                    LOWER_SKILL_TEXT_Y
            );

            g.dispose();
            return skillImage;
        }

        // Drawing skill with xp
        if(args.contains(ARGUMENT.SKILL_XP)) {
            g.setColor(Color.YELLOW);
            final int centreY = skillImage.getHeight() / 2;

            final String xp = skill.isRanked() ? skill.getFormattedXp() : "-";
            g.drawString(level, centreX - (g.getFontMetrics().stringWidth(level) / 2), centreY);

            g.setFont(getGameFont().deriveFont(50f));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(xp, centreX - (fm.stringWidth(xp) / 2), centreY + fm.getMaxAscent());

            final BufferedImage icon = skill.getImage(false);

            // Skip icon if it is missing
            if(icon != null) {
                g.drawImage(icon, BORDER, BORDER, null);
            }

            // Outline the skill(s) with the highest XP
            final Skill highestXpSkill = stats.getHighestXpSkill();
            if(highestXpSkill != null && skill.getXp() == highestXpSkill.getXp()) {
                outlineSkillBox(skillImage, highestXpColour);
            }

            // Outline the skill(s) closest to leveling
            final Skill closestToLevelSkill = stats.getClosestToLevelSkill(virtual);
            if(closestToLevelSkill != null && skill.getProgressUntilNextLevel() == closestToLevelSkill.getProgressUntilNextLevel()) {
                outlineSkillBox(skillImage, closestToLevelColour);
            }

            // Draw a progress bar at the bottom of the skill image indicating progress to the next level TODO check if group and don't draw progress bar
            if(skill.isRanked() && !skill.isMaxed(virtual)) {
                final int progressBarHeight = 20;
                final double levelProgress = skill.getProgressUntilNextLevel();

                Rectangle underlay = new Rectangle(
                        SKILL_NOTCH_SIZE,
                        skillImage.getHeight() - SKILL_BORDER - progressBarHeight,
                        skillImage.getWidth() - 2 * SKILL_NOTCH_SIZE,
                        progressBarHeight
                );

                final int arc = progressBarHeight / 2;

                // Draw base progress bar
                g.setColor(blackOverlay);
                g.fillRoundRect(
                        underlay.x,
                        underlay.y,
                        underlay.width,
                        underlay.height,
                        arc,
                        arc
                );

                // Draw progress on top
                g.setColor(greenOverlay);
                g.fillRoundRect(
                        underlay.x,
                        underlay.y,
                        (int) (underlay.width * levelProgress),
                        underlay.height,
                        arc,
                        arc
                );

                // Draw outline
                g.setColor(Color.BLACK);
                g.drawRoundRect(
                        underlay.x,
                        underlay.y,
                        underlay.width,
                        underlay.height,
                        arc,
                        arc
                );

                // Draw progress percentage
                g.setColor(Color.WHITE);
                g.setFont(getGameFont().deriveFont(16f));

                final String progress = percentageFormat.format(levelProgress);
                g.drawString(
                        progress,
                        underlay.x + (underlay.width / 2) - (g.getFontMetrics().stringWidth(progress) / 2),
                        underlay.y + (underlay.height / 2) + (g.getFontMetrics().getMaxAscent() / 2)
                );
            }
        }

        // Drawing normal skill
        else {
            boolean master = displayLevel > Skill.DEFAULT_MAX_LEVEL;

            // Offset x when skill level is higher than 99 (to allow more room to draw)
            final int masterOffset = 10;

            final int upperTextX = master ? UPPER_SKILL_TEXT_X - masterOffset : UPPER_SKILL_TEXT_X;
            final int lowerTextX = master ? LOWER_SKILL_TEXT_X - masterOffset : LOWER_SKILL_TEXT_X;

            // Draw skill level shadow first
            g.setColor(Color.BLACK);

            g.drawString(level, upperTextX + SHADOW_OFFSET, UPPER_SKILL_TEXT_Y + SHADOW_OFFSET);
            g.drawString(level, lowerTextX + SHADOW_OFFSET, LOWER_SKILL_TEXT_Y + SHADOW_OFFSET);

            // Draw skill level over shadow
            g.setColor(Color.YELLOW);

            g.drawString(level, upperTextX, UPPER_SKILL_TEXT_Y);
            g.drawString(level, lowerTextX, LOWER_SKILL_TEXT_Y);

            // Where centre of icon should be
            BufferedImage icon = skill.getImage(true);

            // Skip icon if it is missing
            if(icon != null) {
                g.drawImage(
                        icon,
                        (skillImage.getWidth() / 4) - (icon.getWidth() / 2),
                        (skillImage.getHeight() / 2) - (icon.getHeight() / 2),
                        null
                );
            }
        }

        /*
         * Draw a red overlay on unranked skills (not when showing boxes).
         * This is because unranked skills are reported as 1 regardless of what the player's actual level is.
         */
        if(!skill.isRanked() && !args.contains(ARGUMENT.SHOW_BOXES)) {
            highlightSkillBox(skillImage, redOverlay);
        }

        // Draw a green overlay on maxed skills (if specified)
        if(args.contains(ARGUMENT.MAX) && skill.isMaxed(virtual)) {
            highlightSkillBox(skillImage, greenOverlay);
        }

        g.dispose();
        return skillImage;
    }

    /**
     * Highlight a skill box image with the given colour.
     * This fills the interior of a skill box image, excluding the outer border.
     *
     * @param skillBox Skill box image
     * @param colour   Colour to highlight
     */
    private void highlightSkillBox(BufferedImage skillBox, Color colour) {
        Graphics g = skillBox.getGraphics();
        g.setColor(colour);
        g.fillPolygon(getInnerSkillBoxShape());
        g.dispose();
    }

    /**
     * Outline a skill box image with the given colour.
     * This fills the outer border of a skill box image.
     *
     * @param skillBox Skill box image
     * @param colour   Colour to outline
     */
    private void outlineSkillBox(BufferedImage skillBox, Color colour) {
        Graphics g = skillBox.getGraphics();
        g.setColor(colour);
        g.fillPolygon(getSkillBoxBorderShape());
        g.dispose();
    }

    /**
     * Get a polygon in the shape of the light coloured border that surrounds a skill box image.
     * This is the area between
     * {@link OSRSHiscoresImageBuilder#getOuterSkillBoxShape()} and {@link OSRSHiscoresImageBuilder#getInnerSkillBoxShape()}.
     * Filling this polygon would fill only the border surrounding a skill box image.
     *
     * @return Outer skill box polygon
     */
    private Polygon getSkillBoxBorderShape() {
        final Polygon outerPolygon = getOuterSkillBoxShape();
        final Polygon innerPolygon = getInnerSkillBoxShape();

        final int[] xPoints = ArrayUtils.addAll(outerPolygon.xpoints, innerPolygon.xpoints);

        return new Polygon(
                xPoints,
                ArrayUtils.addAll(outerPolygon.ypoints, innerPolygon.ypoints),
                xPoints.length
        );
    }

    /**
     * Get a polygon that outlines the outer most edge of a skill box.
     * Filling this polygon would fill an entire skill box image, including the border.
     *
     * @return Outer skill box polygon
     * @see <a href="https://i.imgur.com/JMQGFJ0.png">Outer skill box filled green</a>
     */
    private Polygon getOuterSkillBoxShape() {
        final int[] xPoints = new int[]{
                SKILL_NOTCH_INSET, // Start at outside of top left notch

                skillBox.getWidth() - SKILL_NOTCH_INSET,
                skillBox.getWidth() - SKILL_NOTCH_INSET,
                skillBox.getWidth() - SKILL_BORDER,
                skillBox.getWidth(),

                skillBox.getWidth(),
                skillBox.getWidth() - SKILL_BORDER,
                skillBox.getWidth() - SKILL_NOTCH_INSET,
                skillBox.getWidth() - SKILL_NOTCH_INSET,

                SKILL_NOTCH_INSET,
                SKILL_NOTCH_INSET,
                SKILL_BORDER,
                0,

                0,
                SKILL_BORDER,
                SKILL_NOTCH_INSET,
                SKILL_NOTCH_INSET, // Return to outside of top left notch
        };

        final int[] yPoints = new int[]{
                0, // Start at outside of top left notch

                0,
                SKILL_BORDER,
                SKILL_NOTCH_INSET,
                SKILL_NOTCH_INSET,

                skillBox.getHeight() - SKILL_NOTCH_INSET,
                skillBox.getHeight() - SKILL_NOTCH_INSET,
                skillBox.getHeight() - SKILL_BORDER,
                skillBox.getHeight(),

                skillBox.getHeight(),
                skillBox.getHeight() - SKILL_BORDER,
                skillBox.getHeight() - SKILL_NOTCH_INSET,
                skillBox.getHeight() - SKILL_NOTCH_INSET,

                SKILL_NOTCH_INSET,
                SKILL_NOTCH_INSET,
                SKILL_BORDER,
                0, // Return to outside of top left notch
        };

        return new Polygon(xPoints, yPoints, xPoints.length);
    }

    /**
     * Get a polygon that outlines the inner edge of a skill box.
     * Filling this polygon would fill the inside of a skill box image, excluding the outer border.
     *
     * @return Inner skill box polygon
     * @see <a href="https://i.imgur.com/Uaroxw6.png">Inner skill box filled red</a>
     */
    private Polygon getInnerSkillBoxShape() {
        final int[] xPoints = new int[]{
                SKILL_NOTCH_SIZE, // Start at inside of top left notch

                skillBox.getWidth() - SKILL_NOTCH_SIZE,
                skillBox.getWidth() - SKILL_NOTCH_SIZE,
                skillBox.getWidth() - SKILL_NOTCH_INSET,
                skillBox.getWidth() - SKILL_BORDER,

                skillBox.getWidth() - SKILL_BORDER,
                skillBox.getWidth() - SKILL_NOTCH_INSET,
                skillBox.getWidth() - SKILL_NOTCH_SIZE,
                skillBox.getWidth() - SKILL_NOTCH_SIZE,

                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_INSET,
                SKILL_BORDER,

                SKILL_BORDER,
                SKILL_NOTCH_INSET,
                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_SIZE, // Return to inside of top left notch
        };

        final int[] yPoints = new int[]{
                SKILL_BORDER, // Start at inside of top left notch

                SKILL_BORDER,
                SKILL_NOTCH_INSET,
                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_SIZE,

                skillBox.getHeight() - SKILL_NOTCH_SIZE,
                skillBox.getHeight() - SKILL_NOTCH_SIZE,
                skillBox.getHeight() - SKILL_NOTCH_INSET,
                skillBox.getHeight() - SKILL_BORDER,

                skillBox.getHeight() - SKILL_BORDER,
                skillBox.getHeight() - SKILL_NOTCH_INSET,
                skillBox.getHeight() - SKILL_NOTCH_SIZE,
                skillBox.getHeight() - SKILL_NOTCH_SIZE,

                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_SIZE,
                SKILL_NOTCH_INSET,
                SKILL_BORDER, // Return to inside of top left notch
        };

        return new Polygon(xPoints, yPoints, xPoints.length);
    }

    @Override
    public BufferedImage buildHiscoresImage(OSRSPlayerStats playerStats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        BufferedImage image = new BufferedImage(
                calculateImageWidth(playerStats),
                calculateImageHeight(playerStats),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = image.getGraphics();
        BufferedImage titleSection = buildTitleSection(playerStats);
        g.drawImage(titleSection, 0, 0, null);

        BufferedImage skillsSection = buildSkillsSection(playerStats, args);
        g.drawImage(skillsSection, 0, titleSection.getHeight(), null);

        BufferedImage bossSection = buildBossSection(playerStats.getBossStats(), args);
        g.drawImage(bossSection, skillsSection.getWidth(), titleSection.getHeight(), null);

        // When optional sections are displayed vertically, they should be displayed below the base image
        int y = titleSection.getHeight() + skillsSection.getHeight();

        if(shouldDisplayClues(playerStats)) {
            BufferedImage clueSection = buildClueSection(playerStats.getClues(), args);
            g.drawImage(clueSection, 0, y, null);
            y += clueSection.getHeight();
        }

        if(shouldDisplayAchievements(playerStats)) {
            BufferedImage achievementsSections = buildAchievementsSections(playerStats);
            g.drawImage(achievementsSections, 0, y, null);
            y += achievementsSections.getHeight();
        }

        // Player unlocked regions/relics
        if(shouldDisplayLeagueUnlocks(playerStats)) {
            BufferedImage leagueUnlockSection = buildLeagueUnlockSection((OSRSLeaguePlayerStats) playerStats);
            g.drawImage(leagueUnlockSection, 0, y, null);
            y += leagueUnlockSection.getHeight();
        }

        // League points/tier
        if(shouldDisplayLeagueInfo(playerStats)) {
            BufferedImage leagueInfoSection = buildLeagueInfoSection((OSRSLeaguePlayerStats) playerStats);
            g.drawImage(leagueInfoSection, 0, y, null);
        }

        // Display off to the right of the image
        if(shouldDisplayXpTracker(playerStats)) {
            BufferedImage xpTrackerSection = buildXpTrackerSection(playerStats);
            g.drawImage(xpTrackerSection, titleSection.getWidth(), 0, null);
        }

        g.dispose();
        return image;
    }

    /**
     * Build an image displaying the player's league tier & points.
     * This image uses a similar image to the title section
     *
     * @param stats Player stats
     * @return Image displaying player's league tier & points
     */
    private BufferedImage buildLeagueInfoSection(OSRSLeaguePlayerStats stats) {
        BufferedImage container = copyImage(leagueInfoContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int edgePadding = 2 * BORDER; // Padding from outer edges
        final int imagePadding = 15; // Padding from text to image
        final int centreVertical = (container.getHeight() / 2);
        final int textY = centreVertical + (fm.getMaxAscent() / 2); // Centre text vertically

        LeagueTier leagueTier = stats.getLeagueTier();

        // Draw league tier on the left side of the image
        if(leagueTier.hasTier()) {
            BufferedImage tierIcon = getResourceHandler().getImageResource(leagueTier.getTierImagePath());

            g.drawImage(tierIcon, edgePadding, centreVertical - (tierIcon.getHeight() / 2), null);
            g.drawString(
                    leagueTier.getTierName(),
                    edgePadding + tierIcon.getWidth() + imagePadding,
                    textY
            );
        }

        // Draw league points on the right side of the image
        BufferedImage leaguePointImage = stats.getAccountType().getIcon();
        if(leaguePointImage != null) {
            String points = new DecimalFormat("#,### points").format(leagueTier.getPoints());
            int pointsX = container.getWidth() - edgePadding - fm.stringWidth(points);
            g.drawString(points, pointsX, textY);
            g.drawImage(
                    leaguePointImage,
                    pointsX - imagePadding - leaguePointImage.getWidth(),
                    centreVertical - (leaguePointImage.getHeight() / 2),
                    null
            );
        }
        g.dispose();
        return container;
    }

    /**
     * Calculate the height of the hiscores image to create.
     * This is based on the elements which will be displayed which is determined both by the hiscores arguments
     * and the presence of certain stats for the player.
     * E.g the clue scroll section is typically displayed below the boss section, however if the player has no clue
     * scroll completions, there is no need to build this section.
     *
     * @param stats Player stats
     * @return Height of hiscores image to create
     */
    private int calculateImageHeight(OSRSPlayerStats stats) {

        /*
         * Base height = title section, skills section, and boss section are always displayed
         * (boss section is equal in height to the skills section)
         */
        int height = titleContainer.getHeight() + skillsContainer.getHeight();

        // Clue scroll section optional and is displayed below the base image
        if(shouldDisplayClues(stats)) {
            height += cluesContainer.getHeight();
        }

        // Achievements section is optional and is displayed below clue section (or base image)
        if(shouldDisplayAchievements(stats)) {
            height += achievementsTitleContainer.getHeight() + achievementsContainer.getHeight();
        }

        // League unlocks are optional and are displayed at the bottom of the image
        if(shouldDisplayLeagueUnlocks(stats)) {
            height += mapContainer.getHeight();
        }

        // League info attaches a section to the bottom of the image similar to the title section
        if(shouldDisplayLeagueInfo(stats)) {
            height += leagueInfoContainer.getHeight();
        }

        // If too many optional segments are absent, the XP tracker may be cut off
        final int xpTrackerHeight = xpHeader.getHeight() + xpContainer.getHeight();
        if(shouldDisplayXpTracker(stats) && height < xpTrackerHeight) {
            height = xpTrackerHeight;
        }
        return height;
    }

    /**
     * Calculate the width of the hiscores image to create.
     * This is based on the elements which will be displayed which is determined both by the hiscores arguments
     * and the presence of certain stats for the player.
     * E.g the XP tracker section is typically displayed beside the boss section, however if the hiscores arguments
     * haven't specified to display the XP tracker (or the player has no gained XP for the week), there is no need to
     * build this section.
     *
     * @param stats Player stats
     * @return Width of hiscores image to create
     */
    private int calculateImageWidth(OSRSPlayerStats stats) {

        /*
         * Base width - title section, skills section, and boss section are always displayed,
         * the title section is equal in width to the skills section & boss section combined.
         */
        int width = titleContainer.getWidth();

        // XP tracker is optional and is displayed beside the base image, add its width to the base width
        return shouldDisplayXpTracker(stats) ? width + xpHeader.getWidth() : width;
    }

    /**
     * Check if clue scroll section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display player clue scroll completions in hiscores image
     */
    private boolean shouldDisplayClues(OSRSPlayerStats stats) {
        return stats.hasClueCompletions();
    }

    /**
     * Check if the player's league unlocks should be drawn on to the hiscores image.
     * This is the map & relic container
     *
     * @param stats Player stats
     * @return Should display league map & relic unlocks in hiscores image
     */
    private boolean shouldDisplayLeagueUnlocks(OSRSPlayerStats stats) {
        return stats instanceof OSRSLeaguePlayerStats && ((OSRSLeaguePlayerStats) stats).hasLeagueUnlockData();
    }

    /**
     * Check if the player's league points & tier should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display league points/tier in hiscores image
     */
    private boolean shouldDisplayLeagueInfo(OSRSPlayerStats stats) {
        return stats instanceof OSRSLeaguePlayerStats;
    }

    /**
     * Check if the XP tracker section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display XP tracker in hiscores image
     */
    private boolean shouldDisplayXpTracker(OSRSPlayerStats stats) {
        return stats.hasWeeklyGains() || stats.hasWeeklyRecords();
    }

    /**
     * Check if achievements section should be drawn on to the hiscores image
     *
     * @param stats Player stats
     * @return Should display player achievements in hiscores image
     */
    private boolean shouldDisplayAchievements(OSRSPlayerStats stats) {
        return stats.hasAchievements();
    }

    /**
     * Build an image displaying the given player's achievements.
     * This image consists of up to two sections, a section displaying the player's completed achievements,
     * and a section displaying the player's in progress achievements.
     * Only one section is guaranteed to be present
     * e.g a player has completed all achievements therefore has none in progress, or has completed
     * no achievements therefore every achievement is in progress.
     *
     * @param stats Player stats
     * @return Image displaying the player's achievements
     */
    private BufferedImage buildAchievementsSections(OSRSPlayerStats stats) {
        final boolean displayCompleted = !stats.getCompletedAchievements().isEmpty();
        final boolean displayInProgress = !stats.getInProgressAchievements().isEmpty();

        BufferedImage container = new BufferedImage(
                displayInProgress && displayCompleted
                        ? achievementsContainer.getWidth() * 2
                        : achievementsContainer.getWidth(),
                achievementsTitleContainer.getHeight() + achievementsContainer.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = container.getGraphics();
        int x = 0;

        if(displayCompleted) {

            // Sort by most recent completion first
            BufferedImage completedAchievements = buildAchievementsSection(
                    stats.getCompletedAchievements(),
                    (o1, o2) -> o2.getCompletionDate().compareTo(o1.getCompletionDate()),
                    "Recent Achievements"
            );
            g.drawImage(completedAchievements, x, 0, null);
            x += completedAchievements.getWidth();
        }

        if(displayInProgress) {

            // Sort by closest to completion first
            BufferedImage inProgressAchievements = buildAchievementsSection(
                    stats.getInProgressAchievements(),
                    (o1, o2) -> Double.compare(o2.getProgressPercent(), o1.getProgressPercent()),
                    "Upcoming Achievements"
            );
            g.drawImage(inProgressAchievements, x, 0, null);
        }

        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the top 5 achievements from the given list of achievements.
     * The image consists of a title and a box containing rows for the achievements.
     * Each row consists of a description of the achievement, and a pie chart displaying
     * the progress & optional image.
     *
     * @param achievements Achievements to display
     * @param comparator   Comparator for sorting achievements (the first 5 will be displayed after sorting)
     * @param title        Title to display
     * @return Image displaying achievements
     */
    private BufferedImage buildAchievementsSection(ArrayList<Achievement> achievements, Comparator<Achievement> comparator, String title) {
        achievements.sort(comparator);
        BufferedImage titleImage = buildAchievementsTitleSection(title);
        BufferedImage achievementsImage = copyImage(achievementsContainer);
        Graphics g = achievementsImage.getGraphics();

        final int max = 5;
        final int rowCount = Math.min(achievements.size(), max);

        // Always calculate for max achievements
        final int rowHeight = (achievementsImage.getHeight() - (2 * BORDER)) / max;
        final int rowWidth = achievementsImage.getWidth() - (2 * BORDER);

        int y = BORDER;
        for(int i = 0; i < rowCount; i++) {
            Achievement achievement = achievements.get(i);
            final boolean even = i % 2 == 0;
            g.drawImage(
                    buildAchievementRow(
                            achievement,
                            even ? dark : light,
                            even ? light : dark,
                            rowWidth,
                            rowHeight
                    ),
                    BORDER,
                    y,
                    null
            );
            y += rowHeight;
        }

        BufferedImage container = new BufferedImage(
                titleImage.getWidth(),
                titleImage.getHeight() + achievementsImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        g = container.getGraphics();
        g.drawImage(titleImage, 0, 0, null);
        g.drawImage(achievementsImage, 0, titleImage.getHeight(), null);
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given title for an achievements section.
     * This image should be displayed above an achievements section.
     *
     * @param title Title to display
     * @return Image displaying the given title
     */
    private BufferedImage buildAchievementsTitleSection(String title) {
        BufferedImage container = copyImage(achievementsTitleContainer);
        Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(80f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        g.drawString(
                title,
                (container.getWidth() / 2) - (fm.stringWidth(title) / 2),
                (container.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given achievement details.
     * This image is displayed as a row.
     * The row contains a summary of the achievement (including description, progress, and optional completion date)
     * and a pie chart image indicating the achievement progress.
     * The chart may optionally have an image displayed in the centre.
     *
     * @param achievement Achievement to display
     * @param rowColour   Background colour to use behind row
     * @param chartColour Background colour to use behind chart
     * @param width       Width of row
     * @param height      Height of row
     * @return Achievement row image
     */
    private BufferedImage buildAchievementRow(Achievement achievement, Color rowColour, Color chartColour, int width, int height) {
        final BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics g = row.getGraphics();
        g.setFont(trackerFont.deriveFont(35f));

        final int chartSectionWidth = width / 3;
        final int midY = height / 2;
        final int padding = 10;

        g.setColor(rowColour);
        g.fillRect(0, 0, width - chartSectionWidth, height);
        g.setColor(chartColour);
        g.fillRect(width - chartSectionWidth, 0, width, height);

        final BufferedImage progressImage = getAchievementChart(achievement, (height / 2) - padding);

        g.drawImage(
                progressImage,
                (width - chartSectionWidth / 2) - (progressImage.getWidth() / 2),
                midY - (progressImage.getHeight() / 2),
                null
        );


        g.setColor(achievement.isCompleted() ? Color.GREEN : Color.WHITE);
        final FontMetrics fm = g.getFontMetrics();

        final int midLeftX = (width - chartSectionWidth) / 2;

        final DecimalFormat df = new DecimalFormat("#,###");
        final ArrayList<String> summary = new ArrayList<>();
        summary.add(achievement.getName());
        summary.add(
                df.format(achievement.getProgress())
                        + "/"
                        + df.format(achievement.getThreshold())
                        + " " + achievement.getMeasure()
        );

        if(achievement.isCompleted()) {
            String date = achievement.hasCompletionDate()
                    ? displayFormat.format(achievement.getCompletionDate())
                    : "Unknown";
            summary.add("Date: " + date);
        }

        final int lineHeight = fm.getMaxAscent();
        final int gap = 20;
        final int textHeight = (lineHeight * summary.size()) + (gap * (summary.size() - 1)); // total size occupied by text

        // Text draws from bottom up, add line height so first line is drawn with the top at textHeight
        int textY = midY - (textHeight / 2) + lineHeight;

        for(String line : summary) {
            g.drawString(line, midLeftX - (fm.stringWidth(line) / 2), textY);
            textY += lineHeight + gap;
        }
        g.dispose();
        return row;
    }

    /**
     * Get a pie chart image for the given achievement. If the achievement has an associated image,
     * centre it in the chart.
     *
     * @param achievement Achievement to build chart image for
     * @param radius      Radius to use
     * @return Achievement pie chart
     */
    private BufferedImage getAchievementChart(Achievement achievement, int radius) {
        BufferedImage chart = new PieChart(
                new PieChart.Section[]{
                        new PieChart.Section(
                                "Complete",
                                achievement.getProgress(),
                                new Color(2, 215, 59) // Green
                        ),
                        new PieChart.Section(
                                "Incomplete",
                                achievement.getRemaining(),
                                new Color(255, 40, 32) // Red
                        )
                },
                getGameFont(),
                false,
                radius
        ).getChart();

        if(achievement.canHaveImage()) {
            Graphics g = chart.getGraphics();
            BufferedImage achievementImage = achievement.getImage();

            // Skill/boss/etc may not have an image
            if(achievementImage == null) {
                return chart;
            }
            g.drawImage(
                    achievementImage,
                    (chart.getWidth() / 2) - (achievementImage.getWidth() / 2),
                    (chart.getHeight() / 2) - (achievementImage.getHeight() / 2),
                    null
            );
            g.dispose();
        }
        return chart;
    }

    /**
     * Build an image displaying the given player's name, account type, combat level, and rank.
     *
     * @param stats Player stats
     * @return Image displaying player overview
     */
    private BufferedImage buildTitleSection(OSRSPlayerStats stats) {
        final BufferedImage container = copyImage(titleContainer);
        final Graphics g = container.getGraphics();
        g.setFont(getGameFont().deriveFont(140f));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final String name = stats.getName();
        final int centreVertical = container.getHeight() / 2;
        final int x = (container.getWidth() / 2) - (fm.stringWidth(name) / 2);

        g.drawString(name.toUpperCase(), x, centreVertical + (fm.getMaxAscent() / 2));

        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        fm = g.getFontMetrics();

        // Draw account type image if one is available
        final BufferedImage accountTypeImage = stats.getAccountType().getIcon();
        if(accountTypeImage != null) {

            // Add a 50px gap between end of account type image and start of player name;
            final int accountTypeImageX = x - accountTypeImage.getWidth() - 50;
            final int accountTypeImageY = centreVertical - (accountTypeImage.getHeight() / 2);

            // Draw account type image
            g.drawImage(
                    accountTypeImage,
                    accountTypeImageX,
                    accountTypeImageY,
                    null
            );

            // Player is a dead hardcore ironman, draw a death icon inside the hardcore ironman helmet
            if(stats.isHardcore() && ((OSRSHardcorePlayerStats) stats).isDead()) {
                g.drawImage(
                        skull,
                        accountTypeImageX + (accountTypeImage.getWidth() / 2) - (skull.getWidth() / 2),
                        accountTypeImageY + (accountTypeImage.getHeight() / 2) - (skull.getHeight() / 2),
                        null
                );
            }
        }

        final int edgePadding = 2 * BORDER; // Padding from outer edges
        final int imagePadding = 15; // Padding from text to image
        final int textY = centreVertical + (fm.getMaxAscent() / 2); // Centre text vertically

        // Draw combat level on left side of title section
        final String combat = String.valueOf(stats.getCombatLevel());
        final BufferedImage combatImage = getResourceHandler().getImageResource(Skill.LARGE_COMBAT_IMAGE_PATH);
        g.drawImage(
                combatImage,
                edgePadding,
                centreVertical - (combatImage.getHeight() / 2),
                null
        );
        g.drawString(combat, edgePadding + combatImage.getWidth() + imagePadding, textY);

        // Draw rank info on right side of title section
        final Skill totalLevel = stats.getTotalLevel();
        final String rank = "Rank: " + totalLevel.getFormattedRank();
        final BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
        final int rankX = container.getWidth() - edgePadding - fm.stringWidth(rank);

        g.drawString(rank, rankX, textY);
        g.drawImage(
                rankImage,
                rankX - imagePadding - rankImage.getWidth(),
                centreVertical - (rankImage.getHeight() / 2),
                null
        );
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given clue scroll completions.
     * This image is a horizontal list of clue scroll images.
     * Each clue scroll image contains the type of clue, an image of the clue, and the player's completions of the clue.
     * These are displayed left to right in ascending order of difficulty.
     * Clue scrolls without any completions are displayed with a red overlay.
     *
     * @param clues List of clue scroll completions
     * @param args  Hiscores arguments
     * @return Image displaying player clue scroll completions
     */
    private BufferedImage buildClueSection(Clue[] clues, HashSet<ARGUMENT> args) {
        final BufferedImage container = copyImage(cluesContainer);

        Graphics g = container.getGraphics();

        final int maxClues = clues.length - 1; // Not drawing "ALL" type clue

        // Always calculate for max clues
        final int clueWidth = (container.getWidth() - (2 * BORDER)) / maxClues;
        final int clueHeight = container.getHeight() - (2 * BORDER);

        int x = BORDER;

        // Build clue stat images with transparent backgrounds to place in to the clue section
        for(Clue clue : clues) {
            if(clue.getType() == Clue.TYPE.ALL) {
                continue;
            }
            final BufferedImage clueImage = buildClueImage(clue, clueWidth, clueHeight, args);
            g.drawImage(clueImage, x, BORDER, null);
            x += clueWidth;
        }
        g.dispose();
        return container;
    }

    /**
     * Build an image displaying the given clue and a summary detailing the player's completions and rank.
     * This image has a transparent background to be drawn on top of the clue section.
     *
     * @param clue   Clue to display
     * @param width  Width of clue image to build
     * @param height Height of clue image to build
     * @param args   Hiscores arguments
     * @return Clue image
     */
    private BufferedImage buildClueImage(Clue clue, int width, int height, HashSet<ARGUMENT> args) {
        final BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = background.getGraphics();

        g.setFont(getGameFont().deriveFont(STANDARD_FONT_SIZE));
        g.setColor(Color.YELLOW);
        FontMetrics fm = g.getFontMetrics();

        final int centreVertical = height / 2;
        final int centreHorizontal = width / 2;

        // Fill the background of the clue section with a random colour
        if(args.contains(ARGUMENT.SHOW_BOXES)) {
            fillImage(background);
        }

        final BufferedImage clueImage = clue.getFullImage(ResourceHandler.OSRS_BASE_PATH);

        // Clue image may be missing
        if(clueImage == null) {
            return background;
        }

        // Draw clue centred in background
        g.drawImage(
                clueImage,
                centreHorizontal - (clueImage.getWidth() / 2),
                centreVertical - (clueImage.getHeight() / 2),
                null
        );

        // Height of area above & below clue
        final int textBoxHeight = (height - clueImage.getHeight()) / 2;
        final int centreTextVertical = textBoxHeight / 2;

        // Draw name centred in the area above the clue image
        final String name = clue.getType().getName();
        g.drawString(
                name,
                centreHorizontal - (fm.stringWidth(name) / 2),
                centreTextVertical + (fm.getMaxAscent() / 2)
        );

        g.setFont(getGameFont().deriveFont(50f));
        fm = g.getFontMetrics();

        // Centre of the area below the clue image
        final int centreBottom = (height - textBoxHeight) + centreTextVertical;

        // Draw completions above the centre line of the area below the clue image
        final String completions = clue.hasCompletions() ? clue.getFormattedCompletions() : "-";
        g.drawString(
                completions,
                centreHorizontal - (fm.stringWidth(completions) / 2),
                centreBottom
        );

        // Rank title will be different colour so must be drawn as two separate Strings
        final String rankTitle = "Rank: ";
        final String rankValue = clue.getFormattedRank(); // 1,234
        final int rankTitleWidth = fm.stringWidth(rankTitle);

        /*
         * Calculate where to begin drawing the rank title such that when both Strings are drawn beside
         * each other, the full String is centred.
         */
        final int rankX = centreHorizontal - ((rankTitleWidth + fm.stringWidth(rankValue)) / 2);

        // Draw rank below the centre line of the area below the clue image
        final int y = centreBottom + fm.getMaxAscent();
        g.drawString(rankValue, rankX + rankTitleWidth, y); // Add rank title width to leave room for rank title to be drawn

        g.setColor(Color.WHITE);
        g.drawString(rankTitle, rankX, y);

        // Draw a red overlay on incomplete clues (not when showing boxes)
        if(!clue.hasCompletions() && !args.contains(ARGUMENT.SHOW_BOXES)) {
            g.setColor(redOverlay);
            g.fillRect(0, 0, width, height);
        }

        g.dispose();
        return background;
    }

    /**
     * Build an image displaying the player's weekly gained XP
     *
     * @param stats Player stats
     * @return Image displaying player's weekly gained XP
     */
    private BufferedImage buildXpTrackerSection(OSRSPlayerStats stats) {
        BufferedImage container = new BufferedImage(
                xpHeader.getWidth(),
                xpHeader.getHeight() + xpContainer.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        BufferedImage tracker = copyImage(xpContainer);
        Graphics g = tracker.getGraphics();

        final Skill[] skills = stats.getSkills();

        // Adjusted height after factoring in border that surrounds XP container image
        final int adjustedHeight = tracker.getHeight() - (2 * BORDER);

        // Add 1 for total level row
        final int totalRows = skills.length + 1;

        final int rowHeight = adjustedHeight / totalRows;

        // Pad out final skill (total level) row height with what is left after integer division
        final int finalPadding = adjustedHeight - (totalRows * rowHeight);

        final int rowWidth = tracker.getWidth() - (2 * BORDER);
        int y = BORDER;

        Color colour = null;

        // Draw skills
        for(int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            colour = i % 2 == 0 ? dark : light;
            BufferedImage row = buildSkillXpRow(
                    rowWidth,
                    rowHeight,
                    colour,
                    skill
            );
            g.drawImage(row, BORDER, y, null);
            y += rowHeight;
        }

        // Draw total level - no colour will allow the background to show through
        BufferedImage totalLevelRow = buildSkillXpRow(
                rowWidth,
                rowHeight + finalPadding,
                colour == light ? dark : light,
                stats.getTotalLevel()
        );

        g.drawImage(totalLevelRow, BORDER, y, null);

        BufferedImage header = buildXpTrackerHeader(stats);
        g = container.getGraphics();
        g.drawImage(header, 0, 0, null);
        g.drawImage(tracker, 0, header.getHeight(), null);
        g.dispose();
        return container;
    }

    /**
     * Build the XP tracker header image.
     * This displays the weekly period for which the XP gains are from.
     *
     * @param stats Player stats
     * @return XP tracker header image
     */
    private BufferedImage buildXpTrackerHeader(OSRSPlayerStats stats) {
        BufferedImage header = copyImage(xpHeader);
        Graphics g = header.getGraphics();

        g.setFont(trackerFont);
        g.setColor(Color.YELLOW);

        Date startDate = stats.getTrackerStartDate();
        Date endDate = stats.getTrackerEndDate();
        final String unknown = "UNKNOWN";

        String trackerPeriod = (startDate == null ? unknown : displayFormat.format(stats.getTrackerStartDate()))
                + " - "
                + (endDate == null ? unknown : displayFormat.format(stats.getTrackerEndDate()));

        // x & y aligning with hardcoded text in header
        final int headerX = 793;
        final int headerY = 166;

        g.drawString(
                trackerPeriod,
                headerX - (g.getFontMetrics().stringWidth(trackerPeriod) / 2),
                headerY
        );
        g.dispose();
        return header;
    }

    /**
     * Build an image displaying the XP gained in the given skill alongside the icon and skill name
     *
     * @param width  Width of image
     * @param height Height of image
     * @param colour Colour to fill row background
     * @param skill  Skill to display XP from
     * @return XP displaying gained XP in the given skill
     */
    private BufferedImage buildSkillXpRow(int width, int height, Color colour, Skill skill) {
        BufferedImage row = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = row.getGraphics();

        g.setColor(colour);
        g.fillRect(0, 0, width, height);

        g.setFont(trackerFont);

        final int iconX = BORDER, expX = 481, recordX = 962, sectionMid = row.getHeight() / 2;

        BufferedImage icon = skill.getImage(false);

        // Draw skill icon (may be missing but shouldn't ever happen)
        if(icon != null) {
            g.drawImage(icon, iconX, sectionMid - (icon.getHeight() / 2), null);
        }

        // Draw text here to be centred in row
        final int textY = sectionMid + (g.getFontMetrics().getMaxAscent() / 2);
        final DecimalFormat xpFormat = new DecimalFormat("#,### XP");

        // Gained XP is equal/higher than the current record (or no current record)
        final boolean recordBroken = skill.getGainedXp() >= skill.getRecordXp();

        final String name = StringUtils.capitalize(skill.getName().name().toLowerCase());

        String xp = xpFormat.format(skill.getGainedXp());

        // Draw XP gained as either green (if higher than the current record) or yellow (lower) in the format '+100 XP'
        if(skill.hasGainedXp()) {
            g.setColor(recordBroken ? Color.GREEN : Color.YELLOW);
            xp = "+" + xp;
        }

        // Draw XP gained as white without a plus sign e.g '0 XP'
        else {
            g.setColor(skill.getGainedXp() < 0 ? Color.RED : Color.WHITE);
        }

        g.drawString(name, iconX + Skill.ICON_WIDTH + BORDER, textY);
        g.drawString(xp, expX, textY);

        String recordXp;

        // Draw either the weekly XP record or a message indicating that the record has been broken
        if(skill.hasRecordXp()) {
            if(recordBroken) {
                g.setColor(Color.MAGENTA);

                // The current gained XP is now the record (no point showing this XP value twice)
                recordXp = "New record!";
            }
            else {
                g.setColor(Color.GREEN);
                recordXp = xpFormat.format(skill.getRecordXp());
            }
        }

        // No weekly record for the skill, draw a message in white noting this
        else {
            g.setColor(Color.WHITE);
            recordXp = "No record for this skill";
        }

        g.drawString(recordXp, recordX, textY);
        g.dispose();
        return row;
    }

    /**
     * Build an image displaying league relics and region unlocks.
     * These are displayed side by side
     *
     * @param stats Player stats
     * @return Image displaying league relics/region unlocks
     */
    private BufferedImage buildLeagueUnlockSection(OSRSLeaguePlayerStats stats) {
        BufferedImage regionImage = buildRegionSection(stats.getRegions());
        BufferedImage relicImage = buildRelicSection(stats.getRelicTiers());

        BufferedImage leagueImage = new BufferedImage(
                regionImage.getWidth() + relicImage.getWidth(),
                regionImage.getHeight(), // Same height as relic image
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = leagueImage.getGraphics();
        g.drawImage(relicImage, 0, 0, null);
        g.drawImage(regionImage, relicImage.getWidth(), 0, null);
        g.dispose();
        return leagueImage;
    }

    /**
     * Create an image displaying the unlocked/locked relic tiers
     * of a league player
     *
     * @param relicTiers List of unlocked relics
     * @return Image displaying player relics
     */
    private BufferedImage buildRelicSection(ArrayList<RelicTier> relicTiers) {
        BufferedImage relicContainer = copyImage(this.relicContainer);
        BufferedImage lockedRelic = getResourceHandler().getImageResource(Relic.RES + Relic.LOCKED_RELIC_FILENAME);
        Graphics g = relicContainer.getGraphics();
        g.setFont(getGameFont().deriveFont(40f));
        FontMetrics fm = g.getFontMetrics();

        int x = 170, ogX = 170, y = 170;

        // Always display 6 relics - Anything less than max is considered locked
        for(int i = 0; i < RelicTier.MAX_RELICS; i++) {
            BufferedImage relicImage;
            if(i >= relicTiers.size()) {
                relicImage = lockedRelic;
            }
            else {
                Relic relic = relicTiers.get(i).getRelicByIndex(0);
                relicImage = getResourceHandler().getImageResource(relic.getImagePath());
                String name = relic.getName();
                g.drawString(
                        name,
                        x - (fm.stringWidth(name) / 2),
                        y + (relicImage.getHeight() / 2) + 15 + fm.getMaxAscent()
                );

            }
            g.drawImage(
                    relicImage,
                    x - (relicImage.getWidth() / 2),
                    y - (relicImage.getHeight() / 2),
                    null
            );

            if((i + 1) % 3 == 0) {
                x = ogX;
                y += 312;
            }
            else {
                x += 340;
            }
        }
        g.dispose();
        return relicContainer;
    }

    /**
     * Create an image displaying the unlocked/locked regions
     * of a league player
     *
     * @param regions List of unlocked regions
     * @return Image displaying player regions
     */
    private BufferedImage buildRegionSection(ArrayList<Region> regions) {
        BufferedImage map = getResourceHandler().getImageResource(Region.RES + Region.BASE_MAP_FILENAME);
        BufferedImage mapContainer = copyImage(this.mapContainer);
        Graphics g = map.getGraphics();
        for(Region region : regions) {
            g.drawImage(
                    getResourceHandler().getImageResource(region.getImagePath()),
                    0,
                    0,
                    null
            );
        }
        g = mapContainer.getGraphics();
        g.drawImage(
                map,
                (mapContainer.getWidth() / 2) - (map.getWidth() / 2),
                (mapContainer.getHeight() / 2) - (map.getHeight() / 2),
                null
        );
        g.dispose();
        return mapContainer;
    }
}