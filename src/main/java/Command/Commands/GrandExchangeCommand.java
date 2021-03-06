package Command.Commands;

import Command.Structure.*;
import Network.NetworkRequest;
import Runescape.OSRS.GE.GrandExchange;
import Runescape.OSRS.GE.Item;
import Runescape.OSRS.GE.Item.ItemImage;
import Runescape.OSRS.GE.ItemPrice;
import Runescape.OSRS.GE.ItemPrice.Price;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * View OSRS G.E prices
 */
public class GrandExchangeCommand extends OnReadyDiscordCommand {
    private final String thumbnail = "https://i.imgur.com/4z4Aipa.png";
    private final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final GrandExchange grandExchange;
    private String lowAlch, highAlch, sellPrice, buyPrice, freeToPlay, members, buyLimit;

    public GrandExchangeCommand() {
        super("g.e", "View the latest Grand Exchange prices for an item!", "g.e [item name/id]");
        this.grandExchange = GrandExchange.getInstance();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        new Thread(() -> {
            int id = toInteger(query);
            if(id == 0) {
                Item[] items = grandExchange.getItemManager().getItemsByName(query);
                if(items.length == 1) {
                    showItemPriceEmbed(items[0], channel);
                    return;
                }
                showItemSearchResults(context, items, query);
            }
            else {
                Item item = grandExchange.getItemManager().getItemByID(id);
                if(item == null) {
                    channel.sendMessage(
                            context.getMember().getAsMention()
                                    + " I didn't find any items with the id: **" + query + "**!"
                    ).queue();
                    return;
                }
                showItemPriceEmbed(item, channel);
            }
        }).start();
    }

    /**
     * Send a pageable message embed displaying the items found for the given search query
     *
     * @param context Command context
     * @param items   Items found with the given search query
     * @param query   Search query used to find items
     */
    public void showItemSearchResults(CommandContext context, Item[] items, String query) {
        new PageableTableEmbed<Item>(
                context,
                Arrays.asList(items),
                thumbnail,
                "Grand Exchange Search",
                items.length + " Results found for: **" + query + "**!",
                "Type: " + getTrigger() + " for help",
                new String[]{"ID", "Name"},
                5,
                EmbedHelper.GREEN
        ) {
            @Override
            public String getNoItemsDescription() {
                return "No items found for: **" + query + "**!";
            }

            @Override
            public String[] getRowValues(int index, Item item, boolean defaultSort) {
                return new String[]{String.valueOf(item.getId()), item.getName()};
            }

            @Override
            public void sortItems(List<Item> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance<Item>(query, defaultSort) {
                    @Override
                    public String getString(Item o) {
                        return o.getName();
                    }
                });
            }
        }.showMessage();
    }

    /**
     * Send a message embed detailing the given Grand Exchange item price info
     *
     * @param item    Grand Exchange item
     * @param channel Channel to send message embed to
     */
    private void showItemPriceEmbed(Item item, MessageChannel channel) {
        channel.sendTyping().queue();
        ItemPrice itemPrice = grandExchange.getItemPrice(item);
        ItemImage itemImage = item.getItemImage();

        MessageEmbed itemEmbed = new EmbedBuilder()
                .setThumbnail(thumbnail)
                .setImage(itemImage.getHighDetailImageUrl())
                .setTitle("G.E Price: " + item.getName() + " (" + item.getId() + ")")
                .setDescription(
                        "**Note**: Prices are the most recent **instant** buy/sell offers."
                                + "\n**Examine Text**: " + item.getExamine()
                )
                .addField(getPriceField("Buy Price", buyPrice, itemPrice.getHigh()))
                .addField(getPriceField("Sell Price", sellPrice, itemPrice.getLow()))
                .addField(
                        "Daily Volume",
                        itemPrice.hasDailyVolume() ? commaFormat.format(itemPrice.getDailyVolume()) : "-",
                        false
                )
                .addField(
                        "Low Alch " + lowAlch,
                        item.isLowAlchable() ? formatPrice(item.getLowAlch()) : "-",
                        true
                )
                .addField(
                        "High Alch " + highAlch,
                        item.isHighAlchable() ? formatPrice(item.getHighAlch()) : "-",
                        true
                )
                .addField(
                        "Buy Limit " + buyLimit,
                        item.hasBuyLimit() ? commaFormat.format(item.getBuyLimit()) : "-",
                        true
                )
                .addField(
                        "Members " + (item.isMembers() ? members : freeToPlay),
                        item.isMembers() ? "Yes" : "No",
                        true
                )
                .setFooter(
                        "Type: " + getTrigger() + " for help | Trade volume as of: "
                                + dateFormat.format(grandExchange.getVolumeTimestamp()),
                        thumbnail
                )
                .setColor(EmbedHelper.GREEN)
                .build();

        channel.sendMessage(itemEmbed).queue(message -> new Thread(() -> {
            if(isValidImage(itemImage.getHighDetailImageUrl())) {
                return;
            }
            EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().get(0));
            message.editMessage(builder.setImage(itemImage.getInventoryImageUrl()).build()).queue();
        }).start());
    }

    /**
     * Check if the given image exists/is valid
     *
     * @param imageUrl Image URL
     * @return Image returns a 200 OK response code
     */
    private boolean isValidImage(String imageUrl) {
        return new NetworkRequest(imageUrl, false).get().code == 200;
    }

    /**
     * Format the given price to a comma separated String with "GP" appended
     *
     * @param price Price to format
     * @return Formatted price
     */
    private String formatPrice(long price) {
        return commaFormat.format(price) + " GP";
    }

    /**
     * Get a field detailing the given price - display the price & transaction time or "-" if no data is found
     *
     * @param title Field title
     * @param emote Emote to use
     * @param price Price info
     * @return Field detailing given price
     */
    private MessageEmbed.Field getPriceField(String title, String emote, Price price) {
        if(price == null) {
            return new MessageEmbed.Field(emote, "-", true);
        }
        return new MessageEmbed.Field(
                title + " " + emote,
                formatPrice(price.getPrice()) + "\nat " + dateFormat.format(price.getDate()),
                true
        );
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        lowAlch = emoteHelper.getLowAlch().getAsMention();
        highAlch = emoteHelper.getHighAlch().getAsMention();
        sellPrice = emoteHelper.getSellPrice().getAsMention();
        buyPrice = emoteHelper.getBuyPrice().getAsMention();
        freeToPlay = emoteHelper.getFreeToPlay().getAsMention();
        members = emoteHelper.getMembers().getAsMention();
        buyLimit = emoteHelper.getBuyLimit().getAsMention();
    }
}
