package Command.Commands;

import News.Outlets.NZHerald;

/**
 * Take NZ Herald news URLs and embed details about the article
 */
public class NZHeraldCommand extends NewsCommand {

    /**
     * Create an instance of NZ Herald
     */
    public NZHeraldCommand() {
        super(new NZHerald());
    }
}
