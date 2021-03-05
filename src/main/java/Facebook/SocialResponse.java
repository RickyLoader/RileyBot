package Facebook;

/**
 * Social response to Facebook post - number of reactions and comments
 */
public class SocialResponse {
    private final int reactions, comments;

    /**
     * Create the social response
     *
     * @param reactions Total number of reactions to the post
     * @param comments  Total number of comments on the post
     */
    public SocialResponse(int reactions, int comments) {
        this.reactions = reactions;
        this.comments = comments;
    }

    /**
     * Get the total number of comments on the post
     *
     * @return Total number of post comments
     */
    public int getComments() {
        return comments;
    }

    /**
     * Get the total number of reactions on the post
     *
     * @return Total number of post reactions
     */
    public int getReactions() {
        return reactions;
    }

    /**
     * Parse a String social response to an Integer e.g "3,938 Comments" -> 3938
     *
     * @param socialResponse Post social response - likes, comments, etc
     * @return Integer value of social response
     */
    public static int parseSocialResponse(String socialResponse) {
        try {
            boolean k = socialResponse.contains("K");
            String cleaned = socialResponse
                    .replace(",", "")
                    .replace("Comments", "")
                    .replace("K", "")
                    .trim();
            int number = Integer.parseInt(cleaned);
            return k ? number * 1000 : number;
        }
        catch(NumberFormatException e) {
            return 0;
        }
    }
}
