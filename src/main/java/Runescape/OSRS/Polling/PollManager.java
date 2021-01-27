package Runescape.OSRS.Polling;

import Runescape.OSRS.Polling.Poll.PollBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hold information on OSRS polls
 */
public class PollManager {
    private final HashMap<Integer, Poll> polls;
    private ArrayList<Poll> history;
    private long historyFetched;

    public PollManager() {
        this.polls = new HashMap<>();
    }

    /**
     * Navigate to the poll history page on the OSRS wiki.
     * This page contains an overview of every poll - showing poll number,
     * title, URL etc.
     * Create a list of polls initialised with these values (Questions cannot be parsed from this page)
     *
     * @return List of partially complete polls
     */
    private ArrayList<Poll> getPollHistory() {
        ArrayList<Poll> summary = new ArrayList<>();
        try {
            Elements yearHeaders = Jsoup
                    .connect("https://oldschool.runescape.wiki/w/Polls")
                    .get()
                    .select("span.mw-headline")
                    .parents()
                    .select("h3")
                    .stream()
                    .filter(element -> element.childrenSize() == 2)
                    .collect(Collectors.toCollection(Elements::new));

            for(Element yearHeader : yearHeaders) {
                Elements polls = yearHeader.nextElementSibling().select("tr:not(:first-child)");
                String year = yearHeader.selectFirst(".mw-headline").attr("id");

                for(Element poll : polls) {
                    Element titleElement = poll.child(1).child(0);
                    String votes = poll.child(5).text().replace(",", "");
                    summary.add(
                            new PollBuilder()
                                    .setPollNumber(Integer.parseInt(poll.child(0).text()))
                                    .setTitle(titleElement.text())
                                    .setURL(titleElement.absUrl("href"))
                                    .setStartDate(parseWikiDate(poll.child(2).text() + " " + year))
                                    .setEndDate(parseWikiDate(poll.child(3).text() + " " + year))
                                    .setTotalVotes(votes.isEmpty() ? 0 : Integer.parseInt(votes))
                                    .build()
                    );
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        // Reverse so the first element is the first poll
        Collections.reverse(summary);
        return summary;
    }

    /**
     * Navigate to the OSRS wiki page for the given poll and scrape
     * the questions.
     *
     * @param poll Partially complete poll containing number, title, and URL
     * @return Completed poll
     */
    private Poll completePoll(Poll poll) {
        try {
            Document doc = Jsoup.connect(poll.getUrl()).get();

            poll = new PollBuilder()
                    .setPollNumber(poll.getNumber())
                    .setTitle(poll.getTitle())
                    .setURL(poll.getUrl())
                    .setStartDate(poll.getStartDate())
                    .setEndDate(poll.getEndDate())
                    .setTotalVotes(poll.getTotalVotes())
                    .setQuestions(parseQuestions(doc))
                    .build();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error parsing: " + poll.getUrl());
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

            Answer winner = Arrays
                    .stream(answers)
                    .max(Comparator.comparing(Answer::getPercentageVote))
                    .orElse(null);

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
     * Attempt to get a specific poll by poll number
     * Cache the poll if it has not been seen before and is not the latest
     *
     * @param number Desired poll number (0 for latest)
     * @return Poll of given number
     */
    public Poll getPollByNumber(int number) {
        if(polls.containsKey(number)) {
            return polls.get(number);
        }

        updateHistory();

        if(number > history.size()) {
            return null;
        }

        int pollIndex = ((number == 0) ? history.size() : number);
        Poll poll = completePoll(history.get(pollIndex - 1));

        // Don't cache latest poll
        if(poll != null && pollIndex != history.size() && !polls.containsKey(poll.getNumber())) {
            polls.put(poll.getNumber(), poll);
        }
        return poll;

    }

    /**
     * Update the poll history if it does not exist or has been more than an
     * hour since it was last updated.
     */
    private void updateHistory() {
        long currentTime = System.currentTimeMillis();
        if(history != null && currentTime - historyFetched < 3600000) {
            return;
        }
        history = getPollHistory();
        historyFetched = currentTime;
    }

    /**
     * Get a list of polls containing the given search term in the title from
     * the poll history (polls which have not yet been completed).
     * These polls will only contain the poll number, title, and URL.
     *
     * @param title Poll title to search
     * @return List of polls containing the
     */
    public ArrayList<Poll> getPollsByTitle(String title) {
        updateHistory();
        return history
                .stream()
                .filter(poll -> poll.getTitle().toLowerCase().contains(title))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
