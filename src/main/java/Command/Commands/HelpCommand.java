package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.PageableEmbed;
import Command.Structure.PageableEmbedCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;


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
                "Here's some stuff I can do, now fuck off"
        );
    }

    public static class HelpMessage extends PageableEmbed {

        public HelpMessage(MessageChannel channel, Guild guild, ArrayList<?> items, String thumb, String title, String desc) {
            super(channel, guild, items, thumb, title, desc);
        }

        @Override
        public MessageEmbed.Field[] getField(int index, ArrayList<?> items, boolean header, boolean ascending) {
            DiscordCommand c = (DiscordCommand) items.get(index);
            if(header) {
                return new MessageEmbed.Field[]{
                        getTitleField("**Trigger**", c.getHelpName(), true),
                        getBlankField(true),
                        getTitleField("**Description**", c.getDesc(), true)
                };
            }
            return new MessageEmbed.Field[]{
                    new MessageEmbed.Field(getBlankChar(), c.getHelpName(), true),
                    getBlankField(true),
                    new MessageEmbed.Field(getBlankChar(), c.getDesc(), true)
            };
        }

        @Override
        public void sortItems(ArrayList<?> items, boolean defaultSort) {
            items.sort((o1, o2) -> {
                DiscordCommand a = (DiscordCommand) o1;
                DiscordCommand b = (DiscordCommand) o2;
                if(defaultSort) {
                    return a.getHelpName().compareTo(b.getHelpName());
                }
                return b.getHelpName().compareTo(a.getHelpName());
            });
        }
    }
}
