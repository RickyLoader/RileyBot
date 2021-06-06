package Command.Commands;

import Command.Structure.*;
import Runescape.OSRS.GE.BankImageBuilder;
import Runescape.OSRS.GE.GrandExchange;
import Runescape.OSRS.GE.Item;
import Runescape.OSRS.Loan.Loan;
import Runescape.OSRS.Loan.ItemQuantity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Track loaned items/coins
 */
public class OSRSLendingCommand extends OnReadyDiscordCommand {
    private final String
            coinValue = "(\\d+)(\\.\\d+)?",
            coinMultiplier = "(" + StringUtils.join(QUANTITY_ABBREVIATION.getSymbols(GP), "|") + ")",
            coinRegex = coinValue + coinMultiplier,
            quantityRegex = "x\\d+",
            footer;
    private static final String
            FORGIVE = "forgive",
            LOANS = "loans",
            GP = "GP",
            UNKNOWN_USER = "Unknown";
    private final BankImageBuilder bankImageBuilder;
    private final GrandExchange grandExchange;
    private final HashMap<Long, Loan> loanMessages;
    private final SimpleDateFormat dateFormat;
    private final Button accept, decline;

    public OSRSLendingCommand() {
        super(
                "osrslend",
                "Lend items to people!",
                "osrslend [@loanee] [items*]"
                        + "\nosrslend " + FORGIVE + " [loan id]"
                        + "\nosrslend " + LOANS
                        + "\nosrslend " + LOANS + " [loan id]"
                        + "\n\n*e.g: twisted bow, 500" + GP + ", monkfish x5"
        );
        this.bankImageBuilder = new BankImageBuilder();
        this.grandExchange = GrandExchange.getInstance();
        this.loanMessages = new HashMap<>();
        this.footer = "Type: " + getTrigger() + " for help";
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss ");
        this.accept = Button.success("accept", "Accept");
        this.decline = Button.danger("decline", "Decline");
    }

    public enum QUANTITY_ABBREVIATION {
        B,
        M,
        K,
        SINGULAR;

        /**
         * Get the abbreviation to use for the given symbol
         *
         * @param symbol Symbol
         * @return Abbreviation
         */
        public static QUANTITY_ABBREVIATION forSymbol(String symbol) {
            try {
                return QUANTITY_ABBREVIATION.valueOf(symbol.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return SINGULAR;
            }
        }

        /**
         * Get the symbols for the abbreviations
         *
         * @param singular Symbol to use for singular
         * @return Symbols for abbreviations
         */
        public static String[] getSymbols(String singular) {
            QUANTITY_ABBREVIATION[] values = QUANTITY_ABBREVIATION.values();
            String[] symbols = new String[values.length];
            for(int i = 0; i < values.length; i++) {
                symbols[i] = values[i].getSymbol(singular);
            }
            return symbols;
        }

        /**
         * Get the abbreviation to use for the given quantity.
         * This is the abbreviation with the largest multiplier applicable to the quantity.
         * E.g - 2500000 = M 25000 = K
         *
         * @param quantity Quantity
         * @return Largest abbreviation for quantity
         */
        public static QUANTITY_ABBREVIATION forQuantity(long quantity) {
            if(quantity > B.getMultiplier()) {
                return B;
            }
            else if(quantity > M.getMultiplier()) {
                return M;
            }
            else if(quantity > K.getMultiplier()) {
                return K;
            }
            return SINGULAR;
        }

        /**
         * Get the symbol of the abbreviation
         *
         * @param singular Symbol to use for singular
         * @return Symbol of abbreviation
         */
        public String getSymbol(String singular) {
            return this == SINGULAR ? singular.toLowerCase() : this.name().toLowerCase();
        }

        /**
         * Get the multiplier that the symbol represents
         *
         * @return Multiplier
         */
        public int getMultiplier() {
            switch(this) {
                case K:
                    return 1000;
                case M:
                    return 1000000;
                case B:
                    return 1000000000;
                default:
                case SINGULAR:
                    return 1;
            }
        }

        /**
         * Get the colour associated with the symbol
         *
         * @return Colour
         */
        public Color getColour() {
            switch(this) {
                case SINGULAR:
                    return Color.YELLOW;
                case K:
                    return Color.WHITE;
                case B:
                case M:
                default:
                    return new Color(EmbedHelper.GREEN);
            }
        }
    }

