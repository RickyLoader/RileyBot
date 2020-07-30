package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import WordCloud.WordCloudBuilder;

public class WordCloudCommand extends DiscordCommand {
    public WordCloudCommand() {
        super("word cloud", "See a word cloud of the last 100 messages!");
    }

    @Override
    public void execute(CommandContext context) {
        new WordCloudBuilder(context.getMessageChannel());
    }
}
