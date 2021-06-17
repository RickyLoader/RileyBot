package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Countdown.Countdown;
import Twitch.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Look up a Twitch.tv streamer!
 */
public class TTVLookupCommand extends LookupCommand {
    private final String footer;

    public TTVLookupCommand() {
        super("ttvlookup", "Look up a Twitch.tv streamer!", 50);
        this.footer = "Type: " + getTrigger() + " for help";
        setBotInput(true);
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        User user = member.getUser();
        if(user.isBot() && member != context.getGuild().getSelfMember()) {
            return;
        }
        if(name.startsWith(TwitchTV.TWITCH_URL)) {
            showTwitchDetailsEmbed(name, context.getMessage(), channel);
            return;
        }
        channel.sendTyping().queue();
        ArrayList<Streamer> streamers = TwitchTV.getInstance().searchStreamersByName(name);
        if(streamers.isEmpty()) {
            channel.sendMessage(
                    member.getAsMention() + " I didn't find any streamers matching: **" + name + "**"
            ).queue();
            return;
        }
        if(streamers.size() == 1) {
            channel.sendMessage(buildStreamerEmbed(streamers.get(0), null, footer)).queue();
            return;
        }
        showSearchResults(name, streamers, context);
    }

    /**
     * Create and send a message embed when a Twitch.tv URL is posted.
     * Discord creates a basic embed showing the streamer name and URL, delete it and display an embed with
     * followers, current stream info, etc.
     *
     * @param streamerUrl URL to streamer Twitch.tv page
     * @param message     Message containing twitch URL
     * @param channel     Channel to send better message embed to
     */
    private void showTwitchDetailsEmbed(String streamerUrl, Message message, MessageChannel channel) {
        String name = streamerUrl
                .replace(TwitchTV.TWITCH_URL, "")
                .replace("/", "")
                .trim();
        ArrayList<Streamer> streamers = TwitchTV.getInstance().searchStreamersByName(name);
        if(streamers.size() != 1) {
            return;
        }
        message.delete().queue(
                deleted -> channel.sendMessage(
                        buildStreamerEmbed(streamers.get(0), message.getMember(), footer)
                ).queue()
        );
    }

    /**
     * Show a pageable message embed displaying the streamer search results for the given query
     *
     * @param query     Search query used to find the results
     * @param streamers List of streamers found via query
     * @param context   Command context
     */
    private void showSearchResults(String query, ArrayList<Streamer> streamers, CommandContext context) {
        new PageableTableEmbed<Streamer>(
                context,
                streamers,
                TwitchTV.TWITCH_LOGO,
                "TTVLookup: " + streamers.size() + " results found",
                "**Query**: " + query,
                footer,
                new String[]{"Name", "Category", "URL"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, Streamer streamer, boolean defaultSort) {
                return new String[]{
                        streamer.getLoginName(),
                        streamer.isStreaming()
                                ? streamer.getStream().getGame().getName()
                                : "OFFLINE",
                        EmbedHelper.embedURL("Visit", streamer.getUrl())
                };
            }

            @Override
            public void sortItems(List<Streamer> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance<Streamer>(query, defaultSort) {
                    @Override
                    public String getString(Streamer o) {
                        return o.getLoginName();
                    }
                });
            }
        }.showMessage();
    }

    /**
     * Build a message embed detailing the given Twitch streamer
     *
     * @param streamer Twitch streamer to create message embed for
     * @param viewer   Member who posted Twitch.tv link (display in description)
     * @param footer   Footer to use in the embed
     * @return Message embed detailing the given Twitch streamer
     */
    public static MessageEmbed buildStreamerEmbed(Streamer streamer, Member viewer, String footer) {
        return addStreamerToEmbed(new EmbedBuilder(), streamer, viewer, footer).build();
    }

    /**
     * Add the details of a Twitch streamer to the given embed builder
     *
     * @param builder  Embed builder to use
     * @param streamer Twitch streamer to create message embed for
     * @param viewer   Member who posted Twitch.tv link (display in description)
     * @param footer   Footer to use in the embed
     * @return Message embed detailing the given Twitch streamer
     */
    public static EmbedBuilder addStreamerToEmbed(EmbedBuilder builder, Streamer streamer, Member viewer, String footer) {
        boolean live = streamer.isStreaming();
        String description = "";

        if(streamer.hasFollowers()) {
            description += "**Followers**: " + streamer.formatFollowers();
        }

        if(viewer != null) {
            description += "\n**Biggest fan**: " + viewer.getAsMention();
        }

        String title = streamer.getDisplayName() + " | " + (live ? "LIVE" : "OFFLINE");

        if(streamer.hasLanguage() && !streamer.getLanguage().equalsIgnoreCase("english")) {
            title += " (" + streamer.getLanguage() + ")";
        }
        builder
                .setThumbnail(streamer.getThumbnail())
                .setTitle(
                        title,
                        streamer.getUrl()
                )
                .setColor(live ? EmbedHelper.GREEN : EmbedHelper.RED);

        String twitchName = "Twitch.tv";
        if(live) {
            Stream stream = streamer.getStream();
            Game game = stream.getGame();
            String started = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(stream.getStarted());
            footer += " | Online since: " + started;
            Countdown streamDuration = Countdown.from(stream.getStarted().getTime(), System.currentTimeMillis());
            description = "__**Current Stream Details**__\n\n"
                    + "**Title**: " + stream.getTitle()
                    + "\n\n**Streaming**: " + game.getName()
                    + "\n**Online for**: "
                    + streamDuration.formatHoursMinutesSeconds()
                    + "\n**Viewers**: " + stream.formatViewers()
                    + "\n\n" + description;

            builder.setImage(stream.getThumbnail())
                    .setAuthor(
                            twitchName + " - " + game.getName(),
                            game.getUrl(),
                            game.getThumbnail());
        }
        else {
            builder.setAuthor(
                    twitchName,
                    TwitchTV.TWITCH_URL,
                    TwitchTV.TWITCH_LOGO
            );
        }

        return builder
                .setFooter(footer)
                .setDescription(description);
    }


    @Override
    public String stripArguments(String query) {
        if(query.startsWith(TwitchTV.TWITCH_URL)) {
            query = getTrigger() + " " + query;
        }
        return query;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.TTV);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.TTV, channel, user);
    }

    @Override
    public boolean matches(String query, Message message) {
        String regex = TwitchTV.TWITCH_URL + "(\\w)+/?";
        return super.matches(query, message) || query.matches(regex);
    }
}
