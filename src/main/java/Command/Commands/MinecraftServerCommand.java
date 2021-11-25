package Command.Commands;

import Command.Structure.*;
import Minecraft.MinecraftServer;
import Minecraft.Player;
import Network.Secret;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * View online players/server info for the Minecraft server
 */
public class MinecraftServerCommand extends DiscordCommand {
    private static final String
            TRIGGER = "mc",
            LOGO = "https://i.imgur.com/IoMVICZ.png";
    private final HashMap<String, MinecraftServer> servers;
    private final String mainServerAddress;

    /**
     * Initialise default Minecraft server
     */
    public MinecraftServerCommand() {
        super(
                TRIGGER,
                "View information about the minecraft server!",
                TRIGGER + " [ip:port] | [hostname]"
        );
        this.servers = new HashMap<>();

        MinecraftServer mainServer = new MinecraftServer(Secret.MINECRAFT_SERVER_IP, Secret.MINECRAFT_SERVER_PORT);
        this.mainServerAddress = mainServer.getAddressString();
        mapServer(mainServer);
    }

    /**
     * Map the given server from address String -> server
     *
     * @param server Server to map
     */
    private void mapServer(MinecraftServer server) {
        servers.put(server.getAddressString(), server);
    }

    @Override
    public void execute(CommandContext context) {
        new Thread(() -> {
            MessageChannel channel = context.getMessageChannel();
            channel.sendTyping().queue();

            final String serverAddress = context
                    .getLowerCaseMessage()
                    .replaceFirst(getTrigger(), "")
                    .trim();

            MinecraftServer minecraftServer = serverAddress.isEmpty()
                    ? servers.get(mainServerAddress)
                    : getServer(serverAddress);

            // Attempt to refresh the server data (may still be cached in which case no refresh will occur)
            minecraftServer.refreshServerData();

            displayServerDetails(context, minecraftServer);
        }).start();
    }

    /**
     * Get a minecraft server by address. Retrieve from the map if it is available, otherwise create and map the
     * server.
     *
     * @param serverAddress Server address
     * @return Minecraft server from address
     */
    private MinecraftServer getServer(String serverAddress) {
        final String[] serverArgs = serverAddress.split(":");

        if(servers.containsKey(serverAddress)) {
            return servers.get(serverAddress);
        }

        MinecraftServer server = serverArgs.length == 1
                ? new MinecraftServer(serverAddress)
                : new MinecraftServer(serverArgs[0], toInteger(serverArgs[1]));

        mapServer(server);
        return server;
    }

    /**
     * Display the Minecraft server details in a pageable message embed.
     * The message will display the server details as well as players.
     *
     * @param context Command context
     * @param server  Minecraft server to display
     */
    private void displayServerDetails(CommandContext context, MinecraftServer server) {
        String footer = "Try: " + getHelpName();
        Date nextRefresh = server.getCacheExpiryDate();
        if(nextRefresh != null) {
            footer += " | Next refresh at: "
                    + new SimpleDateFormat("HH:mm:ss").format(nextRefresh);
        }

        final int players = server.getPlayers().size();

        new PageableTableEmbed<Player>(
                context,
                server.getPlayers(),
                LOGO,
                server.getDetailsImage(),
                "Minecraft Server | " + server.getAddressString(),
                null,
                players + " " + (players == 1 ? "Player online, what a loser!" : "Players online:"),
                footer,
                new String[]{"Name", "Details"},
                3,
                server.hasData() ? EmbedHelper.GREEN : EmbedHelper.RED
        ) {
            @Override
            public String getNoItemsDescription() {

                // Player list may be empty even when there are players online, check for this
                return server.getCurrentPlayerCount() == 0 ? "No players online!" : "Player list unavailable!";
            }

            @Override
            public int getNoItemsColour() {
                return getColour();
            }

            @Override
            public String[] getRowValues(int index, Player player, boolean defaultSort) {
                return new String[]{
                        player.getName(),
                        EmbedHelper.embedURL("View", player.getDetailsUrl())
                };
            }

            @Override
            public void sortItems(List<Player> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    String n1 = o1.getName();
                    String n2 = o2.getName();
                    return defaultSort ? n1.compareTo(n2) : n2.compareTo(n1);
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
