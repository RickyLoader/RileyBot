package Command.Commands;

import COD.Assets.Ratio;
import Command.Structure.*;
import Valheim.Character;
import Valheim.LogItem;
import Valheim.PlayerConnection;
import Valheim.SteamProfile;
import Valheim.ValheimServer;
import Valheim.Wiki.ValheimEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.*;

import static Bot.GlobalReference.VALHEIM_WIKI;

/**
 * View online players & log events
 */
public class ValheimServerCommand extends OnReadyDiscordCommand {
    private final ValheimServer valheimServer = new ValheimServer();
    private final HashMap<String, Emote> logEmotes = new HashMap<>();
    private String
            death, respawn, connected, connecting, disconnect, dayStarted, serverStarted, serverStopped, dungeonLoaded;

    public ValheimServerCommand() {
        super("valheim", "View details about the Valheim server!", "valheim [players/logs/deaths]");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace(getTrigger(), "").trim();
        MessageChannel channel = context.getMessageChannel();

        if(message.isEmpty() || !message.equals("players") && !message.equals("logs") && !message.equals("deaths")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        channel.sendTyping().queue();
        valheimServer.updateServer();

        if(message.equals("players")) {
            showPlayersEmbed(context);
        }
        else if(message.equals("logs")) {
            showEventsEmbed(context);
        }
        else {
            showDeathsEmbed(context);
        }
    }

    /**
     * Send a pageable message embed displaying a list of characters and their deaths
     *
     * @param context Command context
     */
    private void showDeathsEmbed(CommandContext context) {
        ArrayList<Character> characters = valheimServer.getPlayerList().getKnownCharacters();
        new ValheimServerMessage(
                context,
                characters,
                valheimServer,
                ValheimServerMessage.TYPE.DEATHS,
                "Try: " + getHelpName(),
                new String[]{
                        "Character",
                        "Deaths/Sessions",
                        "Per Session"
                },
                serverStarted,
                serverStopped
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Character character = (Character) items.get(index);
                Ratio deathsPerSession = new Ratio(character.getDeaths(), character.getSessions());
                return new String[]{
                        character.getName(),
                        character.getDeaths() + "/" + character.getSessions(),
                        String.valueOf(deathsPerSession.formatRatio(deathsPerSession.getRatio()))
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    int d1 = ((Character) o1).getDeaths();
                    int d2 = ((Character) o2).getDeaths();
                    return defaultSort ? d2 - d1 : d1 - d2;
                });
            }
        }.showMessage();
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
                },
                serverStarted,
                serverStopped
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
                        connection.getStatus().name()
                                + " since " + new SimpleDateFormat("HH:mm:ss").format(connection.getDate())
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
     * Send a pageable message embed displaying the list of log events on the Valheim server
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
                },
                serverStarted,
                serverStopped
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                LogItem log = (LogItem) items.get(index);
                String message;
                switch(log.getType()) {
                    case RESPAWN:
                        message = respawn + " " + log.getCharacterName() + " respawned";
                        break;
                    case DISCONNECTION:
                        message = disconnect + " " + getOfflinePlayerMessage(log.getSteamId()) + " disconnected";
                        break;
                    case CONNECTION_STARTED:
                        message = connecting + " " + getOfflinePlayerMessage(log.getSteamId()) + " connecting";
                        break;
                    case CLIENT_SERVER_MISMATCH:
                        message = getOfflinePlayerMessage(log.getSteamId())
                                + " incompatible version:\n" +
                                "Server: **" + log.getServerVersion() + "**\n"
                                + "Player: **" + log.getClientVersion() + "**";
                        break;
                    case CONNECTION_COMPLETE:
                        message = connected + " " + log.getCharacterName() + " has arrived!";
                        break;
                    case SERVER_START:
                        message = serverStarted + " " + "Server started";
                        break;
                    case SERVER_STOP:
                        message = serverStopped + " " + "Server stopped";
                        break;
                    case RANDOM_EVENT:
                        ValheimEvent randomEvent = VALHEIM_WIKI.getEventByCodename(log.getEventCodename());
                        String codename = randomEvent.getCodename();
                        message = getLogEmote(codename) + " " + "Random event: "
                                + randomEvent.getStartMessage()
                                + " (" + codename + ")";
                        break;
                    case DAY_STARTED:
                        message = dayStarted + " " + "Day: " + log.getDay() + " has begun!";
                        break;
                    case LOCATION_FOUND:
                        String bossLocation = log.getLocationFound();
                        message = getLogEmote(bossLocation) + " " + "Location found: " + bossLocation;
                        break;
                    case DUNGEON_LOADED:
                        message = dungeonLoaded + " " + "Dungeon loaded";
                        break;
                    // Death
                    default:
                        message = death + " " + log.getCharacterName() + " died!";
                }
                return new String[]{
                        new SimpleDateFormat("dd/MM HH:mm:ss").format(log.getDate()),
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

    /**
     * Get the log emote from the given key in the String format required to display in a message
     *
     * @param key Emote key - e.g "army_eikthyr"
     * @return String formatted emote
     */
    private String getLogEmote(String key) {
        return EmoteHelper.formatEmote(logEmotes.get(key.toLowerCase()));
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        // Random events
        logEmotes.put("army_eikthyr", emoteHelper.getEikthyr());
        logEmotes.put("army_theelder", emoteHelper.getTheElder());
        logEmotes.put("army_bonemass", emoteHelper.getBonemass());
        logEmotes.put("army_moder", emoteHelper.getModer());
        logEmotes.put("army_goblin", emoteHelper.getFuling());
        logEmotes.put("skeletons", emoteHelper.getSkeletonEvent());
        logEmotes.put("blobs", emoteHelper.getBlobEvent());
        logEmotes.put("foresttrolls", emoteHelper.getForestTrollsEvent());
        logEmotes.put("wolves", emoteHelper.getWolfEvent());
        logEmotes.put("surtlings", emoteHelper.getSurtlingEvent());

        // Location discoveries
        logEmotes.put("bonemass", emoteHelper.getBonemass());
        logEmotes.put("goblinking", emoteHelper.getYagluth());
        logEmotes.put("dragon", emoteHelper.getModer());
        logEmotes.put("gd_king", emoteHelper.getTheElder());
        logEmotes.put("eikthyr", emoteHelper.getEikthyr());

        // Basic events
        this.death = EmoteHelper.formatEmote(emoteHelper.getDeath());
        this.respawn = EmoteHelper.formatEmote(emoteHelper.getRespawn());
        this.connected = EmoteHelper.formatEmote(emoteHelper.getConnected());
        this.connecting = EmoteHelper.formatEmote(emoteHelper.getConnecting());
        this.disconnect = EmoteHelper.formatEmote(emoteHelper.getDisconnected());
        this.dungeonLoaded = EmoteHelper.formatEmote(emoteHelper.getDungeonLoaded());
        this.serverStarted = EmoteHelper.formatEmote(emoteHelper.getServerStarted());
        this.serverStopped = EmoteHelper.formatEmote(emoteHelper.getStop());
        this.dayStarted = EmoteHelper.formatEmote(emoteHelper.getDayFine());
    }
}
