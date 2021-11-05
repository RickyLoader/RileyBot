package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * Main class, start the bot and apply listener
 */
public class DiscordBot {
    public static final String UNKNOWN_USER = "Unknown";

    /**
     * Accepts a Discord token as an argument to link the bot to a Discord application
     *
     * @param args String Discord token used to log bot in
     */
    public static void main(String[] args) {
        if(args.length == 1) {
            login(args[0], new Listener());
        }
        else {
            System.out.println("No discord token specified, please try again using the token as an argument.");
        }
    }

    /**
     * Login procedure for the bot.
     *
     * @param token    Discord token to link bot to application
     * @param listener Event listener
     */
    private static void login(String token, Listener listener) {
        try {
            JDABuilder
                    .create(
                            token,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.DIRECT_MESSAGES
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.ACTIVITY)
                    .addEventListeners(listener)
                    .build();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the name of a user by the given user ID
     *
     * @param jda    JDA for resolving user
     * @param userId ID of user to get name for
     * @return Name of user or "Unknown" if unable to locate in cache
     */
    public static String getUserName(JDA jda, long userId) {
        User user = jda.getUserById(userId);
        return user == null ? UNKNOWN_USER : user.getName();
    }

    /**
     * Get the mention String for the given user ID
     *
     * @param jda    JDA for resolving user
     * @param userId User ID to get mention String for
     * @return Mention String for user ID or "Unknown" if unable to locate in cache
     */
    public static String getUserMention(JDA jda, long userId) {
        User user = jda.getUserById(userId);
        return user == null ? UNKNOWN_USER : user.getAsMention();
    }
}