package Command.Commands.JSON;

import Command.Structure.CommandContext;
import Command.Structure.JSONListCommand;

import java.util.Random;

public class MemeCommand extends JSONListCommand {
    public MemeCommand() {
        super("meme", "Posts a random meme!", "meme\nmeme [1-5]", "meme_command.json", "meme");
    }

    @Override
    public void execute(CommandContext context) {
        String[] args = context.getLowerCaseMessage().split(" ");
        if(!args[0].equals(getTrigger())) {
            return;
        }

        int quantity = args.length == 2 ? getQuantity(args[1]) : 1;
        if(quantity == 0) {
            context.getMessageChannel().sendMessage(getHelpNameCoded()).queue();
            return;
        }
        context.getMessage().delete().queue();
        int bound = Math.min(quantity, 5);
        Random rand = new Random();
        String[] list = getList();
        for(int i = 0; i < Math.min(list.length, bound); i++) {
            context.getMessageChannel().sendMessage(list[rand.nextInt(list.length)]).queue();
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger());
    }
}
