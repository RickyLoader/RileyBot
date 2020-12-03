package Command.Commands.Passive;

import Command.Structure.*;
import Runescape.OSRS.Polling.PollManager;
import Runescape.OSRS.Polling.PollManager.Poll;
import Runescape.OSRS.Polling.PollManager.Poll.Question;
import Runescape.OSRS.Polling.PollManager.Poll.Question.Answer;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class OSRSPollCommand extends DiscordCommand {
    private final PollManager pollManager;

    public OSRSPollCommand() {
        super("osrspoll\nosrspoll [poll number]", "Look at poll results!");
        this.pollManager = new PollManager();
    }

    @Override
    public void execute(CommandContext context) {
        String[] args = context.getLowerCaseMessage().trim().split(" ");
        MessageChannel channel = context.getMessageChannel();

        if(args.length > 2) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        int number = 0;
        if(args.length == 2) {
            number = getQuantity(args[1]);
            if(number <= 0) {
                channel.sendMessage(getHelpNameCoded()).queue();
                return;
            }
        }

        Poll poll = pollManager.getPollByNumber(number);

        if(poll == null) {
            channel.sendMessage("That poll doesn't exist (or I couldn't parse it)").queue();
            return;
        }

        new PollMessage(
                context,
                poll
        ).showMessage();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrspoll");
    }

    /**
     * Pageable OSRS poll message
     */
    private static class PollMessage extends PageableListEmbed {
        private final String handle, section, tip, pass, fail;
        private final boolean open;

        /**
         * Embedded message that can be paged through with emotes and displays as a list of fields
         *
         * @param context Command context
         * @param poll    Poll to display
         */
        public PollMessage(CommandContext context, Poll poll) {
            super(
                    context.getJDA(),
                    context.getMessageChannel(),
                    context.getEmoteHelper(),
                    Arrays.asList(poll.getQuestions()),
                    "https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png",
                    "OSRS Poll #" + poll.getNumber() + "\n\n" + poll.getOpenPeriod(),
                    poll.getTitle(),
                    3,
                    EmbedHelper.GREEN
            );
            EmoteHelper emoteHelper = context.getEmoteHelper();
            this.handle = EmoteHelper.formatEmote(emoteHelper.getSwordHandle());
            this.section = EmoteHelper.formatEmote(emoteHelper.getSwordBlade());
            this.tip = EmoteHelper.formatEmote(emoteHelper.getSwordTip());
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
                builder
                        .append(buildSword(a.getPercentageVote(), question.isOpinionQuestion() && a == question.getWinner()))
                        .append(" ")
                        .append(a.formatVotes())
                        .append(" ")
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
            StringBuilder sword = new StringBuilder();
            if(!open && highestOpinion) {
                sword.append(pass);
            }
            sword.append(handle);
            for(int i = 0; i < sections; i++) {
                sword.append(section);
            }
            if(sections > 0) {
                sword.append(tip);
            }
            return sword.toString();
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
}
