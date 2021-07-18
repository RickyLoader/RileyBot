package Command.Structure;

import COD.Match.Breakdown;
import COD.Match.MatchHistory;
import COD.Match.MatchStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.interactions.button.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;

import java.util.*;

import static COD.API.CODManager.*;

/**
 * Pageable COD match history embed with map/mode breakdown charts
 */
public class PageableMatchHistoryEmbed extends PageableTableEmbed<MatchStats> {
    private final Button maps, modes, goBack;
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
        this.maps = new ButtonImpl(
                "maps",
                "Maps",
                ButtonStyle.PRIMARY,
                false,
                Emoji.ofEmote(emoteHelper.getMapBreakdown())
        );
        this.modes = new ButtonImpl(
                "modes",
                "Modes",
                ButtonStyle.PRIMARY,
                false,
                Emoji.ofEmote(emoteHelper.getModeBreakdown())
        );
        this.goBack = new ButtonImpl(
                "back",
                "Return",
                ButtonStyle.DANGER,
                false,
                Emoji.ofEmote(emoteHelper.getBackward())
        );
        this.matchHistory = matchHistory;
        this.game = game;
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex, MatchStats item) {
        if(showingBreakdown()) {
            return;
        }
        super.displayItem(builder, currentIndex, item);
    }

    /**
     * Build a message embed displaying a breakdown of the match history.
     * A breakdown is a pie chart + key detailing the frequency of an item in the match history.
     *
     * @param breakdown Breakdown to display
     */
    private MessageEmbed buildBreakdownEmbed(Breakdown breakdown) {
        return new EmbedBuilder()
                .setTitle(
                        game.name().toUpperCase()
                                + " Match History "
                                + matchHistory.getName().toUpperCase()
                                + "\n" + breakdown.getTitle() + " Breakdown"
                )
                .setImage(breakdown.getImageUrl())
                .setDescription("Breakdown for the last " + matchHistory.getMatches().size() + " matches:")
                .setColor(EmbedHelper.GREEN)
                .setThumbnail(getThumb())
                .setFooter(getFooter())
                .build();
    }

    @Override
    public MessageEmbed buildMessage() {
        if(showingMaps()) {
            return buildBreakdownEmbed(matchHistory.getMapBreakdown());
        }
        else if(showingModes()) {
            return buildBreakdownEmbed(matchHistory.getModeBreakdown());
        }
        return super.buildMessage();
    }

    @Override
    public void sortItems(List<MatchStats> items, boolean defaultSort) {
        items.sort((o1, o2) -> defaultSort
                ? o2.getStart().compareTo(o1.getStart())
                : o1.getStart().compareTo(o2.getStart()));
    }

    /**
     * Check if the message is currently displaying a breakdown
     *
     * @return Message is displaying a breakdown
     */
    private boolean showingBreakdown() {
        return showingMaps() || showingModes();
    }

    /**
     * Check if the message is currently displaying the map breakdown
     *
     * @return Message is displaying map breakdown
     */
    private boolean showingMaps() {
        return hasLastAction() && getLastAction().equals(maps.getId());
    }

    /**
     * Check if the message is currently displaying the mode breakdown
     *
     * @return Message is displaying mode breakdown
     */
    private boolean showingModes() {
        return hasLastAction() && getLastAction().equals(modes.getId());
    }

    @Override
    public ArrayList<Button> getButtonList() {
        if(showingBreakdown()) {
            Button[] buttons = new Button[]{
                    goBack,
                    showingMaps() ? maps.asDisabled() : maps,
                    showingModes() ? modes.asDisabled() : modes,
            };
            return new ArrayList<>(Arrays.asList(buttons));
        }
        ArrayList<Button> buttons = super.getButtonList();
        buttons.add(maps);
        buttons.add(modes);
        return buttons;
    }

    /**
     * If the added button is for a map/mode breakdown, return true to update the message.
     * Also return true if the emote was for sorting but a map/mode breakdown is currently being displayed.
     * This allows the paging  to act as a back button for returning to the match history from a
     * map/mode breakdown, without actually paging forward/backward or sorting the matches.
     *
     * @param buttonId ID of the button which was pressed
     */
    @Override
    public boolean nonPagingButtonPressed(String buttonId) {
        if(buttonId.equals(maps.getId()) || buttonId.equals(modes.getId()) || buttonId.equals(goBack.getId())) {
            return true;
        }
        return super.nonPagingButtonPressed(buttonId);
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
                return emoteHelper.getComplete().getAsMention();
            case LOSS:
                return emoteHelper.getFail().getAsMention();
            default:
                return emoteHelper.getDraw().getAsMention();
        }
    }

    @Override
    public String[] getRowValues(int index, MatchStats matchStats, boolean defaultSort) {
        int position = defaultSort ? (index + 1) : (getItems().size() - index);
        return new String[]{
                String.valueOf(position),
                matchStats.getMatchSummary(),
                getFormattedResult(matchStats)
        };
    }

    @Override
    public String getNoItemsDescription() {
        return "This player hasn't got any recent matches!";
    }
}
