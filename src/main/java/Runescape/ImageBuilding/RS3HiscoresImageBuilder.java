package Runescape.ImageBuilding;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Commands.Lookup.RunescapeLookupCommand.ARGUMENT;
import Command.Structure.ImageLoadingMessage;
import Command.Structure.PieChart;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Runescape.Hiscores.RS3Hiscores;
import Runescape.Stats.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

import static Command.Structure.PieChart.*;
import static Runescape.Stats.Clan.*;

/**
 * Build an image displaying a player's RS3 stats
 */
public class RS3HiscoresImageBuilder extends HiscoresImageBuilder<RS3PlayerStats, RS3Hiscores> {
    private final Color orange, yellow, red, blue;
    private final BufferedImage skillsSection, clanSection, clueSection, titleSection, questSection;

    /**
     * Create the RS3 Hiscores instance
     *
     * @param hiscores    Hiscores to use
     * @param emoteHelper Emote helper
     * @param helpMessage Help message to display in loading message
     */
    public RS3HiscoresImageBuilder(RS3Hiscores hiscores, EmoteHelper emoteHelper, String helpMessage) {
        super(
                hiscores,
                emoteHelper,
                ResourceHandler.RS3_BASE_PATH + "Templates/",
                FontManager.RS3_FONT,
                helpMessage
        );
        this.orange = new Color(EmbedHelper.RUNESCAPE_ORANGE);
        this.yellow = new Color(EmbedHelper.RUNESCAPE_YELLOW);
        this.blue = new Color(EmbedHelper.RUNESCAPE_BLUE);
        this.red = new Color(EmbedHelper.RUNESCAPE_RED);

        ResourceHandler handler = getResourceHandler();
        String templates = getResourcePath();
        this.skillsSection = handler.getImageResource(templates + "skills_section.png");
        this.clanSection = handler.getImageResource(templates + "clan_section.png");
        this.clueSection = handler.getImageResource(templates + "clue_section.png");
        this.titleSection = handler.getImageResource(templates + "title_section.png");
        this.questSection = handler.getImageResource(templates + "quest_section.png");
    }

