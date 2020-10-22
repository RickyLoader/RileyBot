package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmoteListener;
import Millionaire.MillionaireGameshow;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;

/**
 * Play who wants to be a millionaire with emotes
 */
public class MillionaireCommand extends DiscordCommand {
    private final HashMap<Member, MillionaireGameshow> games;
    private EmoteListener listener;

    public MillionaireCommand() {
        super("millionaire start\nmillionaire forfeit", "Play a game of who wants to be a millionaire!");
        this.games = new HashMap<>();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage().replace("millionaire", "").trim();
        Member owner = context.getMember();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(message.equals("start")) {
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
                    context.getEmoteHelper(),
                    getHelpName().replace("\n", " | ")
            );

            games.put(owner, gameShow);
            if(this.listener == null) {
                this.listener = getEmoteListener();
                context.getJDA().addEventListener(this.listener);
            }
            gameShow.start();
            return;
        }

        if(message.equals("forfeit")) {
            MillionaireGameshow gameShow = games.get(owner);
            if(gameShow == null || !gameShow.isActive()) {
                channel.sendMessage(owner.getAsMention() + " You don't have a game to forfeit!").queue();
                return;
            }
            gameShow.stop();
            games.remove(owner);
        }
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
