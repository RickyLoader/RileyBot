package Command.Structure;

import Network.Secret;
import Valheim.ValheimServer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Server players/logs
 */
public abstract class ValheimServerMessage extends PageableTableEmbed {

    public enum TYPE {
        PLAYERS,
        LOGS
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
     */
    public ValheimServerMessage(CommandContext context, List<?> items, ValheimServer server, TYPE displayType, String footer, String[] columns) {
        super(
                context,
                items,
                PageableValheimWikiSearchEmbed.THUMBNAIL,
                getTitle(server.getWorldName(), displayType, items.size()),
                getServerDescription(server),
                footer,
                columns,
                5,
                items.isEmpty() ? EmbedHelper.ORANGE : EmbedHelper.GREEN
        );
    }

    /**
     * Get the title to use in the server embed
     *
     * @param serverName  Server name
     * @param displayType Display type
     * @param size        Quantity of display items
     * @return Embed title
     */
    private static String getTitle(String serverName, TYPE displayType, int size) {
        return serverName
                + " | "
                + StringUtils.capitalize(displayType.name().toLowerCase())
                + " | "
                + size + " " + (displayType == TYPE.PLAYERS ? "Online" : "Recent Events");
    }

    /**
     * Get the description to use in the server embed
     *
     * @param server Valheim server
     * @return Embed description
     */
    private static String getServerDescription(ValheimServer server) {
        return "**IP**: " + Secret.VALHEIM_IP + ":" + Secret.VALHEIM_SERVER_PORT
                + "\n**Password**: " + Secret.VALHEIM_SERVER_PASS
                + "\n**Online**: " + (server.isOnline() ? "Yes" : "No")
                + "\n**Day**: " + server.getDay();
    }
}
