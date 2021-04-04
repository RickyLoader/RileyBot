package Bot;

import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
        Guild guild = event.getGuild();
        Member joined = event.getMember();
        targetMember(guild, joined);
        welcomeMember(guild.getSelfMember(), joined, guild.getDefaultChannel());
    }

    /**
     * Post a helpful message containing 3 random commands that the bot can do
     *
     * @param self      Bot member
     * @param newMember New member
     * @param channel   Channel to welcome new member in
     */
    private void welcomeMember(Member self, Member newMember, MessageChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(EmbedHelper.YELLOW);
        builder.setTitle("Hey " + newMember.getUser().getName() + "!");
        builder.setThumbnail(self.getUser().getAvatarUrl());
        builder.setDescription("Here's some stuff I can do " + newMember.getAsMention() + ", now fuck off.");

        DiscordCommand[] random = commandManager.pickRandomCommands(3);


        for(int i = 0; i < random.length; i++) {
            DiscordCommand c = random[i];
            String trigger = c.getHelpName();
            String desc = c.getDesc();

            if(i == 0) {
                builder.addField(EmbedHelper.getTitleField("Trigger", trigger));
                builder.addBlankField(true);
                builder.addField(EmbedHelper.getTitleField("Description", desc));
            }
            else {
                builder.addField(EmbedHelper.getValueField(trigger));
                builder.addBlankField(true);
                builder.addField(EmbedHelper.getValueField(desc));
            }
        }
        channel.sendMessage(builder.build()).queue();
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
}
