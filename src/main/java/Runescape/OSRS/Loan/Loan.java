package Runescape.OSRS.Loan;

import Network.NetworkRequest;
import Runescape.OSRS.GE.GrandExchange;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * OSRS Item/coin loan
 */
public class Loan {
    private final User loaner, loanee;
    private final int id;
    private final HashMap<Integer, ItemQuantity> items;
    private final Date date;
    private int coins;
    public static final String
            LOANER_ID = "loaner_id",
            STATUS_KEY = "status",
            ID_KEY = "loan_id",
            LOANEE_ID = "loanee_id",
            ITEM_ID = "item_id",
            ITEMS_KEY = "items",
            DETAILS_KEY = "details",
            DATE_KEY = "date",
            QUANTITY_KEY = "quantity",
            ENDPOINT = "osrs/lending/";
    public static final int COIN_ID = 0, PENDING_LOAN_ID = 0;


    /**
     * Initialise the loan
     *
     * @param loaner User loaning items
     * @param loanee User receiving items
     * @param date   Date of loan
     * @param id     ID of loan
     */
    public Loan(User loaner, User loanee, Date date, int id) {
        this.loaner = loaner;
        this.loanee = loanee;
        this.date = date;
        this.coins = 0;
        this.items = new HashMap<>();
        this.id = id;
    }

    /**
     * Get the total value of the loan as a String
     *
     * @return Total loan value as String
     */
    public String getFormattedTotalValue() {
        long totalValue = getTotalValue();
        return totalValue == Long.MAX_VALUE
                ? "Lots!"
                : new DecimalFormat("#,###").format(totalValue);
    }

    /**
     * Get the unique ID of the loan
     *
     * @return Loan id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the total value of the loan
     *
     * @return Total value of loan
     */
    private long getTotalValue() {
        long totalValue = coins;
        for(ItemQuantity item : getItems()) {
            try {
                long itemValue = Math.multiplyExact(
                        item.getQuantity(),
                        GrandExchange.getInstance().getItemPrice(item.getItem()).getHigh().getPrice()
                );
                totalValue = Math.addExact(totalValue, itemValue);
            }
            catch(ArithmeticException e) {
                break;
            }
        }
        return totalValue;
    }

    /**
     * Get the date of the loan
     *
     * @return Date of loan
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the user who is borrowing items
     *
     * @return Loanee user
     */
    public User getLoanee() {
        return loanee;
    }

    /**
     * Get the user who is loaning items
     *
     * @return Loaner user
     */
    public User getLoaner() {
        return loaner;
    }

    /**
     * Add coins to the loan
     * Return the success of doing so (integer overflow = false)
     *
     * @param coins Coins to add
     * @return Coins successfully added to loan
     */
    public boolean addCoins(int coins) {
        try {
            this.coins = Math.addExact(this.coins, coins);
            return true;
        }
        catch(ArithmeticException e) {
            return false;
        }
    }

    /**
     * Add an item to the loan.
     * If the loan already contains the given item, increment the quantity.
     *
     * @param itemQuantity Item to add to loan
     * @return Success of adding item
     */
    public boolean addItem(ItemQuantity itemQuantity) {
        int itemId = itemQuantity.getItem().getId();
        if(items.containsKey(itemId)) {
            return items.get(itemId).addQuantity(itemQuantity.getQuantity());
        }
        items.put(itemId, itemQuantity);
        return true;
    }

    /**
     * Get a list of the loaned items
     *
     * @return Loaned items
     */
    public ArrayList<ItemQuantity> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Get the number of coins in the loan
     *
     * @return Loaned coins
     */
    public int getCoins() {
        return coins;
    }

