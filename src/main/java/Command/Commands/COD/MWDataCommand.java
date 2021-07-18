package Command.Commands.COD;

import COD.Assets.Weapon;
import COD.Gunfight.Gunfight;
import COD.API.MWManager;
import Command.Structure.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * View Modern Warfare weapons
 */
public class MWDataCommand extends DiscordCommand {
    private final HashMap<String, Weapon.CATEGORY> categories = new HashMap<>();
    private final String helpMessage;

    public MWDataCommand() {
        super("mwdata [weapon name/category]", "Have a look at Modern Warfare weapons!");
        this.helpMessage = "Type: " + getTrigger() + " for help";
        addCategories();
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace("mwdata", "").trim();
        MessageChannel channel = context.getMessageChannel();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        Weapon.CATEGORY weaponCategory = categories.getOrDefault(message, Weapon.CATEGORY.UNKNOWN);
        if(weaponCategory != Weapon.CATEGORY.UNKNOWN) {
            Weapon[] categoryWeapons = MWManager.getInstance().getWeaponsByCategory(weaponCategory);
            showPageableWeapons(
                    context,
                    categoryWeapons,
                    "Category: " + weaponCategory.name() + " (" + categoryWeapons.length + " Items)"
            );
            return;
        }

        Weapon[] searchResults = MWManager.getInstance().getWeaponsByName(message);

        if(searchResults.length == 1) {
            channel.sendMessage(buildWeaponEmbed(searchResults[0])).queue();
            return;
        }
        showPageableWeapons(
                context,
                searchResults,
                "Weapon Search: " + message + " (" + searchResults.length + " Results)"
        );
    }

    /**
     * Build a message embed displaying the given weapon
     *
     * @param weapon Weapon to display
     * @return Message embed displaying weapon
     */
    private MessageEmbed buildWeaponEmbed(Weapon weapon) {
        Weapon.CATEGORY category = weapon.getCategory();
        return new EmbedBuilder()
                .setThumbnail(Gunfight.THUMBNAIL)
                .setImage(weapon.getImageURL())
                .setColor(EmbedHelper.GREEN)
                .setTitle(weapon.getName())
                .setDescription(
                        "**Codename**: " + weapon.getCodename()
                                + "\n**Category**: " + category.name() + " (" + category.getCodename() + ")"
                )
                .setFooter("Try: " + getHelpName())
                .build();
    }

    /**
     * Build a pageable embed showing the weapons in the given weapon category
     *
     * @param context Command context
     * @param weapons Array of weapons to display
     * @param title   Embed title to use
     */
    private void showPageableWeapons(CommandContext context, Weapon[] weapons, String title) {
        new PageableSortEmbed<Weapon>(
                context,
                Arrays.asList(weapons),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder()
                        .setColor(EmbedHelper.PURPLE)
                        .setFooter(pageDetails + " | " + helpMessage);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getDefaultEmbedBuilder()
                        .setColor(EmbedHelper.ORANGE)
                        .setDescription("I didn't find anything for that!")
                        .setFooter(helpMessage)
                        .build();
            }

            /**
             * Get the default embed builder to use
             * @return Default embed builder
             */
            private EmbedBuilder getDefaultEmbedBuilder() {
                return new EmbedBuilder()
                        .setTitle(title)
                        .setThumbnail(Gunfight.THUMBNAIL);
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Weapon current) {
                try {
                    Weapon.CATEGORY weaponCategory = current.getCategory();
                    builder
                            .setDescription(
                                    "**Name**: " + current.getName() + " (" + current.getCodename() + ")"
                                            + "\n**Category**: " + weaponCategory.name()
                                            + " (" + weaponCategory.getCodename() + ")"
                            )
                            .setImage(current.getImageURL());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }

            @Override
            public void sortItems(List<Weapon> items, boolean defaultSort) {
                items.sort((o1, o2) -> defaultSort
                        ? o1.getName().compareTo(o2.getName())
                        : o2.getName().compareTo(o1.getName()));
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("mwdata");
    }

    /**
     * Map category name -> categories
     */
    private void addCategories() {
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_ASSAULT_RIFLE, "assault", "ar", "assault rifle", "assault rifles"},
                Weapon.CATEGORY.ASSAULT_RIFLE
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_SNIPER, "sniper", "sniper rifle", "sniper rifles"},
                Weapon.CATEGORY.SNIPER
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_MARKSMAN, "marksman", "marksman rifle", "marksman rifles"},
                Weapon.CATEGORY.MARKSMAN
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_TACTICALS, "tacticals"},
                Weapon.CATEGORY.TACTICALS
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_LETHALS, "lethals"},
                Weapon.CATEGORY.LETHALS
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_OTHER, "other", "shield"},
                Weapon.CATEGORY.OTHER
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_SHOTGUN, "shotgun", "shotguns"},
                Weapon.CATEGORY.SHOTGUN
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_MELEE, "melee"},
                Weapon.CATEGORY.MELEE
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_LMG, "lmg", "light machine gun", "light machine guns"},
                Weapon.CATEGORY.LMG
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_SMG, "smg", "sub machine gun", "sub machine guns"},
                Weapon.CATEGORY.SMG
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_PISTOL, "pistol", "pistols", "handgun", "handguns"},
                Weapon.CATEGORY.PISTOL
        );
        addCategory(
                new String[]{Weapon.CATEGORY.CODENAME_LAUNCHER, "launcher", "launchers", "rocket launcher", "rocket launchers", "bazooka"},
                Weapon.CATEGORY.LAUNCHER
        );
    }

    /**
     * Map the given weapon category to the array of keys.
     *
     * @param keys     Keys to map category to
     * @param category Weapon category to map
     */
    private void addCategory(String[] keys, Weapon.CATEGORY category) {
        for(String key : keys) {
            categories.put(key, category);
        }
    }
}
