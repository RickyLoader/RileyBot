package Command.Commands;

import Command.Structure.*;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import Stock.Symbol;
import Stock.MarketQuote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static Stock.MarketQuote.*;

/**
 * View info on some stocks/crypto
 */
public class StocksCommand extends OnReadyDiscordCommand {
    private final String
            finnhubBaseUrl = "https://finnhub.io/api/v1/",
            thumbnail = "https://i.imgur.com/HaSlhp2.png";

    private final static String
            stock = "stock",
            crypto = "crypto";

    private final ArrayList<Symbol> marketSymbols;
    private boolean stockSymbols = false;
    private String cryptoEmote;

    private enum SEARCH_TYPE {
        ALL,
        STOCK_ONLY,
        CRYPTO_ONLY;

        /**
         * Check if the given symbol matches the search type
         *
         * @param symbol Symbol to check
         * @return Symbol matches search type
         */
        public boolean matches(Symbol symbol) {
            switch(this) {
                case CRYPTO_ONLY:
                    return symbol.isCrypto();
                case STOCK_ONLY:
                    return !symbol.isCrypto();
                default:
                    return true;
            }
        }
    }

    public StocksCommand() {
        super(
                "$",
                "Check out some stocks & crypto!",
                "$[" + stock + "/" + crypto + "] [stock/crypto symbol or search term]"
        );
        this.marketSymbols = getMarketSymbols();
    }

    /**
     * Get a list of all supported crypto/stock symbols.
     *
     * @return List of crypto/stock symbols
     */
    private ArrayList<Symbol> getMarketSymbols() {
        ArrayList<Symbol> marketSymbols = new ArrayList<>(getCryptoSymbols());
        this.stockSymbols = addStockSymbols(marketSymbols);
        return marketSymbols;
    }

    /**
     * Add the stock symbols to the given list of market symbols.
     * Return the success of fetching the symbols
     *
     * @param marketSymbols List of market symbols to add stock symbols to
     * @return Success of adding stock symbols
     */
    private boolean addStockSymbols(ArrayList<Symbol> marketSymbols) {
        ArrayList<Symbol> stockSymbols = getStockSymbols();
        marketSymbols.addAll(stockSymbols);
        return !stockSymbols.isEmpty();
    }

    /**
     * Parse the list of all crypto currency asset symbols from the Messari API
     *
     * @return List of all supported crypto currency asset symbols from Messari
     */
    private ArrayList<Symbol> getCryptoSymbols() {
        ArrayList<Symbol> cryptoSymbols = new ArrayList<>();
        String endpoint = "v2/assets?limit=500";
        int page = 1;
        NetworkResponse response;
        while((response = messariRequest(endpoint + "&page=" + page)).code != 404) {
            System.out.println("Parsing crypto symbols - Page " + page + "...");
            JSONArray data = new JSONObject(response.body).getJSONArray("data");
            for(int i = 0; i < data.length(); i++) {
                JSONObject symbolData = data.getJSONObject(i);
                if(symbolData.isNull("symbol") || symbolData.isNull("name")) {
                    continue;
                }
                cryptoSymbols.add(
                        new Symbol(
                                symbolData.getString("symbol"),
                                symbolData.getString("name"),
                                symbolData.getString("id"),
                                true
                        )
                );
            }
            page++;
        }
        return cryptoSymbols;
    }

