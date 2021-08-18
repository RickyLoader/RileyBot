package Command.Structure;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;

/**
 * Command which only activates when a given user is absent (or not found)
 */
public abstract class AbsentUserCommand extends DiscordCommand {
    private final long userId;

    /**
     * Create an absent user command. This will only execute when the user of the given ID is offline (or not found).
     *
     * @param userId   ID of user
     * @param trigger  Command trigger
     * @param desc     Description of command
     * @param helpName Command help name
     */
    public AbsentUserCommand(long userId, String trigger, String desc, String helpName) {
        super(trigger, desc, helpName);
        this.userId = userId;
    }

    /**
     * Create an absent user command. This will only execute when the user of the given ID is offline (or not found).
     *
     * @param userId  ID of user
     * @param trigger Command trigger
     * @param desc    Description of command
     */
    public AbsentUserCommand(long userId, String trigger, String desc) {
        this(userId, trigger, desc, trigger);
    }

    @Override
    public void execute(CommandContext context) {
        Member targetMember = context.getGuild().getMemberById(userId);

        // Don't execute if user is online (fine if they're not found)
        if(targetMember != null && targetMember.getOnlineStatus() != OnlineStatus.OFFLINE) {
            return;
        }
        new Thread(() -> onAbsent(context)).start();
    }

    /**
     * Called when the given user is absent (either offline or not found)
     *
     * @param context Command context
     */
    protected abstract void onAbsent(CommandContext context);

    /**
     * Get the mention String of the absent user (independent of whether they exist)
     *
     * @return Mention String
     */
    public String getUserMention() {
        return "<@" + userId + ">";
    }
}
