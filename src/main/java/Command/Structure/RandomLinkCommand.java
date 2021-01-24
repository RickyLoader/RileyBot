package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Random;

/**
 * Send random links from a json file
 */
public class RandomLinkCommand extends JSONListCommand {

    public RandomLinkCommand(String trigger, String desc, String filename, String root) {
        super(trigger, desc, trigger + "\n" + trigger + " [1-5]", filename, root);
    }

    @Override
    public void execute(CommandContext context) {
        String[] args = context.getLowerCaseMessage().split(" ");
        MessageChannel channel = context.getMessageChannel();

        if(!args[0].equals(getTrigger())) {
            return;
        }

        int quantity = args.length == 2 ? getQuantity(args[1]) : 1;
        if(quantity == 0) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        context.getMessage().delete().queue();
        int bound = Math.min(quantity, 5);
        Random rand = new Random();
        String[] list = getList();
        for(int i = 0; i < Math.min(list.length, bound); i++) {
            channel.sendMessage(list[rand.nextInt(list.length)]).queue();
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
