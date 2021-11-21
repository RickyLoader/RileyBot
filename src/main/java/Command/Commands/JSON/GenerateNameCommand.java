package Command.Commands.JSON;

import Bot.DiscordBot;
import Command.Structure.CommandContext;
import Command.Structure.JSONListCommand;

import java.util.Random;

public class GenerateNameCommand extends JSONListCommand {

    public GenerateNameCommand() {
        super("new name!", "Generate a cool name!", "name_command.json", "names");
    }

    @Override
    public void execute(CommandContext context) {
        Random rand = new Random();
        String[] list = getList();
        StringBuilder name = new StringBuilder();
        int maxLength = 15;

        for(int attempts = 0; attempts < 3; attempts++) {
            String word = list[rand.nextInt(list.length)];
            if(name.length() + word.length() <= maxLength) {
                name.append(word);
            }
        }

        if(name.length() < maxLength && DiscordBot.coinFlip()) {
            int charToMax = (maxLength - name.length());
            name.append(rand.nextInt(10 ^ charToMax));
        }
        context.getTextChannel().sendMessage(name.toString()).queue();
    }
}
