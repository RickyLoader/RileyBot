package Valheim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valheim server log item
 */
public class LogItem {
    private final Date date;
    private final TYPE type;
    private final long steamId, zdoid, zdoidIdentifier;
    private final int day;
    private final String message, characterName, worldName, eventName, locationFound, clientVersion, serverVersion;

    public final static String
            DATE = "date",
            STEAM_ID = "steam",
            CHARACTER_NAME = "charactername",
            WORLD_NAME = "worldname",
            EVENT_NAME = "eventname",
            ZDOID = "zdoid",
            DAY = "day",
            ZDOID_IDENTIFIER = "zdoididentifier",
            LOCATION = "location",
            CLIENT_VERSION = "clientversion",
            SERVER_VERSION = "serverversion";

    public enum TYPE {
        CONNECTION_STARTED,
        CONNECTION_COMPLETE,
        DEATH,
        RESPAWN,
        WORLD_INFO,
        DISCONNECTION,
        SERVER_STOP,
        SERVER_START,
        RANDOM_EVENT,
        DAY_STARTED,
        LOCATION_FOUND,
        DUNGEON_LOADED,
        CLIENT_SERVER_MISMATCH,
        IGNORE;

        private final String prefix = "\\[Info   : Unity Log\\] (?<" + DATE + ">\\d{2}\\/\\d{2}\\/\\d{4} \\d{2}:\\d{2}:\\d{2}): ";

        /**
         * Get the log type from the given log message
         *
         * @param logMessage Log message to get type for
         * @return Log type
         */
        public static TYPE fromLogMessage(String logMessage) {
            for(TYPE type : TYPE.values()) {
                if(logMessage.matches(type.getRegex())) {
                    return type;
                }
            }
            return IGNORE;
        }

        /**
         * Get the regular expression used to match a log message of the given type
         *
         * @return Regular expression
         */
        public String getRegex() {
            String defaultZdoid = "-?\\d+";
            String version = "(\\d\\.? ?@?)+";
            switch(this) {
                case DISCONNECTION:
                    return prefix + "Closing socket (?<" + STEAM_ID + ">\\d+)";
                case CONNECTION_STARTED:
                    return prefix + "Got connection SteamID (?<" + STEAM_ID + ">\\d+)";
                case CONNECTION_COMPLETE:
                    return getPlayerEventRegex(defaultZdoid, "1");
                case DEATH:
                    return getPlayerEventRegex("0", "0");
                case RESPAWN:
                    return getPlayerEventRegex(defaultZdoid, "\\d+");
                case WORLD_INFO:
                    return prefix + "Get create world (?<" + WORLD_NAME + ">.+)";
                case SERVER_START:
                    return prefix + "Game server connected";
                case SERVER_STOP:
                    return prefix + "Net scene destroyed";
                case RANDOM_EVENT:
                    return prefix + "Random event set:(?<" + EVENT_NAME + ">.+)";
                case DAY_STARTED:
                    return prefix + "Time \\d+.?\\d+, day:(?<" + DAY + ">\\d+)    nextm:\\d+.?\\d+  skipspeed:\\d+.?\\d+";
                case LOCATION_FOUND:
                    return prefix + "Found location of type (?<" + LOCATION + ">.+)";
                case DUNGEON_LOADED:
                    return prefix + "Dungeon loaded \\d+";
                case CLIENT_SERVER_MISMATCH:
                    String clientVersion = "(?<" + CLIENT_VERSION + ">" + version + ")";
                    String serverVersion = "(?<" + SERVER_VERSION + ">" + version + ")";
                    return prefix + "Peer (?<" + STEAM_ID + ">\\d+) has incompatible version, mine:"
                            + serverVersion
                            + " remote " + clientVersion;
                default:
                    return prefix;
            }
        }