    /**
     * Convert the loan to JSON format
     *
     * @return JSON formatted loan
     */
    public String toJSON() {
        JSONObject loan = new JSONObject()
                .put(LOANER_ID, loaner.getIdLong())
                .put(LOANEE_ID, loanee.getIdLong())
                .put(DATE_KEY, date.getTime());

        JSONArray items = new JSONArray();
        for(ItemQuantity item : getItems()) {
            items.put(
                    new JSONObject()
                            .put(ITEM_ID, item.getItem().getId())
                            .put(QUANTITY_KEY, item.getQuantity())
            );
        }

        items.put(
                new JSONObject()
                        .put(ITEM_ID, COIN_ID)
                        .put(QUANTITY_KEY, coins)
        );
        return new JSONObject()
                .put(DETAILS_KEY, loan)
                .put(ITEMS_KEY, items)
                .toString();
    }

    /**
     * Parse the loan details (loaner, loanee, date) from the given JSON
     *
     * @param loanDetails Loan details JSON
     * @param jda         JDA for resolving users
     * @return Loan from JSON
     */
    private static Loan parseLoanDetails(JSONObject loanDetails, JDA jda) {
        return new Loan(
                jda.getUserById(loanDetails.getLong(LOANER_ID)),
                jda.getUserById(loanDetails.getLong(LOANEE_ID)),
                new Date(loanDetails.getLong(DATE_KEY)),
                loanDetails.getInt(ID_KEY)
        );
    }

    /**
     * Parse a loan from the given loan JSON
     *
     * @param loanJson Loan JSON (loan details & item list)
     * @param jda      JDA for resolving users
     * @return Loan from JSON
     */
    public static Loan fromJSON(JSONObject loanJson, JDA jda) {
        Loan loan = parseLoanDetails(loanJson.getJSONObject(Loan.DETAILS_KEY), jda);
        JSONArray items = loanJson.getJSONArray(ITEMS_KEY);

        for(int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            int id = item.getInt(ITEM_ID);
            int quantity = item.getInt(QUANTITY_KEY);

            if(id == COIN_ID) {
                loan.addCoins(quantity);
            }
            else {
                loan.addItem(
                        new ItemQuantity(
                                GrandExchange.getInstance().getItemManager().getItemByID(id),
                                quantity
                        )
                );
            }
        }
        return loan;
    }

    /**
     * Fetch the details of loans involving the given user
     *
     * @param user User to fetch loans for
     * @param jda  JDA for resolving users
     * @return List of loans involving the given user
     */
    public static ArrayList<Loan> fetchLoans(User user, JDA jda) {
        ArrayList<Loan> loans = new ArrayList<>();
        JSONArray loanDetails = new JSONArray(
                new NetworkRequest(
                        ENDPOINT + "user/" + user.getIdLong(),
                        true
                ).get().body
        );
        for(int i = 0; i < loanDetails.length(); i++) {
            loans.add(parseLoanDetails(loanDetails.getJSONObject(i), jda));
        }
        return loans;
    }

    /**
     * Fetch an existing loan from the database.
     * Return value will be null if no loan exists between the two users
     *
     * @param id  Loan id
     * @param jda JDA for resolving users
     * @return Loan or null
     */
    public static Loan fetchLoanDetails(int id, JDA jda) {
        JSONObject loanDetails = new JSONObject(
                new NetworkRequest(
                        ENDPOINT + id,
                        true
                ).get().body
        );
        return loanDetails.getBoolean(STATUS_KEY) ? fromJSON(loanDetails, jda) : null;
    }


    /**
     * Delete a loan from the database
     *
     * @param id Unique id of loan
     * @return Success of deletion
     */
    public static boolean deleteLoan(int id) {
        JSONObject response = new JSONObject(
                new NetworkRequest(
                        ENDPOINT + "delete/" + id,
                        true
                ).delete().body
        );
        return response.getBoolean(STATUS_KEY);
    }

    /**
     * Submit or update a loan in the database
     *
     * @param loan Loan to submit
     * @param jda  JDA for resolving users
     * @return Updated loan or null
     */
    public static Loan submitLoan(Loan loan, JDA jda) {
        JSONObject response = new JSONObject(
                new NetworkRequest(
                        ENDPOINT + "create",
                        true
                ).post(loan.toJSON()).body
        );
        if(!response.getBoolean("status")) {
            return null;
        }
        return fromJSON(response, jda);
    }
}
