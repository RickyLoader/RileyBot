package Command.Commands;

import Command.Structure.*;
import Minecraft.MinecraftServer;
import Minecraft.Player;
import Network.Secret;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * View online players/server info for the Minecraft server
 */
public class MinecraftServerCommand extends DiscordCommand {
    private static final String LOGO = "https://i.imgur.com/IoMVICZ.png";
    private final MinecraftServer minecraftServer;

    /**
     * Initialise default Minecraft server
     */
    public MinecraftServerCommand() {
        super("mc", "View information about the minecraft server!");
        this.minecraftServer = new MinecraftServer(Secret.MINECRAFT_SERVER_IP, Secret.MINECRAFT_SERVER_PORT);
    }

    @Override
    public void execute(CommandContext context) {
        new Thread(() -> {
            MessageChannel channel = context.getMessageChannel();
            channel.sendTyping().queue();

            // Attempt to refresh the server data (may still be cached in which case no refresh will occur)
            minecraftServer.refreshServerData();

            displayServerDetails(context);
        }).start();
    }

    /**
     * Display the Minecraft server details in a pageable message embed.
     * The message will display the server details as well as players.
     *
     * @param context Command context
     */
    private void displayServerDetails(CommandContext context) {
        String footer = "Try: " + getTrigger();
        Date nextRefresh = minecraftServer.getCacheExpiryDate();
        if(nextRefresh != null) {
            footer += " | Next refresh at: "
                    + new SimpleDateFormat("HH:mm:ss").format(nextRefresh);
        }

        final int players = minecraftServer.getPlayers().size();

        new PageableTableEmbed<Player>(
                context,
                minecraftServer.getPlayers(),
                LOGO,
                minecraftServer.getDetailsImage(),
                "Minecraft Server | " + minecraftServer.getAddressString(),
                players + " " + (players == 1 ? "Player online, what a loser!" : "Players online:"),
                footer,
                new String[]{"Name", "Details"},
                3,
                minecraftServer.hasData() ? EmbedHelper.GREEN : EmbedHelper.RED
        ) {
            @Override
            public String getNoItemsDescription() {
                return "No players online!";
            }

            @Override
            public int getNoItemsColour() {
                return getColour();
            }

            @Override
            public String[] getRowValues(int index, Player player, boolean defaultSort) {
                return new String[]{
                        player.getName(),
                        EmbedHelper.embedURL("View",player.getDetailsUrl())
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
}
