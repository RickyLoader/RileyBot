package Command.Commands.Passive;

import COD.Gunfight;
import Command.Structure.*;
import COD.Leaderboard;
import COD.Session;

public class LeaderboardCommand extends PageableEmbedCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!");
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new Leaderboard(
                context.getMessageChannel(),
                context.getGuild(),
                Session.getHistory(),
                Gunfight.getThumb(),
                "GUNFIGHT LEADERBOARD!",
                "Here are the top gunfight performances!"
        );
    }
}
