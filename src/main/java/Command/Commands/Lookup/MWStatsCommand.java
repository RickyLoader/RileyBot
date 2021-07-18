package Command.Commands.Lookup;

import Bot.FontManager;
import COD.CombatRecordImageBuilder;
import Command.Structure.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Look up a Modern Warfare player and build an image with their stats
 */
public class MWStatsCommand extends MWLookupCommand {
    private CombatRecordImageBuilder combatRecordImageBuilder;
    private String weaponName;

    public MWStatsCommand() {
        super(
                "mwlookup",
                "Have a gander at a player's Modern Warfare stats!",
                getHelpText("mwlookup") + " +[weapon/equipment/etc name]"
        );
    }

    @Override
    public void onArgumentsSet(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        if(weaponName == null) {
            combatRecordImageBuilder.buildCombatRecordImage(
                    name,
                    getPlatform(),
                    channel
            );
        }
        else {
            combatRecordImageBuilder.buildQueryImage(
                    name,
                    getPlatform(),
                    weaponName,
                    context
            );
        }
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.combatRecordImageBuilder = new CombatRecordImageBuilder(
                "Type: " + getTrigger() + " for help",
                emoteHelper,
                "MW",
                FontManager.MODERN_WARFARE_FONT
        );
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
