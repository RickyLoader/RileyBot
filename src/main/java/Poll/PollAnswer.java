package Poll;

/**
 * Poll answer
 */
public class PollAnswer {
    private final String title;
    private int votes;

    /**
     * Create the poll answer
     *
     * @param title Title of the answer
     */
    public PollAnswer(String title) {
        this.title = title.length() > 100 ? title.substring(0, 100) : title;
        this.votes = 0;
    }

    /**
     * Get the title of the poll answer
     *
     * @return Poll answer title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Increment the total number of votes
     *
     * @return Total votes
     */
    public int incrementVotes() {
        this.votes++;
        return votes;
    }

    /**
     * Get the number of votes the poll answer has
     *
     * @return Number of votes
     */
    public int getVotes() {
        return votes;
    }
}