package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import TheHub.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;


/**
 * Check out some rankings on the hub
 */
public class HubCommand extends DiscordCommand {
    private final TheHub theHub;

    public HubCommand() {
        super("hub [#]\nhub [name]", "Check out your favourite hub homies!");
        theHub = new TheHub();
    }

    @Override
    public void execute(CommandContext context) {
        Member member = context.getMember();
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage();
        String arg = message.replaceFirst("hub", "").replaceAll("\\s+", " ").trim();

        if(message.split(" ").length == 1 || arg.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
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

        new Thread(() -> {
            Performer performer = rank == 0 ? theHub.getPerformerByName(arg) : theHub.getPerformerByRank(rank);
            if(performer == null) {
                String info = rank > 0 ? "I couldn't find a **rank " + rank + "** cunt"
                        :
                        "I couldn't find anything for **" + arg + "**!";
                channel.sendMessage(
                        member.getAsMention() + " " + info
                ).queue();
                return;
            }
            channel.sendMessage(buildEmbed(performer)).queue();
        }).start();

    }

    /**
     * Build a message embed from the provided Performer
     *
     * @param performer Performer to build embed for
     * @return Message embed detailing Performer
     */
    private MessageEmbed buildEmbed(Performer performer) {
        String thumbnail = "https://i.imgur.com/ngRnecW.png";
        String rank = "Rank " + (performer.hasRank() ? "#" + performer.getRank() : "N/A");
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(
                        StringUtils.capitalize(
                                performer.getType().name().toLowerCase()
                        ) + " " + rank + " - " + performer.getName()
                )
                .setThumbnail(thumbnail)
                .setFooter("Type " + getHelpName().replace("\n", " | "), thumbnail)
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
    public boolean matches(String query) {
        return query.startsWith("hub");
    }
}
