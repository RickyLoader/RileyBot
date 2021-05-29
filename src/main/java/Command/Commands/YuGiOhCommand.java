package Command.Commands;

import Command.Structure.*;
import YuGiOh.Card;
import YuGiOh.CardManager;
import YuGiOh.CardStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateAction;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Search and view Yu-Gi-Oh cards
 */
public class YuGiOhCommand extends OnReadyDiscordCommand {
    private final CardManager cardManager = new CardManager();
    private final HashMap<Long, Card> cardMessages = new HashMap<>();
    private final String switchImageId = "switch";
    private Button switchImage;
    private String upvote, downvote;

    public YuGiOhCommand() {
        super("yugi\nyugi [card name]", "Take a look at some Yu-Gi-Oh cards!");
    }

    @Override
    public void execute(CommandContext context) {
        String cardName = context.getLowerCaseMessage().replace("yugi", "").trim();
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        new Thread(() -> {
            boolean random = cardName.isEmpty();
            Card card = random ? cardManager.getRandomCard() : cardManager.getCard(cardName);

            if(card == null) {
                Member member = context.getMember();
                if(random) {
                    channel.sendMessage(
                            member.getAsMention() + " Sorry bro, I wasn't able to grab a random card."
                    ).queue();
                    return;
                }
                channel.sendMessage(buildErrorMessage(cardName, member)).queue();
                return;
            }
            MessageAction sendMessage = channel.sendMessage(buildCardMessage(card));

            // Don't need to keep track of cards with a single image
            if(card.hasMultipleImages()) {
                sendMessage
                        .setActionRows(ActionRow.of(switchImage))
                        .queue(message -> cardMessages.put(message.getIdLong(), card));
            }
            else {
                sendMessage.queue();
            }
        }).start();
    }

    /**
     * Build the error message to display when a card is not found for the
     * given search term
     *
     * @param cardName Name which yielded no results
     * @param member   Member requesting search
     * @return Error message embed
     */
    private MessageEmbed buildErrorMessage(String cardName, Member member) {
        return getDefaultEmbedBuilder()
                .setTitle("No results!")
                .setColor(EmbedHelper.RED)
                .setDescription(
                        "Sorry " + member.getAsMention()
                                + ", I didn't find any cards matching: **" + cardName + "**.\n\n"
                                + "I checked for both an exact and partial match but found nothing!"
                )
                .build();
    }

    /**
     * Build a message embed detailing the given Yu-Gi-Oh card
     *
     * @param card Card to display
     * @return Message embed displaying card details
     */
    private MessageEmbed buildCardMessage(Card card) {
        CardStats stats = card.getStats();
        DecimalFormat commaFormat = new DecimalFormat("#,###");

        EmbedBuilder builder = getDefaultEmbedBuilder()
                .setTitle(card.getName())
                .addField("Total views", commaFormat.format(stats.getTotalViews()), true)
                .addField("Weekly views", commaFormat.format(stats.getWeeklyViews()), true)
                .addField(
                        "Popularity",
                        upvote + " " + commaFormat.format(stats.getUpvotes())
                                + "\n"
                                + downvote + " " + commaFormat.format(stats.getDownvotes()),
                        true)
                .setImage(card.getCurrentImage())
                .setColor(card.getType().getColour());

        if(card.hasMultipleImages()) {
            builder.setDescription("**Image**: " + (card.getImageIndex() + 1) + "/" + card.getTotalImages());
        }

        if(stats.hasPrice()) {
            builder.addField("Price", stats.getFormattedPrice(), true);
        }

        return builder
                .addField("View", EmbedHelper.embedURL("View page", card.getUrl()), true)
                .addBlankField(true)
                .build();
    }

    /**
     * Get the default embed builder to use
     *
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder() {
        return new EmbedBuilder()
                .setThumbnail("https://i.imgur.com/TT7tZia.png")
                .setFooter(
                        "Try: " + getTrigger().replace("\n", " | "),
                        "https://i.imgur.com/g8KJAIl.png"
                );
    }

    /**
     * Update the image currently displayed in a card's message embed
     * Progress from the first image through to the last & then loop back
     *
     * @param event Button click event to acknowledge
     */
    private void updateCardImage(ButtonClickEvent event) {
        Card card = cardMessages.get(event.getMessageIdLong());
        card.updateImage();
        UpdateAction updateAction = event.deferEdit().setEmbeds(buildCardMessage(card));
        if(card.hasMultipleImages()) {
            updateAction = updateAction.setActionRows(ActionRow.of(switchImage));
        }
        updateAction.queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("yugi");
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.upvote = emoteHelper.getUpvote().getAsMention();
        this.downvote = emoteHelper.getDownvote().getAsMention();
        this.switchImage = Button.primary(switchImageId, Emoji.ofEmote(emoteHelper.getNextImage()));
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                long messageId = event.getMessageIdLong();
                String buttonId = event.getComponentId();
                if(!cardMessages.containsKey(messageId) || !buttonId.equals(switchImageId)) {
                    return;
                }
                updateCardImage(event);
            }
        });
    }
}
