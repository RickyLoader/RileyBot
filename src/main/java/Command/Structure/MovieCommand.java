package Command.Structure;

import Movies.Movie;
import Movies.MovieEmbedBuilder;
import Movies.TheMovieDatabase;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Take movie URLs from a movie site and embed details about the Movie using The Movie Database
 */
public abstract class MovieCommand extends DiscordCommand {
    private final String urlRegex, logoUrl;
    private final int colour;

    /**
     * Create a movie command
     *
     * @param siteName Website name - e.g "IMDB" or "Rotten Tomatoes"
     * @param urlRegex Regular expression to match a URL for the given website
     * @param logoUrl  URL to the website logo
     * @param colour   Colour to use in movie embeds
     */
    public MovieCommand(String siteName, String urlRegex, String logoUrl, int colour) {
        super(
                "[" + siteName + " URL]",
                "Take movie URLs from " + siteName + " and view details about the movie"
        );
        this.urlRegex = urlRegex;
        this.logoUrl = logoUrl;
        this.colour = colour;
    }

    @Override
    public void execute(CommandContext context) {
        String url = context.getMessageContent();
        String id = getSupportedId(url);
        if(id == null) {
            return;
        }

        Movie movie = TheMovieDatabase.getMovieById(id);
        if(movie == null) {
            return;
        }

        MessageEmbed movieEmbed = new MovieEmbedBuilder(
                movie,
                context.getEmoteHelper(),
                logoUrl,
                colour
        ) {
            @Override
            public String getTitleUrl() {
                return url;
            }

            @Override
            public String getDescription() {
                return super.getDescription() + "\n\n**Biggest Fan**: " + context.getMember().getAsMention();
            }
        }.build();

        context.getMessage().delete().queue();
        context.getMessageChannel().sendMessage(movieEmbed).queue();
    }

    /**
     * Get a TMDB supported movie ID given the URL to the movie on a website.
     * This ID may be located in the URL itself, or require additional means to find.
     *
     * @param url URL to movie on the website
     * @return TMDB supported ID for the movie or null (if unable to find)
     * @see <a href="https://developers.themoviedb.org/3/getting-started/external-ids">For supported IDs</a>
     */
    public abstract String getSupportedId(String url);

    @Override
    public boolean matches(String query, Message message) {
        return message.getContentDisplay().matches(urlRegex);
    }
}
