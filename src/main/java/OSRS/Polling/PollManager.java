package OSRS.Polling;

import OSRS.Polling.PollManager.Poll.Question.Answer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static OSRS.Polling.PollManager.Poll.*;

/**
 * Hold information on OSRS polls
 */
public class PollManager {
    private final HashMap<Integer, Poll> polls;

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
            Elements dates = doc.select("b");
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");

            poll = new Poll(
                    Integer.parseInt(summary.child(0).text()),
                    titleElement.text(),
                    truncateDescription(doc.select("center").first().text()),
                    isLatest ? 0 : Integer.parseInt(doc.select("center b").first().text().replace("Total Number of Votes: ", "")),
                    format.parse(dates.get(0).text()),
                    format.parse(dates.get(1).text()),
                    parseQuestions(doc)
            );
        }
        catch(IOException | ParseException | NumberFormatException e) {
            System.out.println("Error parsing: https://oldschool.runescape.wiki/w/Poll:" + titleElement.text().replaceAll(" ", "_"));
        }
        return poll;
    }

    /**
     * Create an array of questions from a poll HTML document
     *
     * @param doc HTML document containing poll information
     * @return Array of poll questions
     */
    private Question[] parseQuestions(Document doc) {
        Elements questionElements = doc.select(".pollquestionborder");
        Question[] questions = new Question[questionElements.size()];
        ArrayList<String> options = new ArrayList<>(Arrays.asList("Yes", "No", "Skip question"));

        for(int i = 0; i < questionElements.size(); i++) {
            Element questionElement = questionElements.get(i);
            Answer[] answers = parseAnswers(questionElement.select("div:not(.pollquestionborder)"));

            Answer winner = Arrays.stream(answers).max(Comparator.comparing(Answer::getPercentageVote)).get();
            boolean opinionPoll = Arrays.stream(answers).noneMatch(a -> options.contains(a.getText()));
            questions[i] = new Question(
                    i + 1,
                    questionElement.select("b").first().text(),
                    answers,
                    winner.getText().equals("Yes") && winner.getPercentageVote() >= 75.0,
                    winner,
                    opinionPoll
            );
        }
        return questions;
    }

    /**
     * Create an array of answers from a given list of answer elements
     *
     * @param answerElements Answer elements from question
     * @return Array of answers
     */
    private Answer[] parseAnswers(Elements answerElements) {
        answerElements = answerElements.stream().filter(e -> e.hasText() && !e.text().matches("Question \\d+")).collect(Collectors.toCollection(Elements::new));
        Answer[] answers = new Answer[answerElements.size()];

        for(int i = 0; i < answerElements.size(); i++) {
            Elements row = answerElements.get(i).select("span");
            String[] voteInfo = row
                    .get(2)
                    .text()
                    .replace(" (", "")
                    .replace(" votes)", "")
                    .split("%");

            answers[i] = new Answer(
                    Integer.parseInt(voteInfo[1]),
                    Double.parseDouble(voteInfo[0]),
                    row.get(0).text()
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
        ArrayList<Element> history = getPollHistory();
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
                    (new Date(System.currentTimeMillis()).after(end) ? "Closed" : "Open") +
                    ")";
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
                    return new DecimalFormat("0.00").format(percentageVote) + "%";
                }
            }
        }
    }
}
