package TikTokAPI;

/**
 * TikTok creator social stats
 */
public class SocialStats {
    private final int following, followers, likes, videos;

    /**
     * Create the social stats
     *
     * @param following Number of people the creator follows
     * @param followers Number of people following the creator
     * @param likes     Total number of times the creator's posts have been liked
     * @param videos    Number of videos the creator has uploaded
     */
    public SocialStats(int following, int followers, int likes, int videos) {
        this.following = following;
        this.followers = followers;
        this.likes = likes;
        this.videos = videos;
    }

    /**
     * Get the number of videos the creator has uploaded
     *
     * @return Number of uploaded videos
     */
    public int getVideoCount() {
        return videos;
    }

    /**
     * Get the number of people following the creator
     *
     * @return Number of people following creator
     */
    public int getFollowers() {
        return followers;
    }

    /**
     * Get the number of people the creator is following
     *
     * @return Number of people creator is following
     */
    public int getFollowing() {
        return following;
    }

    /**
     * Get the total number of times the creator's posts have been liked
     *
     * @return Total number of likes
     */
    public int getLikes() {
        return likes;
    }

}
