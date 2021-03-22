package Reddit;

import COD.Assets.Ratio;

import java.util.Date;

/**
 * Post on Reddit
 */
public class RedditPost {
    private final String title, author, url;
    private final PostContent content;
    private final Ratio votes;
    private final int comments;
    private final Date datePosted;
    private final Subreddit subreddit;

    /**
     * Create a Reddit post
     *
     * @param title      Post title
     * @param author     Name of post author
     * @param url        Post URL
     * @param content    Post content
     * @param votes      Upvotes/downvotes
     * @param comments   Number of comments
     * @param subreddit  Subreddit where post was created
     * @param datePosted Date of post
     */
    public RedditPost(String title, String author, String url, PostContent content, Ratio votes, int comments, Subreddit subreddit, Date datePosted) {
        this.title = title;
        this.author = author;
        this.url = url;
        this.content = content;
        this.votes = votes;
        this.comments = comments;
        this.subreddit = subreddit;
        this.datePosted = datePosted;
    }

    /**
     * Get the post content - may be text or a URL to an image/video
     *
     * @return Post content
     */
    public PostContent getContent() {
        return content;
    }

    /**
     * Get the date of the post
     *
     * @return Date of post
     */
    public Date getDatePosted() {
        return datePosted;
    }

    /**
     * Get the post title
     *
     * @return Post title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the number of comments on the post
     *
     * @return Number of post comments
     */
    public int getComments() {
        return comments;
    }

    /**
     * Get the ratio of upvotes/downvotes
     *
     * @return Vote ratio
     */
    public Ratio getVotes() {
        return votes;
    }

    /**
     * Get the name of the post author
     *
     * @return Post author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get the subreddit where the post was created
     *
     * @return Subreddit
     */
    public Subreddit getSubreddit() {
        return subreddit;
    }

    /**
     * Get the URL to the post
     *
     * @return URL to post
     */
    public String getUrl() {
        return url;
    }
}
