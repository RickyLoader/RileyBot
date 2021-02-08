package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableTableEmbed;
import Network.NetworkRequest;
import Network.Secret;
import Stock.Company;
import Stock.StockQuote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.*;

/**
 * View info on some stocks
 */
public class StocksCommand extends DiscordCommand {
    private final String BASE_URL = "https://finnhub.io/api/v1/", THUMBNAIL = "https://i.imgur.com/HaSlhp2.png";
    private final HashMap<String, Company> companies;

    public StocksCommand() {
        super("$[symbol/search term]", "Check out some stocks!");
        this.companies = parseCompanies();
    }

    /**
     * Parse the list of all publicly traded companies in to a map
     *
     * @return Map of company symbol -> company
     */
    private HashMap<String, Company> parseCompanies() {
        HashMap<String, Company> companies = new HashMap<>();
        JSONArray data = new JSONArray(
                new NetworkRequest(BASE_URL + "stock/symbol/?exchange=US&token=" + Secret.FINNHUB_KEY, false)
                        .get()
                        .body
        );
        for(int i = 0; i < data.length(); i++) {
            JSONObject companyData = data.getJSONObject(i);
            String symbol = companyData.getString("displaySymbol");
            companies.put(
                    symbol.toLowerCase(),
                    new Company(
                            symbol,
                            companyData.getString("description")
                    )
            );
        }
        return companies;
    }

    @Override
    public void execute(CommandContext context) {
        String query = context.getMessageContent().replace("$", "").trim();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        Company[] companies = getCompaniesByQuery(query.toLowerCase());
        if(companies.length == 0) {
            channel.sendMessage(member.getAsMention() + " I couldn't find anything for **" + query + "**!").queue();
            return;
        }
        if(companies.length == 1) {
            StockQuote quote = getStockQuote(companies[0]);
            channel.sendMessage(buildStockQuoteEmbed(quote)).queue();
            return;
        }
        buildSearchResultsEmbed(context, query, companies);
    }

    /**
     * Build a message embed showing the company search results for the given query
     *
     * @param context   Command context
     * @param query     Search query
     * @param companies Company search results
     */
    private void buildSearchResultsEmbed(CommandContext context, String query, Company[] companies) {
        new PageableTableEmbed(
                context,
                Arrays.asList(companies),
                THUMBNAIL,
                "Stock Search Results",
                companies.length + " results found for **" + query + "**",
                "Try: " + getHelpName(),
                new String[]{"Symbol", "Name"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Company company = (Company) items.get(index);
                return new String[]{"$" + company.getSymbol(), company.getName()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String c1 = ((Company) o1).getSymbol();
                    String c2 = ((Company) o2).getSymbol();
                    if(defaultSort) {
                        return c1.compareTo(c2);
                    }
                    return c2.compareTo(c1);
                });
            }
        }.showMessage();
    }

    /**
     * Build a message embed detailing the given stock quote
     *
     * @param quote Stock quote to display
     * @return Message embed displaying stock quote
     */
    private MessageEmbed buildStockQuoteEmbed(StockQuote quote) {
        DecimalFormat currFormat = new DecimalFormat("0.00");
        Company company = quote.getCompany();
        boolean down = quote.getDiff() < 0;
        return new EmbedBuilder()
                .setTitle(company.getName() + " (" + company.getSymbol() + ")")
                .setFooter("Try: " + getHelpName())
                .setThumbnail(company.hasLogoURL() ? company.getLogoURL() : THUMBNAIL)
                .setImage(down ? "https://i.imgur.com/o2ohqxr.png" : "https://i.imgur.com/BNSHZDo.png")
                .setColor(down ? EmbedHelper.RED : EmbedHelper.GREEN)
                .addField(
                        "Current Price",
                        "$" + currFormat.format(quote.getCurrentPrice()),
                        true
                )
                .addField(
                        "Difference",
                        (down ? "-" : "+") + currFormat.format(Math.abs(quote.getDiff()))
                                + " (" + currFormat.format(quote.getDiffPercent()) + "%)",
                        true
                )
                .addBlankField(true)
                .addField("Opening Price", "$" + currFormat.format(quote.getOpenPrice()), true)
                .addField(
                        "Previous Close Price",
                        "$" + currFormat.format(quote.getPreviousClosePrice()),
                        true
                )
                .addBlankField(true)
                .addField(
                        "Day Range",
                        "$" + currFormat.format(quote.getLowPrice())
                                + " - "
                                + "$" + currFormat.format(quote.getHighPrice()),
                        true
                )
                .build();
    }

    /**
     * Get the stock quote for the given company
     *
     * @param company Company to get stock quote for
     * @return Stock quote for company
     */
    private StockQuote getStockQuote(Company company) {
        JSONObject data = new JSONObject(
                new NetworkRequest(
                        BASE_URL + "quote?symbol=" + company.getSymbol() + "&token=" + Secret.FINNHUB_KEY,
                        false
                ).get().body
        );
        return new StockQuote.StockQuoteBuilder()
                .setCompany(company)
                .setHighPrice(data.getDouble("h"))
                .setLowPrice(data.getDouble("l"))
                .setOpenPrice(data.getDouble("o"))
                .setPreviousClosePrice(data.getDouble("pc"))
                .setCurrentPrice(data.getDouble("c"))
                .build();
    }

    /**
     * Attempt to get companies from the given query.
     * Check the map for a symbol match otherwise search within
     * the company name.
     * Get the company logo if it has not been seen before and there is one result
     *
     * @param query Query - either symbol or search query
     * @return Array of search results
     */
    private Company[] getCompaniesByQuery(String query) {
        if(companies.containsKey(query)) {
            Company company = companies.get(query);
            if(!company.hasLogoURL()) {
                JSONObject info = new JSONObject(
                        new NetworkRequest(
                                BASE_URL
                                        + "stock/profile2?symbol=" + company.getSymbol()
                                        + "&token=" + Secret.FINNHUB_KEY,
                                false
                        ).get().body
                );
                if(info.has("logo")) {
                    String logoURL = info.getString("logo");
                    if(!logoURL.isEmpty()) {
                        company.setLogoURL(logoURL);
                    }
                }
            }
            return new Company[]{company};
        }
        return companies
                .values()
                .stream()
                .filter(c -> c.getName().toLowerCase().contains(query))
                .toArray(Company[]::new);
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("$");
    }
}
