package Command.Structure;

import java.util.Arrays;
import java.util.List;

public abstract class CODLookupCommand extends LookupCommand {
    private final List<String> platforms;
    private String platform;

    public CODLookupCommand(String trigger, String desc) {
        super(
                trigger,
                desc,
                getHelpText(trigger),
                30
        );
        platforms = getPlatforms();
    }

    public CODLookupCommand(String trigger, String desc, String helpText) {
        super(
                trigger,
                desc,
                getHelpText(trigger) + "\n" + helpText,
                30
        );
        platforms = getPlatforms();
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
     * Get the platforms that can be searched
     *
     * @return List of platforms
     */
    private List<String> getPlatforms() {
        return Arrays.asList("battle", "acti", "xbox", "psn");
    }

    /**
     * Get the requested platform
     *
     * @return Platform
     */
    public String getPlatform() {
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
     * Remove trailing zero from name if present
     *
     * @param name Name to fix
     * @return name#0 -> name
     */
    public String fixName(String name) {
        return name.endsWith("#0") ? name.replace("#0", "") : name;
    }

    /**
     * Strip the platform from the given query
     * and save to a variable
     *
     * @param query Query to remove platform from
     * @return Query with platform removed
     */
    public String setPlatform(String query) {
        String platform = query.split(" ")[0];
        if(platform.equals(getTrigger())) {
            this.platform = "acti";
        }
        else {
            this.platform = platform;
            query = query.replaceFirst(platform, "").trim();
        }
        return query;
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || platforms.contains(args[0]) && args[1].matches(getTrigger());
    }
}
