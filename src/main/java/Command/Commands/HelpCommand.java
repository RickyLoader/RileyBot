package Command.Commands;

import Command.Structure.*;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;


public class HelpCommand extends PageableEmbedCommand {

    public HelpCommand() {
        super("help!", "Find out what I can do!");
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new HelpMessage(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                context.getCommands(),
                context.getJDA().getSelfUser().getAvatarUrl(),
                "RileyBot Commands",
                "Here's some stuff I can do, now fuck off.",
                new String[]{"Trigger", "Description"}
        );
    }

    public static class HelpMessage extends PageableTableEmbed {

        /**
         * Embedded message that can be paged through with emotes and displays as a table.
         *
         * @param channel     Channel to send embed to
         * @param emoteHelper Emote helper
         * @param items       List of items to be displayed
         * @param thumb       Thumbnail to use for embed
         * @param title       Title to use for embed
         * @param desc        Description to use for embed
         * @param columns     Column headers to display at the top of message
         * @param colour      Optional colour to use for embed
         */
        public HelpMessage(MessageChannel channel, EmoteHelper emoteHelper, List<?> items, String thumb, String title, String desc, String[] columns, int... colour) {
            super(channel, emoteHelper, items, thumb, title, desc, columns, 5, colour);
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

        @Override
        public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
            DiscordCommand command = (DiscordCommand) items.get(index);
            return new String[]{command.getHelpName(), command.getDesc()};
        }
    }
}
