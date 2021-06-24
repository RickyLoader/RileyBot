package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

import static Command.Structure.EmbedHelper.getTitleField;
import static Command.Structure.EmbedHelper.getValueField;

public abstract class PageableTableEmbed<T> extends PageableTemplateEmbed<T> {
    public static final int MAX_COLUMNS = 3;
    private final String[] columns;

    /**
     * Embedded message that can be paged through with buttons and displays as a table.
     * Takes an image file to use as the embed image.
     *
     * @param context   Command context
     * @param items     List of items to be displayed
     * @param thumb     Thumbnail to use for embed
     * @param imageFile Image file to use as embed image
     * @param title     Title to use for embed
     * @param desc      Description to use for embed
     * @param footer    Footer to use in the embed
     * @param columns   Column headers to display at the top of message
     * @param bound     Maximum items to display
     * @param colour    Optional colour to use for embed
     */
    public PageableTableEmbed(CommandContext context, List<T> items, String thumb, byte[] imageFile, String title, String desc, String footer, String[] columns, int bound, int... colour) {
        super(context, items, thumb, imageFile, title, desc, footer, bound, colour);
        this.columns = columns;
        try {
            if(columns.length > MAX_COLUMNS) {
                throw new IncorrectQuantityException(
                        "A maximum of " + MAX_COLUMNS + " columns can be displayed as a table," +
                                " you provided " + columns.length + " column headers"
                );
            }
        }
        catch(IncorrectQuantityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Embedded message that can be paged through with buttons and displays as a table.
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param thumb   Thumbnail to use for embed
     * @param title   Title to use for embed
     * @param desc    Description to use for embed
     * @param footer  Footer to use in the embed
     * @param columns Column headers to display at the top of message
     * @param bound   Maximum items to display
     * @param colour  Optional colour to use for embed
     */
    public PageableTableEmbed(CommandContext context, List<T> items, String thumb, String title, String desc, String footer, String[] columns, int bound, int... colour) {
        this(context, items, thumb, null, title, desc, footer, columns, bound, colour);
    }

    /**
     * Add the fields required to build a table
     *
     * @param builder      Embed builder to add fields to
     * @param currentIndex Current index within list of items
     * @param item         Item at current index
     */
    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex, T item) {
        String[] rowValues = getRowValues(currentIndex, getItems().get(currentIndex), isDefaultSort());
        try {
            if(rowValues.length != columns.length) {
                throw new IncorrectQuantityException("You must provide an equal quantity of row values to column values");
            }
        }
        catch(IncorrectQuantityException e) {
            e.printStackTrace();
            return;
        }
        for(int i = 0; i < columns.length; i++) {
            if(currentIndex == getIndex()) {
                builder.addField(getTitleField(columns[i], rowValues[i]));
            }
            else {
                builder.addField(getValueField(rowValues[i]));
            }

            // Add blank inline fields to pad out columns such that each column hits the max length
            if(columns.length < MAX_COLUMNS && i == 0) {
                builder.addBlankField(true);

                // Add 2 blank fields for single column tables
                if(columns.length == 1) {
                    builder.addBlankField(true);
                }
            }
        }
    }

    /**
     * Get the list of values to display that coincide with the list of column headers
     *
     * @param index       Current index within items
     * @param item        Item at current index
     * @param defaultSort Sort value
     * @return List of values to display as a row
     */
    public abstract String[] getRowValues(int index, T item, boolean defaultSort);

    /**
     * Exception to throw when incorrect quantity of row or column values provided
     */
    public static class IncorrectQuantityException extends Exception {
        public IncorrectQuantityException(String message) {
            super(message);
        }
    }
}
