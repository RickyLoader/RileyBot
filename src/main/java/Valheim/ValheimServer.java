package Valheim;

import Network.Secret;
import Valheim.LogItem.LogItemBuilder;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Valheim server details
 */
public class ValheimServer {
    private final PlayerList playerList;
    private final ArrayList<LogItem> logs;
    private String worldName;
    private int logIndex;

    /**
     * Parse the server logs and populate the current online players/server events
     */
    public ValheimServer() {
        this.logIndex = 0;
        this.playerList = new PlayerList();
        this.logs = new ArrayList<>();
        updateServer();
    }

    /**
     * Get a list of server events (non ignore type logs)
     *
     * @return Server events
     */
    public ArrayList<LogItem> getServerEvents() {
        return logs.stream()
                .filter(logItem -> logItem.getType() != LogItem.TYPE.IGNORE)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get the server player list
     *
     * @return Player list
     */
    public PlayerList getPlayerList() {
        return playerList;
    }

    /**
     * Fetch and parse the server logs, maintaining the lists of events & online players
     */
    public void updateServer() {
        ArrayList<LogItem> logs = fetchLogs();
        if(logs.isEmpty() || logIndex == logs.size() - 1) {
            return;
        }

        for(int i = logIndex; i < logs.size(); i++) {
            LogItem log = logs.get(i);
            switch(log.getType()) {
                case IGNORE:
                    continue;
                case DISCONNECTION:
                    playerList.playerDisconnected(log.getSteamId());
                    break;
                case CONNECTION_COMPLETE:
                    playerList.playerConnected(log.getZdoid(), log.getCharacterName(), log.getDate());
                    break;
                case RESPAWN:
                    // Player is joining not respawning
                    if(playerList.getConnectedPlayerByZdoid(log.getZdoid()) == null) {
                        log = new LogItemBuilder(log.getDate(), log.getMessage(), LogItem.TYPE.CONNECTION_COMPLETE)
                                .setCharacterName(log.getCharacterName())
                                .setZdoid(log.getZdoid())
                                .build();
                        playerList.playerConnected(log.getZdoid(), log.getCharacterName(), log.getDate());
                    }
                    break;
                case CONNECTION_STARTED:
                    playerList.connectionStarted(log.getSteamId(), log.getDate());
                    break;
                case WORLD_INFO:
                    this.worldName = log.getWorldName();
            }
            this.logs.add(log);
        }
        logIndex = logs.size() - 1;
    }

    /**
     * Fetch the server log file as an array of logged lines
     *
     * @return Server logs
     */
    private ArrayList<LogItem> fetchLogs() {
        ArrayList<LogItem> logs = new ArrayList<>();
        try {
            InputStream logInputStream = new URL(getFTPUrl() + "/server.log")
                    .openConnection()
                    .getInputStream();
            String[] logMessages = IOUtils.toString(logInputStream, StandardCharsets.UTF_8).split("\n");
            logInputStream.close();

            for(String logMessage : logMessages) {
                LogItem log = LogItem.fromLogMessage(logMessage);
                if(log == null) {
                    continue;
                }
                logs.add(log);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Get the URL required to connect to the Valheim server via FTP
     *
     * @return FTP URL
     */
    private String getFTPUrl() {
        return "ftp://" + Secret.VALHEIM_USER + ":" + Secret.VALHEIM_FTP_PASS
                + "@" + Secret.VALHEIM_IP + ":" + Secret.VALHEIM_FTP_PORT;
    }

    /**
     * Get the name of the server world
     *
     * @return World name
     */
    public String getWorldName() {
        return worldName;
    }
}
