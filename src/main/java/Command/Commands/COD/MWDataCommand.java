package Command.Commands.COD;

import COD.Assets.Weapon;
import COD.Gunfight;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static Bot.DiscordCommandManager.codManager;

/**
 * View Modern Warfare weapons
 */
public class MWDataCommand extends DiscordCommand {
    private final HashMap<String, Category> categories = new HashMap<>();

    public MWDataCommand() {
        super("mwdata [weapon name/category]", "Have a look at Modern Warfare weapons!");
        addCategories();
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace("mwdata", "").trim();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        Category weaponCategory = categories.get(message);
        if(weaponCategory != null) {
            Weapon[] categoryWeapons = codManager.getWeaponsByCategory(
                    weaponCategory.getCodename()
            );
            showPageableWeapons(
                    context,
                    categoryWeapons,
                    "Category: " + weaponCategory.getName() + " (" + categoryWeapons.length + " Items)"
            );
            return;
        }

        Weapon[] searchResults = codManager.getWeaponsByName(message);
        if(searchResults.length == 0) {
            channel.sendMessage(
                    member.getAsMention() + " I didn't find any weapons matching **" + message + "**!"
            ).queue();
            return;
        }

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
        Category category = categories.get(weapon.getCategory());
        return new EmbedBuilder()
                .setThumbnail(Gunfight.thumbnail)
                .setImage(weapon.getImageURL())
                .setColor(EmbedHelper.GREEN)
                .setTitle(weapon.getName())
                .setDescription(
                        "**Codename**: " + weapon.getCodename()
                                + "\n**Category**: " + category.getName() + " (" + category.getCodename() + ")"
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
        new PageableEmbed(
                context,
                Arrays.asList(weapons),
                Gunfight.thumbnail,
                title,
                null,
                1,
                EmbedHelper.PURPLE
        ) {
            @Override
            public void addFields(EmbedBuilder builder, int currentIndex) {
                try {
                    Weapon current = (Weapon) getItems().get(currentIndex);
                    Category weaponCategory = categories.get(current.getCategory());
                    builder
                            .setDescription(
                                    "**Name**: " + current.getName() + " (" + current.getCodename() + ")"
                                            + "\n**Category**: " + weaponCategory.getName()
                                            + " (" + weaponCategory.getCodename() + ")"
                            )
                            .setImage(current.getImageURL());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String w1 = ((Weapon) o1).getName();
                    String w2 = ((Weapon) o2).getName();
                    if(defaultSort) {
                        return w1.compareTo(w2);
                    }
                    return w2.compareTo(w1);
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("mwdata");
    }

    /**
     * Weapon category
     */
    private static class Category {
        private final String name, codename;

        /**
         * Create the weapon category
         *
         * @param name     Category name - e.g "Assault Rifle"
         * @param codename Category codename - e.g "weapon_assault_rifle"
         */
        public Category(String name, String codename) {
            this.name = name;
            this.codename = codename;
        }

        /**
         * Get the category name - e.g "Assault Rifle"
         *
         * @return Category name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the category codename - e.g "weapon_assault_rifle"
         *
         * @return Category codename
         */
        public String getCodename() {
            return codename;
        }
    }

    /**
     * Map category name -> categories
     */
    private void addCategories() {
        addCategory(
                new String[]{"weapon_assault_rifle", "assault", "ar", "assault rifle", "assault rifles"},
                new Category("Assault Rifle", "weapon_assault_rifle")
        );
        addCategory(
                new String[]{"weapon_sniper", "sniper", "sniper rifle", "sniper rifles"},
                new Category("Sniper Rifle", "weapon_sniper")
        );
        addCategory(
                new String[]{"weapon_marksman", "marksman", "marksman rifle", "marksman rifles"},
                new Category("Marksman Rifle", "weapon_marksman")
        );
        addCategory(
                new String[]{"tactical", "tacticals"},
                new Category("Tactical", "tacticals")
        );
        addCategory(
                new String[]{"lethal", "lethals"},
                new Category("Lethal", "lethals")
        );
        addCategory(
                new String[]{"weapon_other", "other", "shield"},
                new Category("Other", "weapon_other")
        );
        addCategory(
                new String[]{"weapon_shotgun", "shotgun", "shotguns"},
                new Category("Shotgun", "weapon_shotgun")
        );
        addCategory(
                new String[]{"weapon_melee", "melee"},
                new Category("Melee", "weapon_melee")
        );
        addCategory(
                new String[]{"weapon_lmg", "lmg", "light machine gun", "light machine guns"},
                new Category("Light Machine Gun", "weapon_lmg")
        );
        addCategory(
                new String[]{"weapon_smg", "smg", "sub machine gun", "sub machine guns"},
                new Category("Sub Machine Gun", "weapon_smg")
        );
        addCategory(
                new String[]{"weapon_pistol", "pistol", "pistols", "handgun", "handguns"},
                new Category("Pistol", "weapon_pistol")
        );
        addCategory(
                new String[]{"weapon_launcher", "launcher", "launchers", "rocket launcher", "rocket launchers", "bazooka"},
                new Category("Launcher", "weapon_launcher")
        );
    }

    /**
     * Map the given category to the array of keys.
     *
     * @param keys     Keys to map category to
     * @param category Category to map
     */
    private void addCategory(String[] keys, Category category) {
        for(String key : keys) {
            categories.put(key, category);
        }
    }
}
