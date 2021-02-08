package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Plex.PlexServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PlexCommand extends DiscordCommand {
    private final PlexServer plex;
    private boolean refreshing = false;

    public PlexCommand() {
        super("plex! random\nplex! [movie query/movie id]\nplex! add: [movie query/movie id]", "Search & add movies to Plex!");
        this.plex = new PlexServer("Type: plex! for help");
    }

    @Override
    public void execute(CommandContext context) {
        long timePassed = System.currentTimeMillis() - plex.getTimeFetched();
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage();
        Member member = context.getMember();

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
        channel.sendTyping().queue(typing -> new Thread(() -> {
            // Been more than an hour
            if(timePassed / 3600000 > 1 || plex.libraryEmpty()) {
                refreshing = true;
                channel.sendMessage("Let me refresh the Plex library first").queue();
                plex.refreshData();
                refreshing = false;
                if(plex.libraryEmpty()) {
                    channel.sendMessage("Request to Plex timed out, try again").queue();
                    return;
                }
            }
            if(message.equals("plex!")) {
                channel.sendMessage(getHelpNameCoded()).queue();
                return;
            }
            if(message.equals("plex! random")) {
                channel.sendMessage(plex.getMovieEmbed(plex.getRandomMovie(), false)).queue();
                return;
            }
            String query = message.replaceFirst("plex! ", "").trim();
            if(query.startsWith("add:")) {
                query = query.replace("add:", "").trim();
                if(query.isEmpty()) {
                    channel.sendMessage(member.getAsMention() + " How am I going to add that?").queue();
                    return;
                }
                if(!context.getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                    channel.sendMessage("I need the manage webhooks permission to do that").queue();
                    return;
                }

                String finalQuery = query;
                context.getGuild().retrieveWebhooks().queue(webhooks -> {
                    String webhook = context.filterWebhooks(webhooks, "plex");
                    if(webhook == null) {
                        channel.sendMessage("I need a webhook named: ```plex``` to do that!").queue();
                        return;
                    }
                    plex.searchRadarr(finalQuery, webhook, context);
                });
                return;
            }
            plex.searchLibrary(query, context);
        }).start());
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("plex");
    }
}
