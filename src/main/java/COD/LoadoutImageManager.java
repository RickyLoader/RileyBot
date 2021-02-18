package COD;

import Bot.FontManager;
import Bot.ResourceHandler;
import COD.Assets.Attachment;
import COD.Assets.Attributes;
import COD.Assets.Attributes.Attribute;
import COD.Assets.Perk;
import COD.Assets.Weapon;
import COD.Match.Loadout;
import COD.Match.LoadoutWeapon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Build COD loadout images
 */
public class LoadoutImageManager {
    private final BufferedImage
            WEAPON_SECTION,
            WEAPON_NAME_SECTION,
            WEAPON_CONTAINER,
            LOADOUT_HEADER,
            ATTACHMENT_SECTION,
            ATTACHMENTS_CONTAINER,
            ATTACHMENT_NAME_SECTION,
            EQUIPMENT_NAME_SECTION,
            EQUIPMENT_SECTION,
            EQUIPMENT_CONTAINER,
            PERKS_CONTAINER,
            UNKNOWN_WEAPON,
            UNKNOWN_ATTACHMENT,
            UNKNOWN_EQUIPMENT;

    public LoadoutImageManager() {
        ResourceHandler handler = new ResourceHandler();
        String basePath = "/COD/MW/Templates/Loadout/";
        this.LOADOUT_HEADER = handler.getImageResource(basePath + "loadout_header.png");
        this.WEAPON_SECTION = handler.getImageResource(basePath + "weapon_section.png");
        this.WEAPON_CONTAINER = handler.getImageResource(basePath + "weapon_container.png");
        this.WEAPON_NAME_SECTION = handler.getImageResource(basePath + "weapon_name_section.png");
        this.ATTACHMENT_SECTION = handler.getImageResource(basePath + "attachment_section.png");
        this.ATTACHMENTS_CONTAINER = handler.getImageResource(basePath + "attachments_container.png");
        this.ATTACHMENT_NAME_SECTION = handler.getImageResource(basePath + "attachment_name_section.png");
        this.EQUIPMENT_SECTION = handler.getImageResource(basePath + "equipment_section.png");
        this.EQUIPMENT_NAME_SECTION = handler.getImageResource(basePath + "equipment_name_section.png");
        this.EQUIPMENT_CONTAINER = handler.getImageResource(basePath + "equipment_container.png");
        this.PERKS_CONTAINER = handler.getImageResource(basePath + "perks_container.png");
        this.UNKNOWN_WEAPON = handler.getImageResource(basePath + "unknown_weapon.png");
        this.UNKNOWN_ATTACHMENT = handler.getImageResource(basePath + "unknown_attachment.png");
        this.UNKNOWN_EQUIPMENT = handler.getImageResource(basePath + "unknown_equipment.png");
    }

    /**
     * Build an image displaying the given loadout.
     * Display weapons, attachments, and equipment
     *
     * @param loadout Loadout to build image for
     * @param title   Title to display above loadout
     * @return Image displaying loadout
     */
    public BufferedImage buildLoadoutImage(Loadout loadout, String title) {
        BufferedImage header = buildHeaderImage(title);
        BufferedImage primary = buildWeaponImage(loadout.getPrimary());
        BufferedImage secondary = buildWeaponImage(loadout.getSecondary());
        BufferedImage lethal = buildEquipmentImage(loadout.getLethal());
        BufferedImage tactical = buildEquipmentImage(loadout.getTactical());
        BufferedImage perks = buildPerksImage(loadout.getPerks());

        BufferedImage container = new BufferedImage(
                header.getWidth(),
                header.getHeight()
                        + primary.getHeight()
                        + secondary.getHeight()
                        + lethal.getHeight()
                        + perks.getHeight()
                        + 40,
                BufferedImage.TYPE_INT_RGB
        );
        Graphics g = container.getGraphics();
        int x = 0, y = 0;

        g.drawImage(header, x, y, null);
        y += header.getHeight() + 10;

        g.drawImage(primary, x, y, null);
        y += primary.getHeight() + 10;

        g.drawImage(secondary, x, y, null);
        y += secondary.getHeight() + 10;

        g.drawImage(lethal, x, y, null);
        g.drawImage(tactical, x + lethal.getWidth() + 10, y, null);
        y += tactical.getHeight() + 10;

        g.drawImage(perks, x, y, null);
        g.dispose();
        return container;
    }

