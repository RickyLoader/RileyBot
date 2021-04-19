package Valheim;

import Network.FTPHandler;
import Network.Secret;
import Valheim.LogItem.LogItemBuilder;
import org.apache.commons.net.ftp.FTPFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Valheim server details
 */
public class ValheimServer {
    private final PlayerList playerList;
    private final ArrayList<LogItem> logs;
    private boolean online = false;
    private final FTPHandler ftpHandler;
    private final String worldName;
    private int logIndex, day;

    /**
     * Parse the server logs and populate the current online players/server events
     */
    public ValheimServer() {
        this.logIndex = 0;
        this.playerList = new PlayerList();
        this.logs = new ArrayList<>();
        this.ftpHandler = new FTPHandler(
                Secret.VALHEIM_IP,
                Secret.VALHEIM_FTP_PORT,
                Secret.VALHEIM_USER,
                Secret.VALHEIM_FTP_PASS
        );
        this.worldName = fetchWorldName();
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
                    String characterName = log.getCharacterName();
                    playerList.playerConnected(log.getZdoid(), characterName, log.getDate());
                    playerList.getCharacterByName(characterName).addSession();
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
                case SERVER_START:
                    online = true;
                    break;
                case SERVER_STOP:
                    online = false;
                    break;
                case DAY_STARTED:
                    day = log.getDay();
                    break;
                case DEATH:
                    playerList.getCharacterByName(log.getCharacterName()).addDeath();
                    break;
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
            String[] logMessages = ftpHandler.getFileAsString("/BepInEx/LogOutput.log").split("\n");
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
     * Fetch the world name
     *
     * @return World name
     */
    private String fetchWorldName() {
        String dir = "/.config/unity3d/IronGate/Valheim/worlds/";
        FTPFile[] dirContents = ftpHandler.getDirectoryContents(dir);
        String[] infoFiles = Arrays.stream(dirContents)
                .map(FTPFile::getName)
                .filter(name -> name.matches(".+.fwl"))
                .toArray(String[]::new);

        if(infoFiles.length == 0) {
            return "-";
        }
        String contents = ftpHandler.getFileAsString(dir + infoFiles[0]).replaceAll("-", "");
        return contents.split("\n")[0].replaceAll("\\P{Print}", "");
    }

    /**
     * Check if the server is online
     *
     * @return Server is online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Get the name of the server world
     *
     * @return World name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Get the current game day
     *
     * @return Game day
     */
    public int getDay() {
        return day;
    }
}
