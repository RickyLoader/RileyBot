package Command.Commands;

import Command.Structure.EmbedHelper;
import Command.Structure.MovieCommand;
import Movies.TheMovieDatabase;

/**
 * Take TMDB URLs and embed details about the movie
 */
public class TMDBMovieCommand extends MovieCommand {

    public TMDBMovieCommand() {
        super(
                "The Movie Database",
                "https://www.themoviedb.org/movie/.+/?",
                TheMovieDatabase.LOGO,
                EmbedHelper.BLUE
        );
    }

    /**
     * TMDB supports own IDs, strip the TMDB ID from the URL
     *
     * @param url URL to movie on TMDB
     * @return TMDB ID
     */
    @Override
    public String getSupportedId(String url) {
        String[] args = url.split("/");
        String id = args[args.length - 1];

        // ID is either in format "123" or "123-MOVIE-NAME", "-MOVIE-NAME" must be removed to work with the TMDB API
        return id.split("-")[0];
    }
}
