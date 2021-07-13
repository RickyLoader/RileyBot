package Command.Commands;

import News.StuffNews;

/**
 * Take Stuff news URLs and embed details about the article
 */
public class StuffNewsCommand extends NewsCommand {

    /**
     * Create an instance of Stuff news
     */
    public StuffNewsCommand() {
        super(new StuffNews());
    }
}
