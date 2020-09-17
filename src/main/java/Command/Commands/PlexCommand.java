package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Webhook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlexCommand extends DiscordCommand {
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super("plex!\nplex! [search query/movie id]", "Get a movie recommendation from Plex!");
        plex = new PlexServer(getHelpName().replace("\n", " | "));
    }

    @Override
    public void execute(CommandContext context) {
        long timePassed = System.currentTimeMillis() - plex.getTimeFetched();
        MessageChannel channel = context.getMessageChannel();
        final String message = context.getLowerCaseMessage();

        if(!message.equals("plex!") && !message.startsWith("plex! ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(refreshing) {
            channel.sendMessage("I told you I was refreshing cunt").queue();
            return;
        }

        if(!plex.hasEmotes()) {
            plex.setEmoteHelper(context.getEmoteHelper());
        }

        new Thread(() -> {
            // Been more than an hour
            if(timePassed / 3600000 > 1 || plex.libraryEmpty()) {
                refreshing = true;
                channel.sendMessage("Let me refresh the Plex library first").queue();
                plex.refreshData();
                refreshing = false;
                if(plex.libraryEmpty()) {
                    channel.sendMessage("Plex is unavailable right now").queue();
                    return;
                }
            }
            if(message.equals("plex!")) {
                channel.sendMessage(plex.getMovieEmbed(plex.getRandomMovie(), false)).queue();
                return;
            }
            String query = message.replaceFirst("plex! ", "").trim();
            context.getGuild().retrieveWebhooks().queue(webhooks -> {
                String webhook = context.filterWebhooks(webhooks, "plex");
                if(webhook == null) {
                    channel.sendMessage("I need a webhook named: ```plex``` to do that!").queue();
                    return;
                }
                channel.sendMessage(plex.searchLibrary(query, context.getMember(), webhook)).queue();
            });
        }).start();
    }

    @Override
    public boolean matches(String query) {
        return query.contains("plex");
    }
}
