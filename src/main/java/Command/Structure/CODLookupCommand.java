package Command.Structure;

public abstract class CODLookupCommand extends LookupCommand {
    private PLATFORM platform;

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
            try {
                return valueOf(name.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return NONE;
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
        return fixName(setPlatform(query));
    }

    /**
     * Remove trailing zero from name in query if present
     * and remove hashtag from beginning of uno name
     *
     * @param query Query containing name
     * @return Fixed query
     */
    public String fixName(String query) {
        String name = query.replaceFirst(getTrigger(), "").trim();
        if(name.endsWith("#0")) {
            name = name.replace("#0", "");
        }
        else if(name.startsWith("#")) {
            name = name.replace("#", "");
        }
        return getTrigger() + " " + name;
    }

    /**
     * Strip the platform from the given query
     * and save to a variable
     *
     * @param query Query to remove platform from
     * @return Query with platform removed
     */
    public String setPlatform(String query) {
        PLATFORM platform = PLATFORM.byName(query.split(" ")[0]);
        if(platform == PLATFORM.NONE) {
            this.platform = PLATFORM.ACTI;
        }
        else {
            this.platform = platform;
            query = query.replaceFirst(platform.name().toLowerCase(), "").trim();
        }
        return query;
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || PLATFORM.byName(args[0]) != PLATFORM.NONE && args[1].matches(getTrigger());
    }
}
