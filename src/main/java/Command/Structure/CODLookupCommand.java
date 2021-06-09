package Command.Structure;

import net.dv8tion.jda.api.entities.Message;

public abstract class CODLookupCommand extends LookupCommand {
    private PLATFORM platform;

    public enum PLATFORM {
        BATTLE,
        XBOX,
        PSN,
        NONE;

        /**
         * Get a platform by name
         *
         * @param name Name of platform - "battle"
         * @return Platform
         */
        public static PLATFORM byName(String name) {
            name = name.toUpperCase();
            try {
                return valueOf(name);
            }
            catch(IllegalArgumentException e) {
                switch(name) {
                    case "XBL":
                        return XBOX;
                    case "BATTLENET":
                        return BATTLE;
                    default:
                        return NONE;
                }
            }
        }
    }

    public CODLookupCommand(String trigger, String desc, String helpText) {
        super(
                trigger,
                desc,
                getHelpText(trigger)
                        + "\n" + helpText
                        + "\n\nPlatform is assumed to be Battle.net (name#123) unless a platform is specified."
                        + "\n\nAccepted platforms: XBOX, XBL, PSN, BATTLE",
                30
        );
    }

    /**
     * Get the default help text of platform information
     *
     * @param trigger Trigger to prepend
     * @return Default help text
     */
    public static String getHelpText(String trigger) {
        return "[platform] " + trigger + " " + DEFAULT_LOOKUP_ARGS;
    }

    /**
     * Get the requested platform
     *
     * @return Platform
     */
    public PLATFORM getPlatform() {
        return platform;
    }

    /**
     * Strip platform out of query
     *
     * @param query String which triggered command
     * @return trigger [name]
     */
    @Override
    public String stripArguments(String query) {
        return setPlatform(query);
    }

    @Override
    public void processName(String name, CommandContext context) {
        onArgumentsSet(name, context);
    }

    /**
     * Called when player name & platform are set
     *
     * @param name    Player name
     * @param context Context of command
     */
    public abstract void onArgumentsSet(String name, CommandContext context);

    /**
     * Strip the platform from the given query
     * and save to a variable. If no platform is provided,
     * assume Battle.net.
     *
     * @param query Query to remove platform from
     * @return Query with platform removed
     */
    public String setPlatform(String query) {
        String[] args = query.split(" ");
        String platformName = args[0];
        PLATFORM platform = PLATFORM.byName(platformName);
        if(platform == PLATFORM.NONE) {
            this.platform = PLATFORM.BATTLE;
        }
        else {
            this.platform = platform;
            query = query.replaceFirst(platformName, "").trim();
        }
        return query;
    }

    @Override
    public boolean matches(String query, Message message) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || PLATFORM.byName(args[0]) != PLATFORM.NONE && args[1].matches(getTrigger());
    }
}
