package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;


public class StoryTimeCommand extends DiscordCommand {

    public StoryTimeCommand() {
        super("storytime [prompt]", "Get the AI to finish your story!");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getMessageContent();
        MessageChannel channel = context.getMessageChannel();

        if(message.equals("storytime")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        final String text = message.replace("storytime ", "");

        new Thread(() -> channel.sendMessage(buildRequestEmbed(text)).queue(requestReceived -> {
            String json = new NetworkRequest("https://api.shortlyread.com/stories/write-for-me/", false).post(getBody(text));
            if(json == null || json.equals("err")) {
                requestReceived.editMessage(buildFailedEmbed(text, "Something went wrong!")).queue();
                return;
            }
            JSONObject response = new JSONObject(json);
            if(response.has("message")) {
                requestReceived.editMessage(buildFailedEmbed(text, response.getString("message"))).queue();
                return;
            }
            requestReceived.editMessage(buildCompleteEmbed("**" + text + "**" + response.getString("text"))).queue();
        })).start();
    }

    /**
     * Get the default embed builder
     *
     * @return Default embed builder
     */
    private EmbedBuilder getEmbedBuilder() {
        String icon = "https://i.imgur.com/CCD5ghn.png";
        return new EmbedBuilder()
                .setThumbnail(icon)
                .setTitle("Story Time")
                .setFooter("Try: " + getHelpName(), icon);
    }

    /**
     * Build the embed for requesting the story
     *
     * @param text Story prompt
     * @return Embed showing that the story has been requested
     */
    private MessageEmbed buildRequestEmbed(String text) {
        EmbedBuilder builder = getEmbedBuilder();
        return builder
                .setColor(EmbedHelper.BLUE)
                .setDescription("One moment while I complete your story with "
                        + EmbedHelper.embedURL("Shortly Read AI", "https://www.shortlyread.com/write")
                        + "\n\n**Prompt**: "
                        + text
                )
                .build();
    }

    /**
     * Build the embed showing the completed story
     *
     * @param story Completed story
     * @return Embed showing completed story
     */
    private MessageEmbed buildCompleteEmbed(String story) {
        EmbedBuilder builder = getEmbedBuilder();
        return builder
                .setColor(EmbedHelper.GREEN)
                .setDescription(story)
                .build();
    }

    /**
     * Build the embed showing that something went wrong
     *
     * @param text  Story prompt
     * @param error Error which occurred
     * @return Embed showing error during story request
     */
    private MessageEmbed buildFailedEmbed(String text, String error) {
        EmbedBuilder builder = getEmbedBuilder();
        return builder
                .setColor(EmbedHelper.RED)
                .setDescription("**Error**: " + error + "\n\n**For prompt**: " + text)
                .build();
    }

    /**
     * Get the body required to make the request
     *
     * @param text Story text
     * @return Request body
     */
    private String getBody(String text) {
        return new JSONObject()
                .put("prompt", "Write a story about someone using an AI creative writing assistant for the first time.")
                .put("content", text)
                .toString();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("storytime");
    }
}
