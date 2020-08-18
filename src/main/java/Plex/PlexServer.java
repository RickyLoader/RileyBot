package Plex;

import Command.Structure.EmbedHelper;
import Network.NetworkInfo;
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
    private final String publicIP;

    public PlexServer() {
        this.publicIP = NetworkInfo.getPublicAddress() + Secret.getExternalPlexPort();
        this.library = getLibraryOverview();
        this.timeFetched = System.currentTimeMillis();
    }

    private ArrayList<Movie> getLibraryOverview() {
        JSONArray jsonArr = new JSONObject(new NetworkRequest(getPlexURL(), false).get()).getJSONObject("MediaContainer").getJSONArray("Metadata");
        ArrayList<Movie> movies = new ArrayList<>();
        for(int i = 0; i < jsonArr.length(); i++) {
            JSONObject movie = jsonArr.getJSONObject(i);
            if(!movieDataExists(movie)) {
                System.out.println("Skipping movie");
                continue;
            }
            movies.add(new Movie(
                    getMovieID(movie.getString("guid")),
                    movie.getString("title"),
                    movie.has("contentRating") ? movie.getString("contentRating") : "N/A",
                    movie.getString("summary"),
                    movie.has("tagline") ? movie.getString("tagline") : null,
                    movie.getString("originallyAvailableAt"),
                    publicIP + movie.getString("thumb") + Secret.getPlexToken(),
                    movie.getLong("duration")
            ));
        }
        return movies;
    }

    /**
     * Check whether Plex has the required information for a movie
     *
     * @param movie JSON object representing a movie
     * @return Whether Plex has the sufficient information
     */
    private boolean movieDataExists(JSONObject movie) {
        return movie.has("guid") && movie.has("title") && movie.has("summary") && movie.has("originallyAvailableAt");
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

        String desc = "**Synopsis**: " + movie.getSummary();
        if(movie.getTagLine() != null) {
            desc += "\n\n**Tagline**: " + movie.getTagLine();
        }
        ArrayList<String> directors = movie.getDirectors();
        String director = directors.size() == 1 ? "Director" : "Directors";
        desc += "\n\n**" + director + "**: " + StringUtils.join(directors.toArray(), ", ") + "\n\n**Cast**: " + movie.getCast() + "\n\n**Duration**: " + movie.getDuration();
        builder.setThumbnail(thumb);
        builder.setColor(EmbedHelper.getOrange());
        builder.setTitle(movie.getTitle());
        builder.setImage(movie.getThumbnail());
        builder.setDescription(desc);

        builder.setFooter("Content Rating: " + movie.getContentRating() + " | Release Date: " + movie.getReleaseDate(), thumb);
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

    public static class Movie {
        private final String id, title, contentRating, summary, tagLine, releaseDate, thumbnail;
        private final long duration;
        private String castDetails;

        public Movie(String id, String title, String contentRating, String summary, String tagLine, String releaseDate, String thumbnail, long duration) {
            this.id = id;
            this.title = title;
            this.contentRating = contentRating;
            this.summary = summary;
            this.tagLine = tagLine;
            this.releaseDate = releaseDate;
            this.thumbnail = thumbnail;
            this.duration = duration;
        }

        /**
         * Get the cast details JSON for the movie
         */
        private void getCastDetails() {
            String url = "https://api.themoviedb.org/3/movie/" + id + "/credits?api_key=" + Secret.getTMDBKey();
            this.castDetails = new NetworkRequest(url, false).get();
        }

        /**
         * Get the director(s) of the movie in a comma separated String
         *
         * @return Comma separated String of directors
         */
        public ArrayList<String> getDirectors() {
            if(castDetails == null) {
                getCastDetails();
            }
            JSONArray crewMembers = new JSONObject(castDetails).getJSONArray("crew");
            ArrayList<String> directors = new ArrayList<>();
            for(int i = 0; i < crewMembers.length(); i++) {
                JSONObject crewMember = crewMembers.getJSONObject(i);
                if(crewMember.getString("job").equals("Director")) {
                    directors.add(crewMember.getString("name"));
                }
            }
            return directors;
        }

        /**
         * Get the cast members of the movie in a comma separated String
         *
         * @return Comma separated String of cast members
         */
        public String getCast() {
            if(castDetails == null) {
                getCastDetails();
            }
            JSONArray cast = new JSONObject(castDetails).getJSONArray("cast");
            ArrayList<String> actors = new ArrayList<>();
            for(int i = 0; i < Math.min(cast.length(), 5); i++) {
                actors.add(cast.getJSONObject(i).getString("name"));
            }
            return StringUtils.join(actors.toArray(), ", ");
        }

        /**
         * Plex thumbnails are often too large for Discord to send properly, first attempt to retrieve the thumbnail from
         * The Movie Database
         *
         * @return Movie poster thumbnail
         */
        public String getThumbnail() {
            try {
                String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + Secret.getTMDBKey() + "&language=en-US";
                return "https://image.tmdb.org/t/p/original/" + new JSONObject(new NetworkRequest(url, false).get()).getString("poster_path");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return thumbnail;
        }

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

        public String getDuration() {
            return EmbedHelper.formatTime(duration);
        }

        public String getTitle() {
            return title;
        }

        public String getContentRating() {
            return contentRating;
        }

        public String getSummary() {
            return summary;
        }

        public String getTagLine() {
            return tagLine;
        }
    }
}
