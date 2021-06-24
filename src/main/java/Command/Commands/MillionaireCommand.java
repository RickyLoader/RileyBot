package Command.Commands;

import Command.Structure.*;
import Millionaire.Bank;
import Millionaire.MillionaireGameshow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Play who wants to be a millionaire with emotes
 */
public class MillionaireCommand extends OnReadyDiscordCommand {
    private final HashMap<Long, MillionaireGameshow> games;
    private static final String
            TRIGGER = "millionaire",
            BANK_TRIGGER = TRIGGER + " bank",
            BANK_ARGS = BANK_TRIGGER + "\n" + BANK_TRIGGER + " [@someone]";
    private final String helpMessage;

    public MillionaireCommand() {
        super(
                TRIGGER,
                "Play a game of who wants to be a millionaire!",
                TRIGGER + " start\n"
                        + TRIGGER + " forfeit\n"
                        + BANK_ARGS
                        + "\n" + TRIGGER + " leaderboard"
        );
        this.games = new HashMap<>();
        this.helpMessage = "Type: " + getTrigger() + " for help";
    }

    /**
     * Strip the trigger and mentioned members from the message content
     *
     * @param message Raw message
     * @return Stripped message content
     */
    private String stripMessage(String message) {
        return message
                .toLowerCase()
                .replace(getTrigger(), "")
                .replaceAll("<@![\\d]+>", "")
                .trim();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member owner = context.getMember();
        String message = stripMessage(context.getMessage().getContentRaw());

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        switch(message) {
            case "start":
                startGame(owner, channel, context.getEmoteHelper());
                break;
            case "forfeit":
                forfeitGame(owner, channel);
                break;
            case "bank":
                getBank(owner, context.getMessage(), channel);
                break;
            case "leaderboard":
                getLeaderboard(context);
                break;
            default:
                channel.sendMessage(getHelpNameCoded()).queue();
                break;
        }
    }

    /**
     * Get the leaderboard message
     *
     * @param context Command context
     */
    private void getLeaderboard(CommandContext context) {
        ArrayList<Bank> leaderboard = Bank.getLeaderboard();
        MessageChannel channel = context.getMessageChannel();
        if(leaderboard == null) {
            channel.sendMessage("There are no scores to show, play some games!").queue();
            return;
        }
        new PageableTableEmbed<Bank>(
                context,
                Bank.getLeaderboard(),
                MillionaireGameshow.THUMB,
                "Millionaire Leaderboard",
                "Check out **" + BANK_TRIGGER + "** to view your personal stats!",
                helpMessage,
                new String[]{
                        "Rank",
                        "Details",
                        "Games"
                },
                5,
                EmbedHelper.PURPLE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "The leaderboard is empty!";
            }

            @Override
            public String[] getRowValues(int index, Bank bank, boolean defaultSort) {
                int rank = defaultSort ? (index + 1) : (getItems().size() - index);
                return new String[]{
                        String.valueOf(rank),
                        bank.getSummary(),
                        String.valueOf(bank.getGamesPlayed())
                };
            }

            @Override
            public void sortItems(List<Bank> items, boolean defaultSort) {
                Bank.sortBanks(items, defaultSort);
            }
        }.showMessage();
    }

    /**
     * Build and send a message embed displaying the user's bank
     *
     * @param instigator Member asking for bank data
     * @param message    Message to find which user to get bank for
     * @param channel    Channel to send bank to
     */
    private void getBank(Member instigator, Message message, MessageChannel channel) {
        List<Member> mentioned = message.getMentionedMembers();
        Member member = mentioned.isEmpty() ? instigator : mentioned.get(0);
        Bank bank = Bank.getMemberBank(member);

        if(bank == null) {
            String who = member == instigator ? "You haven't" : member.getEffectiveName() + " hasn't";
            channel.sendMessage(
                    "Oi " + instigator.getAsMention() + ", " + who
                            + " played any games of **" + getTrigger() + "** cunt."
            ).queue();
            return;
        }

        channel.sendMessage(
                bank.getBankMessage("Try: " + BANK_ARGS.replaceAll("\n", " | "))
        ).queue();
    }

    /**
     * Forfeit a game of who wants to be a millionaire
     *
     * @param owner   Game owner
     * @param channel Channel to inform of incorrect usage
     */
    private void forfeitGame(Member owner, MessageChannel channel) {
        MillionaireGameshow gameShow = games.get(owner.getIdLong());
        if(gameShow == null || !gameShow.isActive()) {
            channel.sendMessage(owner.getAsMention() + " You don't have a game to forfeit!").queue();
            return;
        }
        gameShow.forfeit();
        games.remove(owner.getIdLong());
    }

    /**
     * Start a game of who wants to be a millionaire
     *
     * @param owner   Game owner
     * @param channel Channel to play in
     * @param helper  Emote helper
     */
    private void startGame(Member owner, MessageChannel channel, EmoteHelper helper) {
        MillionaireGameshow gameShow = games.get(owner.getIdLong());
        if(gameShow != null && gameShow.isActive()) {
            channel.sendMessage(
                    owner.getAsMention() + " You need to forfeit or finish your current game first!"
            ).queue();
            return;
        }

        gameShow = new MillionaireGameshow(
                owner,
                channel,
                helper,
                helpMessage
        );

        games.put(owner.getIdLong(), gameShow);
        gameShow.start();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                Member member = event.getMember();
                if(member == null || !games.containsKey(member.getIdLong())) {
                    return;
                }
                MillionaireGameshow gameshow = games.get(member.getIdLong());
                if(event.getMessageIdLong() != gameshow.getGameId() || !gameshow.isActive()) {
                    return;
                }
                gameshow.buttonClicked(event);
            }
        });
    }
}
