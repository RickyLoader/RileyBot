package LOL.Blitz;

import Bot.ResourceHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;

import static Command.Structure.ImageBuilder.registerFont;

public class BlitzImageBuilder {
    private final Font blitzFont;
    private final BufferedImage next, abilityOrderTemplate;

    public BlitzImageBuilder() {
        ResourceHandler handler = new ResourceHandler();
        this.blitzFont = registerFont("/LOL/blitz_font.ttf", handler);
        this.next = handler.getImageResource("/LOL/next.png");
        this.abilityOrderTemplate = handler.getImageResource(Ability.ORDER_PATH + "ability_order.png");
    }

    /**
     * Build an image showing the build data
     *
     * @param buildData Build data for champion
     * @return Image URL
     */
    public byte[] buildImage(BuildData buildData) {
        if(blitzFont == null) {
            System.out.println("Failed to read Blitz font");
            return null;
        }
        try {
            BufferedImage spellsImage = getSpellsImage(buildData.getSpells());
            BufferedImage startingItemImage = getItemImage(buildData.getStartingItems(), false);
            BufferedImage finalItemImage = getItemImage(buildData.getFinalBuild(), false);
            BufferedImage buildOrderImage = getItemImage(buildData.getBuildOrder(), true);
            BufferedImage runeImage = getRuneImage(buildData.getRunes());
            BufferedImage abilityOrderImage = getAbilityOrderImage(buildData.getAbilityOrder());
            BufferedImage championImage = buildData.getChampion().getChampionImage();

            int width = abilityOrderImage.getWidth();
            int height = championImage.getHeight() + startingItemImage.getHeight() + buildOrderImage.getHeight() + runeImage.getHeight() + abilityOrderImage.getHeight() + 244;
            int border = 20;

            BufferedImage image = new BufferedImage(
                    width + (border * 2),
                    height + (border * 2),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics g = image.getGraphics();
            g.setColor(Color.decode("#242c44"));
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setFont(blitzFont.deriveFont(50f));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();

            g.drawImage(championImage, border, border, null);

            int x = championImage.getWidth() + 20 + border;
            int y = border + fm.getAscent();

            String championName = buildData.getChampion().getName() + " - " + buildData.getRole();
            g.drawString(championName, x, y);

            g.setFont(blitzFont.deriveFont(35f));
            fm = g.getFontMetrics();
            String stats = new DecimalFormat("0.00% Win Rate").format(buildData.getWinRate()) + new DecimalFormat(" (#,### Games)").format(buildData.getGames());
            g.drawString(stats, x, y + 20 + fm.getHeight());

            g.setFont(blitzFont.deriveFont(20f));
            fm = g.getFontMetrics();
            y = championImage.getHeight() + border + 20 + fm.getHeight();

            g.drawString("Summoner Spells", border, y);
            g.drawImage(spellsImage, border, y + 20, null);

            g.drawString("Starting Items", image.getWidth() - border - startingItemImage.getWidth(), y);
            g.drawImage(startingItemImage, image.getWidth() - border - startingItemImage.getWidth(), y + 20, null);

            y += startingItemImage.getHeight() + fm.getHeight() + 40;

            g.drawString("Core Build Path", border, y);
            g.drawImage(buildOrderImage, border, y + 20, null);

            g.drawString("Core Final Build", image.getWidth() - border - finalItemImage.getWidth(), y);
            g.drawImage(finalItemImage, image.getWidth() - border - finalItemImage.getWidth(), y + 20, null);

            y += buildOrderImage.getHeight() + fm.getHeight() + 40;

            int mid = image.getWidth() / 2;
            String runeText = "Runes";
            g.drawString(runeText, mid - (fm.stringWidth(runeText) / 2), y);
            g.drawImage(runeImage, mid - (runeImage.getWidth() / 2), y + 20, null);

            y += runeImage.getHeight() + fm.getHeight() + 40;

            String abilityString = "Ability Order";
            g.drawString(abilityString, mid - (fm.stringWidth(abilityString) / 2), y);
            g.drawImage(abilityOrderImage, mid - (abilityOrderImage.getWidth() / 2), y + 20, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            outputStream.close();
            return outputStream.toByteArray();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build an image displaying the ability order
     *
     * @param abilityOrder Ability order
     * @return Image displaying ability order
     */
    private BufferedImage getAbilityOrderImage(Ability[] abilityOrder) {
        int height = 232, width = 900;
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        try {
            BufferedImage abilityImage = new BufferedImage(abilityOrderTemplate.getWidth(), abilityOrderTemplate.getHeight(), abilityOrderTemplate.getType());
            Graphics g = abilityImage.getGraphics();
            g.drawImage(abilityOrderTemplate, 0, 0, null);
            g = abilityImage.getGraphics();

            HashMap<Ability, Integer> seen = new HashMap<>();

            int y = 74;
            for(int i = 0; i < abilityOrder.length; i++) {
                Ability a = abilityOrder[i];
                if(!seen.containsKey(a)) {
                    seen.put(a, y);
                    g.drawImage(a.getAbilityImage(), 0, y, null);
                    y += 74;
                }
                g.drawImage(a.getButtonImage(), (i + 1) * 74, seen.get(a), null);
            }

            g = resized.getGraphics();
            g.drawImage(abilityImage, 0, 0, width, height, null);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return resized;
    }

    /**
     * Build an image displaying the provided summoner spells
     *
     * @param spells Summoner spells
     * @return Image displaying summoner spells
     */
    private BufferedImage getSpellsImage(SummonerSpell[] spells) {
        BufferedImage spellsImage = new BufferedImage(138, 64, BufferedImage.TYPE_INT_ARGB);
        try {
            Graphics g = spellsImage.getGraphics();
            int x = 0;
            for(SummonerSpell spell : spells) {
                g.drawImage(spell.getSpellImage(), x, 0, null);
                x += 74;
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return spellsImage;
    }

    /**
     * Build an image displaying the provided runes
     *
     * @param runes Runes
     * @return Image displaying summoner runes
     */
    private BufferedImage getRuneImage(Rune[] runes) {
        BufferedImage runeImage = new BufferedImage(434, 138, BufferedImage.TYPE_INT_ARGB);
        try {
            Graphics g = runeImage.getGraphics();
            int x = 32, y = 0;
            for(int i = 0; i < runes.length; i++) {
                Rune rune = runes[i];
                if(rune.isKeyRune() && i > 0) {
                    x = 0;
                    y += 74;
                }
                g.drawImage(rune.getImage(), x, y, null);
                x += 74;
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return runeImage;
    }

    /**
     * Build an image displaying a list of items
     *
     * @param items     Items to display
     * @param buildPath Display an arrow indicating order of building items
     * @return Image displaying items
     */
    private BufferedImage getItemImage(Item[] items, boolean buildPath) {
        int rowWidth = Math.min(5, items.length);
        int rows = (int) (Math.ceil(items.length / (double) rowWidth));

        int width = buildPath ? (rowWidth * 96) - 32 : (rowWidth * 74) - 10;
        int height = rows > 1 ? (rows * 74) - 10 : 64;

        BufferedImage itemImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        try {
            Graphics g = itemImage.getGraphics();
            int x = 0, y = 0;
            for(int i = 0; i < items.length; i++) {
                g.drawImage(items[i].getItemImage(), x, y, null);
                if(buildPath && i < items.length - 1) {
                    g.drawImage(next, x + 64, y + 16, null);
                }
                x += buildPath ? 96 : 74;
                if((i + 1) % rowWidth == 0) {
                    x = 0;
                    y += 74;
                }
            }
            g.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return itemImage;
    }
}
