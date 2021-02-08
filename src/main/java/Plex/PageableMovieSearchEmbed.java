package Plex;

import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.PageableTableEmbed;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PageableMovieSearchEmbed extends PageableTableEmbed {
    private final String radarrEmote, plexEmote;

    /**
     * Embedded message that can be paged through with emotes and displays as a table.
     * Displays movie search results from either the Radarr library, or outside the Radarr library
     *
     * @param context Command context
     * @param query   Search query used to find results
     * @param movies  Array of movies found for search query
     * @param footer  Footer to use in the embed
     * @param library Search was performed on library items
     */
    public PageableMovieSearchEmbed(CommandContext context, String query, Movie[] movies, String footer, boolean library) {
        super(
                context,
                Arrays.asList(movies),
                library ? PlexServer.PLEX_ICON : PlexServer.RADARR_ICON,
                library ? "Plex Movie Search" : "Radarr Movie Search",
                buildDescription(query, movies, context.getEmoteHelper(), library),
                footer,
                new String[]{"Platform", "Title", "Release Date"},
                5,
                library ? EmbedHelper.ORANGE : EmbedHelper.BLUE
        );
        EmoteHelper emoteHelper = context.getEmoteHelper();
        this.radarrEmote = EmoteHelper.formatEmote(emoteHelper.getRadarr());
        this.plexEmote = EmoteHelper.formatEmote(emoteHelper.getPlex());
    }

    /**
     * Build the description to display in the pageable embed. Show a key for the platform
     * icons that will be displayed in the message (Based on the platform the movies were found on).
     *
     * @param query       Search query used to find results
     * @param movies      Array of movies found for search query
     * @param emoteHelper Emote helper
     * @param library     Search was performed on library items
     * @return Description showing emote key
     */
    private static String buildDescription(String query, Movie[] movies, EmoteHelper emoteHelper, boolean library) {
        if(movies.length == 0) {
            String description = "No movie results found for: **" + query + "**, try again cunt.";
            if(!library) {
                description += "\n\nI won't find movies that are already on Radarr.";
            }
            return description;
        }
        return buildEmoteKey(movies, library, emoteHelper)
                + "I found "
                + movies.length
                + " results for: **"
                + query
                + "**"
                + "\n\nNarrow it down next time cunt, here they are:";
    }

    @Override
    public void sortItems(List<?> items, boolean defaultSort) {
        items.sort((Comparator<Object>) (o1, o2) -> {
            Comparator<Movie> sort = Movie.getSortComparator();
            Movie m1 = (Movie) o1;
            Movie m2 = (Movie) o2;
            if(defaultSort) {
                return sort.compare(m1, m2);
            }
            return sort.reversed().compare(m1, m2);
        });
    }

    @Override
    public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
        Movie movie = (Movie) items.get(index);
        return new String[]{
                movie.isOnPlex() ? plexEmote : radarrEmote,
                movie.getFormattedTitle(),
                movie.getFormattedReleaseDate()
        };
    }

    /**
     * Build an emote key to explain the platform icons that are required
     * based on the platform that the movies were found on
     *
     * @param movies      Array of movies found for search query
     * @param library     Search was performed on library items
     * @param emoteHelper Emote helper
     * @return Emote key
     */
    private static String buildEmoteKey(Movie[] movies, boolean library, EmoteHelper emoteHelper) {
        StringBuilder key = new StringBuilder();
        if(Arrays.stream(movies).anyMatch(Movie::isOnPlex)) {
            key.append(EmoteHelper.formatEmote(emoteHelper.getPlex())).append(" = On Plex\n\n");
        }
        if(Arrays.stream(movies).anyMatch(movie -> !movie.isOnPlex())) {
            key
                    .append(EmoteHelper.formatEmote(emoteHelper.getRadarr()))
                    .append(library
                            ? " = On Radarr - Movie **is not** on Plex but is being monitored.\n\n"
                            : " = Available on Radarr - Search by the id to add to Plex.\n\n"
                    );
        }
        return key.toString();
    }
}
