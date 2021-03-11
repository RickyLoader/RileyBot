package Command.Commands;

import Command.Structure.*;
import Valheim.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

import static Valheim.ValheimPageSummary.*;


/**
 * Search the Valheim wiki
 */
public class ValheimWikiCommand extends DiscordCommand {
    private final ValheimWiki valheimWiki;

    public ValheimWikiCommand() {
        super("valheim", "Search the Valheim wiki!", "valheim [query/all]");
        this.valheimWiki = new ValheimWiki();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage().replace(getTrigger(), "").trim();
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        ArrayList<ValheimPageSummary> searchResults = query.equals("all") ? valheimWiki.getPageSummaries() : valheimWiki.searchWiki(query);
        if(searchResults.size() == 1) {
            showSearchResult(searchResults.get(0), channel, context);
            return;
        }
        new PageableValheimWikiSearchEmbed(
                context,
                searchResults,
                query,
                "Try: " + getHelpName()
        ).showMessage();
    }

    /**
     * Send a message embed displaying the Valheim wiki page that was found
     *
     * @param valheimPageSummary Summary of the page to be displayed
     * @param channel            Channel to send embed to
     */
    private void showSearchResult(ValheimPageSummary valheimPageSummary, MessageChannel channel, CommandContext context) {
        try {
            CATEGORY category = valheimPageSummary.getCategory();
            if(category == CATEGORY.BIOME) {
                ValheimBiome biome = valheimWiki.getBiome(valheimPageSummary);
                if(biome == null) {
                    throw new ParseException();
                }
                new BiomeMessage(biome, context, "Try: " + getHelpName()).showMessage();
            }
            else if(category == CATEGORY.ITEM) {
                ValheimItem item = valheimWiki.getItem(valheimPageSummary);
                if(item == null) {
                    throw new ParseException();
                }
                MessageEmbed itemEmbed;
                switch(item.getType()) {
                    case MEAD:
                    case FOOD:
                        itemEmbed = buildFoodEmbed((ValheimFood) item);
                        break;
                    case MISC:
                    case MATERIAL:
                    case SEED:
                    case TROPHY:
                    default:
                        itemEmbed = getDefaultItemEmbedBuilder(item).build();
                        break;
                    case WORN:
                        itemEmbed = buildWornItemEmbed((ValheimWornItem) item);
                        break;
                    case KNIFE:
                    case AXE:
                    case BOW:
                    case CLUB:
                    case ARROW:
                    case SPEAR:
                    case SHIELD:
                    case PICKAXE:
                    case TOOL:
                        itemEmbed = buildWieldedItemEmbed((ValheimWieldedItem) item);
                        break;
                }
                channel.sendMessage(itemEmbed).queue();
            }
            else {
                ValheimCreature creature = valheimWiki.getCreature(valheimPageSummary);
                if(creature == null) {
                    throw new ParseException();
                }
                channel.sendMessage(buildCreatureEmbed(creature)).queue();
            }
        }
        catch(ParseException e) {
            channel.sendMessage(
                    "I wasn't able to parse the wiki page for **" + valheimPageSummary.getTitle() + "**: "
                            + valheimPageSummary.getUrl()
            ).queue();
        }
    }

    /**
     * Get the default embed builder to use based on the given valheim asset
     *
     * @param asset Valheim asset to build embed for
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(ValheimWikiAsset asset) {
        return ValheimMessage.getDefaultEmbedBuilder(asset, "Try: " + getHelpName());
    }

    /**
     * Get the default embed builder to use for an item
     * Initialise with common item values
     *
     * @param item Item to build embed for
     * @return Default item embed builder
     */
    private EmbedBuilder getDefaultItemEmbedBuilder(ValheimItem item) {
        ValheimItem.ItemStats itemStats = item.getItemStats();
        EmbedBuilder builder = getDefaultEmbedBuilder(item)
                .setDescription(item.getDescription());

        if(itemStats.hasStackSize()) {
            builder.addField("Stack", String.valueOf(itemStats.getStackSize()), true);
        }

        if(itemStats.hasWeight()) {
            builder.addField("Weight", String.valueOf(itemStats.getWeight()), true);
        }

        if(itemStats.hasSize()) {
            builder.addField("Size", itemStats.getSize(), true);
        }

        builder.addField("Can Teleport", itemStats.isTeleportable() ? "Yes" : "No", true)
                .addField("Item Type", item.getType().name(), true);

        if(itemStats.isDropped()) {
            builder.addField("Dropped By", StringUtils.join(itemStats.getDroppedBy(), "\n"), true);
        }

        if(itemStats.hasUsage()) {
            builder.addField("Usage", StringUtils.join(itemStats.getUsage(), "\n"), true);
        }

        if(itemStats.isCraftable()) {
            builder.addField(
                    "Crafting Materials",
                    StringUtils.join(itemStats.getCraftingMaterials(), "\n"), true
            );
        }

        if(itemStats.isBought()) {
            builder.addField("Trader Cost", itemStats.getCost() + " coins", true);
        }
        return builder;
    }

