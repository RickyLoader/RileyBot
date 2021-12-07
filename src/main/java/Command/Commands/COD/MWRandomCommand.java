package Command.Commands.COD;

import COD.Assets.*;
import COD.Loadouts.LoadoutAnalysis;
import COD.API.MWManager;
import COD.Loadouts.LoadoutImageManager;
import COD.Match.Loadout;
import COD.Match.LoadoutWeapon;
import Command.Structure.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;

import java.util.*;

/**
 * Get a random MW loadout
 */
public class MWRandomCommand extends DiscordCommand {
    private final ArrayList<String> words;
    private final HashSet<String> excludedWeaponIds;
    private final Random rand;
    private static final String TRIGGER = "mwrandom";
    private static final int MAX_ATTACHMENTS = 5;

    public MWRandomCommand() {
        super(TRIGGER, "Generate a random Modern Warfare loadout!", TRIGGER + "\n" + TRIGGER + " [#]");
        this.words = readWords();
        this.excludedWeaponIds = new HashSet<>(
                Arrays.asList(
                        "iw8_fists", // Fists - Unequippable
                        "iw8_la_mike32", // MGL-32 Grenade Launcher - Only equippable in spec ops
                        "iw8_lm_slima", // Unreleased LMG

                        // Purchasable Throwing knife variants
                        "equip_throwing_knife_drill",
                        "equip_throwing_knife_fire",
                        "equip_throwing_knife_electric"
                )
        );
        this.rand = new Random();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        channel.sendTyping().queue();

        int goalAttachments = toInteger(
                context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim()
        );

        String loadoutName = generateLoadoutName();
        Loadout loadout = goalAttachments > 0 ? generateRandomLoadout(goalAttachments) : generateRandomLoadout();
        byte[] loadoutImage = buildLoadoutImage(loadout, loadoutName, member);
        String imageName = System.currentTimeMillis() + ".png";
        channel.sendMessage(
                buildLoadoutEmbed(member, loadout, imageName)
        ).addFile(loadoutImage, imageName).queue();
    }

    /**
     * Read the list of words to be used for loadout name generation from the JSON
     * file
     *
     * @return List of words
     */
    private ArrayList<String> readWords() {
        JSONArray wordList = readJSONFile("/COD/MW/Data/loadout_names.json").getJSONArray("words");
        ArrayList<String> words = new ArrayList<>();
        for(int i = 0; i < wordList.length(); i++) {
            words.add(wordList.getString(i));
        }
        return words;
    }

    /**
     * Generate a random loadout name from the list of words
     *
     * @return Random loadout name
     */
    private String generateLoadoutName() {
        return "The " + getRandomWord() + " " + getRandomWord();
    }

    /**
     * Get a random word from the list of words
     *
     * @return Random word
     */
    private String getRandomWord() {
        return words.get(rand.nextInt(words.size()));
    }

    /**
     * Build an image displaying the given loadout
     *
     * @param loadout     Loadout to build image for
     * @param loadoutName Name for loadout
     * @param member      Member who requested loadout (for loadout title)
     * @return Image displaying loadout
     */
    private byte[] buildLoadoutImage(Loadout loadout, String loadoutName, Member member) {
        return ImageLoadingMessage.imageToByteArray(
                LoadoutImageManager.getInstance().buildLoadoutImage(
                        loadout,
                        loadoutName + " | " + member.getEffectiveName()
                )
        );
    }

    /**
     * Build an embed displaying the randomly generated loadout
     *
     * @param member    Member who requested loadout
     * @param loadout   Randomly generated loadout
     * @param imageName File image name (for attaching)
     * @return Message embed displaying random loadout
     */
    private MessageEmbed buildLoadoutEmbed(Member member, Loadout loadout, String imageName) {
        return new EmbedBuilder()
                .setTitle("MW Random Loadout | " + member.getEffectiveName())
                .setDescription(member.getAsMention() + " " + new LoadoutAnalysis(loadout).getRandomAnalysis())
                .setThumbnail("https://i.imgur.com/x9ziS9u.png")
                .setColor(EmbedHelper.FIRE_ORANGE)
                .setImage("attachment://" + imageName)
                .setFooter(
                        "Try: " + getHelpName().replace("\n", " | "),
                        "https://i.imgur.com/rNkulfS.png"
                )
                .build();
    }