    /**
     * Build the header image to display at the top of the loadout
     *
     * @param title Text to display
     * @return Header image with title
     */
    private BufferedImage buildHeaderImage(String title) {
        BufferedImage header = copyImage(LOADOUT_HEADER);
        Graphics g = header.getGraphics();
        g.setFont(FontManager.MODERN_WARFARE_FONT.deriveFont(50f));
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                title,
                (header.getWidth() / 2) - (fm.stringWidth(title) / 2),
                (header.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return header;
    }

    /**
     * Build an image displaying the given equipment and its name
     *
     * @param equipment Equipment to draw
     * @return Image displaying equipment
     */
    private BufferedImage buildEquipmentImage(Weapon equipment) {
        if(equipment == null) {
            return EQUIPMENT_CONTAINER;
        }
        return buildLabelledImage(
                equipment.getImage(),
                equipment.getName(),
                equipment.getType() == Weapon.TYPE.UNKNOWN ? UNKNOWN_EQUIPMENT : EQUIPMENT_SECTION,
                EQUIPMENT_NAME_SECTION
        );
    }

    /**
     * Build an image displaying the loadout perks
     *
     * @param perks Array of perks to display
     * @return Image displaying perks
     */
    private BufferedImage buildPerksImage(Perk[] perks) {
        BufferedImage perksImage = copyImage(PERKS_CONTAINER);
        int x = 0;
        Graphics g = perksImage.getGraphics();
        for(Perk perk : perks) {
            BufferedImage perkImage = buildLabelledImage(
                    perk.getImage(),
                    perk.getName(),
                    perk.getCategory() == Perk.CATEGORY.UNKNOWN ? UNKNOWN_ATTACHMENT : ATTACHMENT_SECTION,
                    ATTACHMENT_NAME_SECTION
            );
            g.drawImage(perkImage, x, 0, null);
            x += perkImage.getWidth() + 10;
        }
        g.dispose();
        return perksImage;
    }

    /**
     * Build an image displaying the given list of weapon attachments
     *
     * @param attachments List of attachments to display
     * @param attributes  Weapon attributes (effect of attachments) to display
     * @return Image displaying weapon attachments
     */
    private BufferedImage buildAttachmentsImage(ArrayList<Attachment> attachments, Attributes attributes) {
        BufferedImage image = copyImage(ATTACHMENTS_CONTAINER);
        Graphics g = image.getGraphics();
        int x = 0, y = 0;
        for(int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            BufferedImage attachmentImage = buildLabelledImage(
                    attachment.getCategory() == Attachment.CATEGORY.UNKNOWN ? UNKNOWN_ATTACHMENT : attachment.getImage(),
                    attachment.getName(),
                    ATTACHMENT_SECTION,
                    ATTACHMENT_NAME_SECTION
            );
            g.drawImage(attachmentImage, x, y, null);
            if((i + 1) % 3 == 0) {
                y += attachmentImage.getHeight() + 10;
                x = 0;
            }
            else {
                x += attachmentImage.getWidth() + 10;
            }
        }
        BufferedImage attributesImage = buildAttributesImage(attributes);
        g.drawImage(
                attributesImage,
                ATTACHMENTS_CONTAINER.getWidth() - attributesImage.getWidth(),
                ATTACHMENTS_CONTAINER.getHeight() - attributesImage.getHeight(),
                null
        );
        g.dispose();
        return image;
    }

    /**
     * Build an image displaying the given weapon attributes
     *
     * @param attributes Weapon attributes (effect of attachments)
     * @return Image displaying attributes
     */
    private BufferedImage buildAttributesImage(Attributes attributes) {
        BufferedImage image = new BufferedImage(
                ATTACHMENT_SECTION.getWidth(),
                ATTACHMENT_SECTION.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        int blank = 20;
        int gap = blank / 5;
        int sectionHeight = (image.getHeight() - blank) / 6;
        int width = image.getWidth();

        Graphics g = image.getGraphics();
        int y = 0;
        ArrayList<Attribute> availableAttributes = attributes.getAttributes();
        for(Attribute attribute : availableAttributes) {
            g.drawImage(
                    buildAttributeImage(attribute, width, sectionHeight),
                    0,
                    y,
                    null
            );
            y += sectionHeight + gap;
        }
        return labelContainerImage(image, buildLabelImage("Stats", ATTACHMENT_NAME_SECTION));
    }

    /**
     * Build an image for the given attribute.
     * Set the colour based on the attribute being positive/negative/zero
     *
     * @param attribute Attribute to draw
     * @param width     Image width
     * @param height    Image height
     */
    private BufferedImage buildAttributeImage(Attribute attribute, int width, int height) {
        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = image.getGraphics();
        g.setColor(getAttributeColour(attribute.getValue()));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.BLACK);
        g.setFont(FontManager.MODERN_WARFARE_FONT.deriveFont(20f));
        FontMetrics fm = g.getFontMetrics();

        int border = 10;
        int mid = (image.getHeight() / 2) + (fm.getMaxAscent() / 2);

        g.drawString(attribute.getName(), border, mid);
        String text = attribute.formatValue();
        g.drawString(text, image.getWidth() - fm.stringWidth(text) - border, mid);
        g.dispose();
        return image;
    }

    /**
     * Get the colour to use when drawing the given attribute based on it being positive/negative/zero
     *
     * @param attribute Attribute to get colour for
     * @return Attribute colour
     */
    private Color getAttributeColour(int attribute) {
        if(attribute == 0) {
            return Color.WHITE;
        }
        return attribute > 0 ? new Color(40, 245, 150) : new Color(250, 100, 100);
    }

    /**
     * Build a labelled image. The provided image is drawn centered in a container
     * with the name written in a separate container below.
     *
     * @param image          Image to draw
     * @param label          Label to write
     * @param imageContainer Image to draw image inside
     * @param labelContainer Image to draw label inside
     * @return Image with label
     */
    private BufferedImage buildLabelledImage(BufferedImage image, String label, BufferedImage imageContainer, BufferedImage labelContainer) {
        BufferedImage title = buildLabelImage(label, labelContainer);
        BufferedImage container = copyImage(imageContainer);
        Graphics g = container.getGraphics();
        g.drawImage(
                image,
                (container.getWidth() / 2) - (image.getWidth() / 2),
                (container.getHeight() / 2) - (image.getHeight() / 2),
                null
        );
        g.dispose();
        return labelContainerImage(container, title);
    }

    /**
     * Add the given image label underneath the container image
     *
     * @param container Image to label
     * @param label     Label image
     * @return Container image with label underneath
     */
    private BufferedImage labelContainerImage(BufferedImage container, BufferedImage label) {
        BufferedImage background = new BufferedImage(
                container.getWidth(),
                container.getHeight() + 10 + label.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = background.getGraphics();
        g.drawImage(container, 0, 0, null);
        g.drawImage(label, 0, container.getHeight() + 10, null);
        g.dispose();
        return background;
    }

    /**
     * Build a label image.
     * Draw the given label on to a copy of the given label container image.
     *
     * @param label          Text to draw
     * @param labelContainer Image to copy and draw text on to
     * @return Label image
     */
    private BufferedImage buildLabelImage(String label, BufferedImage labelContainer) {
        BufferedImage title = copyImage(labelContainer);
        Graphics g = title.getGraphics();
        g.setFont(FontManager.MODERN_WARFARE_FONT.deriveFont(30f));
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                label,
                (title.getWidth() / 2) - (fm.stringWidth(label) / 2),
                (title.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return title;
    }

    /**
     * Build an image displaying the given loadout weapon with its name
     * and attachments
     *
     * @param loadoutWeapon Loadout weapon to draw
     * @return Image displaying weapon
     */
    private BufferedImage buildWeaponImage(LoadoutWeapon loadoutWeapon) {
        if(loadoutWeapon == null) {
            return WEAPON_CONTAINER;
        }
        Weapon weapon = loadoutWeapon.getWeapon();
        BufferedImage weaponImage = buildLabelledImage(
                weapon.getImage(),
                weapon.getName(),
                weapon.getType() == Weapon.TYPE.UNKNOWN ? UNKNOWN_WEAPON : WEAPON_SECTION,
                WEAPON_NAME_SECTION
        );

        // Don't show empty attachment slots under a launcher/knife
        if(!weapon.hasEquipableAttachments()) {
            return weaponImage;
        }
        BufferedImage attachmentImage = buildAttachmentsImage(
                loadoutWeapon.getAttachments(),
                loadoutWeapon.getAttributes()
        );
        BufferedImage container = new BufferedImage(
                weaponImage.getWidth(),
                weaponImage.getHeight() + 10 + attachmentImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = container.getGraphics();
        g.drawImage(weaponImage, 0, 0, null);
        g.drawImage(attachmentImage, 0, weaponImage.getHeight() + 10, null);
        g.dispose();
        return container;
    }

    /**
     * Create a copy of the given image
     *
     * @param source Source to copy
     * @return Copy of source image
     */
    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = copy.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }
}
