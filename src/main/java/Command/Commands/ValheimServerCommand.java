package Command.Commands;

import COD.Assets.Ratio;
import Command.Structure.*;
import Valheim.Character;
import Valheim.LogItem;
import Valheim.PlayerConnection;
import Valheim.SteamProfile;
import Valheim.ValheimServer;
import Valheim.Wiki.ValheimEvent;
import Valheim.Wiki.ValheimWiki;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

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

        if(valheimServer.getServerEvents().isEmpty()) {
            channel.sendMessage("I am very sorry, I was unable to contact the server!").queue();
            return;
        }

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
        new ValheimServerMessage<Character>(
                context,
                characters,
                getDeathsEmbedTitle(characters),
                valheimServer,
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
            public String getNoItemsDescription() {
                return "I haven't seen anyone die before!";
            }

            @Override
            public String[] getRowValues(int index, Character character, boolean defaultSort) {
                Ratio deathsPerSession = new Ratio(character.getDeaths(), character.getSessions());
                return new String[]{
                        character.getName(),
                        character.getDeaths() + "/" + character.getSessions(),
                        String.valueOf(deathsPerSession.formatRatio(deathsPerSession.getRatio()))
                };
            }

            @Override
            public void sortItems(List<Character> items, boolean defaultSort) {
                items.sort((o1, o2) -> defaultSort
                        ? o2.getDeaths() - o1.getDeaths()
                        : o1.getDeaths() - o2.getDeaths());
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
        new ValheimServerMessage<PlayerConnection>(
                context,
                connections,
                getPlayersEmbedTitle(connections),
                valheimServer,
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
            public String getNoItemsDescription() {
                return "No players online!";
            }

            @Override
            public void sortItems(List<PlayerConnection> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    Date d1 = o1.getDate();
                    Date d2 = o2.getDate();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }

            @Override
            public String[] getRowValues(int index, PlayerConnection connection, boolean defaultSort) {
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
        }.showMessage();
    }

    /**
     * Get the default title to use in the embeds.
     * This is in the format "[World_Name] | [TYPE] |"
     *
     * @param type Type of object to be displayed e.g "Deaths"
     * @return Default embed title
     */
    private String getDefaultEmbedTitle(String type) {
        return valheimServer.getWorldName()
                + " | "
                + StringUtils.capitalize(type.toLowerCase())
                + " |";
    }

    /**
     * Get the title to use in the online players embed.
     * This is in the format "[World_Name] | Players | x Online"
     *
     * @param connections List of connected/connecting players
     * @return Players embed title
     */
    private String getPlayersEmbedTitle(ArrayList<PlayerConnection> connections) {
        return getDefaultEmbedTitle("Players") + connections.size() + " Online";
    }

    /**
     * Get the title to use in the deaths embed.
     * This is in the format "[World_Name] | Deaths | x Total"
     *
     * @param characters List of known characters who have been on the server
     * @return Deaths embed title
     */
    private String getDeathsEmbedTitle(ArrayList<Character> characters) {
        int deaths = 0;
        for(Character c : characters) {
            deaths += c.getDeaths();
        }
        return getDefaultEmbedTitle("Deaths") + " " + deaths + " Total";
    }

    /**
     * Get the title to use in the logs embed.
     * This is in the format "[World_Name] | Logs | x Events"
     *
     * @param logItems List of log events
     * @return Logs embed title
     */
    private String getEventsEmbedTitle(ArrayList<LogItem> logItems) {
        return getDefaultEmbedTitle("Logs") + logItems.size() + " Events";
    }

    /**
     * Send a pageable message embed displaying the list of log events on the Valheim server
     *
     * @param context Command context
     */
    private void showEventsEmbed(CommandContext context) {
        ArrayList<LogItem> logs = valheimServer.getServerEvents();
        new ValheimServerMessage<LogItem>(
                context,
                logs,
                getEventsEmbedTitle(logs),
                valheimServer,
                "Try: " + getHelpName(),
                new String[]{
                        "Time",
                        "Message"
                },
                serverStarted,
                serverStopped
        ) {
            @Override
            public String getNoItemsDescription() {
                return "I can't find any logs!";
            }

            @Override
            public String[] getRowValues(int index, LogItem log, boolean defaultSort) {
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
                        ValheimEvent randomEvent = ValheimWiki.getInstance().getEventByCodename(log.getEventCodename());
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

            @Override
            public void sortItems(List<LogItem> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    Date d1 = o1.getDate();
                    Date d2 = o2.getDate();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
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
        return logEmotes.get(key.toLowerCase()).getAsMention();
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
        this.death = emoteHelper.getDeath().getAsMention();
        this.respawn = emoteHelper.getRespawn().getAsMention();
        this.connected = emoteHelper.getConnected().getAsMention();
        this.connecting = emoteHelper.getConnecting().getAsMention();
        this.disconnect = emoteHelper.getDisconnected().getAsMention();
        this.dungeonLoaded = emoteHelper.getDungeonLoaded().getAsMention();
        this.serverStarted = emoteHelper.getServerStarted().getAsMention();
        this.serverStopped = emoteHelper.getStop().getAsMention();
        this.dayStarted = emoteHelper.getDayFine().getAsMention();
    }
}
