package Command.Structure;

import Valheim.Wiki.ValheimBiome;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.Button;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Valheim biome message which can be interacted with via buttons
 */
public class BiomeMessage extends ValheimMessage {
    private final ValheimBiome biome;
    private final Button creatures, pointOfInterest, resources;

    /**
     * Create the interactive valheim biome message
     *
     * @param biome   Biome to display
     * @param context Command context
     * @param footer  Footer to use in message
     */
    public BiomeMessage(ValheimBiome biome, CommandContext context, String footer) {
        super(biome, context, footer);
        this.biome = biome;
        EmoteHelper emoteHelper = context.getEmoteHelper();
        this.creatures = Button.success("creatures", Emoji.fromEmote(emoteHelper.getCreatures()));
        this.pointOfInterest = Button.success("interest", Emoji.fromEmote(emoteHelper.getPointOfInterest()));
        this.resources = Button.success("resources", Emoji.fromEmote(emoteHelper.getResources()));
    }

    @Override
    public boolean shouldPerformAction(String buttonId) {
        return buttonId.equals(creatures.getId()) && biome.hasCreatures()
                || buttonId.equals(pointOfInterest.getId()) && biome.hasInterestPoints()
                || buttonId.equals(resources.getId()) && biome.hasResources();
    }

    @Override
    public boolean hasButtons() {
        return biome.hasResources() || biome.hasCreatures() || biome.hasInterestPoints();
    }

    @Override
    public ArrayList<Button> getButtonList() {
        ArrayList<Button> buttons = new ArrayList<>();
        if(biome.hasCreatures()) {
            buttons.add(creatures);
        }
        if(biome.hasInterestPoints()) {
            buttons.add(pointOfInterest);
        }
        if(biome.hasResources()) {
            buttons.add(resources);
        }
        return buttons;
    }

    @Override
    public MessageEmbed buildMessage(EmbedBuilder builder) {
        if(getLastButtonId().equals(creatures.getId())) {
            return buildCreaturesEmbed(builder);
        }
        return getLastButtonId().equals(pointOfInterest.getId())
                ? buildInterestPointEmbed(builder)
                : buildResourcesEmbed(builder);
    }

    /**
     * Add the given biome attributes to the embed builder and return the completed message embed
     *
     * @param builder    Embed builder to add attributes to
     * @param attributes Map of attribute name e.g Passive -> array of attribute e.g [Deer, Gull]
     * @param desc       Description to use in the embed
     * @return Message embed
     */
    private MessageEmbed addBiomeAttributes(EmbedBuilder builder, HashMap<String, String[]> attributes, String desc) {
        builder.setDescription("**" + desc + "**:");
        for(String attributeName : attributes.keySet()) {
            String[] values = attributes.get(attributeName);
            builder.addField(attributeName, StringUtils.join(values, ", "), false);
        }
        return builder.build();
    }

    /**
     * Build a message embed detailing the points of interest in the biome
     *
     * @param builder Embed builder initialised to biome values
     * @return Message embed displaying biome interest points
     */
    private MessageEmbed buildInterestPointEmbed(EmbedBuilder builder) {
        return addBiomeAttributes(builder, biome.getInterestPoints(), "Biome points of interest");
    }

    /**
     * Build a message embed detailing the creatures found in the biome
     *
     * @param builder Embed builder initialised to biome values
     * @return Message embed displaying biome creatures
     */
    private MessageEmbed buildCreaturesEmbed(EmbedBuilder builder) {
        return addBiomeAttributes(builder, biome.getCreatures(), "Biome creatures");
    }

    /**
     * Build a message embed detailing the resources found in the biome
     *
     * @param builder Embed builder initialised to biome values
     * @return Message embed displaying biome resources
     */
    private MessageEmbed buildResourcesEmbed(EmbedBuilder builder) {
        return addBiomeAttributes(builder, biome.getResources(), "Biome resources");
    }
}
