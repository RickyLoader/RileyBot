package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Countdown.Countdown;
import Twitch.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Override to check if the incoming message is/contains a streamer URL, otherwise pass back.
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {

        // Could be/contain a streamer URL, or "ttvlookup [name]"
        final String query = context.getLowerCaseMessage();

        final String streamerUrl = getStreamerUrlFromMessage(query);

        // Query does not contain a URL to a streamer
        if(streamerUrl == null) {
            super.execute(context);
            return;
        }

        // https://www.twitch.tv/dave -> dave
        final String name = streamerUrl
                .replace(TwitchTV.TWITCH_URL, "")
                .replace("/", "")
                .split("\\?")[0]
                .trim();

        ArrayList<Streamer> streamers = TwitchTV.getInstance().searchStreamersByName(name);

        // Too many results (shouldn't happen unless the URL is fake as the name in the URL is unique)
        if(streamers.size() != 1) {
            return;
        }

        MessageChannel channel = context.getMessageChannel();

        MessageAction sendAction = channel.sendMessage(
                buildStreamerEmbed(streamers.get(0), context.getMember(), footer)
        );

        Message initiatingMessage = context.getMessage();

        // Delete the initiating message if it only contains a streamer URL e.g "https://www.twitch.tv/dave"
        if(TwitchTV.isStreamerUrl(query)) {
            initiatingMessage.delete().queue(deleted -> sendAction.queue());
        }

        // Don't delete if it contains other stuff e.g "Check out this stream: https://www.twitch.tv/dave Dave is cool!"
        else {

            /*
             * Check the original message for any Discord Twitch embeds and delete them.
             * Do this after sending the new embed to give Discord time to do it.
             */
            sendAction.queue(message -> {
                List<MessageEmbed> embeds = initiatingMessage.getEmbeds();

                if(!embeds.isEmpty()) {
                    for(MessageEmbed embed : embeds) {
                        MessageEmbed.VideoInfo videoInfo = embed.getVideoInfo();

                        // Twitch embeds contain a live video (only Discord can send video embeds)
                        if(videoInfo != null) {
                            initiatingMessage.suppressEmbeds(true).queue();
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        channel.sendTyping().queue();

        ArrayList<Streamer> streamers = TwitchTV.getInstance().searchStreamersByName(name);

        if(streamers.isEmpty()) {
            channel.sendMessage(
                    member.getAsMention() + " I didn't find any streamers matching: **" + name + "**"
            ).queue();
            return;
        }

        if(streamers.size() == 1) {
            channel.sendMessage(buildStreamerEmbed(streamers.get(0), member, footer)).queue();
            return;
        }

        showSearchResults(name, streamers, context);
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
            public String getNoItemsDescription() {
                return "Nobody here by that name";
            }

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
    public static EmbedBuilder addStreamerToEmbed(EmbedBuilder builder, Streamer streamer, @Nullable Member viewer, String footer) {
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
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.TTV);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.TTV, channel, user);
    }

    /**
     * Attempt to get a URL to a Twitch.tv streamer from the given message.
     * If there are multiple, only the first will be returned.
     *
     * @param message Message to check
     * @return Streamer URL or null.
     */
    @Nullable
    private String getStreamerUrlFromMessage(String message) {

        // The message is a URL
        if(TwitchTV.isStreamerUrl(message)) {
            return message;
        }

        Matcher matcher = Pattern.compile(TwitchTV.STREAMER_URL_REGEX).matcher(message);

        // No streamer URLs in message
        if(!matcher.find()) {
            return null;
        }

        return message.substring(matcher.start(), matcher.end());
    }


    @Override
    public boolean matches(String query, Message message) {
        return super.matches(query, message) || getStreamerUrlFromMessage(query) != null;
    }
}
