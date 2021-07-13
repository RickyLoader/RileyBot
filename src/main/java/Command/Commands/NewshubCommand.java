package Command.Commands;

import News.Newshub;

/**
 * Take Newshub news URLs and embed details about the article
 */
public class NewshubCommand extends NewsCommand {

    /**
     * Create an instance of Newshub
     */
    public NewshubCommand() {
        super(new Newshub());
    }
}
