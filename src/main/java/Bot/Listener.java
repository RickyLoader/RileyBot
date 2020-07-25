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
        System.out.println("\n\nBot is now running!");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        commandManager.handleCommand(event);
    }

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

    private int[] pickRandomCommands(int bound, int size) {
        int[] indexes = new int[bound];
        ArrayList<Integer> seen = new ArrayList<>();
        Random rand = new Random();
        for(int i = 0; i < bound; i++) {
            int j = rand.nextInt(size);
            while(seen.contains(j)) {
                j = rand.nextInt(size);
            }
            indexes[i] = j;
            seen.add(j);
        }
        return indexes;
    }

    private void welcomeCunt(Member self, Member cunt, MessageChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("Hey " + cunt.getUser().getName() + "!");
        builder.setThumbnail(self.getUser().getAvatarUrl());
        builder.setDescription("Here's some stuff I can do, now fuck off.");

        ArrayList<DiscordCommand> commands = commandManager.getCommands();
        int[] indexes = pickRandomCommands(5, commands.size());

        builder.addField("**Trigger**", commands.get(indexes[0]).getHelpName(), true);
        builder.addBlankField(true);
        builder.addField("**Description**", commands.get(indexes[0]).getDesc(), true);

        for(int i = 1; i < indexes.length; i++) {
            DiscordCommand c = commands.get(i);
            builder.addField("\u200B", c.getHelpName(), true);
            builder.addBlankField(true);
            builder.addField("\u200B", c.getDesc(), true);
        }
        channel.sendMessage(cunt.getAsMention()).queue();
        channel.sendMessage(builder.build()).queue();
    }

    private void targetCunt(Guild guild, Member cunt) {
        Role targetRole = guild.getRolesByName("target", true).get(0);
        guild.addRoleToMember(cunt, targetRole).complete();
    }
}
