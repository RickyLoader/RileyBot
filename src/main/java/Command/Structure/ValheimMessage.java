package Command.Structure;

import Valheim.Wiki.ValheimPageSummary;
import Valheim.Wiki.ValheimWikiAsset;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

/**
 * Interactive Valheim message
 */
public abstract class ValheimMessage {
    private final ValheimWikiAsset asset;
    private final EmoteHelper emoteHelper;
    private final MessageChannel channel;
    private final String footer;
    private final Emote home;
    private long id;
    private Emote last;

    /**
     * Create the interactive valheim message
     *
     * @param asset   Valheim asset to display
     * @param context Command context
     * @param footer  Footer to use in message
     */
    public ValheimMessage(ValheimWikiAsset asset, CommandContext context, String footer) {
        this.asset = asset;
        this.emoteHelper = context.getEmoteHelper();
        this.channel = context.getMessageChannel();
        this.footer = footer;
        this.home = context.getEmoteHelper().getHome();
        context.getJDA().addEventListener(new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();
                Emote emote = reaction.getReactionEmote().getEmote();
                if(reactID != id && (emote != home || !shouldPerformAction(emote))) {
                    return;
                }
                last = emote;
                if(emote == home && hasEmotes()) {
                    updateMessage(getDefaultMessageEmbed());
                }
                else {
                    updateMessage(buildMessage(getDefaultEmbedBuilder()));
                }
            }
        });
    }

    /**
     * Get the most recent actionable emote which was added to the valheim message
     *
     * @return Most recent actionable emote
     */
    public Emote getLast() {
        return last;
    }

    /**
     * Return whether to perform an action based on an emote which was added to the message
     *
     * @param emote Emote added to message
     * @return Perform action based on emote
     */
    public abstract boolean shouldPerformAction(Emote emote);

    /**
     * Get the emote helper
     *
     * @return Emote helper
     */
    public EmoteHelper getEmoteHelper() {
        return emoteHelper;
    }

    /**
     * Get the default message embed to display - an embed showing the valheim asset title, description, and image.
     *
     * @return Default valheim asset embed
     */
    private MessageEmbed getDefaultMessageEmbed() {
        return getDefaultEmbedBuilder().setDescription(asset.getDescription()).build();
    }

    /**
     * Send the embed and add the paging emotes
     */
    public void showMessage() {
        channel.sendMessage(getDefaultMessageEmbed()).queue(message -> {
            id = message.getIdLong();
            if(hasEmotes()) {
                message.addReaction(home).queue();
            }
            addReactions(message);
        });
    }

    /**
     * Return whether there are any other emotes to listen for.
     * If not, no emotes will be added as there will be only a singular message embed to display.
     *
     * @return Listening for other emotes
     */
    public abstract boolean hasEmotes();

    /**
     * Add reactions to the message
     *
     * @param message Message to add reactions to
     */
    public abstract void addReactions(Message message);

    /**
     * Build the message embed
     *
     * @param builder Embed builder initialised to valheim asset values
     * @return Message embed
     */
    public abstract MessageEmbed buildMessage(EmbedBuilder builder);

    /**
     * Edit the embedded message in place
     *
     * @param updatedContent Content to replace message with
     */
    private void updateMessage(MessageEmbed updatedContent) {
        channel.editMessageById(id, updatedContent).queue();
    }

    /**
     * Get the default embed builder to use based on the given valheim asset
     *
     * @param asset  Valheim asset to build embed for
     * @param footer Footer to use in embed
     * @return Default embed builder
     */
    public static EmbedBuilder getDefaultEmbedBuilder(ValheimWikiAsset asset, String footer) {
        ValheimPageSummary pageSummary = asset.getPageSummary();
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(
                        "Valheim Wiki: " + pageSummary.getTitle() + " (" + pageSummary.getCategory().name() + ")",
                        pageSummary.getUrl()
                )
                .setColor(pageSummary.getCategory().getColour())
                .setThumbnail(PageableValheimWikiSearchEmbed.THUMBNAIL)
                .setFooter(footer);
        if(asset.hasImageUrl()) {
            builder.setImage(asset.getImageUrl());
        }
        return builder;
    }

    /**
     * Get the default embed builder to use based on the valheim asset
     *
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder() {
        return getDefaultEmbedBuilder(asset, footer);
    }
}