    @Override
    public void execute(CommandContext context) {
        String command = context.getLowerCaseMessage().replace(getTrigger(), "").trim();
        User user = context.getUser();
        MessageChannel channel = context.getMessageChannel();

        if(command.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        channel.sendTyping().queue();

        if(command.startsWith(LOANS)) {
            command = command.replace(LOANS, "").trim();
            if(command.isEmpty()) {
                showLoans(context, user);
            }
            else {
                showLoanContents(toInteger(command), context);
            }
            return;
        }
        if(command.startsWith(FORGIVE)) {
            command = command.replace(FORGIVE, "").trim();
            forgiveLoan(toInteger(command), context);
            return;
        }

        processLoan(command, context);
    }

    /**
     * Forgive a loan
     *
     * @param id      Loan id
     * @param context Command context
     */
    private void forgiveLoan(int id, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        User user = context.getUser();

        if(id == 0) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        Loan loan = Loan.fetchLoanDetails(id);
        if(loan == null) {
            channel.sendMessage(user.getAsMention() + " That loan doesn't exist!").queue();
            return;
        }
        if(user.getIdLong() != loan.getLoaner()) {
            channel.sendMessage(user.getAsMention() + " That's not your loan to forgive!").queue();
            return;
        }
        if(Loan.deleteLoan(id)) {
            channel.sendMessage(user.getAsMention() + " Loan deleted!").queue();
        }
        else {
            channel.sendMessage(user.getAsMention() + " I wasn't able to delete that loan!").queue();
        }
    }

    /**
     * Show the contents of a loan with the given id
     *
     * @param id      Loan id
     * @param context Command context
     */
    private void showLoanContents(int id, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Loan loan = Loan.fetchLoanDetails(id);
        if(loan == null) {
            channel.sendMessage(context.getUser().getAsMention() + " No loan exists with that ID!").queue();
            return;
        }
        getLoanMessageAction(loan, context.getJDA(), channel, false).queue();
    }

    /**
     * Display a list of loans in a pageable message embed
     *
     * @param context Command context
     * @param user    User who loans belong to
     */
    private void showLoans(CommandContext context, User user) {
        ArrayList<Loan> loans = Loan.fetchLoans(context.getUser().getIdLong());
        if(loans.isEmpty()) {
            context.getMessageChannel().sendMessage(user.getAsMention() + " You don't have any loans!").queue();
            return;
        }
        new PageableTableEmbed(
                context,
                loans,
                EmbedHelper.OSRS_LOGO,
                "Loans | " + user.getName(),
                null,
                footer,
                new String[]{"ID", "User", "Date"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Loan loan = (Loan) items.get(index);
                boolean lending = loan.getLoaner() == user.getIdLong();
                String targetName = getLoanUserName(context.getJDA(), lending ? loan.getLoanee() : loan.getLoaner());
                return new String[]{
                        String.valueOf(loan.getId()),
                        lending ? " to " + targetName : "from " + targetName,
                        dateFormat.format(loan.getDate())
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((Loan) o1).getDate();
                    Date d2 = ((Loan) o2).getDate();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }
        }.showMessage();
    }

    /**
     * Process a loan request
     *
     * @param args    Command args
     * @param context Command context
     */
    private void processLoan(String args, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        User loaner = context.getUser();
        List<Member> targets = context.getMessage().getMentionedMembers();

        if(targets.size() != 1) {
            channel.sendMessage(
                    loaner.getAsMention() + " who are you lending to?\n" + getHelpNameCoded()
            ).queue();
            return;
        }

        User loanee = targets.get(0).getUser();
        if(loaner == loanee) {
            channel.sendMessage(
                    loaner.getAsMention() + " How about you loan yourself off a building cunt"
            ).queue();
            return;
        }
        if(loanee.isBot()) {
            channel.sendMessage(loaner.getAsMention() + " that's a bot bro").queue();
            return;
        }

        args = args
                .replace("@" + loanee.getName().toLowerCase(), "")
                .trim();

        if(args.isEmpty()) {
            channel.sendMessage(
                    loaner.getAsMention()
                            + " I'm sure " + loanee.getAsMention()
                            + " would like a little more than fuck all\n" + getHelpNameCoded()
            ).queue();
            return;
        }

        Loan loan = new Loan(loaner.getIdLong(), loanee.getIdLong(), new Date(), Loan.PENDING_LOAN_ID);
        if(addItems(loan, args.split(","), channel, loaner)) {
            proposeLoan(loan, context.getJDA(), channel);
        }
    }

    /**
     * Send a message detailing a loan to the given channel. Await input from the loanee on whether
     * to accept or decline the loan. Automatically decline after 2 minutes.
     *
     * @param loan    Loan to propose
     * @param jda     JDA for resolving users
     * @param channel Channel to send loan to
     */
    private void proposeLoan(Loan loan, JDA jda, MessageChannel channel) {
        getLoanMessageAction(loan, jda, channel, true).setActionRows(ActionRow.of(accept, decline)).queue(message -> {
            long id = message.getIdLong();
            loanMessages.put(id, loan);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!loanMessages.containsKey(id)) {
                        return;
                    }
                    message.delete().queue();
                    channel.sendMessage(
                            "Loan request timed out, too slow!"
                    ).queue();
                }
            }, 120000);
        });
    }

    /**
     * Get an image of the contents of the loan in a bank
     *
     * @param loan Loan to build image for
     * @param jda  JDA for resolving users
     * @return Loan image
     */
    private byte[] getLoanImage(Loan loan, JDA jda) {
        return ImageLoadingMessage.imageToByteArray(
                bankImageBuilder.buildImage(
                        getLoanUserName(jda, loan.getLoaner())
                                + " -> " + getLoanUserName(jda, loan.getLoanee())
                                + " (" + loan.getFormattedTotalValue() + ")",
                        loan.getItems(),
                        loan.getCoins()
                )
        );
    }

    /**
     * Get the name of a loan user
     *
     * @param jda    JDA for resolving user
     * @param userId ID of user to get name for
     * @return Name of user or "Unknown" if unable to locate in cache
     */
    private String getLoanUserName(JDA jda, long userId) {
        User user = jda.getUserById(userId);
        return user == null ? "Unknown" : user.getName();
    }

    /**
     * Get the mention String for the given user ID
     *
     * @param jda    JDA for resolving user
     * @param userId User ID to get mention String for
     * @return Mention String for user ID or "Unknown" if unable to locate in cache
     */
    private String getLoanUserMention(JDA jda, long userId) {
        User user = jda.getUserById(userId);
        return user == null ? UNKNOWN_USER : user.getAsMention();
    }

    /**
     * Get a message action for sending a message embed detailing the given loan and adding an image of the contents.
     *
     * @param loan    Loan to display
     * @param jda     JDA for resolving users
     * @param request Loan request
     * @param channel Channel to send to
     * @return Loan message action
     */
    private MessageAction getLoanMessageAction(Loan loan, JDA jda, MessageChannel channel, boolean request) {
        String description = "**ID**: " + (loan.getId() == Loan.PENDING_LOAN_ID ? "Pending" : loan.getId())
                + "\n**Date**: " + dateFormat.format(loan.getDate());
        String title = "Loan";

        if(request) {
            title += " Request";
            description += "\n\n**Respond**: " + getLoanUserMention(jda, loan.getLoanee()) + " You have 2 minutes to respond!";
        }

        String attachmentName = "loan.png";
        byte[] loanImage = getLoanImage(loan, jda);

        MessageEmbed loanMessage = new EmbedBuilder()
                .setTitle(title + " | " + getLoanUserName(jda, loan.getLoaner()) + " -> " + getLoanUserName(jda, loan.getLoanee()))
                .setDescription(description)
                .setThumbnail(EmbedHelper.OSRS_LOGO)
                .setImage("attachment://" + attachmentName)
                .setColor(EmbedHelper.RUNESCAPE_ORANGE)
                .setFooter("Type: " + getTrigger() + " for help", EmbedHelper.OSRS_LOGO)
                .build();

        return channel.sendMessage(loanMessage).addFile(loanImage, attachmentName);
    }

    /**
     * Format the given quantity with the provided abbreviation.
     * E.g - 2500000 & M = "2.5M", 25000 & K = "25K"
     *
     * @param quantity     Quantity to format - e.g 25000
     * @param abbreviation Abbreviation to use
     * @param coins        Quantity is coins
     * @return Formatted quantity
     */
    public static String formatQuantity(long quantity, QUANTITY_ABBREVIATION abbreviation, boolean coins) {
        double value = ((double) quantity) / abbreviation.getMultiplier();
        return new DecimalFormat("#.##").format(value) + abbreviation.getSymbol(coins ? GP : "");
    }

    /**
     * Attempt to add the array of item arguments to the loan
     * Return false on errors and send a message with the error to the user.
     *
     * @param loan     Loan to add items to
     * @param itemArgs Item names with optional quantity values
     * @param channel  Channel to send updates to
     * @param loaner   User attempting to add items
     * @return Success of adding items
     */
    private boolean addItems(Loan loan, String[] itemArgs, MessageChannel channel, User loaner) {
        try {
            for(String arg : itemArgs) {
                int quantity = parseQuantity(arg);
                arg = arg.replaceFirst(quantityRegex, "").trim();

                if(arg.matches(coinRegex)) {
                    int coins = parseCoins(arg);
                    if(coins == 0 || !loan.addCoins(coins)) {
                        channel.sendMessage(
                                loaner.getAsMention() + " I will not add **" + arg + "**!"
                        ).queue();
                        throw new Exception();
                    }
                    continue;
                }

                int id = toInteger(arg);
                Item item;
                if(id == 0) {
                    Item[] matching = grandExchange.getItemManager().getItemsByName(arg);
                    if(matching.length == 0) {
                        channel.sendMessage(
                                loaner.getAsMention() + " what the fuck is a **" + arg + "**?"
                        ).queue();
                        throw new Exception();
                    }
                    if(matching.length > 1) {
                        channel.sendMessage(
                                loaner.getAsMention()
                                        + " there are **" + matching.length
                                        + "** items matching **" + arg + "**, use an ID or be more specific!"
                        ).queue();
                        throw new Exception();
                    }
                    item = matching[0];
                }
                else {
                    Item itemById = grandExchange.getItemManager().getItemByID(id);
                    if(itemById == null) {
                        channel.sendMessage(
                                loaner.getAsMention() + " there are no items with the ID **" + id + "**!"
                        ).queue();
                        throw new Exception();
                    }
                    item = itemById;
                }

                if(!loan.addItem(new ItemQuantity(item, quantity))) {
                    channel.sendMessage(
                            loaner.getAsMention()
                                    + " I can't add **x" + quantity + "** "
                                    + item.getName() + ", there would be too many!"
                    ).queue();
                    throw new Exception();
                }
            }
            if(loan.getItems().size() > bankImageBuilder.getMaxItems()) {
                channel.sendMessage(
                        loaner.getAsMention() + " That's too many items bro, settle down"
                ).queue();
                throw new Exception();
            }
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    /**
     * Parse a quantity from an item String - e.g "dragon longsword x5" -> 5
     * If the quantity is too large
     *
     * @param itemString Item String - e.g "dragon longsword x5"
     * @return Integer quantity from String or 1 if a quantity is 0
     */
    private int parseQuantity(String itemString) {
        Matcher matcher = Pattern.compile(quantityRegex).matcher(itemString);
        try {
            if(!matcher.find()) {
                return 1;
            }
            itemString = itemString
                    .substring(matcher.start(), matcher.end())
                    .replace("x", "");

            int value = Integer.parseInt(itemString);
            return value == 0 ? 1 : value;
        }
        catch(NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Parse a String coin value - e.g "2.5m" to the integer value - e.g 2500000
     *
     * @param coinString Coin String - e.g "2.5m"
     * @return Integer coin value - e.g 2500000 or 0 if the value is unable to be parsed
     */
    private int parseCoins(String coinString) {
        try {
            Matcher matcher = Pattern.compile(coinMultiplier).matcher(coinString);
            if(!matcher.find()) {
                throw new NumberFormatException();
            }
            QUANTITY_ABBREVIATION abbreviation = QUANTITY_ABBREVIATION.forSymbol(
                    coinString.substring(matcher.start(), matcher.end())
            );
            coinString = coinString.replace(abbreviation.getSymbol(GP), "");

            double value = Double.parseDouble(coinString); // 2.5m -> 2.5
            if(Double.isInfinite(value)) {
                throw new NumberFormatException();
            }
            value = value * abbreviation.getMultiplier();
            if(value > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
            return (int) (value);
        }
        catch(NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                long messageId = event.getMessageIdLong();
                long userId = event.getUser().getIdLong();

                String buttonId = event.getComponentId();
                Loan loan = loanMessages.get(messageId);
                MessageChannel channel = event.getChannel();

                if(loan == null) {
                    return;
                }

                if(userId != loan.getLoanee() && userId != loan.getLoaner()) {
                    return;
                }

                if(userId == loan.getLoaner() && buttonId.equals(accept.getId())) {
                    return;
                }

                event.deferEdit().queue();
                loanMessages.remove(messageId);
                channel.deleteMessageById(messageId).queue();

                String loaner = getLoanUserMention(jda, loan.getLoaner());
                String loanee = getLoanUserMention(jda, loan.getLoanee());

                // Both users may decline
                if(buttonId.equals(decline.getId())) {
                    if(userId == loan.getLoaner()) {
                        channel.sendMessage(
                                loanee
                                        + " " + loaner + " has revoked the loan offer!"
                        ).queue();
                    }
                    else {
                        channel.sendMessage(
                                loaner
                                        + " " + loanee + " has declined!"
                        ).queue();
                    }
                }
                else {
                    channel.sendTyping().queue();
                    Loan submittedLoan = Loan.submitLoan(loan);
                    if(submittedLoan == null) {
                        channel.sendMessage(
                                loaner
                                        + " something went wrong submitting that loan!"
                        ).queue();
                    }
                    else {
                        getLoanMessageAction(submittedLoan, jda, channel, false).queue();
                    }
                }
            }
        });
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
