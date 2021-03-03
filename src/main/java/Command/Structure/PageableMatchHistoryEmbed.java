package Command.Structure;

import COD.Assets.Breakdown;
import COD.Match.MatchHistory;
import COD.Match.MatchStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static COD.CODManager.*;

/**
 * Pageable COD match history embed with map/mode breakdown charts
 */
public class PageableMatchHistoryEmbed extends PageableTableEmbed {
    private final Emote maps, modes;
    private final MatchHistory matchHistory;
    private final GAME game;

    /**
     * Embedded COD match history message which can be paged through with emotes.
     * Breakdown charts of maps & modes can also be viewed using emotes.
     *
     * @param context      Command context
     * @param matchHistory Match history to
     * @param game         COD game
     * @param thumb        Thumbnail to use for embed
     * @param trigger      Trigger to display in footer
     */
    public PageableMatchHistoryEmbed(CommandContext context, MatchHistory matchHistory, GAME game, String thumb, String trigger) {
        super(
                context,
                matchHistory.getMatches(),
                thumb,
                game.name().toUpperCase() + " Match History: " + matchHistory.getName().toUpperCase(),
                matchHistory.getSummary(),
                "Type: " + trigger + " for help",
                new String[]{"Match", "Details", "Result"},
                3,
                matchHistory.getWins() > matchHistory.getLosses() ? EmbedHelper.GREEN : EmbedHelper.RED
        );
        EmoteHelper emoteHelper = context.getEmoteHelper();
        this.maps = emoteHelper.getMapBreakdown();
        this.modes = emoteHelper.getModeBreakdown();
        this.matchHistory = matchHistory;
        this.game = game;
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex) {
        if(showingBreakdown()) {
            return;
        }
        super.displayItem(builder, currentIndex);
    }

    /**
     * Build a message embed displaying a breakdown of the match history.
     * A breakdown is a pie chart + key detailing the frequency of an item in the match history.
     *
     * @param breakdown     Breakdown to display
     * @param breakdownItem Name of item being broken down - e.g "Map"
     */
    private MessageEmbed buildBreakdownEmbed(Breakdown breakdown, String breakdownItem) {
        return new EmbedBuilder()
                .setTitle(
                        game.name().toUpperCase()
                                + " Match History "
                                + matchHistory.getName().toUpperCase()
                                + "\n" + breakdownItem + " Breakdown"
                )
                .setImage(breakdown.getImageUrl())
                .setDescription("Breakdown for the last " + matchHistory.getMatches().size() + " matches:")
                .setColor(EmbedHelper.GREEN)
                .setThumbnail(getThumb())
                .setFooter(getFooter() + " | Use the paging emotes to return")
                .build();
    }


    @Override
    public MessageEmbed buildMessage() {
        Emote lastAction = getLastAction();
        if(lastAction == maps) {
            return buildBreakdownEmbed(matchHistory.getMapBreakdown(), "Map");
        }
        else if(lastAction == modes) {
            return buildBreakdownEmbed(matchHistory.getModeBreakdown(), "Mode");
        }
        return super.buildMessage();
    }

    @Override
    public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
        MatchStats matchStats = (MatchStats) items.get(index);
        int position = defaultSort ? (index + 1) : (items.size() - index);
        return new String[]{
                String.valueOf(position),
                matchStats.getMatchSummary(),
                getFormattedResult(matchStats)
        };
    }

    @Override
    public void sortItems(List<?> items, boolean defaultSort) {
        items.sort((Comparator<Object>) (o1, o2) -> {
            Date d1 = ((MatchStats) o1).getStart();
            Date d2 = ((MatchStats) o2).getStart();
            return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
        });
    }

    /**
     * Check if a map or mode breakdown is currently being shown in the message.
     *
     * @return Map/mode breakdown is being shown
     */
    private boolean showingBreakdown() {
        return getLastAction() == maps || getLastAction() == modes;
    }

    /**
     * Page forward if a map/mode breakdown is not being displayed.
     */
    @Override
    public void pageForward() {
        if(showingBreakdown()) {
            return;
        }
        super.pageForward();
    }

    /**
     * Page backward if a map/mode breakdown is not being displayed
     */
    @Override
    public void pageBackward() {
        if(showingBreakdown()) {
            return;
        }
        super.pageBackward();
    }

    @Override
    public void addReactions(Message message) {
        super.addReactions(message);
        message.addReaction(maps).queue();
        message.addReaction(modes).queue();
    }

    /**
     * If the added emote is for a map/mode breakdown, return true to update the message.
     * Also return true if the emote was for sorting but a map/mode breakdown is currently being displayed.
     * This allows the paging emotes to act as a back button for returning to the match history from a
     * map/mode breakdown, without actually paging forward/backward or sorting the matches.
     */
    @Override
    public boolean nonPagingEmoteAdded(Emote e) {
        if(e == maps || e == modes || e == getReverse() && showingBreakdown()) {
            return true;
        }
        return super.nonPagingEmoteAdded(e);
    }

    /**
     * Get the result formatted for use in a message embed with an emote and score
     *
     * @param matchStats Match
     * @return Formatted result
     */
    public String getFormattedResult(MatchStats matchStats) {
        MatchStats.RESULT result = matchStats.getResult();
        return result.toString() + " " + getResultEmote(result, getEmoteHelper()) + "\n(" + matchStats.getScore() + ")";
    }

    /**
     * Get the embed String for the emote to use for the given match result
     *
     * @param result      Match result
     * @param emoteHelper Emote helper
     * @return Embed String for emote indicating the result of the match
     */
    public static String getResultEmote(MatchStats.RESULT result, EmoteHelper emoteHelper) {
        switch(result) {
            case WIN:
                return EmoteHelper.formatEmote(emoteHelper.getComplete());
            case LOSS:
                return EmoteHelper.formatEmote(emoteHelper.getFail());
            default:
                return EmoteHelper.formatEmote(emoteHelper.getDraw());
        }
    }
}
