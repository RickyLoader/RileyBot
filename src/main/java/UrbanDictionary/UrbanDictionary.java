package UrbanDictionary;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import static UrbanDictionary.Definition.parseSubmissionDate;

/**
 * Wrap the urban dictionary API
 */
public class UrbanDictionary {
    private final String upvote, downvote, help;

    /**
     * Create the Urban Dictionary and set emotes
     *
     * @param emoteHelper Emote helper
     */
    public UrbanDictionary(EmoteHelper emoteHelper, String help) {
        this.upvote = EmoteHelper.formatEmote(emoteHelper.getUpvote());
        this.downvote = EmoteHelper.formatEmote(emoteHelper.getDownvote());
        this.help = help;
    }

    /**
     * Show the definition of a term
     *
     * @param definition Definition to display
     * @return Embed displaying the definition
     */
    public MessageEmbed getDefinitionEmbed(Definition definition) {
        String icon = "https://i.imgur.com/vQXfaJX.png";

        StringBuilder description = new StringBuilder("**Definition**\n\n");
        description.append(definition.getExplanation()).append("\n\n");
        if(definition.hasQuote()) {
            description.append("**Usage**\n\n").append(definition.getQuote()).append("\n\n");
        }
        description.append(EmbedHelper.embedURL("- *" + definition.getAuthor() + ", " + definition.formatSubmitted() + "*", definition.getUrl()));

        return new EmbedBuilder()
                .setTitle("Urban Dictionary: " + definition.getTerm())
                .setDescription(description.toString())
                .addField(upvote, String.valueOf(definition.getUpvote()), true)
                .addField(downvote, String.valueOf(definition.getDownvote()), true)
                .setFooter("Try: " + help, icon)
                .setThumbnail(icon)
                .setImage(EmbedHelper.getSpacerImage())
                .setColor(EmbedHelper.getUrbanDictBlue())
                .build();
    }

    /**
     * Strip the embedded links from given String
     *
     * @return String stripped of embedded links
     */
    private String stripFormatting(String string) {
        return string
                .replace("[", "")
                .replace("]", "")
                .replace("*", "");
    }

    /**
     * Search for a definition on the Urban Dictionary
     *
     * @param term Term to search for
     * @return Search result or null
     */
    public Definition searchDefinition(String term) {
        return definitionRequest("https://api.urbandictionary.com/v0/define?term=" + term);
    }

    /**
     * Get a random definition from the Urban Dictionary
     *
     * @return Random definition
     */
    public Definition getRandomDefinition() {
        return definitionRequest("https://api.urbandictionary.com/v0/random");
    }

    /**
     * Retrieve a random definition from the given URL
     *
     * @param url URL to urban dictionary API endpoint
     * @return Random definition from provided results
     */
    private Definition definitionRequest(String url) {
        JSONArray results = new JSONObject(
                new NetworkRequest(url, false).get()
        ).getJSONArray("list");
        return results.isEmpty() ? null : parseDefinition(results.getJSONObject(new Random().nextInt(results.length())));
    }

    /**
     * Parse a definition from a JSON response
     *
     * @param definition JSON response of definition
     * @return Definition
     */
    private Definition parseDefinition(JSONObject definition) {
        String explanation = stripFormatting(definition.getString("definition"));
        String term = definition.getString("word");
        return new Definition(
                term,
                explanation,
                stripFormatting(definition.getString("example")),
                definition.getInt("thumbs_up"),
                definition.getInt("thumbs_down"),
                definition.getString("permalink"),
                parseSubmissionDate(definition.getString("written_on")),
                definition.getString("author")
        );
    }
}
