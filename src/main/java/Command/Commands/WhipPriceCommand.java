package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Runescape.OSRS.GE.GrandExchange;
import Runescape.OSRS.GE.Item;

import java.text.DecimalFormat;

/**
 * View the price of using a tentacle whip
 */
public class WhipPriceCommand extends DiscordCommand {
    private static final int
            WHIP_ID = 4151,
            TENTACLE_CHARGES = 10000;
    private final Item whip;
    private final GrandExchange grandExchange;

    public WhipPriceCommand() {
        super("whip", "View the price of using a tentacle whip!");
        this.grandExchange = GrandExchange.getInstance();
        this.whip = grandExchange.getItemManager().getItemByID(WHIP_ID);
    }

    @Override
    public void execute(CommandContext context) {
        long buyPrice = grandExchange.getItemPrice(whip).getHigh().getPrice();
        String perChargePrice = new DecimalFormat("#,### GP").format(buyPrice / TENTACLE_CHARGES);

        context.getMessageChannel().sendMessage(
                context.getMember().getAsMention()
                        + " The Tentacle Whip currently costs **"
                        + perChargePrice
                        + "** to use per charge!"
        ).queue();
    }
}
