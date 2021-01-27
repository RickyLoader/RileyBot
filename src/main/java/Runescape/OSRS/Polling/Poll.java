package Runescape.OSRS.Polling;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wrap OSRS poll data
 */
public class Poll {
    private final Date start, end;
    private final String title, description, url;
    private final int votes, number;
    private final Question[] questions;
    private final boolean open;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Create the poll from the builder
     */
    private Poll(PollBuilder builder) {
        this.number = builder.number;
        this.title = builder.title;
        this.url = builder.url;
        this.description = builder.description;
        this.votes = builder.votes;
        this.start = builder.start;
        this.end = builder.end;
        this.questions = builder.questions;
        this.open = new Date(System.currentTimeMillis()).before(end);
    }

    public static class PollBuilder {
        private Date start, end;
        private String title, description, url;
        private int votes, number;
        private Question[] questions;

        /**
         * Set the start date of the poll
         *
         * @param start Start date of poll
         * @return Builder
         */
        public PollBuilder setStartDate(Date start) {
            this.start = start;
            return this;
        }

        /**
         * Set the end date of the poll
         *
         * @param end End date of poll
         * @return Builder
         */
        public PollBuilder setEndDate(Date end) {
            this.end = end;
            return this;
        }

        /**
         * Set the title of the poll
         *
         * @param title Poll title
         * @return Builder
         */
        public PollBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the description of the poll.
         * Usually a summary of/reasoning behind the poll contents shown to the player prior to voting.
         *
         * @param description Poll description
         * @return Builder
         */
        public PollBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Set the URL to the poll on the OSRS wiki
         *
         * @param url URL to wiki page
         * @return Builder
         */
        public PollBuilder setURL(String url) {
            this.url = url;
            return this;
        }

        /**
         * Set the total number of votes in the poll
         *
         * @param votes Total number of votes
         * @return Builder
         */
        public PollBuilder setTotalVotes(int votes) {
            this.votes = votes;
            return this;
        }

        /**
         * Set the poll number - E.g the first ever poll was Poll #1
         *
         * @param number Poll number
         * @return Builder
         */
        public PollBuilder setPollNumber(int number) {
            this.number = number;
            return this;
        }

        /**
         * Set the questions in the poll
         *
         * @param questions Poll questions
         * @return Builder
         */
        public PollBuilder setQuestions(Question[] questions) {
            this.questions = questions;
            return this;
        }

        /**
         * Build the poll
         *
         * @return Poll from builder values
         */
        public Poll build() {
            if(start == null || end == null) {
                throw new IllegalArgumentException("A poll must include a start and end date");
            }
            return new Poll(this);
        }
    }

    /**
     * Get the URL to the poll
     *
     * @return Poll URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the open period of the poll.
     * Start date - End date (Open/Closed)
     *
     * @return String containing poll open period
     */
    public String getOpenPeriod() {
        return getStartDateFormatted() +
                " - " +
                getEndDateFormatted() +
                " (" +
                (open ? "Open" : "Closed") +
                ")";
    }

    /**
     * Get the start date formatted as dd/MM/yyyy
     *
     * @return Formatted start date
     */
    public String getStartDateFormatted() {
        return dateFormat.format(start);
    }

    /**
     * Get the end date formatted as dd/MM/yyyy
     *
     * @return Formatted end date
     */
    public String getEndDateFormatted() {
        return dateFormat.format(end);
    }

    /**
     * Get whether poll is open
     *
     * @return Poll is running
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Get the title of the poll
     *
     * @return Poll title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the start date of the poll
     *
     * @return Poll start date
     */
    public Date getStartDate() {
        return start;
    }

    /**
     * Get the end date of the poll
     *
     * @return Poll end date
     */
    public Date getEndDate() {
        return end;
    }

    /**
     * Get the poll number
     *
     * @return Poll number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Get the total number of votes for the poll
     *
     * @return Total votes
     */
    public int getTotalVotes() {
        return votes;
    }

    /**
     * Get the description of the poll
     *
     * @return Poll description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the poll questions
     *
     * @return Poll questions
     */
    public Question[] getQuestions() {
        return questions;
    }

    /**
     * Get the total number of votes as a String-
     * formatted with a comma
     *
     * @return Formatted votes
     */
    public String getFormattedTotalVotes() {
        return new DecimalFormat("#,###").format(votes);
    }
}