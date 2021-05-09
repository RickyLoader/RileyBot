package Runescape.OSRS.Loan;

import Runescape.OSRS.GE.Item;

/**
 * Item and quantity
 */
public class ItemQuantity {
    private final Item item;
    private int quantity;

    /**
     * Create the item with a quantity
     *
     * @param item     Item
     * @param quantity Quantity
     */
    public ItemQuantity(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * Get the item
     *
     * @return Item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Increment the quantity
     *
     * @param quantity Quantity to increment by
     */
    public boolean addQuantity(int quantity) {
        try {
            this.quantity = Math.addExact(this.quantity, quantity);
            return true;
        }
        catch(ArithmeticException e) {
            return false;
        }
    }

    /**
     * Get the quantity of the item
     *
     * @return Quantity
     */
    public int getQuantity() {
        return quantity;
    }
}
