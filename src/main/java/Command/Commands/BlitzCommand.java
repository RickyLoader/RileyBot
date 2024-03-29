package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Riot.LOL.Blitz.Blitz;
import Riot.LOL.Blitz.BlitzImageBuilder;
import Riot.LOL.Blitz.BuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Get Champion build information from Blitz.gg
 */
public class BlitzCommand extends DiscordCommand {
    private final Blitz blitz;
    private final BlitzImageBuilder blitzImageBuilder;
    private final List<String> roles = Arrays.asList("TOP", "MID", "ADC", "SUPP", "SUP", "SUPPORT", "JG", "JUNGLE");

    public BlitzCommand() {
        super("blitz", "Get the blitz.gg build info for a champion!", "blitz [champion] [role]");
        this.blitz = new Blitz();
        this.blitzImageBuilder = new BlitzImageBuilder();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage();

        if(message.equals(getTrigger())) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        message = message.replaceFirst(getTrigger(), "").trim();
        String[] args = message.split(" ");
        String givenRole = args[args.length - 1].toUpperCase();

        if(!roles.contains(givenRole)) {
            channel.sendMessage("```Role must be one of: " + StringUtils.join(roles, ", ") + "```" + getHelpNameCoded()).queue();
            return;
        }

        String role = deriveRole(givenRole);
        String champion = message.replace(givenRole.toLowerCase(), "").trim();

        if(champion.isEmpty()) {
            channel.sendMessage("```You didn't include a champion!```" + getHelpNameCoded()).queue();
            return;
        }

        new Thread(() -> {
            BuildData championBuildData = blitz.getBuildData(champion, role);

            if(championBuildData == null) {
                channel.sendMessage(getErrorMessage(champion, role)).queue();
                return;
            }

            byte[] image = blitzImageBuilder.buildImage(championBuildData);

            if(image == null) {
                channel.sendMessage("No.").queue();
                return;
            }

            channel.sendMessage(getEmbedBuilder()
                    .setImage("attachment://image.png")
                    .setColor(EmbedHelper.ORANGE)
                    .setDescription(championBuildData.getDescription())
                    .build()).addFile(image, "image.png").queue();
        }).start();
    }

    /**
     * Derive the intended role
     *
     * @param role Provided role
     * @return Corrected role
     */
    private String deriveRole(String role) {
        String result = role;
        switch(role) {
            case "JG":
                result = "JUNGLE";
                break;
            case "SUP":
            case "SUPP":
                result = "SUPPORT";
                break;
        }
        return result;
    }

    /**
     * Get the default embed builder
     *
     * @return Default embed builder
     */
    private EmbedBuilder getEmbedBuilder() {
        return new EmbedBuilder()
                .setTitle("Blitz.gg Champion Build")
                .setFooter("Try: " + getHelpName(), "https://i.imgur.com/CdEyT5v.png")
                .setThumbnail("https://i.imgur.com/eja43Q3.png");
    }

    /**
     * Get a failed lookup message for the given champion
     *
     * @param query Champion name query
     * @param role  Champion role
     * @return Error message
     */
    private MessageEmbed getErrorMessage(String query, String role) {
        return getEmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setDescription("No data found for **" + query.toUpperCase() + " " + role + "**")
                .setImage(EmbedHelper.SPACER_IMAGE)
                .build();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
