package Command.Commands;

import Bot.GlobalReference;
import Command.Structure.*;
import Valheim.LogItem;
import Valheim.PlayerConnection;
import Valheim.SteamProfile;
import Valheim.ValheimServer;
import Valheim.Wiki.ValheimEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * View online players & recent events
 */
public class ValheimServerCommand extends DiscordCommand {
    private final ValheimServer valheimServer = new ValheimServer();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ValheimServerCommand() {
        super("valheim", "View details about the Valheim server!", "valheim [players/logs]");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace(getTrigger(), "").trim();
        MessageChannel channel = context.getMessageChannel();

        if(message.isEmpty() || !message.equals("players") && !message.equals("logs")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        channel.sendTyping().queue();
        valheimServer.updateServer();

        if(message.equals("players")) {
            showPlayersEmbed(context);
        }
        else {
            showEventsEmbed(context);
        }
    }

    /**
     * Send a pageable message embed displaying the list of online players on the Valheim server
     *
     * @param context Command context
     */
    private void showPlayersEmbed(CommandContext context) {
        ArrayList<PlayerConnection> connections = valheimServer.getPlayerList().getAllConnections();
        new ValheimServerMessage(
                context,
                connections,
                valheimServer,
                ValheimServerMessage.TYPE.PLAYERS,
                "Try: " + getHelpName(),
                new String[]{
                        "Character",
                        "Steam",
                        "Status"
                }
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                PlayerConnection connection = (PlayerConnection) items.get(index);
                SteamProfile profile = connection.getSteamProfile();

                // Can't know character name prior to connect, can know previously seen character names
                String name = connection.getStatus() == PlayerConnection.STATUS.CONNECTING
                        ? profile.getCharacterNames()
                        : connection.getCharacter().getName();

                return new String[]{
                        name,
                        EmbedHelper.embedURL(profile.getName(), profile.getUrl()),
                        connection.getStatus().name() + " since " + dateFormat.format(connection.getDate())
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((PlayerConnection) o1).getDate();
                    Date d2 = ((PlayerConnection) o2).getDate();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }
        }.showMessage();
    }

    /**
     * Send a pageable message embed displaying the list of recent log events on the Valheim server
     *
     * @param context Command context
     */
    private void showEventsEmbed(CommandContext context) {
        ArrayList<LogItem> logs = valheimServer.getServerEvents();
        new ValheimServerMessage(
                context,
                logs,
                valheimServer,
                ValheimServerMessage.TYPE.LOGS,
                "Try: " + getHelpName(),
                new String[]{
                        "Time",
                        "Message"
                }
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                LogItem log = (LogItem) items.get(index);
                String message;
                switch(log.getType()) {
                    case RESPAWN:
                        message = log.getCharacterName() + " respawned";
                        break;
                    case DISCONNECTION:
                        message = getOfflinePlayerMessage(log.getSteamId()) + " disconnected";
                        break;
                    case CONNECTION_STARTED:
                        message = getOfflinePlayerMessage(log.getSteamId()) + " connecting";
                        break;
                    case CONNECTION_COMPLETE:
                        message = log.getCharacterName() + " has arrived!";
                        break;
                    case WORLD_INFO:
                        message = "Created world " + log.getWorldName();
                        break;
                    case SERVER_START:
                        message = "Server started";
                        break;
                    case SERVER_STOP:
                        message = "Server stopped";
                        break;
                    case RANDOM_EVENT:
                        ValheimEvent randomEvent = GlobalReference.VALHEIM_WIKI.getEventByCodename(log.getEventCodename());
                        message = "Random event: "
                                + randomEvent.getStartMessage()
                                + " (" + randomEvent.getCodename() + ")";
                        break;
                    case DAY_STARTED:
                        message = "Day: " + log.getDay() + " has begun!";
                        break;
                    case LOCATION_FOUND:
                        message = "Location found: " + log.getLocationFound();
                        break;
                    case DUNGEON_LOADED:
                        message = "Character entered dungeon";
                        break;
                    // Death
                    default:
                        message = log.getCharacterName() + " died!";
                }
                return new String[]{
                        dateFormat.format(log.getDate()),
                        message
                };
            }

            /**
             * Get a message in the format [STEAM_NAME](STEAM_URL) (KNOWN_CHARACTER_NAMES)
             *
             * @param steamId Steam id of offline player
             * @return [STEAM_NAME](STEAM_URL) (KNOWN_CHARACTER_NAMES)
             */
            private String getOfflinePlayerMessage(long steamId) {
                SteamProfile profile = valheimServer.getPlayerList().getSteamProfileById(steamId);
                String characterNames = "(" + profile.getCharacterNames().trim() + ")";
                return EmbedHelper.embedURL(profile.getName(), profile.getUrl()) + " " + characterNames;
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((LogItem) o1).getDate();
                    Date d2 = ((LogItem) o2).getDate();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) && !query.equals(getTrigger() + "!");
    }
}
