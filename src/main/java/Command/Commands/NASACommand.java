package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import NASA.APOD;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Random;

/**
 * View the NASA astronomy picture of the day
 */
public class NASACommand extends DiscordCommand {
    private final DateTimeFormatter NASA_FORMAT, NZ_READ_FORMAT, NZ_WRITE_FORMAT;
    private final LocalDate START_DATE;
    private LocalDate CURRENT_DATE;
    private final ArrayList<LocalDate> dates;
    private final Random random = new Random();

    public NASACommand() {
        super("nasa\nnasa [day/month/year]\nnasa latest", "NASA astronomy picture of the day!");
        NASA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NZ_READ_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy"); // Optional leading zero
        NZ_WRITE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Enforced leading zero
        START_DATE = LocalDate.parse("1995-06-16", NASA_FORMAT);
        CURRENT_DATE = getCurrentNASADate();
        dates = getDatesBetween(START_DATE, CURRENT_DATE);
    }

    /**
     * Get the current date where NASA is located
     *
     * @return Current date for NASA
     */
    private LocalDate getCurrentNASADate() {
        return LocalDate.now(ZoneId.of("America/New_York"));
    }

    /**
     * Get a list of dates between the given start and end dates
     *
     * @param start Start date
     * @param end   End date
     * @return List of dates between the given dates
     */
    private ArrayList<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
        ArrayList<LocalDate> dates = new ArrayList<>();
        while(!start.isAfter(end)) {
            dates.add(start);
            start = start.plusDays(1);
        }
        return dates;
    }

    @Override
    public void execute(CommandContext context) {
        LocalDate now = getCurrentNASADate();
        if(now.isAfter(CURRENT_DATE)) {
            dates.add(now);
            CURRENT_DATE = now;
        }

        String message = context.getLowerCaseMessage().replace("nasa", "").trim();
        MessageChannel channel = context.getMessageChannel();

        if(message.equals("help")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        LocalDate date;
        if(message.isEmpty()) {
            date = dates.get(random.nextInt(dates.size()));
        }
        else if(message.equals("latest")) {
            date = CURRENT_DATE;
        }
        else {
            date = parseNZDate(message);
            if(date == null) {
                channel.sendMessage(
                        context.getMember().getAsMention()
                                + " Sorry bro, I couldn't parse **" + message + "** as a date."
                ).queue();
                return;
            }
            if(date.isBefore(START_DATE)) {
                date = START_DATE;
            }
            if(date.isAfter(CURRENT_DATE)) {
                date = CURRENT_DATE;
            }
        }
        LocalDate finalDate = date;
        new Thread(() -> channel.sendMessage(buildAPODEmbed(getAPOD(finalDate))).queue()).start();
    }

    /**
     * Build a message embed detailing the given APOD
     *
     * @param apod Astronomy picture of the day
     * @return Message embed displaying APOD
     */
    private MessageEmbed buildAPODEmbed(APOD apod) {
        String range = "Range: " + NZ_WRITE_FORMAT.format(START_DATE) + " - " + NZ_WRITE_FORMAT.format(CURRENT_DATE);
        return new EmbedBuilder()
                .setTitle(apod.getTitle() + " - " + NZ_WRITE_FORMAT.format(apod.getDate()))
                .setDescription(
                        apod.getTruncatedExplanation() + "\n\n" + EmbedHelper.embedURL("View", apod.getUrl())
                )
                .setThumbnail("https://i.imgur.com/3mAzBNf.png")
                .setFooter(
                        range + " | Try: nasa help",
                        "https://i.imgur.com/6YB5aMV.png"
                )
                .setColor(EmbedHelper.PURPLE)
                .setImage(apod.getImage())
                .build();
    }

    /**
     * Attempt to parse the provided date String to a LocalDate object
     * Return null if a parse exception is thrown
     *
     * @param date Date String to parse (NZ formatted)
     * @return LocalDate
     */
    private LocalDate parseNZDate(String date) {
        try {
            return LocalDate.parse(date, NZ_READ_FORMAT);
        }
        catch(DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Get the NASA astronomy picture of the day for the given date
     *
     * @param date Date of picture to get
     * @return Picture of the day
     */
    private APOD getAPOD(LocalDate date) {
        String dateString = date.format(NASA_FORMAT);
        String url = "https://api.nasa.gov/planetary/apod?api_key=" + Secret.getNASAKey() + "&date=" + dateString;
        JSONObject data = new JSONObject(new NetworkRequest(url, false).get().body);
        LocalDate parsedDate = LocalDate.parse(data.getString("date"), NASA_FORMAT);

        return new APOD.APODBuilder()
                .setDate(parsedDate)
                .setUrl(getAPODPageURL(parsedDate))
                .setExplanation(data.getString("explanation"))
                .setImage(data.getString("hdurl"))
                .setTitle(data.getString("title"))
                .build();
    }

    /**
     * Get the URL to the APOD page online for the given date
     *
     * @param date Date to get URL for
     * @return URL to APOD online
     */
    private String getAPODPageURL(LocalDate date) {
        return "https://apod.nasa.gov/apod/" + DateTimeFormatter.ofPattern("'ap'yyMMdd").format(date) + ".html";
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("nasa");
    }
}
