package Command.Structure;

import Network.Secret;

/**
 * Command which executes when the main man Brock is offline
 */
public abstract class BrockCommand extends AbsentUserCommand {

    /**
     * Create a command which only executes when Brock is offline (or not found).
     *
     * @param trigger  Command trigger
     * @param desc     Description of command
     * @param helpName Command help name
     */
    public BrockCommand(String trigger, String desc, String helpName) {
        super(Secret.BROCK_ID, trigger, desc, helpName);
    }

    /**
     * Create a command which only executes when Brock is offline (or not found).
     *
     * @param trigger Command trigger
     * @param desc    Description of command
     */
    public BrockCommand(String trigger, String desc) {
        this(trigger, desc, trigger);
    }
}
