package Command.Structure;

import Network.Secret;
import Valheim.ValheimServer;

import java.util.List;

/**
 * Server players/logs
 */
public abstract class ValheimServerMessage<T> extends PageableTableEmbed<T> {

    /**
     * Valheim server message
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param title   Title to use in the embed
     * @param server  Valheim server
     * @param footer  Footer to use in the embed
     * @param columns Column headers to display at the top of message
     * @param online  String formatted emote to display when the server is online
     * @param offline String formatted emote to display when the server is offline
     */
    public ValheimServerMessage(CommandContext context, List<T> items, String title, ValheimServer server, String footer, String[] columns, String online, String offline) {
        super(
                context,
                items,
                PageableValheimWikiSearchEmbed.THUMBNAIL,
                title,
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
