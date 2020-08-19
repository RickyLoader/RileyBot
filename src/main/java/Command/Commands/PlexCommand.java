package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PlexCommand extends DiscordCommand {
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super("plex!\nplex! [search query]", "Get a movie recommendation from Plex!");
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

        String query = context.getLowerCaseMessage().trim();
        if(query.equals("plex!")) {
            channel.sendMessage(plex.getMovieEmbed(plex.getRandomMovie())).queue();
            return;
        }
        query = query.replaceFirst("plex!", "").trim();
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        channel.sendMessage(plex.search(query)).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("plex!");
    }
}
