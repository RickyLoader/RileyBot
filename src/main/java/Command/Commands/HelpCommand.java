package Command.Commands;

import Command.Structure.*;

import java.util.List;


public class HelpCommand extends DiscordCommand {

    public HelpCommand() {
        super("help!", "Find out what I can do!");
    }

    @Override
    public void execute(CommandContext context) {
        PageableTableEmbed helpTable = getHelpTable(context);
        helpTable.showMessage();
    }

    public PageableTableEmbed getHelpTable(CommandContext context) {
        return new PageableTableEmbed(
                context,
                context.getCommands(),
                context.getJDA().getSelfUser().getAvatarUrl(),
                "RileyBot Commands",
                "Here's some stuff I can do, now fuck off.",
                new String[]{"Trigger", "Description"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                DiscordCommand command = (DiscordCommand) items.get(index);
                return new String[]{command.getHelpName(), command.getDesc()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    DiscordCommand a = (DiscordCommand) o1;
                    DiscordCommand b = (DiscordCommand) o2;
                    if(defaultSort) {
                        return a.getHelpName().compareTo(b.getHelpName());
                    }
                    return b.getHelpName().compareTo(a.getHelpName());
                });
            }
        };
    }
}