    @Override
    public BufferedImage buildHiscoresImage(RS3PlayerStats playerStats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage) {
        int overhang = 65; // title section left overhang

        BufferedImage skillSection = buildSkillSection(playerStats, args);
        BufferedImage clueSection = buildClueSection(playerStats.getClues());
        BufferedImage titleSection = buildTitleSection(playerStats);

        BufferedImage playerImage = new BufferedImage(
                titleSection.getWidth(),
                skillSection.getHeight() + titleSection.getHeight() + overhang,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = playerImage.getGraphics();
        g.drawImage(titleSection, 0, 0, null);
        g.drawImage(skillSection, overhang, titleSection.getHeight(), null);
        g.drawImage(clueSection, overhang + skillSection.getWidth(), titleSection.getHeight(), null);

        if(playerStats.hasRuneMetrics()) {
            g.drawImage(
                    buildQuestSection(playerStats.getRuneMetrics()),
                    overhang + skillSection.getWidth(),
                    titleSection.getHeight() + clueSection.getHeight(),
                    null
            );
        }

        if(playerStats.isClanMember()) {
            BufferedImage clanSection = buildClanSection(playerStats.getClan(), playerStats.getName());
            BufferedImage expandedImage = new BufferedImage(
                    playerImage.getWidth(),
                    playerImage.getHeight() + clanSection.getHeight(),
                    playerImage.getType()
            );
            g = expandedImage.getGraphics();
            g.drawImage(playerImage, 0, 0, null);
            g.drawImage(
                    clanSection,
                    overhang,
                    titleSection.getHeight() + skillSection.getHeight(),
                    null
            );
            playerImage = expandedImage;
        }
        g.dispose();
        return playerImage;
    }

    /**
     * Build the clan section of the image
     *
     * @param clan Player clan
     * @param name Player name
     * @return Clan image section
     */
    private BufferedImage buildClanSection(Clan clan, String name) {
        final BufferedImage clanSection = copyImage(this.clanSection);
        Graphics g = clanSection.getGraphics();
        g.setFont(getGameFont().deriveFont(40f));
        g.setColor(Color.WHITE);
        g.drawImage(clan.getBanner(), clanSection.getWidth() - clan.getBanner().getWidth(), 0, null);
        g.drawString(clan.getName(), 100, 50);

        g.setColor(orange);
        ArrayList<String> owners = clan.getPlayersByRole(ROLE.OWNER);
        String owner = owners.isEmpty() ? "Unknown" : owners.get(0);

        int x = 300;
        g.setFont(getGameFont().deriveFont(25f));

        g.drawString(owner, x, 150);
        g.drawString(clan.getRoleByPlayerName(name).getName(), x, 200);
        g.drawString(String.valueOf(clan.getMemberCount()), x, 250);
        g.dispose();
        return clanSection;
    }

    /**
     * Build the clue section of the image
     *
     * @param clues Clue data
     * @return Clue scroll image section
     */
    private BufferedImage buildClueSection(Clue[] clues) {
        final BufferedImage clueSection = copyImage(this.clueSection);
        Graphics g = clueSection.getGraphics();
        g.setFont(getGameFont().deriveFont(40f));
        g.setColor(orange);
        int x = 330;
        int y = 174;
        FontMetrics fm = g.getFontMetrics();
        for(Clue clue : clues) {
            g.drawString(clue.getFormattedCompletions(), x, y - (fm.getHeight() / 2) + fm.getAscent());
            y += 140;
        }
        g.dispose();
        return clueSection;
    }

    /**
     * Build the skill section of the image
     *
     * @param playerStats Player stats
     * @param args        Hiscores arguments
     * @return Skill image section
     */
    private BufferedImage buildSkillSection(RS3PlayerStats playerStats, HashSet<ARGUMENT> args) {
        final BufferedImage skillsSection = copyImage(this.skillsSection);
        Graphics g = skillsSection.getGraphics();
        g.setFont(getGameFont().deriveFont(55f));

        // First skill location
        int x = 170, ogX = x;
        int y = 72;

        int totalY = 1560;

        g.setColor(yellow);

        final Skill[] skills = playerStats.getSkills();

        // Draw skills
        for(int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            int displayLevel = args.contains(ARGUMENT.VIRTUAL) ? skill.getVirtualLevel() : skill.getLevel();
            boolean master = displayLevel > Skill.DEFAULT_MAX;
            String level = String.valueOf(displayLevel);

            g.drawString(level, master ? x - 15 : x, y); // top
            g.drawString(level, master ? x + 60 : x + 78, y + 64); // bottom

            // Currently 3rd column, reset back to first column and go down a row
            if((i + 1) % 3 == 0) {
                x = ogX;
                y += 142;
            }
            // Move to next column
            else {
                x += 330;
            }
        }

        g.setColor(Color.WHITE);
        BufferedImage rankImage = getResourceHandler().getImageResource(Skill.RANK_IMAGE_PATH);
        String rank = "Rank: " + playerStats.getTotalLevel().getFormattedRank();

        x = 365;
        y = 1430 - rankImage.getHeight(); // Align with bottom skill row

        // Draw rank icon and hiscores rank
        g.drawImage(rankImage, x, y, null);
        g.drawString(
                rank,
                x + rankImage.getWidth() + 20,
                y + (rankImage.getHeight() / 2) + (g.getFontMetrics().getMaxAscent() / 2)
        );

        // Draw total level
        final Skill totalLevel = playerStats.getTotalLevel();
        g.drawString(
                String.valueOf(
                        args.contains(ARGUMENT.VIRTUAL)
                                ? totalLevel.getVirtualLevel()
                                : totalLevel.getLevel()
                ),
                240,
                totalY
        );

        // Draw combat level
        g.drawString(String.valueOf(playerStats.getCombatLevel()), 730, totalY);
        g.dispose();
        return skillsSection;
    }

    /**
     * Build the title section of the image
     *
     * @param playerStats Player stats
     * @return Title section of image
     */
    private BufferedImage buildTitleSection(RS3PlayerStats playerStats) {
        final BufferedImage titleSection = copyImage(this.titleSection);
        final String name = playerStats.getName();

        // Get and resize player avatar
        BufferedImage avatar = hiscores.getPlayerAvatar(name);
        BufferedImage scaledAvatar = new BufferedImage(287, 287, BufferedImage.TYPE_INT_ARGB);
        Graphics g = scaledAvatar.createGraphics();
        g.drawImage(avatar, 0, 0, 287, 287, null);
        g = titleSection.getGraphics();
        g.drawImage(scaledAvatar, 110, 124, null);

        g.setFont(getGameFont().deriveFont(75f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(orange);

        // Align with bottom right of player avatar box
        int x = 445;
        int y = 415;

        // Ironman etc, draw helmet
        final BufferedImage accountTypeImage = playerStats.getAccountType().getIcon();
        if(accountTypeImage != null) {
            g.drawImage(accountTypeImage, x, y - accountTypeImage.getHeight(), null);

            // Offset X for before drawing player name
            x += accountTypeImage.getWidth() + 50;
        }

        final String drawnName = name.toUpperCase();
        g.drawString(drawnName, x, y);

        // Player was at one point a hardcore ironman and died - draw the cause of death
        if(playerStats.hasHCIMStatus() && playerStats.getHcimStatus().isDead()) {
            final HCIMStatus hcimStatus = playerStats.getHcimStatus();

            // Currently viewing hardcore stats - draw a line through the player name to look cool
            if(playerStats.isHardcore()) {

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(red);
                g2d.setStroke(
                        new BasicStroke(
                                10f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                final int nameWidth = fm.stringWidth(drawnName);

                // Stroke is drawn around mid point - don't need to account for stroke width
                final int nameVertCentre = y - (fm.getMaxAscent() / 2);
                g2d.drawLine(x, nameVertCentre, x + nameWidth, nameVertCentre);
            }

            // Build death section outlining how/when/where they died
            BufferedImage deathSection = getResourceHandler().getImageResource(
                    getResourcePath() + "death_section.png"
            );

            g = deathSection.getGraphics();
            g.setFont(getGameFont().deriveFont(20f));
            fm = g.getFontMetrics();
            g.setColor(Color.WHITE);

            x = (deathSection.getWidth() / 2);
            y = 45 + fm.getHeight();

            g.drawString(hcimStatus.getDate(), x - (fm.stringWidth(hcimStatus.getDate()) / 2), y);
            y += fm.getHeight();

            String[] cause = (hcimStatus.getLocation() + " " + hcimStatus.getCause()).split(" ");
            String curr = "";

            for(String word : cause) {
                String attempt = curr + " " + word;
                if(fm.stringWidth(attempt) > deathSection.getWidth()) {
                    g.drawString(curr, x - (fm.stringWidth(curr) / 2), y);
                    y += fm.getHeight();
                    curr = word;
                    continue;
                }
                curr = attempt;
            }

            g.drawString(curr, x - (fm.stringWidth(curr) / 2), y);
            g = titleSection.getGraphics();

            g.drawImage(
                    deathSection,
                    titleSection.getWidth() - 65 - deathSection.getWidth(),
                    titleSection.getHeight() - deathSection.getHeight(),
                    null
            );

            return titleSection;
        }

        g.dispose();
        return titleSection;
    }

    /**
     * Build quest image section
     *
     * @param runeMetrics Player RuneMetrics data
     * @return Quest section displaying player quest stats
     */
    private BufferedImage buildQuestSection(RuneMetrics runeMetrics) {
        final BufferedImage questSection = copyImage(this.questSection);
        Section[] sections = new Section[]{
                new Section("Not Started", runeMetrics.getQuestsNotStarted(), red),
                new Section("In Progress", runeMetrics.getQuestsStarted(), blue),
                new Section("Completed", runeMetrics.getQuestsCompleted(), orange)
        };
        PieChart pieChart = new PieChart(sections, getGameFont(), true);
        Graphics g = questSection.getGraphics();
        int y = 150;
        g.drawImage(
                pieChart.getChart(),
                (questSection.getWidth() / 2) - (pieChart.getChart().getWidth() / 2),
                y,
                null
        );

        g.drawImage(
                pieChart.getKey(),
                (questSection.getWidth() / 2) - (pieChart.getKey().getWidth() / 2),
                y + pieChart.getChart().getHeight() + 20,
                null
        );
        g.dispose();
        return questSection;
    }
}
