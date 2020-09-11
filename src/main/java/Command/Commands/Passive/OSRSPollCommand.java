package Command.Commands.Passive;

import Command.Structure.*;
import OSRS.Polling.PollManager;
import OSRS.Polling.PollManager.Poll;
import OSRS.Polling.PollManager.Poll.Question;
import OSRS.Polling.PollManager.Poll.Question.Answer;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class OSRSPollCommand extends PageableEmbedCommand {
    private final PollManager pollManager;

    public OSRSPollCommand() {
        super("osrspoll\nosrspoll [poll number]", "Look at poll results!");
        this.pollManager = new PollManager();
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        String[] args = context.getLowerCaseMessage().trim().split(" ");
        MessageChannel channel = context.getMessageChannel();

        if(args.length > 2) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return null;
        }
        int number = 0;
        if(args.length == 2) {
            number = getQuantity(args[1]);
            if(number <= 0) {
                channel.sendMessage(getHelpNameCoded()).queue();
                return null;
            }
        }

        Poll poll = pollManager.getPollByNumber(number);

        if(poll == null) {
            channel.sendMessage("That poll doesn't exist (or I couldn't parse it)").queue();
            return null;
        }

        return new PollMessage(
                channel,
                context.getEmoteHelper(),
                Arrays.asList(poll.getQuestions()),
                "https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png",
                "OSRS Poll #" + poll.getNumber() + "\n\n" + poll.getOpenPeriod(),
                poll.getTitle(),
                poll.isOpen(),
                EmbedHelper.getGreen()
        );
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrspoll");
    }

    private static class PollMessage extends PageableListEmbed {
        private final String handle, section, tip, pass, fail;
        private final boolean open;

        /**
         * Embedded message that can be paged through with emotes and displays as a list of fields
         *
         * @param channel     Channel to send embed to
         * @param emoteHelper Emote helper
         * @param items       List of items to be displayed
         * @param thumb       Thumbnail to use for embed
         * @param title       Title to use for embed
         * @param desc        Description to use for embed
         * @param open        Poll is open
         * @param colour      Optional colour to use for embed
         */
        public PollMessage(MessageChannel channel, EmoteHelper emoteHelper, List<?> items, String thumb, String title, String desc, boolean open, int... colour) {
            super(channel, emoteHelper, items, thumb, title, desc, colour);
            this.handle = EmoteHelper.formatEmote(emoteHelper.getSwordHandle());
            this.section = EmoteHelper.formatEmote(emoteHelper.getSwordBlade());
            this.tip = EmoteHelper.formatEmote(emoteHelper.getSwordTip());
            this.pass = EmoteHelper.formatEmote(emoteHelper.getComplete());
            this.fail = EmoteHelper.formatEmote(emoteHelper.getFail());
            this.open = open;
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
            if(open && highestOpinion) {
                sword.append(pass);
            }
            sword.append(handle);
            for(int i = 0; i < sections; i++) {
                sword.append(section);
            }
            if(sections > 0) {
                sword.append(tip);
            }
            return sword.toString().replace(" ", "");
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
