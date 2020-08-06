package Bot;


import Command.Structure.DiscordCommand;
import Network.ImgurManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Event listener for bot
 */
public class Listener extends ListenerAdapter {

    private final DiscordCommandManager commandManager = new DiscordCommandManager();

    /**
     * Send a message to the default channel of all guilds the bot is a member of when it starts up
     *
     * @param event Bot is ready
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        System.out.println("\n\nBot is now running!");
        for(Guild g : event.getJDA().getGuilds()) {
            TextChannel channel = g.getDefaultChannel() == null ? g.getTextChannels().get(0) : g.getDefaultChannel();
            channel.sendMessage("I have been restarted").queue();
        }
    }

    /**
     * Message has been received in a chat, check if it pertains to a command
     *
     * @param event Message has been received
     */
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        commandManager.handleCommand(event);
    }

    /**
     * Welcome a new user when they join and add them to the kill list
     *
     * @param event User has joined
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        try {
            Guild guild = event.getGuild();
            Member author = event.getMember();
            targetCunt(guild, author);
            welcomeCunt(guild.getSelfMember(), author, guild.getDefaultChannel());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Post a helpful message containing 5 random commands that the bot can do
     *
     * @param self    Bot member
     * @param cunt    New member
     * @param channel Channel to welcome new member in
     */
    private void welcomeCunt(Member self, Member cunt, MessageChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("Hey " + cunt.getUser().getName() + "!");
        builder.setThumbnail(self.getUser().getAvatarUrl());
        builder.setDescription("Here's some stuff I can do, now fuck off.");

        DiscordCommand[] random = commandManager.pickRandomCommands(5);


        for(int i = 0; i < random.length; i++) {
            DiscordCommand c = random[i];
            String triggerTitle = "\u200B", descTitle = "\u200B";
            if(i == 0) {
                triggerTitle = "**Trigger**";
                descTitle = "**Description**";
            }
            builder.addField(triggerTitle, c.getHelpName(), true);
            builder.addBlankField(true);
            builder.addField(descTitle, c.getDesc(), true);
        }
        channel.sendMessage(cunt.getAsMention()).queue();
        channel.sendMessage(builder.build()).queue();
    }

    /**
     * Add a newly joined member to the kill list by applying the 'target' role
     *
     * @param guild Guild to search for role
     * @param cunt  Newly joined member
     */
    private void targetCunt(Guild guild, Member cunt) {
        Role targetRole = guild.getRolesByName("target", true).get(0);
        guild.addRoleToMember(cunt, targetRole).complete();
    }
}
