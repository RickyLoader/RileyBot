package Exchange;


import java.text.DecimalFormat;

public class Item{

    private String itemName;
    private long itemSellPrice;
    private long itemBuyPrice;
    private long itemAveragePrice;
    private long itemID;
    DecimalFormat df = new DecimalFormat("#,###GP");

    public Item(long itemID, String itemName, long itemSellPrice, long itemBuyPrice, long itemAveragePrice){
        this.itemName = itemName;
        this.itemID = itemID;
        this.itemAveragePrice = itemAveragePrice;
        this.itemSellPrice = itemSellPrice;
        this.itemBuyPrice = itemBuyPrice;
    }

    public String getItemName(){
        return itemName;
    }

    public long getItemID(){
        return itemID;
    }

    public String getItemSellPrice(){
        return format(itemSellPrice);
    }

    public String getItemBuyPrice(){
        return format(itemBuyPrice);
    }

    public String getItemAveragePrice(){
        return format(itemAveragePrice);
    }

    private String format(long price){
        if(price >= 1000){
            return df.format(price);
        }
        else{
            return String.valueOf(price) + "GP";
        }
    }
}
