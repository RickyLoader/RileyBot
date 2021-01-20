package Runescape.OSRS.Polling;

import Runescape.OSRS.Polling.PollManager.Poll.Question;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Runescape.OSRS.Polling.PollManager.Poll.Question.*;

/**
 * Hold information on OSRS polls
 */
public class PollManager {
    private final HashMap<Integer, Poll> polls;
    private ArrayList<Element> history;
    private long historyFetched;

    public PollManager() {
        this.polls = new HashMap<>();
    }

    /**
     * Extract the Poll history from the OSRS wiki.
     * Polls are table rows within tables of a given class,
     * contains the number, name, and date of polls.
     * First element is the most recent poll.
     *
     * @return List of HTML Table rows representing polls
     */
    private ArrayList<Element> getPollHistory() {
        ArrayList<Element> summary = new ArrayList<>();
        try {
            summary = new ArrayList<>(Jsoup
                    .connect("https://oldschool.runescape.wiki/w/Polls")
                    .get()
                    .select(".wikitable.sortable:not(.mw-collapsible) tr:not(:first-child)"));

            // Reverse so first element is poll #1
            Collections.reverse(summary);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Create a Poll object from the HTML summary element
     *
     * @param summary HTML summary element of poll
     * @return Poll object
     */
    private Poll parsePoll(Element summary, boolean isLatest) {
        Poll poll = null;
        Element titleElement = summary.child(1).child(0);
        try {
            Document doc = Jsoup.connect(titleElement.absUrl("href")).get();
            Element container = doc.selectFirst(".pollWrapper");
            Elements dates = doc.select("b");
            Elements details = container.select("p");
            String votes = details.get(details.size() - 2)
                    .selectFirst("b")
                    .text()
                    .replace("Total Number of Votes: ", "")
                    .replace(",", "");

            poll = new Poll(
                    Integer.parseInt(summary.child(0).text()),
                    titleElement.text(),
                    truncateDescription(details.get(0).text()),
                    isLatest ? 0 : Integer.parseInt(votes),
                    parseWikiDate(dates.get(0).text()),
                    parseWikiDate(dates.get(1).text()),
                    parseQuestions(doc)
            );
        }
        catch(IOException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            System.out.println(
                    "Error parsing: https://oldschool.runescape.wiki/w/Poll:"
                            + titleElement.text().replaceAll(" ", "_")
            );
        }
        return poll;
    }

    /**
     * Attempt to parse the given date String from the wiki in to a date
     *
     * @param date Date String to parse
     * @return Date object (or now if parsing fails)
     */
    private Date parseWikiDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        try {
            return format.parse(date);
        }
        catch(ParseException e) {
            return new Date();
        }
    }

    /**
     * Create an array of questions from a poll HTML document
     *
     * @param doc HTML document containing poll information
     * @return Array of poll questions
     */
    private Question[] parseQuestions(Document doc) {
        Elements questionElements = doc.select(".pollquestion");
        Question[] questions = new Question[questionElements.size()];
        ArrayList<String> options = new ArrayList<>(Arrays.asList("Yes", "No", "Skip question"));

        for(int i = 0; i < questionElements.size(); i++) {
            Element questionElement = questionElements.get(i).selectFirst("table");
            Answer[] answers = parseAnswers(questionElement.select("tr"));

            Answer winner = Arrays.stream(answers).max(Comparator.comparing(Answer::getPercentageVote)).orElse(null);
            boolean opinionQuestion = !Arrays.stream(answers).allMatch(a -> options.contains(a.getText()));
            questions[i] = new Question(
                    i + 1,
                    questionElement.selectFirst("caption").text(),
                    answers,
                    winner != null && (winner.getText().equals("Yes") && winner.getPercentageVote() >= 75.0),
                    winner,
                    opinionQuestion
            );
        }
        return questions;
    }

    /**
     * Create an array of answers from the given list of answer elements
     *
     * @param answerElements Answer elements from question
     * @return Array of answers
     */
    private Answer[] parseAnswers(Elements answerElements) {
        Answer[] answers = new Answer[answerElements.size()];
        for(int i = 0; i < answerElements.size(); i++) {
            Elements columns = answerElements.get(i).select("td");
            String[] voteInfo = columns
                    .get(2)
                    .text()
                    .replace(" (", "")
                    .replace(" votes", "")
                    .replace(")", "")
                    .replace(",", "")
                    .split("%");

            boolean skipQuestion = voteInfo.length == 1;

            answers[i] = new Answer(
                    Integer.parseInt(voteInfo[skipQuestion ? 0 : 1]),
                    skipQuestion ? 0 : Double.parseDouble(voteInfo[0]),
                    columns.get(0).text()
            );
        }
        return answers;
    }

    /**
     * Remove the section of the Poll description which details when the poll will close
     * "This poll will close on Wednesday 25th January."
     *
     * @param desc Poll description
     * @return Poll description without closing date
     */
    private String truncateDescription(String desc) {
        String regex = "(This poll will close on [A-Za-z]+ [\\d]+[A-Za-z]* [A-Za-z]+\\.)";
        Matcher matcher = Pattern.compile(regex).matcher(desc);
        if(matcher.find()) {
            desc = desc.replace(matcher.group(1), "").trim();
        }
        return desc;
    }

    /**
     * Get a specific poll by the number
     *
     * @param id Poll number or 0 for latest
     * @return Poll of given number
     */
    public Poll getPollByNumber(int id) {
        if(polls.containsKey(id)) {
            return polls.get(id);
        }

        long currentTime = System.currentTimeMillis();
        if(history == null || currentTime - historyFetched > 3600000) {
            history = getPollHistory();
            historyFetched = currentTime;
        }

        if(id > history.size()) {
            return null;
        }

        int pollIndex = ((id == 0) ? history.size() : id);

        Poll poll = parsePoll(history.get(pollIndex - 1), pollIndex == history.size());

        // Don't cache latest poll
        if(poll != null && pollIndex != history.size() && !polls.containsKey(poll.getNumber())) {
            polls.put(poll.getNumber(), poll);
        }
        return poll;
    }

    /**
     * Wrap poll data
     */
    public static class Poll {
        private final Date start, end;
        private final String title, description;
        private final int votes, number;
        private final Question[] questions;
        private final boolean open;

        /**
         * Initialise the poll
         *
         * @param number      Poll number
         * @param title       Title of the poll
         * @param description Description of the poll
         * @param votes       Quantity of votes in the poll
         * @param start       Poll start date
         * @param end         Poll end date
         * @param questions   Questions in the poll
         */
        public Poll(int number, String title, String description, int votes, Date start, Date end, Question[] questions) {
            this.number = number;
            this.title = title;
            this.description = description;
            this.votes = votes;
            this.start = start;
            this.end = end;
            this.questions = questions;
            this.open = new Date(System.currentTimeMillis()).before(end);
        }

        /**
         * Get the open period of the poll.
         * Start date - End date (Open/Closed)
         *
         * @return String containing poll open period
         */
        public String getOpenPeriod() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            return dateFormat.format(start) +
                    " - " +
                    dateFormat.format(end) +
                    " (" +
                    (open ? "Open" : "Closed") +
                    ")";
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
        public Date getStart() {
            return start;
        }

        /**
         * Get the end date of the poll
         *
         * @return Poll end date
         */
        public Date getEnd() {
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
         * Get the quantity of votes for the poll
         *
         * @return Vote quantity
         */
        public int getVotes() {
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
         * Wrap poll questions
         */
        public static class Question {
            private final int number;
            private final String text;
            private final Answer[] answers;
            private final boolean pass, opinionQuestion;
            private final Answer winner;

            /**
             * Initialise poll question
             *
             * @param number          Question number
             * @param text            Question text
             * @param answers         Array of answers
             * @param pass            Question passed 75% threshold
             * @param winner          Winning answer
             * @param opinionQuestion Question is an opinion question
             */
            public Question(int number, String text, Answer[] answers, boolean pass, Answer winner, boolean opinionQuestion) {
                this.number = number;
                this.text = number + ". " + text;
                this.answers = answers;
                this.pass = pass;
                this.opinionQuestion = opinionQuestion;
                this.winner = winner;
            }

            /**
             * Get the winning answer
             *
             * @return Winning answer
             */
            public Answer getWinner() {
                return winner;
            }

            /**
             * Return whether the question is an opinion question and therefore not subject to the 75% threshold
             *
             * @return Question is an opinion question
             */
            public boolean isOpinionQuestion() {
                return opinionQuestion;
            }

            /**
             * Return whether question received the 75% passing threshold
             *
             * @return Question passed
             */
            public boolean isPassed() {
                return pass;
            }

            /**
             * Get the question number
             *
             * @return Question number
             */
            public int getNumber() {
                return number;
            }

            /**
             * Get the question text
             *
             * @return Question text
             */
            public String getText() {
                return text.length() > 200 ? text.substring(0, 200).trim() + "..." : text;
            }

            /**
             * Get the answers
             *
             * @return Answers
             */
            public Answer[] getAnswers() {
                return answers;
            }

            /**
             * Wrap question answers
             */
            public static class Answer {
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
        }
    }
}
