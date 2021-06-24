package Command.Commands;

import Command.Structure.*;
import TheHub.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.*;

import static TheHub.HubVideo.*;


/**
 * Check out some rankings on the hub
 */
public class HubCommand extends DiscordCommand {
    private final TheHub theHub;
    private final Random random;
    private String thumbsUp, thumbsDown, blankGap;

    public HubCommand() {
        super("hub [#]\nhub [name]\nhub random", "Check out your favourite hub homies!");
        this.theHub = new TheHub();
        this.random = new Random();
    }

    @Override
    public void execute(CommandContext context) {
        if(thumbsUp == null) {
            EmoteHelper emoteHelper = context.getEmoteHelper();
            this.thumbsUp = emoteHelper.getThumbsUp().getAsMention();
            this.thumbsDown = emoteHelper.getThumbsDown().getAsMention();
            this.blankGap = emoteHelper.getBlankGap().getAsMention();
        }
        new Thread(() -> {
            Member member = context.getMember();
            MessageChannel channel = context.getMessageChannel();
            Message message = context.getMessage();
            String content = context.getMessageContent();

            if(TheHub.isVideoUrl(content)) {
                HubVideo video = theHub.getVideo(content);
                if(video != null) {
                    message.delete().queue(deleted -> displayVideoEmbed(channel, video));
                }
                return;
            }

            if(TheHub.isProfileUrl(content)) {
                Performer performer = theHub.getPerformerByUrl(content);
                if(performer != null) {
                    message.delete().queue(deleted -> channel.sendMessage(buildEmbed(performer)).queue());
                }
                return;
            }

            String arg = content
                    .replaceFirst("hub", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            if(arg.isEmpty()) {
                channel.sendMessage(getHelpNameCoded()).queue();
                return;
            }

            channel.sendTyping().queue();
            if(arg.equals("random")) {
                Performer performer = null;
                while(performer == null) {
                    performer = theHub.getPerformerByRank(random.nextInt(15000) + 1);
                }
                channel.sendMessage(buildEmbed(performer)).queue();
                return;
            }
            int rank = toInteger(arg);
            if(rank < 0) {
                channel.sendMessage(
                        member.getAsMention()
                                + " How the fuck could someone be ranked in the negatives? What kind of ogres do you watch?"
                ).queue();
                return;
            }

            if(rank == 0) {
                ArrayList<Performer> searchResults = theHub.getPerformersByName(arg);
                if(searchResults.size() == 1) {
                    channel.sendMessage(buildEmbed(searchResults.get(0))).queue();
                    return;
                }
                showSearchResults(searchResults, arg, context);
            }
            else {
                Performer performer = theHub.getPerformerByRank(rank);
                if(performer == null) {
                    channel.sendMessage(
                            member.getAsMention() + " I couldn't find a **rank " + rank + "**!"
                    ).queue();
                    return;
                }
                channel.sendMessage(buildEmbed(performer)).queue();
            }
        }).start();

    }

    /**
     * Display a message embed detailing the given hub video
     *
     * @param channel Channel to send embed to
     * @param video   Video to display details on
     */
    private void displayVideoEmbed(MessageChannel channel, HubVideo video) {
        VideoInfo videoInfo = video.getVideoInfo();
        Channel uploadChannel = video.getChannel();

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(
                        uploadChannel.getName(),
                        uploadChannel.getUrl(),
                        uploadChannel.getImageUrl()
                )
                .setFooter("Uploaded " + video.getDate())
                .setColor(EmbedHelper.FIRE_ORANGE)
                .setTitle(videoInfo.getTitle(), videoInfo.getUrl())
                .setImage(videoInfo.getThumbnailUrl())
                .setThumbnail(TheHub.LOGO);

        DecimalFormat commaFormat = new DecimalFormat("#,###");

        String desc = "**Views**: " + commaFormat.format(video.getViews());

        if(video.hasCast()) {
            desc += "\n**Cast**: " + StringUtils.join(video.getCast(3), ", ");
        }

        if(video.hasCategories()) {
            desc += "\n**Categories**: " + StringUtils.join(video.getCategories(3), ", ");
        }

        desc += "\n\n"
                + thumbsUp + " " + commaFormat.format(video.getLikes()) + " (" + video.getLikeRatio() + ")"
                + blankGap
                + thumbsDown + " " + commaFormat.format(video.getDislikes());

        channel.sendMessage(builder.setDescription(desc).build()).queue();
    }

    /**
     * Show the performers found for the given search query in a pageable message embed
     *
     * @param searchResults List of performers found for the given query
     * @param searchQuery   Query used to find results
     * @param context       Command context
     */
    private void showSearchResults(ArrayList<Performer> searchResults, String searchQuery, CommandContext context) {
        new PageableTableEmbed<Performer>(
                context,
                searchResults,
                TheHub.LOGO,
                "Hub Search",
                searchResults.size() + " Results found for **" + searchQuery + "**:",
                "Try: " + getHelpName().replace("\n", " | "),
                new String[]{"Name", "Profile Type"},
                5,
                EmbedHelper.ORANGE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "There's no one around these parts named **" + searchQuery + "**!";
            }

            @Override
            public String[] getRowValues(int index, Performer performer, boolean defaultSort) {
                return new String[]{
                        performer.getName(),
                        EmbedHelper.embedURL(performer.getType().name(), performer.getURL())
                };
            }

            @Override
            public void sortItems(List<Performer> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance<Performer>(searchQuery, defaultSort) {
                    @Override
                    public String getString(Performer o) {
                        return o.getName();
                    }
                });
            }
        }.showMessage();
    }

    /**
     * Build a message embed from the provided Performer
     *
     * @param performer Performer to build embed for
     * @return Message embed detailing Performer
     */
    private MessageEmbed buildEmbed(Performer performer) {
        String rank = "Rank " + (performer.hasRank() ? "#" + performer.getRank() : "N/A");
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(
                        StringUtils.capitalize(
                                performer.getType().name().toLowerCase()
                        ) + " " + rank + " - " + performer.getName()
                )
                .setThumbnail(TheHub.LOGO)
                .setFooter("Try: " + getHelpName().replace("\n", " | "), TheHub.LOGO)
                .setImage(performer.getImage())
                .setDescription(performer.getDesc())
                .addField("Views", performer.getViews(), true)
                .addField("Subscribers", performer.getSubscribers(), true);

        if(performer.hasGender()) {
            String gender = performer.getGender();
            builder.setColor(gender.equals("Female") ? EmbedHelper.PURPLE : EmbedHelper.ORANGE);
            builder.addField("Gender", gender, true);
        }
        else {
            builder.setColor(EmbedHelper.YELLOW);
        }

        if(performer.hasAge()) {
            builder.addField("Age", String.valueOf(performer.getAge()), true);
        }

        return builder.addField(
                "URL",
                EmbedHelper.embedURL("View on the Hub", performer.getURL()),
                true)
                .build();
    }

    @Override
    public boolean matches(String query, Message message) {
        return message.getContentRaw().startsWith("hub") || TheHub.isVideoUrl(query) || TheHub.isProfileUrl(query);
    }
}
