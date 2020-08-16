package Bot;


import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;

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
            channel.sendMessage(getUpMessage()).queue();
        }
    }

    /**
     * Get an up message to send to all guilds the bot is a member of when starting
     *
     * @return Bot is up message
     */
    private String getUpMessage() {
        String[] messages = new String[]{
                "I have been restarted",
                "I'm back!",
                "Let's do this!",
                "I'm back, I was wriggling the wobbly"
        };
        return jiggle(messages[new Random().nextInt(messages.length)]);
    }

    /**
     * Alternate upper and lower case randomly
     *
     * @param message String to jiggle
     * @return Jiggled string
     */
    private String jiggle(String message) {
        Random rand = new Random();
        message = message.toLowerCase();
        return StringUtils.join(Arrays.stream(message.split("")).map(s -> rand.nextInt(2) == 0 ? s.toUpperCase() : s).toArray());
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
        builder.setColor(EmbedHelper.getYellow());
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
    private void targetCunt(Guild guild, Member target) {
        Role targetRole = guild.getRolesByName("target", true).get(0);
        guild.addRoleToMember(target, targetRole).complete();
    }
}
