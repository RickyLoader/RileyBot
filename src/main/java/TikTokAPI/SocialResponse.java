package TikTokAPI;

/**
 * TikTok social response to a post
 */
public class SocialResponse {
    private final int likes, comments, shares, plays;

    /**
     * Create the social response
     *
     * @param likes    Number of likes
     * @param comments Number of comments
     * @param shares   Number of times the post has been shared on various platforms
     * @param plays    Total number of plays
     */
    public SocialResponse(int likes, int comments, int shares, int plays) {
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.plays = plays;
    }

    /**
     * Get the number of comments on the post
     *
     * @return Number of comments
     */
    public int getComments() {
        return comments;
    }

    /**
     * Get the number of likes on the post
     *
     * @return Number of likes
     */
    public int getLikes() {
        return likes;
    }

    /**
     * Get the total number of plays the post has received
     *
     * @return Total plays
     */
    public int getPlays() {
        return plays;
    }

    /**
     * Get the number of times the post has been shared on facebook, twitter, etc.
     *
     * @return Number of times the post has been shared
     */
    public int getShares() {
        return shares;
    }
}
