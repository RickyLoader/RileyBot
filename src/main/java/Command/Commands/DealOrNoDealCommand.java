package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.*;
import DOND.DealOrNoDeal;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Play Deal or No Deal
 */
public class DealOrNoDealCommand extends OnReadyDiscordCommand {
    private final HashMap<Long, DealOrNoDeal> gamesByMessageId; // Message Id -> game
    private final HashMap<Long, DealOrNoDeal> gamesByMemberId; // Member Id -> game
    public final static String
            TRIGGER = "dd",
            SELECT_CASE = TRIGGER + " select [#]",
            OPEN_CASE = TRIGGER + " open [#, #, #...]",
            START_GAME = TRIGGER + " start",
            FORFEIT_GAME = TRIGGER + " forfeit";

    public DealOrNoDealCommand() {
        super(
                START_GAME + "\n"
                        + FORFEIT_GAME + "\n"
                        + SELECT_CASE + "\n"
                        + OPEN_CASE,
                "Play Deal or No Deal!"
        );
        this.gamesByMessageId = new HashMap<>();
        this.gamesByMemberId = new HashMap<>();
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

        String action = message.split(" ")[0];
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
                default:
                    channel.sendMessage(getHelpNameCoded()).queue();
            }
        }).start();
    }

    /**
     * Select the case(s) to play for/open
     *
     * @param contestant Member playing
     * @param message    Message containing case number
     * @param channel    Channel to send response
     * @param open       Opening cases (message may contain multiple case numbers)
     */
    private void selectCase(Member contestant, String message, MessageChannel channel, boolean open) {
        DealOrNoDeal game = gamesByMemberId.get(contestant.getIdLong());
        if(game == null || !game.isActive()) {
            channel.sendMessage(contestant.getAsMention() + " You aren't playing Deal or No Deal!").queue();
            return;
        }
        if(open) {
            String[] caseArgs = message.split(",");
            int[] caseNumbers = new int[caseArgs.length];
            for(int i = 0; i < caseArgs.length; i++) {
                caseNumbers[i] = DiscordCommand.toInteger(caseArgs[i].trim());
            }
            game.openCases(caseNumbers);
        }
        else {
            game.selectCase(DiscordCommand.toInteger(message));
        }
    }

    /**
     * Start a game of Deal or No Deal
     *
     * @param contestant Member to play
     * @param channel    Channel to play in
     */
    private void startGame(Member contestant, MessageChannel channel) {
        DealOrNoDeal game = gamesByMemberId.get(contestant.getIdLong());
        if(game != null && game.isActive()) {
            channel.sendMessage(
                    contestant.getAsMention() + " You need to forfeit or finish your current game first!"
            ).queue();
            return;
        }
        game = new DealOrNoDeal(contestant, channel, (updated, oldMessageId) -> {
            gamesByMessageId.remove(oldMessageId);
            if(updated.isActive()) {
                gamesByMessageId.put(updated.getMessageId(), updated);
            }
        });
        gamesByMemberId.put(contestant.getIdLong(), game);
        game.start();
    }

    /**
     * Forfeit the given contestant's game
     *
     * @param contestant Contestant forfeiting
     */
    public void forfeitGame(Member contestant, MessageChannel channel) {
        DealOrNoDeal game = gamesByMemberId.get(contestant.getIdLong());
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

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                long messageId = event.getMessageIdLong();
                Member member = event.getMember();

                if(member == null) {
                    return;
                }

                DealOrNoDeal game = gamesByMessageId.get(messageId);
                if(game == null || messageId != game.getMessageId() || member.getIdLong() != game.getContestantId()) {
                    return;
                }

                game.buttonPressed(event);
            }
        });
    }
}
