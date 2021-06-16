package Runescape.OSRS.Polling;

import java.text.DecimalFormat;

/**
 * Wrap question answers
 */
public class Answer {
    private final int votes;
    private final double percentageVote;
    private final String text;

    /**
     * Initialise answer
     *
     * @param votes          Number of votes
     * @param percentageVote Percentage of total vote
     * @param text           Answer text - "Yes/No/Skip"
     */
    public Answer(int votes, double percentageVote, String text) {
        this.votes = votes;
        this.percentageVote = percentageVote;
        this.text = text;
    }

    /**
     * Get the number of votes
     *
     * @return Number of votes
     */
    public int getVotes() {
        return votes;
    }

    /**
     * Get text of answer
     *
     * @return Answer text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the text of the answer truncated to the given index
     *
     * @param truncatedTo Index to truncate to
     * @return Answer text truncated to the given index
     */
    public String getText(int truncatedTo) {
        boolean truncated = true;
        if(truncatedTo < 0) {
            truncatedTo = 0;
        }
        else if(truncatedTo > text.length()) {
            truncatedTo = text.length();
            truncated = false;
        }
        String text = this.text.substring(0, truncatedTo);
        return truncated ? text + "..." : text;
    }

    /**
     * Get the percentage vote
     *
     * @return Percentage vote
     */
    public double getPercentageVote() {
        return percentageVote;
    }

    /**
     * Get a formatted String displaying votes and percentage of total votes
     *
     * @return Formatted String displaying vote summary
     */
    public String formatVotes() {
        return percentageVote == 0
                ? new DecimalFormat("#,### Votes").format(votes)
                : new DecimalFormat("0.00'%'").format(percentageVote);
    }
}
