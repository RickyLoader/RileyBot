package Command.Commands.Lookup;

import Bot.DiscordUser;
import COD.CODAPI;
import COD.Gunfight;
import Command.Structure.MatchHistoryCommand;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * View a Modern Warfare player's match history
 */
public class MWHistoryCommand extends MatchHistoryCommand {

    public MWHistoryCommand() {
        super("mwhistory", "MW/Data/");
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.MW);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.MW, channel, user);
    }

    @Override
    public String getSummaryEmbedTitle(String name) {
        return "MW Match Summary: " + name;
    }

    @Override
    public String getHistoryEmbedTitle(String name) {
        return "MW Match History: " + name;
    }

    @Override
    public String getEmbedThumbnail() {
        return Gunfight.thumbnail;
    }

    @Override
    public String getMapImageURL(String mapName) {
        return "https://www.callofduty.com/cdn/app/base-maps/mw/" + mapName + ".jpg";
    }

    @Override
    public String getMatchHistoryJSON(String name, PLATFORM platform) {
        if(platform == PLATFORM.UNO && name.startsWith("#")) {
            name = name.replace("#", "");
        }
        return CODAPI.getMWMatchHistory(name, platform);
    }

    @Override
    public String getMatchPlayersJSON(String matchID, PLATFORM platform) {
        return CODAPI.getMWMatchPlayers(matchID, platform);
    }
}
