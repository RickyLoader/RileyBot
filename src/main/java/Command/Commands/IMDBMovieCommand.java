package Command.Commands;

import Command.Structure.EmbedHelper;
import Command.Structure.MovieCommand;

/**
 * Take IMDB URLs and embed details about the movie
 */
public class IMDBMovieCommand extends MovieCommand {

    public IMDBMovieCommand() {
        super(
                "IMDB",
                "https://www.imdb.com/title/tt\\d+/?",
                "https://i.imgur.com/2XQeN28.png",
                EmbedHelper.YELLOW
        );
    }

    /**
     * TMDB supports IMDB IDs, strip the IMDB ID from the URL
     *
     * @param url URL to movie on IMDB
     * @return IMDB ID
     */
    @Override
    public String getSupportedId(String url) {
        String[] args = url.split("/");
        return args[args.length - 1];
    }
}
