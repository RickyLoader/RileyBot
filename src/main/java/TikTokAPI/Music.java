package TikTokAPI;

import java.util.Arrays;

/**
 * Music track used in a TikTok post
 */
public class Music {
    private final String title;
    private final byte[] thumbnailImage;
    private final String[] authors;
    public static final String DEFAULT_THUMBNAIL_URL = "https://i.imgur.com/UH6M504.png";

    /**
     * Create a music track
     *
     * @param title          Title of the track e.g "The Walrus Song"
     * @param author         Author(s) of the track e.g "author 1 & author 2..."
     * @param thumbnailImage Album thumbnail (may be null)
     */
    public Music(String title, String author, byte[] thumbnailImage) {
        this.title = title;
        this.authors = author.split(" & ");
        this.thumbnailImage = thumbnailImage;
    }

    /**
     * Get the title of the track
     *
     * @return Track title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the author(s) of the track. This is in format "author 1, author 2..."
     *
     * @param number Number of authors to retrieve - will default to the max if too many/few are requested
     * @return Author(s) of the track
     */
    public String getAuthors(int number) {
        int max = authors.length;
        if(number > max || number <= 0) {
            number = max;
        }

        String[] requestedAuthors = Arrays.copyOfRange(authors, 0, number);
        StringBuilder authorString = new StringBuilder();
        final String separator = (number == max && max == 2) ? " & " : ", ";

        for(int i = 0; i < number; i++) {
            authorString.append(requestedAuthors[i]);

            // Append ", and x more" to the final requested author (if there are more not shown)
            if(i == number - 1) {
                if(number != max) {
                    if(number > 1) {
                        authorString.append(",");
                    }
                    authorString.append(" and ").append(max - number).append(" more");
                }
            }
            else if(i == number - 2) {
                authorString.append(number == max && max > 2 ? ", and " : separator);
            }
            else {
                authorString.append(separator);
            }
        }
        return authorString.toString();
    }

    /**
     * Check if the music track has an album thumbnail
     *
     * @return Creator has profile thumbnail
     */
    public boolean hasThumbnailImage() {
        return thumbnailImage != null;
    }

    /**
     * Get the the album thumbnail.
     * This may be null if the thumbnail was unable to be downloaded.
     *
     * @return Album thumbnail
     */
    public byte[] getThumbnailImage() {
        return thumbnailImage;
    }
}
