package Command.Commands;

import COD.Leaderboard;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class LeaderboardCommand extends DiscordCommand {

    public LeaderboardCommand() {
        super("leaderboard!", "Have a gander at the gunfight leaderboard!");
    }

    @Override
    public void execute(CommandContext context) {
        new Leaderboard(context.getMessageChannel());
    }
}
