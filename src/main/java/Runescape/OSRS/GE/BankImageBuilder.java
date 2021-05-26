package Runescape.OSRS.GE;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Commands.OSRSLendingCommand;
import Command.Commands.OSRSLendingCommand.QUANTITY_ABBREVIATION;
import Command.Structure.EmbedHelper;
import Runescape.OSRS.Loan.ItemQuantity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static Command.Structure.ImageBuilder.copyImage;

/**
 * Build an image displaying a list of OSRS items in a bank
 */
public class BankImageBuilder {
    private final int border = 6, itemContainerDimension = 30, itemStartY = 35, columnRows = 8;
    private final BufferedImage bankImage, coinsImage;


    /**
     * Initialise the empty bank image & coin image
     */
    public BankImageBuilder() {
        String path = "/Runescape/OSRS/";
        ResourceHandler handler = new ResourceHandler();
        this.bankImage = handler.getImageResource(path + "Templates/empty_bank.png");
        this.coinsImage = handler.getImageResource(path + "Sprites/coins.png");
    }

    /**
     * Get the max number of items that can be displayed in the bank
     *
     * @return Max number of items
     */
    public int getMaxItems() {
        return columnRows * columnRows;
    }

    /**
     * Build an image displaying the given OSRS items in a bank
     *
     * @param title Title to display at the top of the bank
     * @param items Items to draw
     * @param coins Coins to draw
     * @return Image displaying the given items in a bank
     */
    public BufferedImage buildImage(String title, ArrayList<ItemQuantity> items, int coins) {
        BufferedImage bankImage = copyImage(this.bankImage);
        drawTitle(bankImage, title);
        drawItems(bankImage, items, coins);
        return bankImage;
    }

    /**
     * Build an image displaying the given item sprite and quantity (if more than 1)
     *
     * @param sprite   Item sprite image
     * @param quantity Item quantity
     * @param coins    Item is coins
     * @return Image displaying item
     */
    private BufferedImage buildItemImage(BufferedImage sprite, int quantity, boolean coins) {
        BufferedImage container = new BufferedImage(
                itemContainerDimension,
                itemContainerDimension,
                BufferedImage.TYPE_INT_ARGB
        );

        int midpoint = itemContainerDimension / 2;
        Graphics g = container.getGraphics();

        g.drawImage(
                sprite,
                midpoint - (sprite.getWidth() / 2),
                midpoint - (sprite.getHeight() / 2),
                null
        );

        if(quantity > 1) {
            g.setFont(FontManager.OSRS_BANK_FONT.deriveFont(12f));
            QUANTITY_ABBREVIATION abbreviation = QUANTITY_ABBREVIATION.forQuantity(quantity);

            String quantityText = OSRSLendingCommand.formatQuantity(quantity, abbreviation, coins);

            drawShadowedString(
                    g,
                    quantityText,
                    0,
                    g.getFontMetrics().getMaxAscent(),
                    abbreviation.getColour()
            );
        }

        g.dispose();
        return container;
    }

    /**
     * Draw the given list of items and their quantities on to the bank image
     *
     * @param bankImage Bank image
     * @param items     Items to draw
     * @param coins     Coins to draw
     */
    private void drawItems(BufferedImage bankImage, ArrayList<ItemQuantity> items, int coins) {
        Graphics g = bankImage.getGraphics();

        int gapDivisor = columnRows + 1;
        int usedByItemImages = columnRows * itemContainerDimension;

        int xGap = (bankImage.getWidth() - usedByItemImages - (2 * border)) / gapDivisor;
        int yGap = (bankImage.getHeight() - usedByItemImages - itemStartY - border) / gapDivisor;

        int startX = border + xGap;
        int x = startX;
        int y = itemStartY + yGap;

        for(int i = 0; i < Math.min(items.size(), getMaxItems()); i++) {
            ItemQuantity itemQuantity = items.get(i);
            g.drawImage(
                    buildItemImage(
                            itemQuantity.getItem().getItemImage().getInventoryImage(),
                            itemQuantity.getQuantity(),
                            false
                    ),
                    x,
                    y,
                    null
            );

            if((i + 1) % columnRows == 0) {
                x = startX;
                y = y + itemContainerDimension + yGap;
            }
            else {
                x += xGap + itemContainerDimension;
            }
        }
        if(coins > 0) {
            g.drawImage(buildItemImage(coinsImage, coins, true), x, y, null);
        }
        g.dispose();
    }

    /**
     * Draw the given title on to the given bank image.
     *
     * @param bankImage Bank image
     * @param title     Title to draw
     */
    private void drawTitle(BufferedImage bankImage, String title) {
        Graphics g = bankImage.getGraphics();
        g.setFont(FontManager.OSRS_BANK_FONT.deriveFont(16f));
        FontMetrics fm = g.getFontMetrics();
        int titleHeight = itemStartY - border * 2;

        drawShadowedString(
                g,
                title,
                (bankImage.getWidth() / 2) - (fm.stringWidth(title) / 2),
                (titleHeight / 2) + (fm.getMaxAscent() / 2) + border,
                new Color(EmbedHelper.OSRS_BANK_TITLE)
        );
        g.dispose();
    }

    /**
     * Draw the given String with an offset drop shadow using the given Graphics instance.
     *
     * @param g      Graphics instance to use
     * @param string String to draw
     * @param x      X coordinate for String
     * @param y      Y coordinate for String
     * @param color  Colour to use (Shadow will be black)
     */
    private void drawShadowedString(Graphics g, String string, int x, int y, Color color) {
        int offset = 1;

        // Drop shadow
        g.setColor(Color.BLACK);
        g.drawString(string, x + offset, y + offset);

        // Text
        g.setColor(color);
        g.drawString(string, x, y);
    }
}
