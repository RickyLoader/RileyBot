package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import OSRS.Stats.Hiscores;

import java.io.File;
import java.util.ArrayList;

public class PlayerLookupCommand extends DiscordCommand {
    private final ArrayList<String> currentLookups = new ArrayList<>();
    private final Hiscores hiscores = new Hiscores();

    public PlayerLookupCommand() {
        super("lookup PLAYER", "Lookup a player on the OSRS HiScores!");
    }

    @Override
    public void execute(CommandContext context) {
        String name = context.getMessageContent().replace("lookup ", "").toLowerCase();
        if(currentLookups.contains(name)) {
            context.getMessageChannel().sendMessage("Oi I told you their website is slow, patience is a virtue cunt").queue();
            return;
        }
        currentLookups.add(name);
        new Thread(() -> {
            context.getMessageChannel().sendMessage("Give me a second, their website is slow as fuck").queue();
            context.getMessageChannel().sendTyping().queue();
            File stats = hiscores.lookupPlayer(name);
            if(stats == null) {
                context.getMessageChannel().sendMessage(name + " doesn't exist cunt").queue();
                return;
            }
            context.getMessageChannel().sendFile(stats).queue(fileSent -> {
                currentLookups.remove(name);
                stats.delete();
            });
        }).start();
    }

    @Override
    public boolean matches(String query) {
        return query.split(" ")[0].equalsIgnoreCase("lookup");
    }
}
