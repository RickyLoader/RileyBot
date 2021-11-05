package Bot;

import Network.Secret;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Event listener for bot
 */
public class Listener extends ListenerAdapter {
    private final DiscordCommandManager commandManager = new DiscordCommandManager();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        commandManager.onReady(event.getJDA());
        System.out.println("\n\nBot is now running!");
    }

    /**
     * Message has been received in a chat, check if it pertains to a command
     *
     * @param event Message has been received
     */
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        commandManager.handleCommand(event);
    }

    /**
     * Welcome a new member when they join and add them to the kill list
     *
     * @param event Member has joined event
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        targetMember(event.getGuild(), event.getMember());
    }

    /**
     * Remove the target role if it is added to a member that the bot cannot kick
     *
     * @param event Role added event
     */
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        Role targetRole = guild.getRolesByName("target", true).get(0);
        Member target = event.getMember();

        if(!event.getRoles().contains(targetRole)) {
            return;
        }

        if(!guild.getSelfMember().canInteract(target)) {
            guild.removeRoleFromMember(target, targetRole).queue();
        }
    }

    /**
     * Add a newly joined member to the kill list by applying the 'target' role
     *
     * @param guild  Guild to search for role
     * @param target Newly joined member
     */
    private void targetMember(Guild guild, Member target) {
        List<Role> targetRole = guild.getRolesByName("target", true);
        if(!targetRole.isEmpty()) {
            guild.addRoleToMember(target, targetRole.get(0)).queue();
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        final MessageChannel channel = event.getJDA().getTextChannelById(Secret.SELF_CHANNEL_ID);

        // Cannot find channel
        if(channel == null) {
            return;
        }

        channel.sendMessage(event.getMessage()).queue();
    }
}
