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
    private final long steamId, zdoid;
    private final String message, characterName, worldName;

    public final static String
            DATE = "date",
            STEAM_ID = "steam",
            CHARACTER_NAME = "charactername",
            WORLD_NAME = "worldname",
            ZDOID = "zdoid";

    public enum TYPE {
        CONNECTION_STARTED,
        CONNECTION_COMPLETE,
        DEATH,
        RESPAWN,
        WORLD_INFO,
        DISCONNECTION,
        IGNORE;

        private final String date = "(?<" + DATE + ">\\d{2}\\/\\d{2}\\/\\d{4} \\d{2}:\\d{2}:\\d{2})";

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
            switch(this) {
                case DISCONNECTION:
                    return date + ": Closing socket (?<" + STEAM_ID + ">\\d+)";
                case CONNECTION_STARTED:
                    return date + ": Got connection SteamID (?<" + STEAM_ID + ">\\d+)";
                case CONNECTION_COMPLETE:
                    return getPlayerEventRegex(defaultZdoid, "1");
                case DEATH:
                    return getPlayerEventRegex("0", "0");
                case RESPAWN:
                    return getPlayerEventRegex(defaultZdoid, "\\d+");
                case WORLD_INFO:
                    return date + ": Get create world (?<" + WORLD_NAME + ">.+)";
                default:
                    return date;
            }
        }

        /**
         * Events involving a player follow the format - "Got character ZDOID from CHARACTER_NAME : ZDOID:VALUE"
         * where the ZDOID is the character's session id and the value dictates the event which has occurred
         *
         * @param zdoid ZDOID regex
         * @param value Value regex
         * @return Player event regex
         */
        private String getPlayerEventRegex(String zdoid, String value) {
            return date + ": Got character ZDOID from (?<" + CHARACTER_NAME + ">.+) : (?<" + ZDOID + ">"
                    + zdoid + "):" + value;
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
        this.characterName = builder.characterName;
        this.worldName = builder.worldName;
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
                builder.setZdoid(Long.parseLong(matcher.group(ZDOID)))
                        .setCharacterName(matcher.group(CHARACTER_NAME));
                break;
            case DEATH:
                builder.setCharacterName(matcher.group(CHARACTER_NAME));
                break;
            case WORLD_INFO:
                builder.setWorldName(matcher.group(WORLD_NAME));
                break;
        }
        return builder.build();
    }

    public static class LogItemBuilder {
        private final Date date;
        private final TYPE type;
        private final String message;
        private String characterName, worldName;
        private long steamId, zdoid;

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
     * Get the world name
     *
     * @return World name
     */
    public String getWorldName() {
        return worldName;
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
