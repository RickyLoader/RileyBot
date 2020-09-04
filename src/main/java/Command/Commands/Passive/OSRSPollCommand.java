package Command.Commands.Passive;

import Command.Structure.*;
import OSRS.Polling.PollManager;
import OSRS.Polling.PollManager.Poll;
import OSRS.Polling.PollManager.Poll.Question;
import OSRS.Polling.PollManager.Poll.Question.Answer;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class OSRSPollCommand extends PageableEmbedCommand {
    private final PollManager pollManager;

    public OSRSPollCommand() {
        super("osrspoll | osrspoll [poll number]", "Look at poll results!");
        this.pollManager = new PollManager();
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        String[] args = context.getLowerCaseMessage().trim().split(" ");
        MessageChannel channel = context.getMessageChannel();
        Guild guild = context.getHomeGuild();

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
            channel.sendMessage("That poll doesn't exist!").queue();
            return null;
        }
        return new PollMessage(
                channel,
                guild,
                Arrays.asList(poll.getQuestions()),
                "https://support.runescape.com/hc/article_attachments/360002485738/App_Icon-Circle.png",
                "OSRS Poll #" + poll.getNumber() + "\n\n" + poll.getOpenPeriod(),
                poll.getTitle(),
                EmbedHelper.formatEmote(guild.getEmotesByName("s1", true).get(0)),
                EmbedHelper.formatEmote(guild.getEmotesByName("s2", true).get(0)),
                EmbedHelper.formatEmote(guild.getEmotesByName("s3", true).get(0)),
                EmbedHelper.getGreen()
        );
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrspoll");
    }

    private static class PollMessage extends PageableListEmbed {
        private final String handle, section, tip;

        /**
         * Embedded message that can be paged through with emotes and displays as a list of fields
         *
         * @param channel Channel to send embed to
         * @param guild   Guild to find emotes
         * @param items   List of items to be displayed
         * @param thumb   Thumbnail to use for embed
         * @param title   Title to use for embed
         * @param desc    Description to use for embed
         * @param colour  Optional colour to use for embed
         */
        public PollMessage(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String handle, String section, String tip, int... colour) {
            super(channel, guild, items, thumb, title, desc, colour);
            this.handle = handle;
            this.section = section;
            this.tip = tip;
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
         * @param answers Answers to build summary for
         * @return String summary of answers
         */
        private String buildAnswerDisplay(Answer[] answers) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < answers.length; i++) {
                Answer a = answers[i];
                builder
                        .append(buildSword(a.getPercentageVote()))
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
         * @return Image of a sword
         */
        private String buildSword(double percentageVotes) {
            int sections = (int) (percentageVotes / 15);
            StringBuilder sword = new StringBuilder(handle);
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
            return ((Question) getItems().get(currentIndex)).getText();
        }

        @Override
        public String getValue(int currentIndex) {
            return buildAnswerDisplay(((Question) getItems().get(currentIndex)).getAnswers());
        }
    }
}
