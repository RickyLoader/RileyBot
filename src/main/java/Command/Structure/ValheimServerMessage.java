package Command.Structure;

import Network.Secret;
import Valheim.Character;
import Valheim.ValheimServer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Server players/logs
 */
public abstract class ValheimServerMessage extends PageableTableEmbed {

    public enum TYPE {
        PLAYERS,
        LOGS,
        DEATHS;

        /**
         * Get the embed title to use based on the type
         *
         * @param serverName Server name
         * @param items      Display items
         * @return Embed title
         */
        public String getTitle(String serverName, List<?> items) {
            String title = serverName
                    + " | "
                    + StringUtils.capitalize(this.name().toLowerCase())
                    + " | ";
            switch(this) {
                case LOGS:
                    title += items.size() + " Events";
                    break;
                case DEATHS:
                    int deaths = 0;
                    for(Object o : items) {
                        Character c = (Character) o;
                        deaths += c.getDeaths();
                    }
                    title += deaths + " Total";
                    break;
                case PLAYERS:
                    title += items.size() + " Online";
                    break;
            }
            return title;
        }
    }

    /**
     * Valheim server message
     *
     * @param context     Command context
     * @param items       List of items to be displayed
     * @param server      Valheim server
     * @param displayType Display type
     * @param footer      Footer to use in the embed
     * @param columns     Column headers to display at the top of message
     * @param online      String formatted emote to display when the server is online
     * @param offline     String formatted emote to display when the server is offline
     */
    public ValheimServerMessage(CommandContext context, List<?> items, ValheimServer server, TYPE displayType, String footer, String[] columns, String online, String offline) {
        super(
                context,
                items,
                PageableValheimWikiSearchEmbed.THUMBNAIL,
                displayType.getTitle(server.getWorldName(), items),
                getServerDescription(server, online, offline),
                footer,
                columns,
                5,
                items.isEmpty() ? EmbedHelper.ORANGE : EmbedHelper.GREEN
        );
    }

    /**
     * Get the description to use in the server embed
     *
     * @param server  Valheim server
     * @param online  String formatted emote to display when the server is online
     * @param offline String formatted emote to display when the server is offline
     * @return Embed description
     */
    private static String getServerDescription(ValheimServer server, String online, String offline) {
        return "**IP**: " + Secret.VALHEIM_IP + ":" + Secret.VALHEIM_SERVER_PORT
                + "\n**Password**: " + Secret.VALHEIM_SERVER_PASS
                + "\n**Online**: " + (server.isOnline() ? online : offline)
                + "\n**Day**: ~" + server.getDay() + " (Last slept to)";
    }
}
