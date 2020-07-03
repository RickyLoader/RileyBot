package Bot;


import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Random;

public class Listener extends ListenerAdapter {

    private final DiscordCommandManager commandManager = new DiscordCommandManager();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        System.out.println("Bot is now running!");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        //commandManager.handleCommand(event);
        welcomeCunt(event.getGuild().getSelfMember(), event.getMember(), event.getChannel());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        try {
            Guild guild = event.getGuild();
            Member author = event.getMember();
            targetCunt(guild, author);
            welcomeCunt(guild.getSelfMember(), author, guild.getDefaultChannel());
            //TODO guild.getDefaultChannel().sendMessage(quickCommand(author.getUser())).queue();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int[] pickRandomCommands(int bound, int size) {
        int[] indexes = new int[bound];
        Random rand = new Random();
        for(int i = 0; i < bound; i++) {
            indexes[i] = rand.nextInt(size);
        }
        return indexes;
    }

    private void welcomeCunt(Member self, Member cunt, MessageChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("Hey " + cunt.getUser().getName() + "!");
        builder.setAuthor(self.getUser().getName(), self.getUser().getAvatarUrl(), self.getUser().getAvatarUrl());
        builder.setThumbnail(self.getUser().getAvatarUrl());
        builder.setDescription("Here's some stuff I can do");
        ArrayList<DiscordCommand> commands = commandManager.getCommands();
        for(int index : pickRandomCommands(5, commands.size())) {
            DiscordCommand c = commands.get(index);
            builder.addField("**Trigger**", c.getHelpName(), true);
            builder.addField("**Description**", c.getDesc(), true);
            builder.addBlankField(true);
        }
        channel.sendMessage(cunt.getAsMention()).queue();
        channel.sendMessage(builder.build()).queue();
    }

    private void targetCunt(Guild guild, Member cunt) {
        Role targetRole = guild.getRolesByName("target", true).get(0);
        guild.addRoleToMember(cunt, targetRole).complete();
    }
}
