package Command.Commands;

import Command.Structure.*;
import UrbanDictionary.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

/**
 * Search or get random definitions from the Urban Dictionary
 */
public class UrbanDictionaryCommand extends OnReadyDiscordCommand {
    private final UrbanDictionary urbanDictionary;
    private String upvote, downvote;

    public UrbanDictionaryCommand() {
        super(
                "urbandict",
                "Get cool definitions from Urban Dictionary",
                "urbandict [search term/random]"
        );
        this.urbanDictionary = new UrbanDictionary();
    }

    @Override
    public void execute(CommandContext context) {
        String word = context.getMessageContent().substring(getTrigger().length()).trim();
        MessageChannel channel = context.getMessageChannel();

        if(word.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        new Thread(() -> {
            channel.sendTyping().queue();
            if(word.equalsIgnoreCase("random")) {
                Definition definition = urbanDictionary.getRandomDefinition();
                MessageEmbed definitionEmbed = addDefinitionToEmbed(
                        getDefaultEmbedBuilder(),
                        definition,
                        "Urban Dictionary Random | " + definition.getTerm()
                )
                        .build();
                channel.sendMessage(definitionEmbed).queue();
                return;
            }
            ArrayList<Definition> definitions = urbanDictionary.searchDefinition(word);
            if(definitions.isEmpty()) {
                channel.sendMessage(
                        context.getMember().getAsMention() + " No definitions found for **" + word + "**!"
                ).queue();
                return;
            }
            showDefinitions(context, definitions, word);
        }).start();
    }

    /**
     * Display the given definitions in a pageable message embed
     *
     * @param context     Command context
     * @param definitions Definitions to display
     * @param query       Query used to find definitions
     */
    private void showDefinitions(CommandContext context, ArrayList<Definition> definitions, String query) {
        new CyclicalPageableEmbed<Definition>(
                context,
                definitions,
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return getDefaultEmbedBuilder();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Definition definition) {
                addDefinitionToEmbed(
                        builder,
                        definition,
                        "Urban Dictionary | " + query + " | " + (currentIndex + 1) + "/" + getItems().size()
                );
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Add the given definition to the given embed builder.
     * Display upvotes/downvotes & definition usage etc.
     * Don't set title
     *
     * @param builder    Embed builder to add definition to
     * @param definition Definition to add to the embed builder
     * @return Embed builder with definition added
     */
    private EmbedBuilder addDefinitionToEmbed(EmbedBuilder builder, Definition definition, String title) {
        return builder
                .setTitle(title, definition.getUrl())
                .setDescription(getEmbedDescription(definition))
                .addField(upvote, String.valueOf(definition.getUpvote()), true)
                .addField(downvote, String.valueOf(definition.getDownvote()), true);
    }

    /**
     * Get the embed builder to use when displaying definitions
     *
     * @return Embed builder
     */
    public EmbedBuilder getDefaultEmbedBuilder() {
        String icon = "https://i.imgur.com/vQXfaJX.png";
        return new EmbedBuilder()
                .setColor(EmbedHelper.URBAN_DICT_BLUE)
                .setImage(EmbedHelper.SPACER_IMAGE)
                .setThumbnail(icon)
                .setFooter("Try: " + getHelpName(), icon);
    }

    /**
     * Get the String description of the given definition to use in a message embed
     *
     * @param definition Definition to display
     * @return String description of the given definition
     */
    private String getEmbedDescription(Definition definition) {
        String description = "**Definition**\n\n"
                + definition.getExplanation() + "\n\n";

        if(definition.hasQuote()) {
            description += "**Usage**\n\n" + definition.getQuote() + "\n\n";
        }
        return description + EmbedHelper.embedURL(
                "- *"
                        + definition.getAuthor()
                        + ", "
                        + definition.formatSubmitted()
                        + "*",
                definition.getUrl()
        );
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.upvote = emoteHelper.getUpvote().getAsMention();
        this.downvote = emoteHelper.getDownvote().getAsMention();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
