package Movies;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * The Movie Database API functions
 */
public class TheMovieDatabase {
    private static final String
            BASE_API_URL = "https://api.themoviedb.org/3/",
            AUTHENTICATION = "?api_key=" + Secret.TMDB_KEY,
            STATUS_KEY = "success",
            EXTERNAL_IDS_KEY = "external_ids",
            VIDEOS_KEY = "videos",
            CREDITS_KEY = "credits",
            RELEASE_DATES_KEY = "release_dates";
    public static final String LOGO = "https://i.imgur.com/J1JGC4J.png";

    /**
     * Get a movie from TMDB using a supported movie ID.
     *
     * @param id Supported movie ID
     * @return Movie from ID or null (if no movie is found with the given ID)
     * @see <a href="https://developers.themoviedb.org/3/getting-started/external-ids">For supported IDs</a>
     */
    public static Movie getMovieById(String id) {
        String url = BASE_API_URL
                + "movie/"
                + id
                + AUTHENTICATION
                + "&append_to_response="
                + CREDITS_KEY
                + ","
                + RELEASE_DATES_KEY
                + ","
                + VIDEOS_KEY
                + ","
                + EXTERNAL_IDS_KEY
                + "&language=en-US";

        JSONObject movie = new JSONObject(new NetworkRequest(url, false).get().body);
        if(movie.has(STATUS_KEY) && !movie.getBoolean(STATUS_KEY)) {
            return null;
        }
        return parseMovie(movie);
    }

    /**
     * Take a JSON object representing a movie and return a movie object
     *
     * @param movieDetails Movie JSON object from the TMDB API
     * @return Movie from JSON
     */
    private static Movie parseMovie(JSONObject movieDetails) {
        Movie.MovieBuilder builder = new Movie.MovieBuilder(
                movieDetails.getString("title"),
                movieDetails.getString("overview"),
                parseReleaseDate(movieDetails.getString("release_date")),
                parseGenres(movieDetails.getJSONArray("genres")),
                parseLanguage(movieDetails),
                parseRatingIds(movieDetails)
        );
        builder.setBudget(movieDetails.getLong("budget"))
                .setRevenue(movieDetails.getLong("revenue"))
                .setRating(movieDetails.getDouble("vote_average"))
                .setContentRating(
                        parseContentRating(movieDetails.getJSONObject(RELEASE_DATES_KEY).getJSONArray("results"))
                )
                .setSocialConnections(parseSocialConnections(movieDetails))
                .setCrew(parseCrew(movieDetails.getJSONObject(CREDITS_KEY)));

        String tagline = movieDetails.getString("tagline");
        if(!tagline.isEmpty()) {
            builder.setTagline(tagline);
        }

        String durationKey = "duration";
        if(movieDetails.has(durationKey)) {
            builder.setDuration(movieDetails.getInt(durationKey));
        }

        String posterKey = "poster_path";
        if(!movieDetails.isNull(posterKey)) {
            builder.setPosterUrl("https://image.tmdb.org/t/p/original/" + movieDetails.getString(posterKey));
        }

        return builder.build();
    }

    /**
     * Parse the crew details from the given movie JSON
     *
     * @param credits Movie crew details JSON object from the TMDB API
     * @return Movie crew
     */
    private static Crew parseCrew(JSONObject credits) {
        return new Crew.CrewBuilder()
                .setCast(parseCast(credits.getJSONArray("cast")))
                .setDirectors(parseDirectors(credits.getJSONArray("crew")))
                .build();
    }

    /**
     * Parse the director(s) from the given movie JSON
     *
     * @param crewDetails Movie crew JSONArray
     * @return Array of director(s)
     */
    private static String[] parseDirectors(JSONArray crewDetails) {
        ArrayList<String> directors = new ArrayList<>();
        for(int i = 0; i < crewDetails.length(); i++) {
            JSONObject member = crewDetails.getJSONObject(i);
            if(member.getString("department").equals("Directing") && member.getString("job").equals("Director")) {
                directors.add(member.getString("name"));
            }
        }
        return directors.toArray(new String[0]);
    }

    /**
     * Parse cast members from the given movie JSON
     *
     * @param castDetails Movie cast JSONArray
     * @return Array of cast members
     */
    private static String[] parseCast(JSONArray castDetails) {
        String[] cast = new String[castDetails.length()];
        for(int i = 0; i < cast.length; i++) {
            cast[i] = castDetails.getJSONObject(i).getString("name");
        }
        return cast;
    }

    /**
     * Parse the social connections for the movie - Facebook, Youtube, etc from the given movie JSON
     *
     * @param movieDetails Movie JSON object from the TMDB API
     * @return Movie social connections
     */
    private static SocialConnections parseSocialConnections(JSONObject movieDetails) {
        SocialConnections.SocialConnectionsBuilder builder = new SocialConnections.SocialConnectionsBuilder();
        String trailerId = parseTrailerId(movieDetails.getJSONObject(VIDEOS_KEY).getJSONArray("results"));
        String facebookPageId = parseFacebookId(movieDetails.getJSONObject(EXTERNAL_IDS_KEY));

        if(trailerId != null) {
            builder.setTrailerUrl(trailerId);
        }
        if(facebookPageId != null) {
            builder.setFacebookUrl(facebookPageId);
        }
        return builder.build();
    }

