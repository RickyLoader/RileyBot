package Command.Commands.COD;

import COD.Assets.Attachment;
import COD.Assets.Perk;
import COD.Assets.TacticalWeapon;
import COD.Assets.Weapon;
import COD.Match.Loadout;
import COD.Match.LoadoutWeapon;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.ImageLoadingMessage;
import Command.Structure.MatchHistoryCommand;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.image.BufferedImage;
import java.util.*;

import static Bot.DiscordCommandManager.mwAssetManager;

/**
 * Get a random MW loadout
 */
public class MWRandomCommand extends DiscordCommand {
    public MWRandomCommand() {
        super("mwrandom", "Generate a random Modern Warfare loadout!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        BufferedImage image = MatchHistoryCommand.loadoutImageManager.buildLoadoutImage(
                generateRandomLoadout(),
                "Random Loadout -  " + context.getMember().getEffectiveName()
        );
        channel.sendFile(ImageLoadingMessage.imageToByteArray(image), "loadout.png").queue();
    }

    /**
     * Generate a random loadout
     *
     * @return Random loadout
     */
    private Loadout generateRandomLoadout() {
        Perk bluePerk = getRandomPerk(Perk.CATEGORY.BLUE);
        Perk redPerk = getRandomPerk(Perk.CATEGORY.RED);
        Perk yellowPerk = getRandomPerk(Perk.CATEGORY.YELLOW);
        boolean overkill = redPerk.getName().equalsIgnoreCase("overkill");

        Weapon primary = getRandomWeapon(Weapon.TYPE.PRIMARY, null);
        Weapon secondary = getRandomWeapon(overkill ? Weapon.TYPE.PRIMARY : Weapon.TYPE.SECONDARY, primary);
        return new Loadout.LoadoutBuilder()
                .setPrimaryWeapon(getLoadoutWeapon(primary))
                .setSecondaryWeapon(getLoadoutWeapon(secondary))
                .setTacticalEquipment((TacticalWeapon) getRandomWeapon(Weapon.TYPE.TACTICAL, null))
                .setLethalEquipment(getRandomWeapon(Weapon.TYPE.LETHAL, null))
                .setPerks(
                        new Perk[]{
                                bluePerk,
                                redPerk,
                                yellowPerk
                        }
                )
                .build();
    }

    /**
     * Get a random perk of the given colour
     *
     * @param colour Perk colour
     * @return Random perk of given colour
     */
    private Perk getRandomPerk(Perk.CATEGORY colour) {
        Perk[] perks = mwAssetManager.getPerksByColour(colour);
        if(colour == Perk.CATEGORY.BLUE) {
            perks = Arrays.stream(perks)
                    .filter(perk -> !perk.getName().equalsIgnoreCase("gunfight e.o.d"))
                    .toArray(Perk[]::new);
        }
        return perks[new Random().nextInt(perks.length)];
    }

    /**
     * Create a loadout weapon from the given weapon.
     * Add 1 - 5 random attachments (if the weapon has equipable attachments)
     *
     * @param weapon Weapon to create loadout weapon from
     * @return Loadout weapon
     */
    private LoadoutWeapon getLoadoutWeapon(Weapon weapon) {
        if(!weapon.hasEquipableAttachments()) {
            return new LoadoutWeapon(weapon, new ArrayList<>(), 0);
        }
        ArrayList<Attachment.CATEGORY> availableCategories = new ArrayList<>(
                Arrays.asList(weapon.getAttachmentCategories())
        );
        Random rand = new Random();
        int toEquip = rand.nextInt(5) + 1;
        HashMap<Attachment.CATEGORY, Attachment> equipped = new HashMap<>();

        while(equipped.size() < toEquip) {
            Attachment.CATEGORY category = availableCategories.get(rand.nextInt(availableCategories.size()));
            Attachment[] categoryAttachments = weapon.getAttachmentsByCategory(category);
            Attachment attachment = categoryAttachments[rand.nextInt(categoryAttachments.length)];

            if(attachment.blocksCategory()) {
                Attachment.CATEGORY blocked = attachment.getBlockedCategory();
                if(equipped.containsKey(blocked)) {
                    continue;
                }
                availableCategories.remove(blocked);
            }
            equipped.put(category, attachment);
            availableCategories.remove(category);
        }

        return new LoadoutWeapon(weapon, new ArrayList<>(equipped.values()), 0);
    }

    /**
     * Get a random weapon of the given type
     *
     * @param type    Weapon type - e.g PRIMARY
     * @param exclude Weapon to exclude
     * @return Random weapon of the given type
     */
    private Weapon getRandomWeapon(Weapon.TYPE type, Weapon exclude) {
        Weapon.CATEGORY[] typeCategories = type.getCategories();
        Random rand = new Random();
        Weapon.CATEGORY category = typeCategories[rand.nextInt(typeCategories.length)];
        Weapon[] categoryWeapons = Arrays.stream(mwAssetManager.getWeaponsByCategory(category))
                .filter(weapon -> !weapon.equals(exclude) && !weapon.getName().equalsIgnoreCase("fists"))
                .toArray(Weapon[]::new);
        return categoryWeapons[rand.nextInt(categoryWeapons.length)];
    }
}