    /**
     * Make a request to the Messari API, authenticate with the API key for increased rate limit.
     *
     * @param endpoint Endpoint to append to base URL
     * @return API response
     */
    private NetworkResponse messariRequest(String endpoint) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-messari-api-key", Secret.MESSARI_KEY);
        return new NetworkRequest("https://data.messari.io/api/" + endpoint, false).get(headers);
    }

    /**
     * Parse the list of all stock symbols from the Finnhub API
     *
     * @return List of all supported stock symbols from Finnhub
     */
    private ArrayList<Symbol> getStockSymbols() {
        ArrayList<Symbol> stockSymbols = new ArrayList<>();
        NetworkResponse response = new NetworkRequest(
                finnhubBaseUrl + "stock/symbol/?exchange=US&token=" + Secret.FINNHUB_KEY,
                false
        ).get();

        if(response.code != 200) {
            return stockSymbols;
        }

        JSONArray data = new JSONArray(response.body);
        for(int i = 0; i < data.length(); i++) {
            JSONObject symbolData = data.getJSONObject(i);
            stockSymbols.add(
                    new Symbol(
                            symbolData.getString("symbol"),
                            symbolData.getString("description"),
                            symbolData.getString("figi"),
                            false
                    )
            );
        }
        return stockSymbols;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        if(!stockSymbols) {
            stockSymbols = addStockSymbols(marketSymbols);
            if(!stockSymbols) {
                channel.sendMessage("Something went wrong fetching the stock symbols!").queue();
                return;
            }
        }
        String query = context.getMessageContent().replace(getTrigger(), "").trim();
        Member member = context.getMember();

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        SEARCH_TYPE searchType = SEARCH_TYPE.ALL;

        if(query.startsWith(crypto)) {
            query = query.replaceFirst(crypto, "").trim();
            searchType = SEARCH_TYPE.CRYPTO_ONLY;
        }
        else if(query.startsWith(stock)) {
            query = query.replaceFirst(stock, "").trim();
            searchType = SEARCH_TYPE.STOCK_ONLY;
        }

        channel.sendTyping().queue();
        Symbol[] searchResults = getSymbolsByQuery(query.toLowerCase(), searchType);

        if(searchResults.length == 0) {
            channel.sendMessage(member.getAsMention() + " I couldn't find anything for **" + query + "**!").queue();
            return;
        }

        if(searchResults.length == 1) {
            Symbol symbol = searchResults[0];
            MarketQuote quote = symbol.isCrypto() ? getCryptoMarketQuote(symbol) : getStockMarketQuote(symbol);
            if(quote == null) {
                channel.sendMessage(
                        member.getAsMention()
                                + " I wasn't able to parse the market data for: **" + symbol.getSymbol() + "**"
                ).queue();
                return;
            }
            channel.sendMessage(buildMarketQuoteEmbed(quote)).queue();
            return;
        }
        buildSearchResultsEmbed(context, query, searchResults);
    }

    /**
     * Build a message embed showing the symbol search results for the given query
     *
     * @param context Command context
     * @param query   Search query
     * @param symbols Symbol search results
     */
    private void buildSearchResultsEmbed(CommandContext context, String query, Symbol[] symbols) {
        String desc = "__" + symbols.length + " results found for **" + query + "**__\n";
        Symbol[] cryptoResults = Arrays.stream(symbols).filter(Symbol::isCrypto).toArray(Symbol[]::new);
        if(cryptoResults.length > 0) {
            desc += "\n" + cryptoEmote + " = Crypto currency";
        }
        new PageableTableEmbed(
                context,
                Arrays.asList(symbols),
                thumbnail,
                "Market Search Results",
                desc,
                "Try: " + getHelpName(),
                new String[]{"Symbol", "Name", "ID"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Symbol symbol = (Symbol) items.get(index);
                String identifier = getTrigger() + symbol.getSymbol();
                if(symbol.isCrypto()) {
                    identifier = cryptoEmote + " " + identifier;
                }
                return new String[]{identifier, symbol.getName(), getTrigger() + symbol.getId()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String c1 = ((Symbol) o1).getSymbol();
                    String c2 = ((Symbol) o2).getSymbol();
                    if(defaultSort) {
                        return c1.compareTo(c2);
                    }
                    return c2.compareTo(c1);
                });
            }
        }.showMessage();
    }

    /**
     * Build a message embed detailing the given market quote
     *
     * @param quote Market quote to display
     * @return Message embed displaying market quote
     */
    private MessageEmbed buildMarketQuoteEmbed(MarketQuote quote) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        NumberFormat currFormat = NumberFormat.getCurrencyInstance();

        Symbol symbol = quote.getSymbol();
        boolean down = quote.getDiff() < 0;
        String image = symbol.isCrypto()
                ? down ? "https://i.imgur.com/RGKvAAY.png" : "https://i.imgur.com/PfFFF8P.png"
                : down ? "https://i.imgur.com/o2ohqxr.png" : "https://i.imgur.com/BNSHZDo.png";

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(symbol.getName() + " (" + symbol.getSymbol() + ")")
                .setFooter("Try: " + getHelpName())
                .setThumbnail(symbol.hasLogoUrl() ? symbol.getLogoUrl() : thumbnail)
                .setImage(image)
                .setColor(down ? EmbedHelper.RED : EmbedHelper.GREEN)
                .addField(
                        "Current Price\n(C)",
                        currFormat.format(quote.getCurrentPrice()),
                        true
                );

        if(quote.hasOHLCV()) {
            builder.addField(
                    "Difference\n" + (symbol.isCrypto() ? "(O -> C)" : "(PC -> C)"),
                    (down ? "-" : "+") + currFormat.format(Math.abs(quote.getDiff()))
                            + " (" + decimalFormat.format(quote.getDiffPercent()) + "%)",
                    true
            )
                    .addBlankField(true)
                    .addField("Opening Price\n(O)", currFormat.format(quote.getOpenPrice()), true)
                    .addField(
                            "Previous Close Price\n(PC)",
                            currFormat.format(quote.getPreviousClosePrice()),
                            true
                    )
                    .addBlankField(true)
                    .addField(
                            "Day Range",
                            currFormat.format(quote.getLowPrice())
                                    + " - "
                                    + currFormat.format(quote.getHighPrice()),
                            true
                    );
        }
        return builder.build();
    }

    /**
     * Get the market quote for the given stock symbol
     *
     * @param symbol Stock symbol to get market quote for
     * @return Market quote for symbol
     */
    private MarketQuote getStockMarketQuote(Symbol symbol) {
        try {
            JSONObject data = new JSONObject(
                    new NetworkRequest(
                            finnhubBaseUrl + "quote?symbol=" + symbol.getSymbol() + "&token=" + Secret.FINNHUB_KEY,
                            false
                    ).get().body
            );
            return new MarketQuoteBuilder(symbol)
                    .setHighPrice(data.getDouble("h"))
                    .setLowPrice(data.getDouble("l"))
                    .setOpenPrice(data.getDouble("o"))
                    .setPreviousClosePrice(data.getDouble("pc"))
                    .setCurrentPrice(data.getDouble("c"))
                    .build();
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the market quote for the given crypto symbol
     *
     * @param symbol Crypto symbol to get market quote for
     * @return Market quote for symbol
     */
    private MarketQuote getCryptoMarketQuote(Symbol symbol) {
        try {
            JSONObject data = new JSONObject(
                    messariRequest("v1/assets/" + symbol.getId() + "/metrics/market-data").body
            ).getJSONObject("data").getJSONObject("market_data");

            MarketQuoteBuilder builder = new MarketQuoteBuilder(symbol)
                    .setCurrentPrice(data.getDouble("price_usd"));

            String marketDataKey = "ohlcv_last_24_hour";
            if(data.isNull(marketDataKey)) {
                return builder.build();
            }
            JSONObject ohlcv = data.getJSONObject(marketDataKey); // open, high, low, close, volume
            return builder
                    .setHighPrice(ohlcv.getDouble("high"))
                    .setLowPrice(ohlcv.getDouble("low"))
                    .setOpenPrice(ohlcv.getDouble("open"))
                    .setPreviousClosePrice(ohlcv.getDouble("close"))
                    .build();
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Attempt to get stock/crypto symbols from the given query.
     * Check for symbols with an exact match to the query, otherwise perform a fuzzy search.
     * Get the symbol logo if it has not been seen before and there is a singular result
     *
     * @param query      Query - either symbol or search query
     * @param searchType Search type
     * @return Array of search results
     */
    private Symbol[] getSymbolsByQuery(String query, SEARCH_TYPE searchType) {
        Symbol[] symbols = getMatchingSymbolsById(query, searchType);
        if(symbols.length == 1) {
            return new Symbol[]{completeSymbol(symbols[0])};
        }
        symbols = getMatchingSymbolsBySymbol(query, searchType);
        if(symbols.length == 0) {
            return getFuzzyMatchingSymbols(query, searchType);
        }
        if(symbols.length == 1) {
            return new Symbol[]{completeSymbol(symbols[0])};
        }
        return symbols;
    }

    /**
     * Add the symbol logo
     *
     * @param symbol Symbol to add logo to
     * @return Symbol with logo
     */
    private Symbol completeSymbol(Symbol symbol) {
        if(!symbol.hasLogoUrl()) {
            String logo = symbol.isCrypto() ? getCryptoLogoUrl(symbol) : getStockLogoUrl(symbol);
            if(logo != null) {
                symbol.setLogoUrl(logo);
            }
        }
        return symbol;
    }

    /**
     * Get an array of symbols where the symbol matches the given query
     *
     * @param query      Query to match to symbol
     * @param searchType Search type
     * @return Symbols matching query
     */
    private Symbol[] getMatchingSymbolsBySymbol(String query, SEARCH_TYPE searchType) {
        return marketSymbols
                .stream()
                .filter(symbol -> symbol.getSymbol().equalsIgnoreCase(query) && searchType.matches(symbol))
                .toArray(Symbol[]::new);
    }

    /**
     * Get an array of symbols where the name matches the given query
     *
     * @param query      Query to match to name
     * @param searchType Search type
     * @return Symbols with name matching query
     */
    private Symbol[] getMatchingSymbolsById(String query, SEARCH_TYPE searchType) {
        return marketSymbols
                .stream()
                .filter(symbol -> symbol.getId().equalsIgnoreCase(query) && searchType.matches(symbol))
                .toArray(Symbol[]::new);
    }

    /**
     * Get an array of symbols where the name or symbol contain the given query
     *
     * @param query      Query to fuzzy search
     * @param searchType Search type
     * @return Symbols where the name or symbol contain the given query
     */
    private Symbol[] getFuzzyMatchingSymbols(String query, SEARCH_TYPE searchType) {
        return marketSymbols
                .stream()
                .filter(symbol -> {
                    boolean fuzzyMatch = symbol.getSymbol().toLowerCase().contains(query)
                            || symbol.getName().toLowerCase().contains(query);
                    return fuzzyMatch && searchType.matches(symbol);
                })
                .toArray(Symbol[]::new);
    }

    /**
     * Attempt to get the logo URL for the given stock symbol
     *
     * @param stockSymbol Stock symbol to get URL for
     * @return Symbol logo URL
     */
    private String getStockLogoUrl(Symbol stockSymbol) {
        JSONObject info = new JSONObject(
                new NetworkRequest(
                        finnhubBaseUrl
                                + "stock/profile2?symbol=" + stockSymbol.getSymbol()
                                + "&token=" + Secret.FINNHUB_KEY,
                        false
                ).get().body
        );
        if(info.has("logo")) {
            String logoURL = info.getString("logo");
            if(!logoURL.isEmpty()) {
                return logoURL;
            }
        }
        return null;
    }

    /**
     * Attempt to get the logo URL for the given crypto symbol
     *
     * @param cryptoSymbol Crypto symbol to get URL for
     * @return Crypto logo URL
     */
    private String getCryptoLogoUrl(Symbol cryptoSymbol) {
        String url = "https://cryptoicons.org/api/icon/" + cryptoSymbol.getSymbol().toLowerCase() + "/200";
        if(new NetworkRequest(url, false).get().code == 200) {
            return url;
        }
        return null;
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        cryptoEmote = EmoteHelper.formatEmote(emoteHelper.getCrypto());
    }
}
