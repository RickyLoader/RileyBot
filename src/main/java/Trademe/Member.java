package Trademe;

/**
 * Trademe member
 */
public class Member {
    private final String name, url, photo;

    /**
     * Create a Trademe member
     *
     * @param name  Member nickname
     * @param id    Member id
     * @param photo URL to member profile picture
     */
    public Member(String name, long id, String photo) {
        this.name = name;
        this.url = Trademe.BASE_URL + "Members/Profile.aspx?member=" + id;
        this.photo = photo;
    }

    /**
     * Get the member's nickname
     *
     * @return Member nickname
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the member's page
     *
     * @return URL to member page
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the URL to the member's profile picture
     *
     * @return URL to member profile picture
     */
    public String getPhoto() {
        return photo;
    }
}
