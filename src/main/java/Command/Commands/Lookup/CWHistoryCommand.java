package Command.Commands.Lookup;

import Bot.DiscordCommandManager;
import Bot.DiscordUser;
import COD.CODAPI;
import Command.Commands.COD.CWCountdownCommand;
import Command.Structure.MatchHistoryCommand;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * View a Cold War player's match history
 */
public class CWHistoryCommand extends MatchHistoryCommand {

    public CWHistoryCommand() {
        super("cwhistory", DiscordCommandManager.cwManager);
    }

    @Override
    public String getSummaryEmbedTitle(String name) {
        return "CW Match Summary: " + name;
    }

    @Override
    public String getHistoryEmbedTitle(String name) {
        return "CW Match History: " + name;
    }

    @Override
    public String getEmbedThumbnail() {
        return CWCountdownCommand.thumbnail;
    }

    @Override
    public String getMatchHistoryJSON(String name, PLATFORM platform) {
        return CODAPI.getCWMatchHistory(name, platform);
    }

    @Override
    public String getMatchPlayersJSON(String matchID, PLATFORM platform) {
        return CODAPI.getCWMatchPlayers(matchID, platform);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.CW);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.CW, channel, user);
    }
}
