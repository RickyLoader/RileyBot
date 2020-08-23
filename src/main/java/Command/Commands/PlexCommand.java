package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PlexCommand extends DiscordCommand {
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super("plex! | plex! [search query]", "Get a movie recommendation from Plex!");
        plex = new PlexServer(getHelpName());
    }

    @Override
    public void execute(CommandContext context) {
        long timePassed = System.currentTimeMillis() - plex.getTimeFetched();
        MessageChannel channel = context.getMessageChannel();

        if(!context.getLowerCaseMessage().startsWith("plex!")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(refreshing) {
            channel.sendMessage("I told you I was refreshing cunt").queue();
            return;
        }

        new Thread(() -> {
            // Been more than 24 hours
            if(timePassed / 3600000 > 24 || plex.libraryEmpty()) {
                refreshing = true;
                channel.sendMessage("Let me refresh the Plex library first").queue();
                plex.refreshData();
                refreshing = false;
                if(plex.libraryEmpty()) {
                    channel.sendMessage("Plex is unavailable right now").queue();
                    return;
                }
            }
            String query = context.getLowerCaseMessage();
            if(query.equals("plex!")) {
                channel.sendMessage(plex.getMovieEmbed(plex.getRandomMovie())).queue();
                return;
            }
            query = query.replaceFirst("plex!", "").trim();
            channel.sendMessage(plex.search(query)).queue();
        }).start();
    }

    @Override
    public boolean matches(String query) {
        return query.contains("plex");
    }
}
