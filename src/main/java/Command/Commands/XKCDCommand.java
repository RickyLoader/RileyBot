package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import XKCD.Comic;
import net.dv8tion.jda.api.EmbedBuilder;
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
        super("xkcd", "Get a random XKCD comic!");
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
        return parseComicJSON(
                new NetworkRequest(BASE_URL + issue + "/info.0.json", false).get().body
        );
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
        Comic comic = getComic(1 + random.nextInt(LATEST_COMIC - 1));
        String thumbnail = "https://i.imgur.com/gshCGA1.png";

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("#" + comic.getIssue() + " - " + comic.getTitle())
                .setImage(comic.getImage())
                .setDescription(comic.getDesc())
                .setThumbnail(thumbnail)
                .setFooter("Date: " + comic.getFormattedDate(), thumbnail)
                .setColor(EmbedHelper.GREEN);

        context.getMessageChannel().sendMessage(builder.build()).queue();
    }
}