    /**
     * Generate a random loadout
     *
     * @param goalAttachments Optional number of attachments to aim for on each weapon (not guaranteed)
     * @return Random loadout
     */
    private Loadout generateRandomLoadout(Integer... goalAttachments) {
        Perk bluePerk = getRandomPerk(Perk.CATEGORY.BLUE);
        Perk redPerk = getRandomPerk(Perk.CATEGORY.RED);
        Perk yellowPerk = getRandomPerk(Perk.CATEGORY.YELLOW);
        boolean overkill = redPerk.getName().equalsIgnoreCase("overkill");

        Weapon primary = getRandomWeapon(Weapon.TYPE.PRIMARY, null);
        Weapon secondary = getRandomWeapon(overkill ? Weapon.TYPE.PRIMARY : Weapon.TYPE.SECONDARY, primary);
        return new Loadout.LoadoutBuilder()
                .setPrimaryWeapon(getLoadoutWeapon(primary, goalAttachments))
                .setSecondaryWeapon(getLoadoutWeapon(secondary, goalAttachments))
                .setTacticalEquipment((TacticalWeapon) getRandomWeapon(Weapon.TYPE.TACTICAL, null))
                .setLethalEquipment(getRandomWeapon(Weapon.TYPE.LETHAL, null))
                .setPerks(
                        new Perk[]{
                                bluePerk,
                                redPerk,
                                yellowPerk
                        }
                )
                .setFieldUpgrades(getFieldUpgrades())
                .build();
    }

    /**
     * Get the field upgrade(s) for the loadout
     * Roll a 1/10 chance of having 2 field upgrades
     *
     * @return Field upgrade(s)
     */
    private FieldUpgrade[] getFieldUpgrades() {
        int roll = rand.nextInt(10);
        FieldUpgrade[] fieldUpgrades = new FieldUpgrade[roll == 9 ? 2 : 1];
        FieldUpgrade first = getRandomFieldUpgrade(null);
        fieldUpgrades[0] = first;
        if(fieldUpgrades.length == 2) {
            fieldUpgrades[1] = getRandomFieldUpgrade(first);
        }
        return fieldUpgrades;
    }

    /**
     * Get a random field upgrade
     *
     * @param exclude Field upgrade to exclude
     * @return Random field upgrade
     */
    private FieldUpgrade getRandomFieldUpgrade(FieldUpgrade exclude) {
        FieldUpgrade[] fieldUpgrades = Arrays.stream(MWManager.getInstance().getSupers())
                .filter(fieldUpgrade -> !fieldUpgrade.equals(exclude))
                .toArray(FieldUpgrade[]::new);
        return fieldUpgrades[rand.nextInt(fieldUpgrades.length)];
    }

    /**
     * Get a random perk of the given colour
     *
     * @param colour Perk colour
     * @return Random perk of given colour
     */
    private Perk getRandomPerk(Perk.CATEGORY colour) {
        Perk[] perks = MWManager.getInstance().getPerksByColour(colour);
        if(colour == Perk.CATEGORY.BLUE) {
            perks = Arrays.stream(perks)
                    .filter(perk -> !perk.getName().equalsIgnoreCase("gunfight e.o.d"))
                    .toArray(Perk[]::new);
        }
        return perks[rand.nextInt(perks.length)];
    }

    /**
     * Create a loadout weapon from the given weapon.
     * If {@code goalAttachments} is provided, aim to add this many random attachments
     * (may not be possible as some attachments block other categories).
     * Otherwise, add 1 - {@link MWRandomCommand#MAX_ATTACHMENTS} random attachments.
     *
     * @param weapon          Weapon to create loadout weapon from
     * @param goalAttachments Optional number of attachments to aim for on each weapon (not guaranteed)
     * @return Loadout weapon
     */
    private LoadoutWeapon getLoadoutWeapon(Weapon weapon, Integer... goalAttachments) {
        if(!weapon.hasEquipableAttachments()) {
            return new LoadoutWeapon(weapon, new ArrayList<>(), 0);
        }
        ArrayList<Attachment.CATEGORY> availableCategories = new ArrayList<>(
                Arrays.asList(weapon.getAttachmentCategories())
        );

        final int toEquip = goalAttachments.length > 0 ? goalAttachments[0] : rand.nextInt(MAX_ATTACHMENTS) + 1;
        HashMap<Attachment.CATEGORY, Attachment> equipped = new HashMap<>();

        while(equipped.size() < toEquip && !availableCategories.isEmpty()) {
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
        Weapon.CATEGORY category = typeCategories[rand.nextInt(typeCategories.length)];
        Weapon[] categoryWeapons = Arrays.stream(MWManager.getInstance().getWeaponsByCategory(category))
                .filter(weapon -> !weapon.equals(exclude) && !excludedWeaponIds.contains(weapon.getCodename()))
                .toArray(Weapon[]::new);
        return categoryWeapons[rand.nextInt(categoryWeapons.length)];
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
