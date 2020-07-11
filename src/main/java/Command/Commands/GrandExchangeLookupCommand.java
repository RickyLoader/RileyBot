package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import OSRS.Exchange.ExchangeData;

public class GrandExchangeLookupCommand extends DiscordCommand {
    private ExchangeData exchangeData = new ExchangeData();

    public GrandExchangeLookupCommand() {
        super("![a-zA-Z:-_() ]+", "Search the OSBuddy Exchange for shit you can't buy!","![ITEM NAME]");
    }

    @Override
    public void execute(CommandContext context) {
        String item = context.getLowerCaseMessage().split("!")[1];

        // Object containing exchange data is read in on start up. If the data is > 15 minutes old, refresh it
        if((System.currentTimeMillis() - exchangeData.getLastCalled()) > 900000) {
            exchangeData = new ExchangeData();
        }
        context.getMessageChannel().sendMessage(exchangeData.requestItem(item)).queue();
    }
}
