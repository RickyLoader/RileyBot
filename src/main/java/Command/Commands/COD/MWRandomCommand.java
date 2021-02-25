package Command.Commands.COD;

import COD.Assets.Attachment;
import COD.Assets.Perk;
import COD.Assets.TacticalWeapon;
import COD.Assets.Weapon;
import COD.LoadoutAnalysis;
import COD.Match.Loadout;
import COD.Match.LoadoutWeapon;
import Command.Structure.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;

import java.util.*;

import static Bot.DiscordCommandManager.mwAssetManager;

/**
 * Get a random MW loadout
 */
public class MWRandomCommand extends DiscordCommand {
    private final ArrayList<String> words;
    private final HashSet<String> excludedWeaponIds;

    public MWRandomCommand() {
        super("mwrandom", "Generate a random Modern Warfare loadout!");
        this.words = readWords();
        this.excludedWeaponIds = new HashSet<>(
                Arrays.asList("iw8_fists", "iw8_la_mike32", "iw8_sm_secho", "iw8_pi_mike", "iw8_lm_slima")
        );
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        channel.sendTyping().queue();

        String loadoutName = generateLoadoutName();
        Loadout loadout = generateRandomLoadout();
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
        return words.get(new Random().nextInt(words.size()));
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
                MatchHistoryCommand.loadoutImageManager.buildLoadoutImage(
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
                .setFooter("Try: " + getTrigger(), "https://i.imgur.com/rNkulfS.png")
                .build();
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
        Random rand = new Random();
        Weapon.CATEGORY category = typeCategories[rand.nextInt(typeCategories.length)];
        Weapon[] categoryWeapons = Arrays.stream(mwAssetManager.getWeaponsByCategory(category))
                .filter(weapon -> !weapon.equals(exclude) && !excludedWeaponIds.contains(weapon.getCodename()))
                .toArray(Weapon[]::new);
        return categoryWeapons[rand.nextInt(categoryWeapons.length)];
    }
}
