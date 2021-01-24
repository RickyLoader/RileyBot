package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import XKCD.Comic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * View XKCD comics
 */
public class XKCDCommand extends DiscordCommand {
    private final String BASE_URL = "https://xkcd.com/";
    private final Random random = new Random();
    private int LATEST_COMIC_ISSUE;

    public XKCDCommand() {
        super("xkcd\nxkcd [issue #/latest]", "Check out some XKCD comics!");
        this.LATEST_COMIC_ISSUE = getLatestComic().getIssue();
    }

    /**
     * Fetch the latest XKCD comic
     *
     * @return Latest XKCD comic
     */
    private Comic getLatestComic() {
        return parseComicJSON(
                new NetworkRequest(BASE_URL + "info.0.json", false).get().body
        );
    }

    /**
     * Get an XKCD comic by the issue number
     *
     * @param issue Comic issue to get
     * @return Comic
     */
    private Comic getComic(int issue) {
        NetworkResponse response = new NetworkRequest(BASE_URL + issue + "/info.0.json", false).get();
        if(response.code == 404) {
            return null;
        }
        return parseComicJSON(response.body);
    }

    /**
     * Parse a JSON String in to a Comic object
     *
     * @param json JSON to parse
     * @return Comic object from JSON
     */
    private Comic parseComicJSON(String json) {
        JSONObject comic = new JSONObject(json);
        return new Comic(
                comic.getInt("num"),
                comic.getString("img"),
                comic.getString("title"),
                comic.getString("alt"),
                parseDate(
                        comic.getString("day"),
                        comic.getString("month"),
                        comic.getString("year")
                )
        );
    }

    /**
     * Parse a date String into a Date object of dd/MM/yyyy
     *
     * @param day   Day of month - 01
     * @param month Month of year - 08
     * @param year  Year - 2020
     * @return Date object
     */
    private Date parseDate(String day, String month, String year) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(day + "/" + month + "/" + year);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String message = context
                .getLowerCaseMessage()
                .replace("xkcd", "")
                .trim();

        Comic comic;
        if(message.equals("latest")) {
            comic = getLatestComic();
            LATEST_COMIC_ISSUE = comic.getIssue();
        }
        else {
            comic = getComic(message.isEmpty() ? getRandomIssueNumber() : getQuantity(message));
        }

        if(comic == null) {
            channel.sendMessage(
                    context.getMember().getAsMention() + " Couldn't find that one bro, settle down"
            ).queue();
            return;
        }
        channel.sendMessage(buildComicEmbed(comic)).queue();
    }

    /**
     * Build a message embed detailing the given comic
     *
     * @param comic Comic to build embed for
     * @return Message embed detailing comic
     */
    private MessageEmbed buildComicEmbed(Comic comic) {
        String thumbnail = "https://i.imgur.com/gshCGA1.png";
        return new EmbedBuilder()
                .setTitle("#" + comic.getIssue() + " - " + comic.getTitle())
                .setImage(comic.getImage())
                .setDescription(comic.getDesc() + " \n\n" + EmbedHelper.embedURL("View", BASE_URL + "/" + comic.getIssue()))
                .setThumbnail(thumbnail)
                .setFooter(
                        "Date: " + comic.getFormattedDate()
                                + " | "
                                + "Try " + getTrigger().replace("\n", " or "),
                        thumbnail
                )
                .setColor(EmbedHelper.GREEN)
                .build();
    }

    /**
     * Get a random comic issue number between 1 and the most recently released issue (inclusive)
     *
     * @return Random comic issue number
     */
    private int getRandomIssueNumber() {
        return 1 + random.nextInt(LATEST_COMIC_ISSUE);
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("xkcd");
    }
}
