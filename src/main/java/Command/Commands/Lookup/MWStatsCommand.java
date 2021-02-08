package Command.Commands.Lookup;

import Bot.FontManager;
import COD.CombatRecord;
import Command.Structure.*;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWStatsCommand extends MWLookupCommand {

    public MWStatsCommand() {
        super("mwlookup", "Have a gander at a player's Modern Warfare stats!");
    }

    @Override
    public void onArgumentsSet(String name, CommandContext context) {
        CombatRecord combatRecord = new CombatRecord(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                "MW",
                FontManager.MODERN_WARFARE_FONT
        );
        combatRecord.buildImage(
                getLookupName(),
                "Type: " + getTrigger() + " for help",
                getPlatform()
        );
    }
}
