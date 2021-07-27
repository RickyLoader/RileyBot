package Command.Commands;

import News.Outlets.LADbible;

/**
 * Take LADbible news URLs and embed details about the article
 */
public class LADbibleCommand extends NewsCommand {

    /**
     * Create an instance of LADbible
     */
    public LADbibleCommand() {
        super(new LADbible());
    }
}
