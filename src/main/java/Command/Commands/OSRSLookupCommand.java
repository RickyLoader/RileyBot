package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import OSRS.Stats.Hiscores;

import java.util.ArrayList;

public class OSRSLookupCommand extends DiscordCommand {
    private final ArrayList<String> currentLookups = new ArrayList<>();

    public OSRSLookupCommand() {
        super("osrslookup [PLAYER]", "Lookup a player on the OSRS HiScores!");
    }

    @Override
    public void execute(CommandContext context) {
        Hiscores hiscores = new Hiscores(context.getMessageChannel());
        String name = context.getLowerCaseMessage().replace("lookup ", "");
        if(currentLookups.contains(name)) {
            context.getMessageChannel().sendMessage("Oi I told you their website is slow, patience is a virtue cunt").queue();
            return;
        }
        currentLookups.add(name);
        new Thread(() -> {
            boolean success = hiscores.lookupPlayer(name);
            if(!success) {
                currentLookups.remove(name);
                return;
            }
            currentLookups.remove(name);
        }).start();
    }

    @Override
    public boolean matches(String query) {
        return query.split(" ")[0].equalsIgnoreCase("osrslookup");
    }
}
