package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import OSRS.Items.ItemManager;
import OSRS.Items.ItemManager.Item;
import net.dv8tion.jda.api.entities.MessageChannel;

public class OSRSItemCommand extends DiscordCommand {

    private ItemManager itemManager;

    public OSRSItemCommand() {
        super("osrsitem [item name]", "Search for an item on the OSRS wiki!");
    }

    @Override
    public void execute(CommandContext context) {
        String content = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();
        if(content.equals("osrsitem") || !content.startsWith("osrsitem ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(itemManager == null) {
            itemManager = new ItemManager(context.getEmoteHelper());
        }

        String itemName = content.replace("osrsitem ", "");
        Item item = itemManager.searchItem(itemName);
        if(item == null) {
            channel.sendMessage("I wasn't able to find an item matching: \"" + itemName + "\"").queue();
            return;
        }
        itemManager.sendItemEmbed(channel, item, getHelpName());
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrsitem");
    }
}
