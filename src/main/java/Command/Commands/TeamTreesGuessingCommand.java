package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.Secret;
import net.dv8tion.jda.api.entities.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guess how many trees have been planted
 */
public class TeamTreesGuessingCommand extends DiscordCommand {
    private TreeGuess guess;

    public TeamTreesGuessingCommand() {
        super("teamtrees", "Guess how many trees!");
        setBotInput(true);
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        DecimalFormat df = new DecimalFormat("#,###");

        if(context.getLowerCaseMessage().equals(getTrigger())) {
            if(guess != null && (System.currentTimeMillis() - guess.getGuessTime()) < 120000) {
                return;
            }
            guess = new TreeGuess();
            channel.sendMessage("I am going to guess: **" + df.format(guess.getTrees()) + " trees**").queue();
            return;
        }

        String desc = context.getMessage().getEmbeds().get(0).getDescription();
        String regex = "\\[#TeamTrees]\\(https://teamtrees.org/\\) \n```\\d+ Trees Funded!```";

        if(desc == null || !desc.matches(regex) || guess == null) {
            return;
        }

        Matcher matcher = Pattern.compile("\\d+").matcher(desc);
        if(!matcher.find()) {
            guess = null;
            return;
        }

        long trees = Long.parseLong(desc.substring(matcher.start(), matcher.end()));
        long offBy = Math.abs(trees - guess.getTrees());
        channel.sendMessage(
                (offBy == 0) ? "I was right!" : "Fuck, I was off by " + df.format(offBy) + " trees"
        ).queue();
        guess = null;
    }

    @Override
    public boolean matches(String query, Message message) {
        User author = message.getAuthor();
        if(!author.isBot() && query.equals(getTrigger())) {
            return true;
        }
        List<MessageEmbed> embeds = message.getEmbeds();
        return author.getIdLong() == Secret.BROCK_ID && !embeds.isEmpty();
    }

    /**
     * Hold the tree guess
     */
    private static class TreeGuess {
        private final long trees, guessTime;

        public TreeGuess() {
            this.trees = fetchTrees();
            this.guessTime = System.currentTimeMillis();
        }

        /**
         * Fetch the number of trees planted
         *
         * @return Number of trees planted
         */
        private long fetchTrees() {
            try {
                Document doc = Jsoup.connect("https://www.teamtrees.org").get();
                return Long.parseLong(doc.getElementById("totalTrees").attr("data-count"));
            }
            catch(Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        /**
         * Get the time of the guess
         *
         * @return Time of guess
         */
        public long getGuessTime() {
            return guessTime;
        }

        /**
         * Get the guessed amount of trees
         *
         * @return Guessed amount of trees
         */
        public long getTrees() {
            return trees;
        }
    }
}
