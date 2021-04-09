package Command.Commands;

import Command.Commands.Lookup.TTVLookupCommand;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Twitch.Streamer;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Random;

import static Bot.GlobalReference.*;

/**
 * Cool hot tub streamers
 */
public class HotTubCommand extends DiscordCommand {
    private final Random random = new Random();

    public HotTubCommand() {
        super("hot tub", "Take a look at some hot tub streamers!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        ArrayList<Streamer> hotTubStreamers = TWITCH_TV.searchStreamersByStreamTitle(
                "hot tub",
                "Just Chatting"
        );
        if(hotTubStreamers.size() == 0) {
            channel.sendMessage("I didn't see anyone in a hot tub right now!").queue();
            return;
        }
        Streamer streamer = hotTubStreamers.get(random.nextInt(hotTubStreamers.size()));
        streamer.updateFollowers(TWITCH_TV.fetchFollowers(streamer.getId()));
        MessageEmbed streamerEmbed = TTVLookupCommand.buildStreamerEmbed(
                streamer,
                context.getMember(),
                "Try: " + getTrigger()
        );
        channel.sendMessage(hotTubStreamers.size() + " *possible* hot tubbers found, here's one:").
                embed(streamerEmbed)
                .queue();
    }
}
