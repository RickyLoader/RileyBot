package Command.Commands;

import Command.Structure.EmbedHelper;
import Command.Structure.MovieCommand;
import Movies.TheMovieDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Take Rotten Tomatoes URLs and embed details about the movie
 */
public class RottenTomatoesMovieCommand extends MovieCommand {

    public RottenTomatoesMovieCommand() {
        super(
                "Rotten Tomatoes",
                "https://www.rottentomatoes.com/m/.+/?",
                "https://i.imgur.com/O7DEtTo.jpg",
                EmbedHelper.RED
        );
    }

    /**
     * Rotten Tomatoes is not supported by TMDB, scrape the page to locate a title & year and perform a search
     * for the TMDB ID.
     *
     * @param url URL to movie on Rotten Tomatoes
     * @return TMDB ID or null
     */
    @Override
    public String getSupportedId(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            String title = document.getElementsByClass("scoreboard__title").get(0).text();
            String summary = document.getElementsByClass("scoreboard__info").get(0).text();
            String year = summary.split(",")[0];
            return TheMovieDatabase.getIDByTitle(title, year);
        }
        catch(Exception e) {
            return null;
        }
    }
}
