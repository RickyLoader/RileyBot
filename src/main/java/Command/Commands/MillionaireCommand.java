package Command.Commands;

import COD.Session;
import Command.Structure.*;
import Millionaire.Bank;
import Millionaire.MillionaireGameshow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Play who wants to be a millionaire with emotes
 */
public class MillionaireCommand extends DiscordCommand {
    private final HashMap<Member, MillionaireGameshow> games;
    private EmoteListener listener;

    public MillionaireCommand() {
        super(
                "millionaire start\nmillionaire forfeit\nmillionaire bank\nmillionaire bank [@someone]\nmillionaire leaderboard",
                "Play a game of who wants to be a millionaire!"
        );
        this.games = new HashMap<>();
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
                .replace("millionaire", "")
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
                startGame(owner, channel, context.getEmoteHelper(), context.getJDA());
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
        new PageableTableEmbed(
                context.getJDA(),
                channel,
                context.getEmoteHelper(),
                Bank.getLeaderboard(),
                MillionaireGameshow.thumb,
                "Millionaire Leaderboard",
                "Here are the top contestants!",
                new String[]{
                        "Rank",
                        "Details",
                        "Games"
                },
                5,
                EmbedHelper.getPurple()
        ) {
            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                ArrayList<Bank> banks = new ArrayList<>();
                for(Object bank : items) {
                    banks.add((Bank) bank);
                }
                Bank.sortBanks(banks, defaultSort);
                updateItems(banks);
            }

            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Bank bank = (Bank) items.get(index);
                int rank = defaultSort ? (index + 1) : (items.size() - index);
                return new String[]{
                        String.valueOf(rank),
                        bank.getSummary(),
                        String.valueOf(bank.getGamesPlayed())
                };
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
                    "Oi " + instigator.getAsMention() + ", " + who + " played any games of **millionaire** cunt."
            ).queue();
            return;
        }

        String bankHelp = "millionaire bank | millionaire bank [@someone]";
        channel.sendMessage(bank.getBankMessage(bankHelp)).queue();
    }

    /**
     * Forfeit a game of who wants to be a millionaire
     *
     * @param owner   Game owner
     * @param channel Channel to inform of incorrect usage
     */
    private void forfeitGame(Member owner, MessageChannel channel) {
        MillionaireGameshow gameShow = games.get(owner);
        if(gameShow == null || !gameShow.isActive()) {
            channel.sendMessage(owner.getAsMention() + " You don't have a game to forfeit!").queue();
            return;
        }
        gameShow.forfeit();
        games.remove(owner);
    }

    /**
     * Start a game of who wants to be a millionaire
     *
     * @param owner   Game owner
     * @param channel Channel to play in
     * @param helper  Emote helper
     * @param jda     JDA for listener
     */
    private void startGame(Member owner, MessageChannel channel, EmoteHelper helper, JDA jda) {
        MillionaireGameshow gameShow = games.get(owner);
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
                "millionaire start | millionaire forfeit | millionaire leaderboard"
        );

        games.put(owner, gameShow);
        if(this.listener == null) {
            this.listener = getEmoteListener();
            jda.addEventListener(this.listener);
        }
        gameShow.start();
    }

    /**
     * Get an emote listener for calling the Gameshow instance when emotes are clicked
     *
     * @return Emote listener
     */
    private EmoteListener getEmoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();
                Member member = guild.getMember(user);
                MillionaireGameshow gameshow = games.get(member);

                if(gameshow == null) {
                    return;
                }

                if(reactID == gameshow.getGameId() && gameshow.isActive()) {
                    gameshow.reactionAdded(reaction);
                }
            }
        };
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("millionaire");
    }
}
