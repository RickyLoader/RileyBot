package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import XKCD.Comic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Fetch a random XKCD comic
 */
public class XKCDCommand extends DiscordCommand {
    private final int LATEST_COMIC;
    private final String BASE_URL = "https://xkcd.com/";
    private final Random random = new Random();

    public XKCDCommand() {
        super("xkcd\nxkcd [issue #]", "Get a random XKCD comic!");
        this.LATEST_COMIC = getLatestComic().getIssue();
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

        int comicIssue = message.isEmpty() ? getRandomIssueNumber() : getQuantity(message);

        if(comicIssue <= 0) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        Comic comic = getComic(comicIssue);

        if(comic == null) {
            channel.sendMessage(
                    context.getMember().getAsMention() + " They don't have that many bro, settle down"
            ).queue();
            return;
        }

        String thumbnail = "https://i.imgur.com/gshCGA1.png";

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("#" + comic.getIssue() + " - " + comic.getTitle())
                .setImage(comic.getImage())
                .setDescription(comic.getDesc())
                .setThumbnail(thumbnail)
                .setFooter(
                        "Date: " + comic.getFormattedDate()
                                + " | "
                                + "Try " + getTrigger().replace("\n", " or "),
                        thumbnail
                )
                .setColor(EmbedHelper.GREEN);

        channel.sendMessage(builder.build()).queue();
    }

    /**
     * Get a random comic issue number between 1 and the most recently released issue (inclusive)
     *
     * @return Random comic issue number
     */
    private int getRandomIssueNumber() {
        return 1 + random.nextInt(LATEST_COMIC);
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("xkcd");
    }
}
