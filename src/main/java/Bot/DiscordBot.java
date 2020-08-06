package Bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Main class, start the bot and apply listener
 */
public class DiscordBot {

    /**
     * Accepts a Discord token as an argument to link the bot to a Discord application
     *
     * @param args String Discord token used to log bot in
     */
    public static void main(String[] args) {
        if(args.length == 1) {
            login(args[0]);

        }
        else {
            System.out.println("No discord token specified, please try again using the token as an argument.");
        }
    }

    /**
     * Login procedure for the bot.
     *
     * @param token Discord token to link bot to application
     */
    private static void login(String token) {
        try {
            JDABuilder
                    .create(token,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    )
                    .setActivity(Activity.watching("kids through my telescope"))
                    .addEventListeners(new Listener())
                    .build();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}