package Runescape.OSRS.Polling.PageableMessage;

import Command.Structure.*;
import Runescape.OSRS.Polling.Answer;
import Runescape.OSRS.Polling.Poll;
import Runescape.OSRS.Polling.Question;
import net.dv8tion.jda.api.entities.Emote;

import java.util.Arrays;
import java.util.List;

/**
 * Pageable OSRS poll message showing questions and results
 */
public class PollMessage extends PageableListEmbed<Question> {
    private final String pass, fail;
    private final ProgressBar progressBar;
    private final boolean hideResults;
    private final Emote blankEmote;

    /**
     * Initialise the message values
     *
     * @param context Command context
     * @param poll    Poll to display
     * @param footer  Footer to use in the embed
     */
    public PollMessage(CommandContext context, Poll poll, String footer) {
        super(
                context,
                Arrays.asList(poll.getQuestions()),
                EmbedHelper.OSRS_LOGO,
                "OSRS Poll #" + poll.getNumber() + "\n\n" + poll.getOpenPeriod(),
                getDescription(poll),
                footer,
                3,
                poll.isOpen() ? EmbedHelper.ORANGE : EmbedHelper.GREEN
        );
        EmoteHelper emoteHelper = context.getEmoteHelper();
        this.progressBar = new ProgressBar(
                emoteHelper.getSwordHandle(),
                emoteHelper.getSwordBlade(),
                emoteHelper.getSwordTip()
        );
        this.pass = emoteHelper.getComplete().getAsMention();
        this.fail = emoteHelper.getFail().getAsMention();

        // Don't show the results if the poll is still running, or the votes are not correct on the wiki yet
        this.hideResults = poll.isOpen() || !poll.resultsAvailable();
        this.blankEmote = emoteHelper.getBlankGap();
    }

    @Override
    public void sortItems(List<Question> items, boolean defaultSort) {
        items.sort((o1, o2) -> defaultSort ? o1.getNumber() - o2.getNumber() : o2.getNumber() - o1.getNumber());
    }

    /**
     * Get the description to use in the poll message. This displays
     * the total number of votes in the poll, the poll title, etc.
     *
     * @param poll Poll to get description for
     * @return Poll message description
     */
    private static String getDescription(Poll poll) {
        final String desc = "**Title**: "
                + EmbedHelper.embedURL(poll.getTitle(), poll.getUrl())
                + "\n**Votes**: " + (poll.isOpen() ? "*Poll still running*" : poll.getFormattedTotalVotes());

        // Results are available when the poll closes, however may be delayed for some time
        return poll.isOpen()
                ? desc
                : poll.resultsAvailable()
                ? desc
                : desc + "\n**Note**: The poll has ended but the results are not available on the Wiki yet!";
    }

    /**
     * Build a String showing the votes for each answer of a question,
     * and the corresponding sword images.
     *
     * @param question to build summary for answers
     * @return String summary of answers
     */
    private String buildAnswerDisplay(Question question) {
        StringBuilder builder = new StringBuilder();
        Answer[] answers = question.getAnswers();

        final int maxAnswerLength = 4;

        for(int i = 0; i < answers.length; i++) {
            Answer a = answers[i];

            /*
             * Votes are hidden until the poll closes, display only the answer text when the poll is open (or when the
             * results are not on the wiki yet).
             */
            if(hideResults) {
                builder.append(a.getText());
            }
            else {
                builder.append(
                        buildSword(
                                a.getPercentageVote(),
                                question.getWinner().getPercentageVote(),
                                question.isOpinionQuestion()
                        )
                )
                        .append("`")
                        .append(a.formatVotes())
                        .append("` ")
                        .append(a.getText(maxAnswerLength));
            }

            if(i < answers.length - 1) {
                builder.append("\n");
            }
        }

        return hideResults

                // Code block truncates first line if a \n character isn't included?
                ? "```" + "\n" + builder.toString() + "```"
                : builder.toString();
    }

    /**
     * Build the sword emote image based on the percentage of votes an Answer has.
     * Pad the sword out with blank characters to always be a consistent size (the size of the winning answer's sword)
     *
     * @param percentageVotes        Percentage of total votes the answer to a question has
     * @param winningPercentageVotes Winning answer's percentage of total votes
     * @param opinionQuestion        Answer if from an opinion question
     * @return Image of a sword
     */
    private String buildSword(double percentageVotes, double winningPercentageVotes, boolean opinionQuestion) {

        // Emote length of winning answer's sword emote
        int maxSwordEmoteLength = progressBar.getEmoteLength(
                calculateSwordSections(winningPercentageVotes),
                true
        );

        if(opinionQuestion) {
            maxSwordEmoteLength++; // +1 for checkmark emote
        }

        final int sections = calculateSwordSections(percentageVotes);

        String sword = progressBar.build(sections, true);
        int swordEmoteLength = progressBar.getEmoteLength(sections, true);

        /*
         * The question is an opinion question and the answer is the highest voted opinion,
         * prepend a checkmark emote to the sword
         */
        if(opinionQuestion && percentageVotes == winningPercentageVotes) {
            sword = pass + sword;
            swordEmoteLength++; // +1 for checkmark emote
        }

        // How many emotes are required to pad the sword out to the max length
        final int paddingEmotesRequired = maxSwordEmoteLength - swordEmoteLength;

        // Pad out the sword to reach the maximum emote length
        return paddingEmotesRequired > 0
                ? sword + EmoteHelper.getRepeatedEmote(blankEmote, paddingEmotesRequired)
                : sword;
    }

    /**
     * Calculate the number of sections to use when building the sword emote image
     *
     * @param percentageVotes Percentage of total votes the answer to a question has
     * @return Number of sections to use when building the sword emote image
     */
    private int calculateSwordSections(double percentageVotes) {
        final int divisor = 15;
        return (int) percentageVotes / divisor;
    }

    @Override
    public String getName(Question question) {

        /*
         * If the question is an opinion question, the highest voted opinion will have a checkmark beside it instead
         * of a checkmark beside the question.
         */
        if(hideResults || question.isOpinionQuestion()) {
            return question.getText();
        }

        // Add pass/fail emote beside the question
        return (question.isPassed() ? pass : fail) + " " + question.getText();
    }

    @Override
    public String getValue(Question question) {
        return buildAnswerDisplay(question);
    }

    @Override
    public String getNoItemsDescription() {
        return "There are no questions for this poll somehow!";
    }
}