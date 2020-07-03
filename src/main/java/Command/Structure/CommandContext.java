package Command.Structure;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CommandContext {
    private final GuildMessageReceivedEvent event;

    public CommandContext(GuildMessageReceivedEvent event) {
        this.event = event;
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
        return getTextChannel().createInvite().complete().getUrl();
    }

    /**
     * Get all members with the target role
     *
     * @return Members with target role
     */
    public List<Member> getTargets() {
        return getGuild().getMembersWithRoles(getGuild().getRolesByName("target", true));
    }
}