    /**
     * Build a message embed detailing the given Valhheim creature
     *
     * @param creature Creature to display in the message embed
     * @return Message embed detailing creature
     */
    private MessageEmbed buildCreatureEmbed(ValheimCreature creature) {
        EmbedBuilder builder = getDefaultEmbedBuilder(creature)
                .setDescription(creature.getDescription())
                .addField("Behaviour", creature.getBehaviour(), true)
                .addField("Damage", StringUtils.join(creature.getDamage(), ",\n"), true)
                .addField("Locations", StringUtils.join(creature.getLocations(), ",\n"), true)
                .addField("Health", String.valueOf(creature.getHealth()), true)
                .addField("Tameable", creature.isTameable() ? "Yes" : "No", true);

        if(creature.hasDrops()) {
            builder.addField("Drops", StringUtils.join(creature.getDrops(), ",\n"), true);
        }

        if(creature.hasAbilities()) {
            builder.addField("Abilities", StringUtils.join(creature.getAbilities(), ",\n"), true);
        }

        if(creature.hasWeakness()) {
            builder.addField("Weak To", StringUtils.join(creature.getWeakTo(), ",\n"), true);
        }

        if(creature.hasExtremeWeakness()) {
            builder.addField("Very Weak To", StringUtils.join(creature.getVeryWeakTo(), ",\n"), true);
        }

        if(creature.hasResistance()) {
            builder.addField("Resistant To", StringUtils.join(creature.getResistantTo(), ",\n"), true);
        }

        if(creature.hasExtremeResistance()) {
            builder.addField("Very Resistant To", StringUtils.join(creature.getVeryResistantTo(), ",\n"), true);
        }

        if(creature.hasImmunity()) {
            builder.addField("Immune To", StringUtils.join(creature.getImmuneTo(), ",\n"), true);
        }

        if(creature.isSummonable()) {
            builder.addField("Summon", StringUtils.join(creature.getSummonItems(), ",\n"), true);
        }
        return builder.build();
    }

    /**
     * Build a message embed detailing the given Valhheim food item
     *
     * @param food Food to display in the message embed
     * @return Message embed detailing food
     */
    private MessageEmbed buildFoodEmbed(ValheimFood food) {
        EmbedBuilder builder = getDefaultItemEmbedBuilder(food)
                .addField("Duration", food.getDuration() + "s", true);

        if(food.hasMaxHealth()) {
            builder.addField("Max Health", String.valueOf(food.getMaxHealth()), true);
        }

        if(food.hasMaxStamina()) {
            builder.addField("Max Stamina", String.valueOf(food.getMaxStamina()), true);
        }

        if(food.hasHealingDetails()) {
            builder.addField("Healing", food.getHealing(), true);
        }

        if(food.hasOtherEffects()) {
            builder.addField(
                    "Other Effect(s)",
                    StringUtils.join(food.getOtherEffects(), "\n"),
                    true
            );
        }
        return builder.build();
    }

    /**
     * Build a message embed detailing the given Valheim wielded item
     *
     * @param item Wielded item to display in the message embed
     * @return Message embed detailing wielded item
     */
    private MessageEmbed buildWieldedItemEmbed(ValheimWieldedItem item) {
        ValheimWieldedItem.WieldStats wieldStats = item.getWieldStats();
        EmbedBuilder builder = getDefaultItemEmbedBuilder(item);
        if(wieldStats.hasWieldStyle()) {
            builder.addField("Wield Style", wieldStats.getWieldStyle(), true);
        }
        addStatMapToEmbed(wieldStats.getOffensiveStats(), builder);
        addStatMapToEmbed(wieldStats.getDefensiveStats(), builder);
        return addArmourStatsToEmbed(item.getArmourStats(), builder).build();
    }

    /**
     * Add the given stat map to the embed builder
     *
     * @param statMap Stat map to add to embed builder
     * @param builder Embed builder
     */
    private void addStatMapToEmbed(HashMap<String, String> statMap, EmbedBuilder builder) {
        for(String stat : statMap.keySet()) {
            builder.addField(stat, statMap.get(stat), true);
        }
    }

    /**
     * Add the given armour stats to the embed builder
     *
     * @param armourStats Item armour stats
     * @param builder     Embed builder
     * @return Embed builder containing armour stats
     */
    private EmbedBuilder addArmourStatsToEmbed(ValheimWornItem.ArmourStats armourStats, EmbedBuilder builder) {
        if(armourStats.hasArmour()) {
            builder.addField("Armour", armourStats.getArmour(), true);
        }
        if(armourStats.hasDurability()) {
            builder.addField("Durability", armourStats.getDurability(), true);
        }
        if(armourStats.hasCraftingRequirement()) {
            builder.addField("Crafting Level", String.valueOf(armourStats.getCraftingLevel()), true);
        }
        if(armourStats.hasRepairRequirement()) {
            builder.addField("Repair Level", String.valueOf(armourStats.getRepairLevel()), true);
        }
        if(armourStats.hasMovementModifier()) {
            builder.addField("Movement Speed", armourStats.getMovementSpeed() + "%", true);
        }
        return builder;
    }

    /**
     * Build a message embed detailing the given Valheim worn item
     *
     * @param item Worn item to display in the message embed
     * @return Message embed detailing worn item
     */
    private MessageEmbed buildWornItemEmbed(ValheimWornItem item) {
        EmbedBuilder builder = getDefaultItemEmbedBuilder(item);
        return addArmourStatsToEmbed(item.getArmourStats(), builder).build();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
