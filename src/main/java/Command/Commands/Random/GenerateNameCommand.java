package Command.Commands.Random;

import Command.Structure.CommandContext;
import Command.Structure.RandomCommand;

import java.util.Random;

public class GenerateNameCommand extends RandomCommand {

    public GenerateNameCommand() {
        super("new name!", "Generate a cool name!", "name_command.json", "names");
    }

    @Override
    public void execute(CommandContext context) {
        Random rand = new Random();
        int attempts = 0;
        StringBuilder name = new StringBuilder();
        int maxLength = 15;

        // 3 attempts
        while(attempts <= 2) {

            // Get a random word
            String word = getLink();

            // Append it to the current name if it can fit
            if(name.length() + word.length() <= maxLength) {
                name.append(word);
            }

            // Either way the attempts increment
            attempts++;
        }

        if(rand.nextInt(2) == 1) {
            // How many characters left to to make the max length
            int charToMax = (maxLength - name.length());

            // Generate a number from 0 to the maximum number that could fit in that space. E.g (10^5)-1 = 99,999 (5 chars)
            if(charToMax > 0) {
                name.append(rand.nextInt(10 ^ charToMax));
            }
        }
        context.getTextChannel().sendMessage(name.toString()).queue();
    }
}
