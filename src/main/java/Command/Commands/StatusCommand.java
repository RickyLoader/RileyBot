package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.Presence;

/**
 * Change the status of the bot
 */
public class StatusCommand extends DiscordCommand {

    private enum STATUS {
        PLAYING,
        WATCHING,
        DEFAULT;

        /**
         * Get a discord status by name
         *
         * @param name Name of status - "playing"
         * @return Status
         */
        public static STATUS byName(String name) {
            try {
                return valueOf(name.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return DEFAULT;
            }
        }
    }

    public StatusCommand() {
        super("status [status]", "Change the status of RileyBot!");
    }

    @Override
    public void execute(CommandContext context) {
        Presence presence = context.getJDA().getPresence();
        String statusMessage = context.getMessageContent().substring(6).trim();
        STATUS status = STATUS.byName(statusMessage.split(" ")[0].trim());

        if(status != STATUS.DEFAULT) {
            statusMessage = statusMessage.substring(status.name().length()).trim();
        }

        switch(status) {
            case PLAYING:
            case DEFAULT:
                presence.setActivity(Activity.playing(statusMessage));
                break;
            case WATCHING:
                presence.setActivity(Activity.watching(statusMessage));
                break;
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("status");
    }
}
