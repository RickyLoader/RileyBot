package UrbanDictionary;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hold information on an Urban Dictionary definition
 */
public class Definition implements Comparable<Definition> {

    private final String term, explanation, quote, url, author;
    private final int upvote, downvote;
    private final Date submitted;

    /**
     * Create a definition
     *
     * @param term        Term which is defined
     * @param explanation Explanation of term
     * @param quote       Usage of the definition in a quote
     * @param upvote      Quantity of upvotes
     * @param downvote    Quantity of downvotes
     * @param submitted   Date of submission
     * @param author      Author of submission
     */
    public Definition(String term, String explanation, String quote, int upvote, int downvote, String url, Date submitted, String author) {
        this.term = term;
        this.explanation = explanation;
        this.quote = quote;
        this.upvote = upvote;
        this.downvote = downvote;
        this.url = url;
        this.submitted = submitted;
        this.author = author;
    }

    /**
     * Get the author of the definition
     *
     * @return Author of definition
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Parse the submission date from the JSON response
     *
     * @param date Submission date of definition
     * @return Parsed date
     */
    public static Date parseSubmissionDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'").parse(date);
        }
        catch(Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * Get the submission date in a formatted String
     *
     * @return Formatted submission date
     */
    public String formatSubmitted() {
        return new SimpleDateFormat("dd/MM/yyyy").format(submitted);
    }

    /**
     * Get the url to the term definition
     *
     * @return URL to the definition
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the quantity of upvotes
     *
     * @return Upvote quantity
     */
    public int getUpvote() {
        return upvote;
    }

    /**
     * Get the quantity of downvotes
     *
     * @return Downvote quantity
     */
    public int getDownvote() {
        return downvote;
    }

    /**
     * Get the explanation of the term
     *
     * @return Explanation of term
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Get the term used in a quote
     *
     * @return Term used in a quote
     */
    public String getQuote() {
        return quote;
    }

    /**
     * Get the term
     *
     * @return Term
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sort by descending upvotes
     */
    @Override
    public int compareTo(@NotNull Definition o) {
        return o.getUpvote() - getUpvote();
    }
}