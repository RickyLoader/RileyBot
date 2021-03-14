package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableTableEmbed;
import TheHub.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


/**
 * Check out some rankings on the hub
 */
public class HubCommand extends DiscordCommand {
    private final String thumbnail = "https://i.imgur.com/ngRnecW.png";
    private final TheHub theHub;
    private final Random random;

    public HubCommand() {
        super("hub [#]\nhub [name]\nhub random", "Check out your favourite hub homies!");
        this.theHub = new TheHub();
        this.random = new Random();
    }

    @Override
    public void execute(CommandContext context) {
        Member member = context.getMember();
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage();
        String arg = message
                .replaceFirst("hub", "")
                .replaceAll("\\s+", " ")
                .trim();

        if(arg.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        new Thread(() -> {
            channel.sendTyping().queue();
            if(arg.equals("random")) {
                Performer performer = null;
                while(performer == null) {
                    performer = theHub.getPerformerByRank(random.nextInt(15000) + 1);
                }
                channel.sendMessage(buildEmbed(performer)).queue();
                return;
            }
            int rank = getQuantity(arg);
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
     * Show the performers found for the given search query in a pageable message embed
     *
     * @param searchResults List of performers found for the given query
     * @param searchQuery   Query used to find results
     * @param context       Command context
     */
    private void showSearchResults(ArrayList<Performer> searchResults, String searchQuery, CommandContext context) {
        int results = searchResults.size();
        new PageableTableEmbed(
                context,
                searchResults,
                thumbnail,
                "Hub Search",
                searchResults.size() + " Results found for **" + searchQuery + "**:",
                "Try: " + getHelpName().replace("\n", " | "),
                new String[]{"Name", "Profile Type"},
                5,
                results == 0 ? EmbedHelper.RED : EmbedHelper.ORANGE
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Performer performer = (Performer) items.get(index);
                return new String[]{performer.getName(), performer.getType().name()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String name1 = ((Performer) o1).getName();
                    String name2 = ((Performer) o2).getName();
                    if(defaultSort) {
                        return levenshteinDistance(name1, searchQuery) - levenshteinDistance(name2, searchQuery);
                    }
                    return levenshteinDistance(name2, searchQuery) - levenshteinDistance(name1, searchQuery);
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
                .setThumbnail(thumbnail)
                .setFooter("Try: " + getHelpName().replace("\n", " | "), thumbnail)
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
        return query.startsWith("hub");
    }
}
