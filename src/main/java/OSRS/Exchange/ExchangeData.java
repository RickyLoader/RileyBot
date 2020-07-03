package OSRS.Exchange;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ExchangeData {

    private static HashMap<Long, Item> items;
    private static HashMap<Long, String> itemNames;
    private final long lastCalled;

    public ExchangeData() {
        lastCalled = System.currentTimeMillis();
        fetchItems();
    }

    private void fetchItems() {
        try {
            String exchangeURL = "https://rsbuddy.com/exchange/summary.json";
            InputStream inputStream = new URL(exchangeURL).openStream();
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader inputParser = new BufferedReader(inputReader);
            String[] rawItemData;
            String excess = "\\{?,?\"[0-9]+\":";
            rawItemData = inputParser.readLine().split(excess);
            items = new HashMap<>();
            itemNames = new HashMap<>();

            for(String item : rawItemData) {
                if(item.length() > 0) {
                    String[] itemJson = item.
                            replace("{", "")
                            .replace("}", "")
                            .replace("\"", "")
                            .replace("\\u0027", "")
                            .split(",");
                    Item newItem = createItem(itemJson);
                    items.put(newItem.getItemID(), newItem);
                    itemNames.put(newItem.getItemID(), newItem.getItemName().toLowerCase());
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public long getLastCalled() {
        return lastCalled;
    }

    private Item createItem(String[] item) {
        long id = 0;
        String name = "";
        long sellPrice = 0;
        long buyPrice = 0;
        long avgPrice = 0;

        for(String attributeValuePairs : item) {
            String attribute = attributeValuePairs.split(":")[0];
            String value = attributeValuePairs.split(":")[1];

            if(attribute.equals("id")) {
                id = Long.valueOf(value);
            }
            else if(attribute.equals("name")) {
                name = value;
            }
            else if(attribute.equals("sell_average")) {
                sellPrice = Long.valueOf(value);

            }
            else if(attribute.equals("buy_average")) {
                buyPrice = Long.valueOf(value);

            }
            else if(attribute.equals("overall_average")) {
                avgPrice = Long.valueOf(value);
            }
        }
        return new Item(id, name, sellPrice, buyPrice, avgPrice);
    }

    private boolean isLong(String query) {
        boolean result = false;
        try {
            Long.parseLong(query);
            result = true;
        }
        catch(NumberFormatException e) {
        }
        return result;
    }


    public String requestItem(String identifier) {
        Item i = getItem(getItemKey(identifier));
        if(i == null) {
            return "Item " + identifier + " does not exist, try again!\n\n";
        }
        return Summary(i);
    }

    private Long getItemKey(String identifier) {
        Long key = null;
        if(isLong(identifier)) {
            key = Long.valueOf(identifier);
        }
        else if(itemNames.containsValue(identifier)) {
            for(Map.Entry e : itemNames.entrySet()) {
                if(e.getValue().equals(identifier)) {
                    key = (Long) e.getKey();
                }
            }
        }
        return key;
    }

    private Item getItem(Long key) {

        if(key != null && items.keySet().contains(key)) {
            return items.get(key);
        }
        return null;
    }


    private String Summary(Item i) {
        String result = "\n\n";
        String codeBlock = "```";
        result +=
                codeBlock + i.getItemName() + " (ITEM ID: " + i.getItemID() + ")\n" +
                        "\nBUY PRICE:     " + i.getItemBuyPrice() + "\n\n" +
                        "SELL PRICE:    " + i.getItemSellPrice() + "\n\n" +
                        "AVERAGE PRICE: " + i.getItemAveragePrice() + "\n\n" + codeBlock;
        return (result);
    }
}

