package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import DOND.DealOrNoDeal;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.HashMap;

/**
 * Play Deal or No Deal
 */
public class DealOrNoDealCommand extends DiscordCommand {
    private final HashMap<Member, DealOrNoDeal> games;
    public final static String
            TRIGGER = "dd",
            SELECT_CASE = TRIGGER + " select [case #]",
            OPEN_CASE = TRIGGER + " open [case #]",
            DEAL_OFFER = TRIGGER + " [deal/no deal]",
            SWAP_OFFER = TRIGGER + " [keep/swap]",
            START_GAME = TRIGGER + " start",
            FORFEIT_GAME = TRIGGER + " forfeit";

    public DealOrNoDealCommand() {
        super(
                START_GAME + "\n"
                        + FORFEIT_GAME + "\n"
                        + SELECT_CASE + "\n"
                        + OPEN_CASE + "\n"
                        + DEAL_OFFER + "\n"
                        + SWAP_OFFER,
                "Play Deal or No Deal!"
        );
        this.games = new HashMap<>();
        DealOrNoDeal.registerAssets(new ResourceHandler());
    }

    @Override
    public void execute(CommandContext context) {
        Member contestant = context.getMember();
        String message = context.getLowerCaseMessage().replace(TRIGGER, "").trim();
        MessageChannel channel = context.getMessageChannel();
        context.getMessage().delete().queue();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        String action = message.replaceAll("\\d+", "").trim();
        new Thread(() -> {
            switch(action) {
                case "start":
                    startGame(contestant, channel);
                    break;
                case "forfeit":
                    forfeitGame(contestant, channel);
                    break;
                case "open":
                case "select":
                    selectCase(
                            contestant,
                            message.replace(action, "").trim(),
                            channel,
                            action.equals("open")
                    );
                    break;
                case "keep":
                case "swap":
                    answerSwap(contestant, action.equals("swap"), channel);
                    break;
                case "no deal":
                case "deal":
                    answerOffer(contestant, action.equals("deal"), channel);
                    break;
                default:
                    channel.sendMessage(getHelpNameCoded()).queue();
            }
        }).start();
    }

    /**
     * Select the case to play for/open
     *
     * @param contestant Member playing
     * @param message    Message containing case number
     * @param channel    Channel to send response
     * @param open       Opening a case
     */
    private void selectCase(Member contestant, String message, MessageChannel channel, boolean open) {
        DealOrNoDeal game = games.get(contestant);
        if(game == null || !game.isActive()) {
            channel.sendMessage(contestant.getAsMention() + " You aren't playing Deal or No Deal!").queue();
            return;
        }
        int caseNumber = DiscordCommand.getQuantity(message);
        if(open) {
            game.openCase(caseNumber);
        }
        else {
            game.selectCase(caseNumber);
        }
    }

    /**
     * Answer the banker's offer
     *
     * @param contestant Member playing
     * @param deal       Answer to offer
     * @param channel    Channel to send response
     */
    private void answerOffer(Member contestant, boolean deal, MessageChannel channel) {
        DealOrNoDeal game = games.get(contestant);
        if(game == null || !game.isActive()) {
            channel.sendMessage(contestant.getAsMention() + " Who are you talking to?").queue();
            return;
        }
        game.answerOffer(deal);
    }

    /**
     * Answer the offer to swap cases
     *
     * @param contestant Member playing
     * @param swap       Answer to swap offer
     * @param channel    Channel to send response
     */
    private void answerSwap(Member contestant, boolean swap, MessageChannel channel) {
        DealOrNoDeal game = games.get(contestant);
        if(game == null || !game.isActive()) {
            channel.sendMessage(contestant.getAsMention() + " Nobody offered you a swap.").queue();
            return;
        }
        game.answerSwap(swap);
    }

    /**
     * Start a game of Deal or No Deal
     *
     * @param contestant Member to play
     * @param channel    Channel to play in
     */
    private void startGame(Member contestant, MessageChannel channel) {
        DealOrNoDeal game = games.get(contestant);
        if(game != null && game.isActive()) {
            channel.sendMessage(
                    contestant.getAsMention() + " You need to forfeit or finish your current game first!"
            ).queue();
            return;
        }
        game = new DealOrNoDeal(contestant, channel);
        games.put(contestant, game);
        game.start();
    }

    /**
     * Forfeit the given contestant's game
     *
     * @param contestant Contestant forfeiting
     */
    public void forfeitGame(Member contestant, MessageChannel channel) {
        DealOrNoDeal game = games.get(contestant);
        if(game == null || !game.isActive()) {
            channel.sendMessage(contestant.getAsMention() + " forfeit what?").queue();
            return;
        }
        game.forfeit();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(TRIGGER);
    }
}
