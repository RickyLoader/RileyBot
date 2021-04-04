package Command.Structure;

import net.dv8tion.jda.api.JDA;

/**
 * Discord command with a method to fire when the Bot comes online
 */
public abstract class OnReadyDiscordCommand extends DiscordCommand {
    public OnReadyDiscordCommand(String trigger, String desc, String helpName) {
        super(trigger, desc, helpName);
    }

    /**
     * Fired when the Bot comes online
     *
     * @param jda Discord API
     * @param emoteHelper Emote helper
     */
    public abstract void onReady(JDA jda, EmoteHelper emoteHelper);
}
