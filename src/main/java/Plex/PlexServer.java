package Plex;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexServer {
    private long timeFetched;
    private ArrayList<Movie> library;

    /**
     * Read in the Plex library and remember the timestamp
     */
    public PlexServer() {
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    /**
     * Create a list of movies from the Plex API
     *
     * @return List of movies
     */
    private ArrayList<Movie> getLibraryOverview() {
        JSONArray jsonArr = new JSONObject(new NetworkRequest(getPlexURL(), false).get()).getJSONObject("MediaContainer").getJSONArray("Metadata");
        ArrayList<Movie> movies = new ArrayList<>();
        for(int i = 0; i < jsonArr.length(); i++) {
            JSONObject movie = jsonArr.getJSONObject(i);
            if(!movieDataExists(movie)) {
                System.out.println(movie.getJSONArray("Media").getJSONObject(0).getJSONArray("Part").getJSONObject(0).getString("file") + " missing info!");
                continue;
            }
            movies.add(new Movie(
                    getMovieID(movie.getString("guid")),
                    movie.getString("title"),
                    movie.has("contentRating") ? movie.getString("contentRating") : "Not Rated",
                    movie.getString("summary"),
                    movie.has("tagline") ? movie.getString("tagline") : null,
                    movie.getString("originallyAvailableAt"),
                    movie.has("Director") ? stringify(movie.getJSONArray("Director")) : null,
                    movie.has("Role") ? stringify(movie.getJSONArray("Role")) : null,
                    movie.has("Genre") ? stringify(movie.getJSONArray("Genre")) : null,
                    movie.getLong("duration"),
                    movie.getDouble("rating")
            ));
        }
        return movies;
    }

    /**
     * Take a JSONArray of movie properties and convert to a comma separated String
     *
     * @param arr JSONArray of movie properties
     * @return Comma separated String of elements
     */
    private String stringify(JSONArray arr) {
        String[] list = new String[arr.length()];
        for(int i = 0; i < arr.length(); i++) {
            list[i] = arr.getJSONObject(i).getString("tag");
        }
        return StringUtils.join(list, ", ");
    }

    /**
     * Check whether Plex has the required information for a movie
     *
     * @param movie JSON object representing a movie
     * @return Whether Plex has the sufficient information
     */
    private boolean movieDataExists(JSONObject movie) {
        return movie.has("guid") && movie.has("title") && movie.has("summary") && movie.has("originallyAvailableAt") && movie.has("rating");
    }

    /**
     * Extract the unique id from the guid Plex uses. E.g: "com.plexapp.agents.imdb://tt0309593?lang=en" = tt0309593
     * Either points to TMDB or IMDB, TMDB supports either, Plex supplies only one
     *
     * @param guid The rating agent used by Plex to pull the rating for the movie
     * @return id The id of the movie
     */
    private String getMovieID(String guid) {
        String regex;
        String id = null;

        // e.g: "com.plexapp.agents.imdb://tt0309593?lang=en"
        if(guid.contains("imdb")) {
            regex = "tt[0-9]+";
        }
        // e.g: "com.plexapp.agents.themoviedb://14161?lang=en"
        else {
            regex = "[0-9]+";
        }

        Matcher matcher = Pattern.compile(regex).matcher(guid);

        if(matcher.find()) {
            id = guid.substring(matcher.start(), matcher.end());
        }
        return id;
    }

    /**
     * Build a message embed detailing a random movie in the Plex library
     *
     * @return Movie embed
     */
    public MessageEmbed getMovieEmbed() {
        Movie movie = getRandomMovie();
        String thumb = "https://i.imgur.com/FdabwCm.png"; // Plex Logo
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(thumb);
        builder.setColor(EmbedHelper.getOrange());
        builder.setTitle(movie.getTitle());
        builder.setImage(movie.getPoster());
        builder.setDescription(movie.toString());
        builder.setFooter("IMDB: " + movie.getRating() + " | Content Rating: " + movie.getContentRating() + " | Release Date: " + movie.getReleaseDate(), movie.getRatingImage());
        return builder.build();
    }

    /**
     * Get a random movie from the Plex library
     *
     * @return Random movie
     */
    private Movie getRandomMovie() {
        return library.get(new Random().nextInt(library.size()));
    }

    /**
     * Get the URL to fetch movie data from plex
     *
     * @return Plex URL
     */
    private String getPlexURL() {
        return Secret.getPlexIp() + Secret.getInternalPlexPort() + "/library/sections/" + Secret.getPlexLibraryId() + "/all/" + Secret.getPlexToken();
    }

    /**
     * Refresh the plex library
     */
    public void refreshData() {
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    /**
     * Get the time the data was last fetched
     *
     * @return Time fetched of Plex data
     */
    public long getTimeFetched() {
        return timeFetched;
    }

    /**
     * Hold information about a movie on Plex
     */
    public static class Movie {
        private final String id, title, contentRating, summary, tagLine, releaseDate, director, cast;
        private final long duration;
        private final double rating;
        private String movieDetails, language, imdbURL, poster, genre;

        /**
         * Construct the movie
         *
         * @param id            Plex ID of movie, either IMDB (tt6053438) or TMDB (14161) formatted
         * @param title         Title of movie
         * @param contentRating Content rating - G, PG..
         * @param summary       Movie synopsis
         * @param tagLine       Movie tagline
         * @param releaseDate   Release date of movie
         * @param director      Director of movie
         * @param cast          Comma separated list of cast members
         * @param genre         Comma separated list of genres
         * @param duration      Duration of movie
         * @param rating        IMDB rating of movie
         */
        public Movie(String id, String title, String contentRating, String summary, String tagLine, String releaseDate, String director, String cast, String genre, long duration, double rating) {
            this.id = id;
            this.title = title;
            this.contentRating = contentRating;
            this.summary = summary;
            this.tagLine = tagLine;
            this.releaseDate = releaseDate;
            this.director = director;
            this.cast = cast;
            this.genre = genre;
            this.duration = duration;
            this.rating = rating;
        }

        /**
         * Get movie details JSON from TMDB containing movie poster, IMDB ID, and language information
         */
        private void getMovieDetails() {
            if(movieDetails != null) {
                return;
            }
            String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + Secret.getTMDBKey() + "&language=en-US";
            this.movieDetails = new NetworkRequest(url, false).get();
        }

        /**
         * Get the URL to the IMDB page of the movie from The Movie Database
         *
         * @return IMDB URL
         */
        public String getIMDBUrl() {
            if(imdbURL == null) {
                getMovieDetails();
                imdbURL = "https://www.imdb.com/title/" + new JSONObject(movieDetails).getString("imdb_id");
            }
            return imdbURL;
        }

        /**
         * Attempt to retrieve the movie genre(s) from The Movie Database if no genre is available
         *
         * @return Movie genre(s)
         */
        public String getGenre() {
            if(genre == null) {
                getMovieDetails();
                JSONArray genres = new JSONObject(movieDetails).getJSONArray("genres");
                String[] genre = new String[genres.length()];
                for(int i = 0; i < genres.length(); i++) {
                    genre[i] = genres.getJSONObject(i).getString("name");
                }
                this.genre = StringUtils.join(genre, ", ");
            }
            return genre;
        }

        /**
         * Retrieve the movie poster from The Movie Database
         *
         * @return Movie poster thumbnail
         */
        public String getPoster() {
            if(poster == null) {
                getMovieDetails();
                poster = "https://image.tmdb.org/t/p/original/" + new JSONObject(movieDetails).getString("poster_path");
            }
            return poster;
        }

        /**
         * Retrieve the movie language(s) The Movie Database
         *
         * @return Movie language(s)
         */
        public String getLanguage() {
            if(language == null) {
                getMovieDetails();
                language = new JSONObject(movieDetails).getString("original_language");
            }
            return language;
        }

        /**
         * Format the release date to NZ format
         *
         * @return NZ formatted release date
         */
        public String getReleaseDate() {
            try {
                Date us = new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate);
                return new SimpleDateFormat("dd/MM/yyy").format(us);
            }
            catch(ParseException e) {
                e.printStackTrace();
            }
            return releaseDate;
        }

        /**
         * Get the IMDB rating of the movie
         *
         * @return IMDB rating
         */
        public double getRating() {
            return rating;
        }

        /**
         * Get the duration of the movie in HH:MM:SS
         *
         * @return Formatted duration of movie
         */
        public String getDuration() {
            return EmbedHelper.formatTime(duration);
        }

        /**
         * Get the title of the movie
         *
         * @return Movie title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Get the content rating of the movie - G, PG..
         *
         * @return Content rating of movie
         */
        public String getContentRating() {
            return contentRating;
        }

        /**
         * Get the IMDB logo image
         *
         * @return URL to IMDB logo image
         */
        public String getRatingImage() {
            return "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/IMDb_Logo_Square.svg/1024px-IMDb_Logo_Square.svg.png";
        }

        /**
         * Get the id of the movie
         *
         * @return movie id
         */
        public String getId() {
            return id;
        }

        /**
         * Format the movie information in to a summary
         *
         * @return Summary of movie information
         */
        @Override
        public String toString() {
            StringBuilder desc = new StringBuilder("**Synopsis**: " + summary);

            if(tagLine != null) {
                desc.append("\n\n**Tagline**: ").append(tagLine);
            }
            if(director != null) {
                desc.append("\n\n**Director**: ").append(director);
            }
            if(cast != null) {
                desc.append("\n\n**Cast**: ").append(cast);
            }
            desc.append("\n\n**Genre**: ").append(getGenre());
            String language = getLanguage();
            if(!language.equals("en")) {
                desc.append("\n\n**Language**: ").append(language);
            }
            desc.append("\n\n**Duration**: ").append(getDuration());
            desc.append("\n\n**IMDB**: ").append(EmbedHelper.embedURL("View", getIMDBUrl()));

            return desc.toString();
        }
    }
}
