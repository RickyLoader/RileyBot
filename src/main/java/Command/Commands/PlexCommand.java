package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PlexCommand extends DiscordCommand {
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super("Movie!", "Get a movie recommendation!");
        plex = new PlexServer();
    }

    @Override
    public void execute(CommandContext context) {
        long timePassed = System.currentTimeMillis() - plex.getTimeFetched();
        MessageChannel channel = context.getMessageChannel();

        if(refreshing) {
            channel.sendMessage("I told you I was refreshing cunt").queue();
            return;
        }

        // Been more than 24 hours
        if(timePassed / 3600000 > 24) {
            refreshing = true;
            channel.sendMessage("It's been more than a day since I last refreshed the Plex library, let me do that first and i'll get back to you in a second.").queue();
            plex.refreshData();
            refreshing = false;
        }
        context.getMessageChannel().sendMessage(plex.getMovieEmbed()).queue();
    }
}
