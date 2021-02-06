package Command.Commands.Lookup;

import Bot.FontManager;
import Command.Structure.CommandContext;
import LOL.SummonerImage;
import LOL.SummonerOverview;

/**
 * Look up a LOL summoner and create an image displaying their stats
 */
public class LOLLookupCommand extends SummonerLookupCommand {

    public LOLLookupCommand() {
        super("lollookup", "Look up a summoner's stats!", false);
    }

    @Override
    protected void onSummonerFound(SummonerOverview summonerOverview, CommandContext context) {
        SummonerImage summonerImage = new SummonerImage(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                FontManager.LEAGUE_FONT
        );
        summonerImage.buildImage(summonerOverview, "Type " + getTrigger() + " for help");
    }
}
