package Runescape.OSRS.Polling.PageableMessage;

import Command.Structure.*;
import Runescape.OSRS.Polling.Answer;
import Runescape.OSRS.Polling.Poll;
import Runescape.OSRS.Polling.Question;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Pageable OSRS poll message showing questions and results
 */
public class PollMessage extends PageableListEmbed {
    private final String pass, fail;
    private final ProgressBar progressBar;
    private final boolean open;

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
                "**Title**: "
                        + EmbedHelper.embedURL(poll.getTitle(), poll.getUrl())
                        + "\n**Votes**: " + (poll.isOpen() ? "None" : poll.getFormattedTotalVotes()),
                footer,
                3,
                EmbedHelper.GREEN
        );
        EmoteHelper emoteHelper = context.getEmoteHelper();
        this.progressBar = new ProgressBar(
                emoteHelper.getSwordHandle(),
                emoteHelper.getSwordBlade(),
                emoteHelper.getSwordTip()
        );
        this.pass = EmoteHelper.formatEmote(emoteHelper.getComplete());
        this.fail = EmoteHelper.formatEmote(emoteHelper.getFail());
        this.open = poll.isOpen();
    }

    @Override
    public void sortItems(List<?> items, boolean defaultSort) {
        items.sort((Comparator<Object>) (o1, o2) -> {
            Question q1 = (Question) o1;
            Question q2 = (Question) o2;
            if(defaultSort) {
                return q1.getNumber() - q2.getNumber();
            }
            return q2.getNumber() - q1.getNumber();
        });
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
        for(int i = 0; i < answers.length; i++) {
            Answer a = answers[i];
            builder.append(
                    buildSword(
                            a.getPercentageVote(),
                            question.isOpinionQuestion() && a == question.getWinner()
                    )
            )
                    .append(" ")
                    .append(a.formatVotes())
                    .append(" -> ")
                    .append(a.getText());

            if(i < answers.length - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Build the sword image based on the percentage of votes an Answer has
     *
     * @param percentageVotes Percentage of total votes the answer to a question has
     * @param highestOpinion  Answer is the highest voted opinion
     * @return Image of a sword
     */
    private String buildSword(double percentageVotes, boolean highestOpinion) {
        int sections = (int) (percentageVotes / 15);
        String sword = progressBar.build(sections, true);
        if(!open && highestOpinion) {
            sword = pass + sword;
        }
        return sword;
    }

    @Override
    public String getName(int currentIndex) {
        Question question = (Question) getItems().get(currentIndex);
        if(open || question.isOpinionQuestion()) {
            return question.getText();
        }
        String emote = question.isPassed() ? pass : fail;
        return emote + " " + question.getText();
    }

    @Override
    public String getValue(int currentIndex) {
        return buildAnswerDisplay(((Question) getItems().get(currentIndex)));
    }
}