    /**
     * Parse the Youtube trailer ID from the movie JSON
     *
     * @param videos JSONArray of videos available for movie
     * @return Youtube trailer ID
     */
    private static String parseTrailerId(JSONArray videos) {
        for(int i = 0; i < videos.length(); i++) {
            JSONObject video = videos.getJSONObject(i);
            if(video.getString("site").equals("YouTube") && video.getString("type").equals("Trailer")) {
                return video.getString("key");
            }
        }
        return null;
    }

    /**
     * Parse the movie's Facebook page ID from the movie JSON
     *
     * @param socials Movie social media locations
     * @return Facebook page ID
     */
    private static String parseFacebookId(JSONObject socials) {
        return socials.isNull("facebook_id") ? null : socials.getString("facebook_id");
    }

    /**
     * Parse the US content rating from the given movie JSON
     *
     * @param releases Country release JSON array
     * @return Movie content rating/certification - G, PG..
     */
    private static String parseContentRating(JSONArray releases) {
        String contentRating = null;
        for(int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.getJSONObject(i);
            if(!release.getString("iso_3166_1").equals("US")) {
                continue;
            }
            contentRating = release.getJSONArray("release_dates").getJSONObject(0).getString("certification");
        }
        return contentRating == null || contentRating.isEmpty() ? "N/A" : contentRating;
    }

    /**
     * Parse the rating IDs for the given movie JSON
     *
     * @param movieDetails Movie JSON object from the TMDB API
     * @return Movie rating Ids
     */
    private static RatingIds parseRatingIds(JSONObject movieDetails) {
        RatingIds.RatingIdsBuilder builder = new RatingIds.RatingIdsBuilder()
                .setTmdbId(String.valueOf(movieDetails.getInt("id")));

        JSONObject externalIds = movieDetails.getJSONObject(EXTERNAL_IDS_KEY);
        String imdb = "imdb_id";
        if(externalIds.has(imdb)) {
            builder.setImdbId(externalIds.getString(imdb));
        }
        return builder.build();
    }

    /**
     * Parse the movie language name from The Movie Database.
     * This is the English name for the movie's language - e.g Deutsch = German.
     *
     * @param movieDetails Movie JSON object from the TMDB API
     * @return Movie language English name
     */
    public static String parseLanguage(JSONObject movieDetails) {
        String iso = movieDetails.getString("original_language");
        JSONArray spokenLanguages = movieDetails.getJSONArray("spoken_languages");
        String languageName = null;
        String unknown = "N/A";

        /*
         * Attempt to get the name of the language e.g "English", "Deutsch", etc
         */
        for(int i = 0; i < spokenLanguages.length(); i++) {
            JSONObject lang = spokenLanguages.getJSONObject(i);
            if(lang.getString("iso_639_1").equals(iso)) {
                languageName = lang.getString("name");
                break;
            }
        }

        /*
         * Original language is based on where the movie was filmed - English movie filmed in Germany
         * would show German as the original language but German wouldn't be present in spoken languages.
         */
        if(languageName == null) {
            if(spokenLanguages.length() > 0) {

                // Usually corrects the above issue (English should be the first spoken language in the above scenario)
                languageName = spokenLanguages.getJSONObject(0).getString("name");
            }
            else {
                languageName = unknown;
            }
        }

        // Get the English name of the language - "Deutsch" -> "German"
        if(!languageName.equals("English") && !languageName.equals(unknown)) {
            languageName = EmbedHelper.getLanguageFromISO(iso);
        }
        return languageName;
    }

    /**
     * Parse the release date of a movie from the date String
     *
     * @param dateString Date String representing release date
     * @return Release date of movie (or null if the date is unable to be parsed)
     */
    private static Date parseReleaseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        }
        catch(ParseException e) {
            return null;
        }
    }

    /**
     * Parse the genre data from The Movie Database
     *
     * @param genreDetails JSON array of movie genres
     * @return Array of movie genre(s)
     */
    private static String[] parseGenres(JSONArray genreDetails) {
        String[] genres = new String[genreDetails.length()];
        for(int i = 0; i < genreDetails.length(); i++) {
            genres[i] = genreDetails.getJSONObject(i).getString("name");
        }
        return genres;
    }

    /**
     * Get the TMDB ID of a movie from the given title and year.
     *
     * @param title Movie title
     * @param year  Release year
     * @return TMDB ID or null (If no movie is found)
     */
    public static String getIDByTitle(String title, String year) {
        String url = BASE_API_URL
                + "search/movie"
                + AUTHENTICATION
                + "&query=" + title
                + "&year=" + year;

        JSONArray results = new JSONObject(new NetworkRequest(url, false).get().body)
                .getJSONArray("results");

        if(results.isEmpty()) {
            return null;
        }

        return String.valueOf(results.getJSONObject(0).getInt("id"));
    }
}
