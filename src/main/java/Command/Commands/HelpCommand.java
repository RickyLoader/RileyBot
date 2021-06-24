package Command.Commands;

import Command.Structure.*;

import java.util.List;

public class HelpCommand extends DiscordCommand {

    public HelpCommand() {
        super("help!", "Find out what I can do!");
    }

    @Override
    public void execute(CommandContext context) {
        PageableTableEmbed<DiscordCommand> helpTable = getHelpTable(context);
        helpTable.showMessage();
    }

    public PageableTableEmbed<DiscordCommand> getHelpTable(CommandContext context) {
        return new PageableTableEmbed<DiscordCommand>(
                context,
                context.getCommands(),
                context.getJDA().getSelfUser().getAvatarUrl(),
                "RileyBot Commands",
                "Here's some stuff I can do, now fuck off.",
                "Type: " + getTrigger(),
                new String[]{"Trigger", "Description"},
                5
        ) {

            @Override
            public String getNoItemsDescription() {
                return "Where have my commands gone?";
            }

            @Override
            public String[] getRowValues(int index, DiscordCommand item, boolean defaultSort) {
                return new String[]{item.getTrigger(), item.getDesc()};
            }

            @Override
            public void sortItems(List<DiscordCommand> items, boolean defaultSort) {
                items.sort((o1, o2) -> defaultSort
                        ? o1.getTrigger().compareTo(o2.getTrigger())
                        : o2.getTrigger().compareTo(o1.getTrigger()
                ));
            }
        };
    }
}
