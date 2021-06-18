package Command.Commands.Passive;

import Bot.DiscordBot;
import Command.Structure.*;
import COD.Gunfight;
import Countdown.Countdown;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Track Modern Warfare wins/losses with an embedded message
 */
public class GunfightCommand extends OnReadyDiscordCommand {
    private final HashMap<Long, Gunfight> gunfightSessionsByMessageId; // Message ID -> gunfight
    private final HashMap<Long, Gunfight> gunfightSessionsByMemberId; // Member ID -> gunfight
    private static final long OWNER_TRANSFER_THRESHOLD = 1200000; // 20 minutes

    public GunfightCommand() {
        super("gunfight!", "Play a fun game of gunfight!");
        this.gunfightSessionsByMessageId = new HashMap<>();
        this.gunfightSessionsByMemberId = new HashMap<>();
    }

    /**
     * Start the gunfight tracking or relocate the current message
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        Member member = context.getMember();
        long memberId = member.getIdLong();
        Gunfight gunfight = gunfightSessionsByMemberId.get(memberId);

        // Member has a running session, resend the message
        if(gunfight != null && gunfight.isActive()) {
            gunfight.relocate();
            return;
        }

        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();

        // Start new session
        gunfight = new Gunfight(
                channel,
                member,
                context.getEmoteHelper(),
                context.getJDA(),
                (updated, oldMessageId) -> {
                    gunfightSessionsByMessageId.remove(oldMessageId);
                    gunfightSessionsByMessageId.put(updated.getGameId(), updated);
                }
        );
        gunfightSessionsByMemberId.put(memberId, gunfight);
        gunfight.startGame();
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                Member member = event.getMember();
                Gunfight gunfight = gunfightSessionsByMessageId.get(event.getMessageIdLong());

                // Error fetching member who clicked button or the message is not a Gunfight/is an inactive Gunfight
                if(member == null || gunfight == null || !gunfight.isActive()) {
                    return;
                }

                // Not owner, check if ownership should be transferred
                if(member.getUser().getIdLong() != gunfight.getOwnerUserId()) {
                    long lastUpdate = gunfight.getLastUpdate();

                    // Session just started
                    if(lastUpdate == 0) {
                        event.deferReply(true).setContent("This isn't yours to control!").queue();
                        return;
                    }

                    final long now = System.currentTimeMillis();
                    final long timePassed = now - lastUpdate;

                    // Owner reacted too recently
                    if(timePassed < OWNER_TRANSFER_THRESHOLD) {
                        String mention = DiscordBot.getUserMention(jda, gunfight.getOwnerUserId());
                        Countdown countdown = Countdown.from(now, now + (OWNER_TRANSFER_THRESHOLD - timePassed));

                        event.deferReply(true).setContent(
                                mention + " was active too recently, you'll be able to take control in: "
                                        + countdown.formatMinutesSeconds() + "!"
                        ).queue();
                        return;
                    }

                    // Remove old owner mapping
                    gunfightSessionsByMemberId.remove(gunfight.getOwnerMemberId());

                    // Transfer ownership
                    gunfight.setOwner(member);
                    gunfightSessionsByMemberId.put(gunfight.getOwnerMemberId(), gunfight);
                }
                gunfight.buttonClicked(event);
            }
        });
    }
}
