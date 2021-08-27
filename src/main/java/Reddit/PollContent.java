package Reddit;

import java.util.Date;

/**
 * Reddit poll post
 */
public class PollContent extends PostContent {
    private final String text;
    private final RedditPoll poll;

    /**
     * Create the poll post content
     *
     * @param text Post text - usually describes the poll
     * @param poll Poll from post
     */
    public PollContent(String text, RedditPoll poll) {
        super(TYPE.POLL);
        this.text = text;
        this.poll = poll;
    }

    /**
     * Get the post text - usually describes the poll
     *
     * @return Post text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the poll from the post content
     *
     * @return Reddit poll
     */
    public RedditPoll getPoll() {
        return poll;
    }

    /**
     * Poll found in a Reddit post
     */
    public static class RedditPoll {
        private final int totalVotes;
        private final Date closingDate;
        private final boolean resultsAvailable;
        private final Option[] options;

        /**
         * Create the Reddit poll
         *
         * @param totalVotes       Total votes in the poll
         * @param closingDate      Closing date of the poll
         * @param options          Poll options
         * @param resultsAvailable Results of the poll are available
         */
        public RedditPoll(int totalVotes, Date closingDate, Option[] options, boolean resultsAvailable) {
            this.totalVotes = totalVotes;
            this.closingDate = closingDate;
            this.options = options;
            this.resultsAvailable = resultsAvailable;
        }

        /**
         * Get the poll options.
         *
         * @return Array of poll options
         */
        public Option[] getOptions() {
            return options;
        }

        /**
         * Check if the poll is closed
         *
         * @return Poll is closed
         */
        public boolean isClosed() {
            return new Date().after(closingDate);
        }

        /**
         * Check whether the results of the poll are available.
         * This is true if the poll has closed or a user has voted in the poll.
         *
         * @return Results are available
         */
        public boolean resultsAvailable() {
            return resultsAvailable;
        }

        /**
         * Get the total number of votes in the poll
         *
         * @return Number of poll votes
         */
        public int getTotalVotes() {
            return totalVotes;
        }

        /**
         * Get the closing date of the poll
         *
         * @return Closing date
         */
        public Date getClosingDate() {
            return closingDate;
        }

        /**
         * Reddit poll option
         */
        public static class Option {
            private final String text, id;
            private int votes;

            /**
             * Create a poll option
             *
             * @param id    ID of the option
             * @param text  Option text - e.g "Yes"
             * @param votes Number of votes for the option
             */
            public Option(String id, String text, int votes) {
                this.id = id;
                this.text = text;
                this.votes = votes;
            }

            /**
             * Set the number of votes
             *
             * @param votes Number of votes for the option
             */
            public void updateVotes(int votes) {
                this.votes = votes;
            }

            /**
             * Get the ID of the option
             *
             * @return Option ID
             */
            public String getId() {
                return id;
            }

            /**
             * Get the option text - e.g "Yes"
             *
             * @return Option text
             */
            public String getText() {
                return text;
            }

            /**
             * Get the number of votes for the option.
             * This will be 0 if the poll is still running.
             *
             * @return Number of votes for option
             */
            public int getVotes() {
                return votes;
            }
        }
    }
}
