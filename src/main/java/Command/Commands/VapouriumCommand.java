package Command.Commands;

import Command.Structure.*;
import Vape.Option;
import Vape.Product;
import Vape.Vapourium;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Search for products on Vapourium/Embed Vapourium links
 */
public class VapouriumCommand extends DiscordCommand {
    private static final String LATEST = "latest";
    private final Vapourium vapourium;
    private final String footer;
    private final SimpleDateFormat dateFormat;

    public VapouriumCommand() {
        super(
                "vape",
                "Have a look at the cool Vapourium products!",
                "vape [product id/name/" + LATEST + "]\n[vapourium url]"
        );
        this.vapourium = Vapourium.getInstance();
        this.footer = "Type: " + getTrigger() + " for help";
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public void execute(CommandContext context) {
        Member member = context.getMember();
        MessageChannel channel = context.getMessageChannel();
        String content = context.getMessageContent();

        if(Vapourium.isVapouriumUrl(content)) {
            Product product = vapourium.getProductByUrl(content);
            if(product == null) {
                return;
            }
            context.getMessage().delete().queue(unused -> showProduct(context, product));
            return;
        }

        String query = content.substring(getTrigger().length()).trim();
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(query.equalsIgnoreCase(LATEST)) {
            ArrayList<Product> latest = vapourium.getLatestProducts(10);
            showProducts(context, latest);
            return;
        }

        long id = toLong(query);
        if(vapourium.updateDue()) {
            channel.sendTyping().queue();
        }

        if(id == 0) {
            ArrayList<Product> products = vapourium.getProductsByName(query);
            if(products.size() == 1) {
                showProduct(context, products.get(0));
                return;
            }
            showProducts(context, products, query);
        }
        else {
            Product product = vapourium.getProductById(id);
            if(product == null) {
                channel.sendMessage(
                        member.getAsMention() + " There are no products with the ID **" + id + "**"
                ).queue();
                return;
            }
            showProduct(context, product);
        }
    }

    /**
     * Display the given product in a pageable embed where the paging emotes cycle through the product images.
     *
     * @param context Command context
     * @param product Product to display
     */
    private void showProduct(CommandContext context, Product product) {
        new CyclicalPageableEmbed(
                context,
                product.getImages(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(product.getName(), product.getUrl())
                        .setFooter(pageDetails)
                        .setThumbnail(Vapourium.LOGO)
                        .setDescription(product.getTruncatedDescription(100))
                        .addField("ID", String.valueOf(product.getId()), true)
                        .addField("Type", product.getType(), true)
                        .addField("Price", product.formatPriceRange(), true)
                        .setColor(EmbedHelper.GREEN);

                if(product.hasOptions()) {
                    ArrayList<Option> options = product.getOptions();

                    for(Option option : options) {
                        ArrayList<Option.Value> values = option.getValues();
                        String separator = values.size() > 3 ? ", " : ",\n";
                        String valueString;
                        if(option.hasValues()) {
                            StringBuilder valueBuilder = new StringBuilder();
                            for(int i = 0; i < values.size(); i++) {
                                Option.Value value = values.get(i);
                                String markdown = value.isAvailable() ? "**" : "~~";
                                valueBuilder
                                        .append(markdown)
                                        .append(value.getName())
                                        .append(markdown);
                                if(i < (values.size() - 1)) {
                                    valueBuilder.append(separator);
                                }
                            }
                            valueString = valueBuilder.toString();
                        }
                        else {
                            valueString = "-";
                        }
                        builder.addField(
                                option.getName(),
                                valueString,
                                true
                        );
                    }
                }

                return builder
                        .addField("Last Update", dateFormat.format(product.getLastUpdated()), false);
            }

            @Override
            public String getPageDetails() {
                int images = getItems().size();
                if(images == 0) {
                    return "No product images available | " + footer;
                }
                return "Image: " + getPage() + "/" + getPages() + " | " + footer;
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex) {
                builder.setImage((String) getItems().get(currentIndex));
            }

            @Override
            public boolean nonPagingEmoteAdded(Emote e) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Display the given list of products in a pageable embed
     *
     * @param context  Command context
     * @param products List of products matching query
     * @param query    Query used to find products
     */
    private void showProducts(CommandContext context, ArrayList<Product> products, String query) {
        boolean noResults = products.isEmpty();
        new PageableTableEmbed(
                context,
                products,
                Vapourium.LOGO,
                "Vapourium Search",
                "**" + (noResults ? "No" : products.size()) + "** products found for: **" + query + "**",
                footer,
                new String[]{
                        "ID",
                        "Name",
                        "Type"
                },
                5,
                noResults ? EmbedHelper.RED : EmbedHelper.GREEN
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Product product = (Product) items.get(index);
                return new String[]{
                        String.valueOf(product.getId()),
                        EmbedHelper.embedURL(product.getName(), product.getUrl()),
                        product.getType()
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance(query, defaultSort) {
                    @Override
                    public String getString(Object o) {
                        return ((Product) o).getName();
                    }
                });
            }
        }.showMessage();
    }

    /**
     * Display the given list of products in a pageable embed
     *
     * @param context  Command context
     * @param products List of products matching query
     */
    private void showProducts(CommandContext context, ArrayList<Product> products) {
        new PageableTableEmbed(
                context,
                products,
                Vapourium.LOGO,
                "Vapourium " + products.size() + " Latest Products",
                null,
                footer,
                new String[]{
                        "ID",
                        "Name",
                        "Created"
                },
                5,
                EmbedHelper.GREEN
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Product product = (Product) items.get(index);
                return new String[]{
                        String.valueOf(product.getId()),
                        EmbedHelper.embedURL(product.getName(), product.getUrl()),
                        dateFormat.format(product.getCreated())
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((Product) o1).getCreated();
                    Date d2 = ((Product) o2).getCreated();
                    return Vapourium.dateSort(d1, d2, defaultSort);
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || Vapourium.isVapouriumUrl(message.getContentDisplay());
    }
}
