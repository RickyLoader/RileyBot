package Command.Structure;

import Valheim.Wiki.ValheimPageSummary;
import Valheim.Wiki.ValheimWikiAsset;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Interactive Valheim message
 */
public abstract class ValheimMessage {
    private final ValheimWikiAsset asset;
    private final EmoteHelper emoteHelper;
    private final MessageChannel channel;
    private final String footer;
    private final Button home;
    private long id;
    private String lastButtonId;

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
        this.home = Button.success("home", Emoji.ofEmote(emoteHelper.getHome()));
        this.lastButtonId = home.getId();

        context.getJDA().addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                long messageId = event.getMessageIdLong();
                String buttonId = event.getComponentId();

                if(messageId != id || !buttonId.equals(home.getId()) && !shouldPerformAction(buttonId)) {
                    return;
                }
                lastButtonId = buttonId;
                if(buttonId.equals(home.getId())) {
                    updateMessage(getDefaultMessageEmbed(), event);
                }
                else {
                    updateMessage(buildMessage(getDefaultEmbedBuilder()), event);
                }
            }
        });
    }

    /**
     * Get the most recent actionable button ID which was pressed on the valheim message
     *
     * @return Most recent actionable button ID
     */
    public String getLastButtonId() {
        return lastButtonId;
    }

    /**
     * Return whether to perform an action based on a button which was pressed on the message
     *
     * @param buttonId ID of the pressed button
     * @return Should perform action based on button ID
     */
    public abstract boolean shouldPerformAction(String buttonId);

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
     * Send the embed and add the buttons
     */
    public void showMessage() {
        MessageAction sendMessage = channel.sendMessage(getDefaultMessageEmbed());
        if(hasButtons()) {
            sendMessage = sendMessage.setActionRows(getButtonRow());
        }
        sendMessage.queue(message -> id = message.getIdLong());
    }

    /**
     * Return whether there are any other buttons to listen for.
     * If not, no buttons will be added as there will be only a singular message embed to display.
     *
     * @return Listening for other buttons
     */
    public abstract boolean hasButtons();

    /**
     * Get the list of buttons to display
     *
     * @return List of buttons to display
     */
    public abstract ArrayList<Button> getButtonList();

    /**
     * Get the action row of buttons to display
     *
     * @return Action row of buttons
     */
    private ActionRow getButtonRow() {
        ArrayList<Button> availableButtons = new ArrayList<>(Collections.singletonList(home));
        availableButtons.addAll(getButtonList());

        ArrayList<Button> toDisplay = new ArrayList<>();
        for(Button button : availableButtons) {
            toDisplay.add(lastButtonId.equals(button.getId()) ? button.asDisabled() : button);
        }
        return ActionRow.of(toDisplay);
    }

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
     * @param event          Button click event to acknowledge
     */
    private void updateMessage(MessageEmbed updatedContent, ButtonClickEvent event) {
        UpdateAction updateAction = event.deferEdit().setEmbeds(updatedContent);
        if(hasButtons()) {
            updateAction = updateAction.setActionRows(getButtonRow());
        }
        updateAction.queue();
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
