package Command.Commands.Link;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class TobinCommand extends DiscordCommand {
    String link = "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/BB16cpdL.img?h=0&w=720&m=6&q=60&u=t&o=f&l=f&x=555&y=262";

    public TobinCommand() {
        super("tobin", "Tobin Bell!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(link).queue();
    }
}
