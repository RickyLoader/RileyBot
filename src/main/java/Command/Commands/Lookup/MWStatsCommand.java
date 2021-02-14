package Command.Commands.Lookup;

import Bot.FontManager;
import COD.CombatRecordImageBuilder;
import Command.Structure.*;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWStatsCommand extends MWLookupCommand {
    private String weaponName;

    public MWStatsCommand() {
        super(
                "mwlookup",
                "Have a gander at a player's Modern Warfare stats!",
                getHelpText("mwlookup") + " +[weapon/equipment name]"
        );
    }

    @Override
    public void onArgumentsSet(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        CombatRecordImageBuilder combatRecordImageBuilder = new CombatRecordImageBuilder(
                "Type: " + getTrigger() + " for help",
                context.getEmoteHelper(),
                "MW",
                FontManager.MODERN_WARFARE_FONT
        );
        if(weaponName == null) {
            combatRecordImageBuilder.buildCombatRecordImage(
                    getLookupName(),
                    getPlatform(),
                    channel
            );
        }
        else {
            combatRecordImageBuilder.buildWeaponRecordImage(
                    getLookupName(),
                    getPlatform(),
                    weaponName,
                    channel
            );
        }
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query);
        if(!query.contains("+")) {
            weaponName = null;
            return query;
        }
        String[] args = query.split("\\+");
        if(args.length == 1) {
            weaponName = null;
            return query;
        }
        weaponName = args[1].trim();
        return args[0].trim();
    }
}
