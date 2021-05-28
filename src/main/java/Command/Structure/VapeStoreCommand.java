package Command.Structure;

import Vape.Option;
import Vape.Product;
import Vape.VapeStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Search for products on a vape store/Embed vape store links
 */
public class VapeStoreCommand extends DiscordCommand {
    private static final String LATEST = "latest";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final VapeStore vapeStore;
    private final String footer;

    /**
     * Create the vape store command
     *
     * @param trigger   Command trigger
     * @param vapeStore Vape store to use
     */
    public VapeStoreCommand(String trigger, VapeStore vapeStore) {
        super(
                trigger,
                "Have a look at the cool " + vapeStore.getName() + " products!",
                trigger + " [product id/name/" + LATEST + "]\n[" + vapeStore.getName() + " url]"
        );
        this.vapeStore = vapeStore;
        this.footer = "Type: " + getTrigger() + " for help";
    }

    @Override
    public void execute(CommandContext context) {
        Member member = context.getMember();
        MessageChannel channel = context.getMessageChannel();
        String content = context.getMessageContent();

        if(vapeStore.isStoreUrl(content)) {
            Product product = vapeStore.getProductByUrl(content);
            if(product == null) {
                return;
            }
            context.getMessage().delete().queue(unused -> showProduct(context, product, vapeStore, footer));
            return;
        }

        String query = content.substring(getTrigger().length()).trim();
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(query.equalsIgnoreCase(LATEST)) {
            ArrayList<Product> latest = vapeStore.getLatestProducts(10);
            showProducts(context, latest);
            return;
        }

        long id = toLong(query);
        if(vapeStore.updateDue()) {
            channel.sendTyping().queue();
        }

        if(id == 0) {
            ArrayList<Product> products = vapeStore.getProductsByName(query);
            if(products.size() == 1) {
                showProduct(context, products.get(0), vapeStore, footer);
                return;
            }
            showProducts(context, products, query);
        }
        else {
            Product product = vapeStore.getProductById(id);
            if(product == null) {
                channel.sendMessage(
                        member.getAsMention() + " There are no products with the ID **" + id + "**"
                ).queue();
                return;
            }
            showProduct(context, product, vapeStore, footer);
        }
    }

    /**
     * Display the given product in a pageable embed where the paging emotes cycle through the product images.
     *
     * @param context   Command context
     * @param product   Product to display
     * @param vapeStore Store where product is from
     * @param footer    Footer to display alongside paging info
     */
    public static void showProduct(CommandContext context, Product product, VapeStore vapeStore, String footer) {
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
                        .setThumbnail(vapeStore.getLogo())
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
                                "__" + option.getName() + "__",
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
            public boolean nonPagingButtonPressed(String buttonId) {
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
                vapeStore.getLogo(),
                vapeStore.getName() + " Search",
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
                vapeStore.getLogo(),
                vapeStore.getName() + " " + products.size() + " Latest Products",
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
                    return VapeStore.dateSort(d1, d2, defaultSort);
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || vapeStore.isStoreUrl(message.getContentDisplay());
    }
}
