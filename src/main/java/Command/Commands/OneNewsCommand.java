package Command.Commands;

import News.Outlets.OneNews;

/**
 * Take Stuff One News article URLs and embed details about the article
 */
public class OneNewsCommand extends NewsCommand {

    /**
     * Create an instance of One News
     */
    public OneNewsCommand() {
        super(new OneNews());
    }
}
