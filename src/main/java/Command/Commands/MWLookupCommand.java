package Command.Commands;

import COD.CombatRecord;
import COD.Player;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class MWLookupCommand extends DiscordCommand {

    public MWLookupCommand() {
        super("mwlookup [bnet/psn/xbox/acti] [player#1234]", "Have a gander at a player's stats!");
    }


    @Override
    public void execute(CommandContext context) {
        String[] args = context.getMessageContent().split(" ");
        if(args.length < 3) {
            context.getMessageChannel().sendMessage("mwlookup [bnet/psn/xbox/acti] [player#1234]").queue();
            return;
        }
        Player player = new Player(args[2], args[1]);
        if(player.getData() == null) {
            context.getMessageChannel().sendMessage("I couldn't find " + player.getName() + " on " + player.getPlatform()).queue();
            return;
        }
        context.getMessageChannel().sendFile(new CombatRecord(player).buildImage()).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("mwlookup");
    }
}
