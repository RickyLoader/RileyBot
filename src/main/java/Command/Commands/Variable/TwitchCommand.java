package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class TwitchCommand extends VariableLinkCommand {
    public TwitchCommand() {
        super("twitch", "Check out these Twitch streamers!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("cath", "https://www.twitch.tv/catherine");
        versions.put("lcs", "https://www.twitch.tv/riotgames");
        versions.put("chipmunk", "https://www.twitch.tv/lilchiipmunk");
        versions.put("clyde", "https://www.twitch.tv/gsxrclyde");
        versions.put("dizzy", "https://www.twitch.tv/dizzykitten");
    }
}