        /**
         * Events involving a player follow the format - "Got character ZDOID from CHARACTER_NAME : zdoid:zdoid identifier"
         * where the zdoid is the character's session id and the identifier dictates the event which has occurred
         *
         * @param zdoid           Zdoid regex
         * @param zdoidIdentifier Zdoid identifier regex
         * @return Player event regex
         */
        private String getPlayerEventRegex(String zdoid, String zdoidIdentifier) {
            return prefix + "Got character ZDOID from (?<" + CHARACTER_NAME + ">.+) : (?<" + ZDOID + ">"
                    + zdoid + "):" + "(?<" + ZDOID_IDENTIFIER + ">" + zdoidIdentifier + ")";
        }
    }

    /**
     * Create a log item from the builder values
     *
     * @param builder Log item builder
     */
    private LogItem(LogItemBuilder builder) {
        this.date = builder.date;
        this.message = builder.message;
        this.type = builder.type;
        this.steamId = builder.steamId;
        this.zdoid = builder.zdoid;
        this.zdoidIdentifier = builder.zdoidIdentifier;
        this.characterName = builder.characterName;
        this.worldName = builder.worldName;
        this.eventName = builder.eventName;
        this.day = builder.day;
        this.locationFound = builder.locationFound;
        this.clientVersion = builder.clientVersion;
        this.serverVersion = builder.serverVersion;
    }

    /**
     * Parse a message from the server log in to a log item
     *
     * @param logMessage Log message
     */
    public static LogItem fromLogMessage(String logMessage) {
        TYPE type = TYPE.fromLogMessage(logMessage);
        Matcher matcher = Pattern.compile(type.getRegex()).matcher(logMessage);

        if(!matcher.find()) {
            return null;
        }
        LogItemBuilder builder = new LogItemBuilder(
                parseDate(matcher.group(DATE)),
                logMessage,
                type
        );

        switch(type) {
            case CONNECTION_STARTED:
            case DISCONNECTION:
                builder.setSteamId(Long.parseLong(matcher.group(STEAM_ID)));
                break;
            case CONNECTION_COMPLETE:
            case RESPAWN:
            case DEATH:
                builder.setZdoid(Long.parseLong(matcher.group(ZDOID)))
                        .setZdoidIdentifier(Long.parseLong(matcher.group(ZDOID_IDENTIFIER)))
                        .setCharacterName(matcher.group(CHARACTER_NAME));
                break;
            case WORLD_INFO:
                builder.setWorldName(matcher.group(WORLD_NAME));
                break;
            case DAY_STARTED:
                builder.setDay(Integer.parseInt(matcher.group(DAY)));
                break;
            case RANDOM_EVENT:
                builder.setEventCodename(matcher.group(EVENT_NAME));
                break;
            case LOCATION_FOUND:
                builder.setLocationFound(matcher.group(LOCATION));
                break;
            case CLIENT_SERVER_MISMATCH:
                builder.setClientVersion(matcher.group(CLIENT_VERSION))
                        .setServerVersion(matcher.group(SERVER_VERSION))
                        .setSteamId(Long.parseLong(matcher.group(STEAM_ID)));
                break;
        }
        return builder.build();
    }

    public static class LogItemBuilder {
        private final Date date;
        private final TYPE type;
        private final String message;
        private String characterName, worldName, eventName, locationFound, clientVersion, serverVersion, modVersion;
        private int day;
        private long steamId, zdoid, zdoidIdentifier;

        /**
         * Initialise the builder with the required values
         *
         * @param date    Date of log
         * @param message Log message
         * @param type    Log type
         */
        public LogItemBuilder(Date date, String message, TYPE type) {
            this.date = date;
            this.message = message;
            this.type = type;
        }

        /**
         * Set the steam id from the log message
         *
         * @param steamId Steam id
         * @return Builder
         */
        public LogItemBuilder setSteamId(long steamId) {
            this.steamId = steamId;
            return this;
        }

        /**
         * Set the client version from a client server mismatch log message
         *
         * @param clientVersion Client version e.g "0.148.7@0.9.7"
         * @return Builder
         */
        public LogItemBuilder setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
            return this;
        }

