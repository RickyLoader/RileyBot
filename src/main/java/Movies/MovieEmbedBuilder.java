package Movies;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Displaying movies in an embedded message
 */
public class MovieEmbedBuilder {
    private final Movie movie;
    private final EmoteHelper emoteHelper;
    private final String thumbnail;
    private final int colour;

    /**
     * Initialise the movie embed builder
     *
     * @param movie       Movie to build embed for
     * @param emoteHelper Emote helper
     * @param thumbnail   Thumbnail to use
     * @param colour      Colour to use
     */
    public MovieEmbedBuilder(Movie movie, EmoteHelper emoteHelper, String thumbnail, int colour) {
        this.movie = movie;
        this.emoteHelper = emoteHelper;
        this.thumbnail = thumbnail;
        this.colour = colour;
    }

    /**
     * Build the movie message embed
     *
     * @return Message embed detailing movie
     */
    public MessageEmbed build() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(getTitle(), getTitleUrl())
                .setColor(colour)
                .setThumbnail(thumbnail)
                .setDescription(getDescription())
                .setFooter(getFooterText(), getFooterImageUrl());

        if(movie.hasPosterUrl()) {
            builder.setImage(movie.getPosterUrl());
        }
        return builder.build();
    }

    /**
     * Get the title to use in the embed
     *
     * @return Embed title (Default movie title)
     */
    public String getTitle() {
        return movie.getTitle();
    }

    /**
     * Get the title URL to use in the embed
     *
     * @return Embed title URL (Default null)
     */
    public String getTitleUrl() {
        RatingIds ratingIds = movie.getRatingIds();
        if(!ratingIds.hasImdbId()) {
            return null;
        }
        return ratingIds.getImdbId().getUrl();
    }

    /**
     * Get the footer text to use in the embed
     *
     * @return Embed footer text (Default movie rating score, content rating, and release date)
     */
    public String getFooterText() {
        double rating = movie.getRating();
        return "TMDB: " + ((rating == 0) ? "N/A" : rating)
                + " | Content Rating: " + movie.getContentRating()
                + " | Release Date: " + movie.getFormattedReleaseDate();
    }

    /**
     * Get the footer image URL to use in the embed
     *
     * @return Embed footer image URL (Default TMDB logo)
     */
    public String getFooterImageUrl() {
        return TheMovieDatabase.LOGO;
    }

    /**
     * Get the description text to use in the embed
     *
     * @return Embed description (Default genre, cast, etc)
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("**Synopsis**: ").append(movie.getSummary());

        if(movie.hasTagline()) {
            desc.append("\n\n**Tagline**: ").append(movie.getTagline());
        }

        desc.append("\n\n**Genre**: ").append(StringUtils.join(movie.getGenres(), ", "));

        Crew crew = movie.getCrew();
        if(crew.hasDirector()) {
            desc.append("\n\n**Director**: ").append(StringUtils.join(crew.getDirectors(), ", "));
        }

        if(crew.hasCast()) {
            String[] cast = crew.getCast();
            int max = 3;
            String[] topCast = Arrays.copyOfRange(cast, 0, Math.min(cast.length, max));
            desc.append("\n\n**Cast**: ").append(StringUtils.join(topCast, ", "));
        }

        if(!movie.getLanguage().equals("English")) {
            desc.append("\n\n**Language**: ").append(movie.getLanguage());
        }

        if(movie.getDuration() > 0) {
            desc.append("\n\n**Duration**: ").append(movie.getDuration());
        }

        if(movie.getBudget() > 0) {
            desc.append("\n\n**Budget**: ").append(formatUSD(movie.getBudget()));
        }

        if(movie.getRevenue() > 0) {
            desc.append("\n\n**Box Office**: ").append(formatUSD(movie.getRevenue()));
        }

        String socialSummary = getSocialSummary();
        if(socialSummary != null) {
            desc.append("\n\n").append(socialSummary);
        }
        return desc.toString();
    }

    /**
     * Get the social summary String of a movie.
     * This is a '|' separated String containing the movie's social elements
     * Each social element is an emote followed by a name with an embedded URL to the location.
     * E.g [Facebook emote] [Facebook](Facebook URL)
     *
     * @return Movie social summary
     */
    public String getSocialSummary() {
        ArrayList<String> socialElements = getSocialElements();
        return socialElements.isEmpty() ? null : StringUtils.join(socialElements, " | ");
    }

    /**
     * Get the social elements of the movie.
     * Each social element is an emote followed by a name with an embedded URL to the location.
     * E.g [Facebook emote] [Facebook](Facebook URL)
     *
     * @return List of social elements
     */
    public ArrayList<String> getSocialElements() {
        ArrayList<String> elements = new ArrayList<>();
        SocialConnections socialConnections = movie.getSocialConnections();

        if(socialConnections.hasTrailerUrl()) {
            String youtubeEmote = EmoteHelper.formatEmote(emoteHelper.getYoutube());
            elements.add(EmbedHelper.embedURL(youtubeEmote + " Trailer", socialConnections.getTrailerUrl()));
        }

        if(socialConnections.hasFacebookUrl()) {
            String facebookEmote = EmoteHelper.formatEmote(emoteHelper.getFacebook());
            elements.add(EmbedHelper.embedURL(facebookEmote + " Facebook", socialConnections.getFacebookUrl()));
        }
        return elements;
    }

    /**
     * Format a long n to $n USD
     *
     * @param amount Long amount
     * @return Formatted USD currency string
     */
    private String formatUSD(long amount) {
        return String.format("$%,d USD", amount);
    }
}
