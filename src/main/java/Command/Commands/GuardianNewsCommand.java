package Command.Commands;

import News.Outlets.Guardian;

/**
 * Take news URLs from The Guardian and embed details about the article
 */
public class GuardianNewsCommand extends NewsCommand {
    /**
     * Create an instance of The Guardian
     */
    public GuardianNewsCommand() {
        super(new Guardian());
    }
}
