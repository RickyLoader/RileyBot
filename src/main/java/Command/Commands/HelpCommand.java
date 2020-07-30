package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.PageableEmbed;
import Command.Structure.PageableEmbedCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;


public class HelpCommand extends PageableEmbedCommand {

    public HelpCommand() {
        super("help!", "Find out what I can do!");
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new HelpMessage(
                context.getMessageChannel(),
                context.getGuild(),
                context.getCommands(),
                context.getJDA().getSelfUser().getAvatarUrl(),
                "Help!",
                "Here's some stuff I can do, now fuck off",
                new String[]{"Trigger", "Description"}
        );
    }

    public static class HelpMessage extends PageableEmbed {

        public HelpMessage(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
            super(channel, guild, items, thumb, title, desc, columns);
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
        public String[] getValues(int index, List<?> items, boolean defaultSort) {
            DiscordCommand command = (DiscordCommand) items.get(index);
            return new String[]{command.getHelpName(), command.getDesc()};
        }
    }
}