        /**
         * Set the server version from a client server mismatch log message
         *
         * @param serverVersion Server version e.g "0.148.7"
         * @return Builder
         */
        public LogItemBuilder setServerVersion(String serverVersion) {
            this.serverVersion = serverVersion;
            return this;
        }

        /**
         * Set the location found from the log message
         *
         * @param locationFound Location which has been discovered
         * @return Builder
         */
        public LogItemBuilder setLocationFound(String locationFound) {
            this.locationFound = locationFound;
            return this;
        }

        /**
         * Set the game day
         *
         * @param day Game day
         * @return Builder
         */
        public LogItemBuilder setDay(int day) {
            this.day = day;
            return this;
        }

        /**
         * Set the zdoid (session id) from the log message
         *
         * @param zdoid Session id
         * @return Builder
         */
        public LogItemBuilder setZdoid(long zdoid) {
            this.zdoid = zdoid;
            return this;
        }

        /**
         * Set the zdoid identifier from the log message
         * Is paired with the zdoid in the format zdoid:zdoid identifier and is used to identify the event which
         * has occurred to the player of the given zdoid
         *
         * @param zdoidIdentifier Identifier for the zdoid
         * @return Builder
         */
        public LogItemBuilder setZdoidIdentifier(long zdoidIdentifier) {
            this.zdoidIdentifier = zdoidIdentifier;
            return this;
        }

        /**
         * Set the character name from the log message
         *
         * @param characterName Character name
         * @return Builder
         */
        public LogItemBuilder setCharacterName(String characterName) {
            this.characterName = characterName;
            return this;
        }

        /**
         * Set the name of the world from the log message
         *
         * @param worldName World name
         * @return Builder
         */
        public LogItemBuilder setWorldName(String worldName) {
            this.worldName = worldName;
            return this;
        }

        /**
         * Set the codename of a random event from the log message - e.g "skeletons"
         *
         * @param eventName Random event codename
         * @return Builder
         */
        public LogItemBuilder setEventCodename(String eventName) {
            this.eventName = eventName;
            return this;
        }

        /**
         * Create the log item from the builder values
         *
         * @return Log item
         */
        public LogItem build() {
            return new LogItem(this);
        }
    }

    /**
     * Attempt to parse the date from the given date String
     * Date String is assumed to be in UTC time
     *
     * @param date Date String
     * @return Date or null
     */
    public static Date parseDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(date);
        }
        catch(ParseException e) {
            return null;
        }
    }

    /**
     * Get the date of the log
     *
     * @return Log date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the log type
     *
     * @return Log type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the full log message
     *
     * @return Log message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the character name from the log message
     *
     * @return Character name
     */
    public String getCharacterName() {
        return characterName;
    }

    /**
     * Get the zdoid (session id) from the log message
     *
     * @return Session id
     */
    public long getZdoid() {
        return zdoid;
    }

    /**
     * Get the zdoid identifier from the log message
     * Is paired with the zdoid in the format zdoid:zdoid identifier and is used to identify the event which
     * has occurred to the player of the given zdoid;
     *
     * @return Zdoid identifier
     */
    public long getZdoidIdentifier() {
        return zdoidIdentifier;
    }

    /**
     * Get the world name
     *
     * @return World name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Get the location which was found
     *
     * @return Location found
     */
    public String getLocationFound() {
        return locationFound;
    }

    /**
     * Get the client version from a client server mismatch log
     *
     * @return Client version - e.g "0.148.7@0.9.7"
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * Get the server version from a client server mismatch log
     *
     * @return Server version - e.g "0.148.7@0.97"
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Get the random event codename - e.g "skeletons"
     *
     * @return Random event codename
     */
    public String getEventCodename() {
        return eventName;
    }

    /**
     * Get the game day
     *
     * @return Game day
     */
    public int getDay() {
        return day;
    }

    /**
     * Get the steam id from the log message
     *
     * @return Steam id
     */
    public long getSteamId() {
        return steamId;
    }
}
