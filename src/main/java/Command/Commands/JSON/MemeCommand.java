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
        context.getMessage().delete().complete();
        String[] args = context.getLowerCaseMessage().split(" ");
        if(!args[0].equals(getTrigger())) {
            return;
        }
        int bound = Math.min(getQuantity(args[args.length - 1]), 5);
        Random rand = new Random();
        String[] list = getList();
        for(int i = 0; i < Math.min(list.length, bound); i++) {
            context.getMessageChannel().sendMessage(list[rand.nextInt(list.length)]).queue();
        }
    }

    /**
     * Attempts to pull an integer from the given message when a Random type command is called.
     *
     * @return The integer or 1 if not found. Determines how many times to repeat the command
     */
    private int getQuantity(String arg) {
        try {
            int quantity = Integer.parseInt(arg);
            return quantity == 0 ? 1 : quantity;
        }
        catch(Exception e) {
            return 1;
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger());
    }
}
