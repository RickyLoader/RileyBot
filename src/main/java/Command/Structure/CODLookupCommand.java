package Command.Structure;

import net.dv8tion.jda.api.entities.Message;

public abstract class CODLookupCommand extends LookupCommand {
    private PLATFORM platform;
    private String lookupName;

    public enum PLATFORM {
        BATTLE,
        ACTI,
        XBOX,
        PSN,
        UNO,
        NONE;

        /**
         * Get a platform by name
         *
         * @param name Name of platform - "acti"
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

    public CODLookupCommand(String trigger, String desc) {
        super(
                trigger,
                desc,
                getHelpText(trigger),
                30
        );
    }

    public CODLookupCommand(String trigger, String desc, String helpText) {
        super(
                trigger,
                desc,
                getHelpText(trigger) + "\n" + helpText,
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
        return "[platform] " + getDefaultLookupArgs(trigger);
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
        if(platform == PLATFORM.UNO && name.startsWith("#")) {
            lookupName = name.replace("#", "");
        }
        else if(name.endsWith("#0")) {
            lookupName = name.replace("#0", "");
        }
        else {
            lookupName = name;
        }
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
     * Get the lookup name
     * Lookup name is the name stripped of any characters used only for display purposes
     * e.g '#' in an UNO id is used only to discern between a match id & UNO id when
     * calling the MWHistoryCommand
     *
     * @return Lookup name
     */
    public String getLookupName() {
        return lookupName;
    }

    /**
     * Strip the platform from the given query
     * and save to a variable
     *
     * @param query Query to remove platform from
     * @return Query with platform removed
     */
    public String setPlatform(String query) {
        String[] args = query.split(" ");
        String platformName = args[0];
        PLATFORM platform = PLATFORM.byName(platformName);
        if(platform == PLATFORM.NONE) {
            this.platform = args.length > 1 && args[1].startsWith("#") ? PLATFORM.UNO : PLATFORM.ACTI;
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
