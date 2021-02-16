package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Create a message embed when a Phasmophobia invite code is posted
 */
public class PhasmophobiaCommand extends DiscordCommand {
    private final HashMap<MessageChannel, Long> inviteMessages = new HashMap<>();

    public PhasmophobiaCommand() {
        super("[Phasmophobia Invite Code]", "Make an embed displaying a Phasmophobia invite code.");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String code = context.getLowerCaseMessage();
        if(inviteMessages.containsKey(channel)) {
            channel.deleteMessageById(inviteMessages.get(channel)).queue();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        MessageEmbed inviteMessage = new EmbedBuilder()
                .setColor(EmbedHelper.PURPLE)
                .setThumbnail("https://upload.wikimedia.org/wikipedia/en/f/f2/Phasmophobia_VG.jpg")
                .setFooter("Posted at: " + dateFormat.format(new Date()))
                .setTitle("Phasmophobia Invite Code (Maybe?)")
                .setDescription("**Code**: " + code + "\n**Posted by**: " + context.getMember().getAsMention())
                .build();
        channel.sendMessage(inviteMessage).queue(message -> inviteMessages.put(channel, message.getIdLong()));
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.matches("\\d\\d\\d\\d\\d\\d");
    }
}
