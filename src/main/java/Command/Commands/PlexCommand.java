package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.EmoteHelper;
import Command.Structure.OnReadyDiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PlexCommand extends OnReadyDiscordCommand {
    private static final String
            TRIGGER = "plex!",
            ADD = "add:",
            WEBHOOK_NAME = "plex",
            RANDOM = "random",
            REFRESH = "refresh";
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super(
                TRIGGER,
                "Search & add movies to Plex!",
                TRIGGER + " " + RANDOM
                        + "\n" + TRIGGER + " [movie query/movie id]"
                        + "\n" + TRIGGER + " " + ADD + " [movie query/movie id]"
                        + "\n" + TRIGGER + " " + REFRESH
        );
        this.plex = new PlexServer("Type: " + getTrigger() + " for help");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();
        Member member = context.getMember();

        // No query provided, send help message
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // Currently refreshing library, ignore input
        if(refreshing) {
            channel.sendMessage("I told you I was refreshing cunt").queue();
            return;
        }

        channel.sendTyping().queue();

        new Thread(() -> {

            long timePassed = System.currentTimeMillis() - plex.getLastRefreshed();

            // User asked to refresh
            boolean forceRefresh = query.equals(REFRESH);

            // Refresh the library
            if(forceRefresh || timePassed / 3600000 > 1 || plex.libraryEmpty()) {
                refreshing = true;
                channel.sendMessage("Refreshing Plex library...").queue();

                // Resend typing for status of refresh
                channel.sendTyping().queue();

                plex.refreshData();
                refreshing = false;

                // Something went wrong when refreshing
                if(plex.libraryEmpty()) {
                    channel.sendMessage("Request to Plex timed out, try again").queue();
                    return;
                }

                // User asked to refresh so notify of success
                if(forceRefresh) {
                    channel.sendMessage("Refresh complete!").queue();
                    return;
                }
            }

            // Show a random movie
            if(query.equals(RANDOM)) {
                channel.sendMessage(plex.getMovieEmbed(plex.getRandomMovie(), false)).queue();
                return;
            }

            // Attempt to add a movie
            if(query.startsWith(ADD)) {
                String movieName = query.replaceFirst(ADD, "").trim();

                // No name provided
                if(movieName.isEmpty()) {
                    channel.sendMessage(member.getAsMention() + " How am I going to add that?").queue();
                    return;
                }

                // Missing permission to respond to the movie when adding is successful
                if(!context.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                    channel.sendMessage("I need the manage webhooks permission to do that").queue();
                    return;
                }

                // Add the movie to Radarr
                context.getGuild().retrieveWebhooks().queue(webhooks -> {
                    String webhook = context.filterWebhooks(webhooks, WEBHOOK_NAME);
                    if(webhook == null) {
                        channel.sendMessage(
                                "I need a webhook named: ```" + WEBHOOK_NAME + "``` to do that!"
                        ).queue();
                        return;
                    }
                    plex.searchRadarr(movieName, context);
                });
            }

            // Search for movies matching the given query
            plex.searchLibrary(query, context);
        }).start();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        plex.setEmoteHelper(emoteHelper);
    }
}
