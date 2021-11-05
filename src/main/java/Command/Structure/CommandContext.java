package Command.Structure;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandContext {
    private final GuildMessageReceivedEvent event;
    private final ArrayList<DiscordCommand> commands;
    private final EmoteHelper emoteHelper;

    public CommandContext(GuildMessageReceivedEvent event, ArrayList<DiscordCommand> commands, EmoteHelper emoteHelper) {
        this.event = event;
        this.commands = commands;
        this.emoteHelper = emoteHelper;
    }

    public EmoteHelper getEmoteHelper() {
        return emoteHelper;
    }

    /**
     * Filter webhooks by name and return the URL
     *
     * @param webhooks List of webhooks
     * @param name     Name to filter by
     * @return Webhook URL
     */
    public String filterWebhooks(List<Webhook> webhooks, String name) {
        ArrayList<String> urls = webhooks
                .stream()
                .filter(webhook -> webhook.getName().equalsIgnoreCase(name))
                .map(webhook -> webhook.getUrl().replaceFirst("discord", "discordapp"))
                .collect(Collectors.toCollection(ArrayList::new));
        return urls.isEmpty() ? null : urls.get(0);
    }

    public User getSelfUser() {
        return getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    public ArrayList<DiscordCommand> getCommands() {
        return commands;
    }

    public List<Member> getMembers() {
        return new ArrayList<>(getGuild().getMembers());
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public Member getMember() {
        return event.getMember();
    }

    public User getUser() {
        return this.getMember().getUser();
    }

    public MessageChannel getMessageChannel() {
        return event.getChannel();
    }

    public TextChannel getTextChannel() {
        return event.getMessage().getTextChannel();
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public String getMessageContent() {
        return getMessage().getContentDisplay();
    }

    public String getInvite() {
        return getTextChannel()
                .createInvite()
                .setTemporary(true)
                .complete()
                .getUrl();
    }

    public String getLowerCaseMessage() {
        return getMessageContent().toLowerCase();
    }

    public Role getTargetRole() {
        return getGuild().getRolesByName("target", true).get(0);
    }

    /**
     * Get all members with the target role
     *
     * @return Members with target role
     */
    public ArrayList<Member> getTargets() {
        Role targetRole = getTargetRole();
        ArrayList<Member> targets = new ArrayList<>();
        for(Member target : getGuild().getMembersWithRoles(targetRole)) {
            if(!getSelfMember().canInteract(target)) {
                getGuild().removeRoleFromMember(target, targetRole).queue();
                continue;
            }
            targets.add(target);
        }
        return targets;
    }
}
