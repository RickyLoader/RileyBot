package Facebook;

/**
 * Facebook post info
 */
public class FacebookPost {
    private final UserDetails author;
    private final PostDetails postDetails;
    private final SocialResponse socialResponse;
    private final Attachments attachments;

    /**
     * Create a Facebook post
     *
     * @param author         Page which authored the post
     * @param postDetails    Post details - url, date, and text
     * @param socialResponse Social response - reactions and comments
     * @param attachments    Post attachments
     */
    public FacebookPost(UserDetails author, PostDetails postDetails, SocialResponse socialResponse, Attachments attachments) {
        this.author = author;
        this.postDetails = postDetails;
        this.socialResponse = socialResponse;
        this.attachments = attachments;
    }

    /**
     * Get the post attachments
     *
     * @return Post attachments
     */
    public Attachments getAttachments() {
        return attachments;
    }

    /**
     * Get the post details - url, date, and text
     *
     * @return Post details
     */
    public PostDetails getPostDetails() {
        return postDetails;
    }

    /**
     * Get the social response to the post - reactions and comments
     *
     * @return Social response to post
     */
    public SocialResponse getSocialResponse() {
        return socialResponse;
    }

    /**
     * Get the author page of the post
     *
     * @return Post author
     */
    public UserDetails getAuthor() {
        return author;
    }
}
