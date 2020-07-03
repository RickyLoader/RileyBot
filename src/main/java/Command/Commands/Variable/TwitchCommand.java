package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class TwitchCommand extends VariableLinkCommand {
    public TwitchCommand(String json) {
        super(new String[]{"cath", "lcs", "chipmunk", "clyde", "dizzy"}, "cath, lcs, chipmunk, clyde, dizzy", "Check out these Twitch streamers!", json);
    }
}
