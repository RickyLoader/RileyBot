package Command.Structure;

import Valheim.ValheimBiome;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * Valheim biome message which can be interacted with via emotes
 */
public class BiomeMessage extends ValheimMessage {
    private final ValheimBiome biome;
    private final Emote creatures, pointOfInterest, resources;

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
        this.creatures = emoteHelper.getCreatures();
        this.pointOfInterest = emoteHelper.getPointOfInterest();
        this.resources = emoteHelper.getResources();
    }

    @Override
    public boolean shouldPerformAction(Emote emote) {
        return emote == creatures && biome.hasCreatures()
                || emote == pointOfInterest && biome.hasInterestPoints()
                || emote == resources && biome.hasResources();
    }

    @Override
    public boolean hasEmotes() {
        return biome.hasResources() || biome.hasCreatures() || biome.hasInterestPoints();
    }

    @Override
    public void addReactions(Message message) {
        if(biome.hasCreatures()) {
            message.addReaction(creatures).queue();
        }
        if(biome.hasInterestPoints()) {
            message.addReaction(pointOfInterest).queue();
        }
        if(biome.hasResources()) {
            message.addReaction(resources).queue();
        }
    }

    @Override
    public MessageEmbed buildMessage(EmbedBuilder builder) {
        if(getLast() == creatures) {
            return buildCreaturesEmbed(builder);
        }
        return getLast() == pointOfInterest ? buildInterestPointEmbed(builder) : buildResourcesEmbed(builder);
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
