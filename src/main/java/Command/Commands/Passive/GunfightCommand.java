package Command.Commands.Passive;

import Command.Structure.*;
import COD.Gunfight;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Track Modern Warfare wins/losses with an embedded message
 */
public class GunfightCommand extends OnReadyDiscordCommand {
    private final HashMap<Long, Gunfight> gunfightSessions; // Member ID -> gunfight

    public GunfightCommand() {
        super("gunfight!", "Play a fun game of gunfight!");
        this.gunfightSessions = new HashMap<>();
    }

    /**
     * Start the gunfight tracking or relocate the current message
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        long memberId = context.getMember().getIdLong();
        Gunfight gunfight = gunfightSessions.get(memberId);
        if(gunfight != null && gunfight.isActive()) {
            if(System.currentTimeMillis() - gunfight.getLastUpdate() >= 3600000) {
                gunfightSessions.remove(memberId);
            }
            else {
                gunfight.relocate();
                return;
            }
        }
        gunfight = new Gunfight(
                context.getMessageChannel(),
                context.getUser().getIdLong(),
                context.getEmoteHelper(),
                context.getJDA()
        );
        gunfightSessions.put(memberId, gunfight);
        gunfight.startGame();
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                Member member = event.getMember();
                if(member == null) {
                    return;
                }
                Gunfight gunfight = gunfightSessions.get(member.getIdLong());
                if(gunfight == null || !gunfight.isActive() || event.getMessageIdLong() != gunfight.getGameId()) {
                    return;
                }
                gunfight.buttonClicked(event);
            }
        });
    }
}
