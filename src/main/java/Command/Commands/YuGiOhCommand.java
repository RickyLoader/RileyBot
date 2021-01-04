package Command.Commands;

import Command.Structure.*;
import YuGiOh.Card;
import YuGiOh.CardManager;
import YuGiOh.CardStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.text.DecimalFormat;
import java.util.HashMap;


/**
 * Search and view Yu-Gi-Oh cards
 */
public class YuGiOhCommand extends DiscordCommand {
    private final CardManager cardManager = new CardManager();
    private final HashMap<Long, Card> cardMessages = new HashMap<>();
    private boolean listener = false;
    private Emote switchImage;
    private EmoteHelper emoteHelper;

    public YuGiOhCommand() {
        super("yugi\nyugi [card name]", "Take a look at some Yu-Gi-Oh cards!");
    }

    @Override
    public void execute(CommandContext context) {
        if(!listener) {
            registerListener(context.getJDA());
            listener = true;
        }

        if(emoteHelper == null) {
            emoteHelper = context.getEmoteHelper();
            switchImage = emoteHelper.getNextImage();
        }

        String cardName = context.getLowerCaseMessage().replace("yugi", "").trim();
        new Thread(() -> {
            boolean random = cardName.isEmpty();
            Card card = random ? cardManager.getRandomCard() : cardManager.getCard(cardName);
            MessageChannel channel = context.getMessageChannel();

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

            channel.sendMessage(buildCardMessage(card)).queue(message -> {
                if(card.getTotalImages() == 1) {
                    return;
                }
                cardMessages.put(message.getIdLong(), card);
                message.addReaction(switchImage).queue();
            });
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
        String upvote = EmoteHelper.formatEmote(emoteHelper.getUpvote());
        String downvote = EmoteHelper.formatEmote(emoteHelper.getDownvote());

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

        if(card.getTotalImages() > 1) {
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
                .setThumbnail("https://i.imgur.com/5CGmPlb.png")
                .setFooter(
                        "Try: " + getTrigger().replace("\n", " | "),
                        "https://i.imgur.com/g8KJAIl.png"
                );
    }

    /**
     * Register the emote listener for switching card artwork in card messages
     *
     * @param jda JDA for registering listener
     */
    private void registerListener(JDA jda) {
        jda.addEventListener(
                new EmoteListener(jda) {
                    @Override
                    public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                        long id = reaction.getMessageIdLong();
                        if(reaction.getReactionEmote().getEmote() != switchImage || !cardMessages.containsKey(id)) {
                            return;
                        }
                        updateCardImage(id, reaction.getChannel());
                    }
                }
        );
    }

    /**
     * Update the image currently displayed in a card's message embed
     * Progress from the first image through to the last & then loop back
     *
     * @param id      ID of message embed
     * @param channel Channel of message embed
     */
    private void updateCardImage(long id, MessageChannel channel) {
        Card card = cardMessages.get(id);
        card.updateImage();
        channel.editMessageById(id, buildCardMessage(card)).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("yugi");
    }
}
