package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import NASA.APOD;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * View the NASA astronomy picture of the day
 */
public class NASACommand extends DiscordCommand {
    private final ArrayList<APOD> apodList = new ArrayList<>();
    private final HashMap<LocalDate, APOD> apodDateMap = new HashMap<>();
    private final HashMap<String, ArrayList<APOD>> apodSearchMap = new HashMap<>();
    private final Random random = new Random();
    private final LocalDate START_DATE;
    private LocalDate CURRENT_DATE;
    private final DateTimeFormatter
            NASA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            NZ_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public NASACommand() {
        super(
                "nasa\nnasa [day/month/year]\nnasa latest\nnasa [search term]",
                "NASA astronomy picture of the day!"
        );
        fetchApodData();
        START_DATE = apodList.get(0).getDate();
        CURRENT_DATE = apodList.get(apodList.size() - 1).getDate();
        updateData();
    }

    /**
     * Check if there are any missing dates and add them to the database
     */
    private void updateData() {
        LocalDate now = LocalDate.now(ZoneId.of("America/New_York"));
        if(now.isEqual(CURRENT_DATE)) {
            return;
        }
        ArrayList<LocalDate> missingDates = getDatesBetween(CURRENT_DATE, now);
        System.out.println(missingDates.size() + " date(s) missing: ");

        for(LocalDate date : missingDates) {
            APOD apod = fetchApodFromNASA(date);
            String dateString = NZ_FORMAT.format(date);
            if(apod == null) {
                System.out.println("Skipping: " + dateString);
                continue;
            }
            storeApod(apod);
            apodList.add(apod);
            apodDateMap.put(apod.getDate(), apod);
            for(String searchQuery : apodSearchMap.keySet()) {
                if(!apod.getSearchText().toLowerCase().contains(searchQuery)) {
                    continue;
                }
                apodSearchMap.get(searchQuery).add(apod);
            }
            System.out.println("Stored: " + dateString);
        }
        Collections.sort(apodList);
        CURRENT_DATE = now;
    }

    /**
     * Store the given APOD in the database
     *
     * @param apod APOD to store
     */
    private void storeApod(APOD apod) {
        new Thread(() -> {
            String body = new JSONObject()
                    .put("date", NZ_FORMAT.format(apod.getDate()))
                    .put("explanation", apod.getExplanation())
                    .put("url", apod.getUrl())
                    .put("title", apod.getTitle())
                    .put("image", apod.getImage())
                    .toString();
            new NetworkRequest("nasa", true).post(body);
        }).start();
    }

    /**
     * Get a list of dates between the given start and end dates
     *
     * @param start Start date exclusive
     * @param end   End date
     * @return List of dates between the given dates
     */
    private ArrayList<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
        ArrayList<LocalDate> dates = new ArrayList<>();
        start = start.plusDays(1);
        while(!start.isAfter(end)) {
            dates.add(start);
            start = start.plusDays(1);
        }
        return dates;
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage().replace("nasa", "").trim();
        MessageChannel channel = context.getMessageChannel();
        updateData();

        if(message.equals("help")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        APOD result;
        if(message.isEmpty()) {
            result = apodList.get(random.nextInt(apodList.size()));
        }
        else if(message.equals("latest")) {
            result = apodDateMap.get(CURRENT_DATE);
        }
        else {
            LocalDate date = parseNZDate(message);
            Member member = context.getMember();
            if(date == null) {
                ArrayList<APOD> searchResults = searchApodList(message);
                if(searchResults.isEmpty()) {
                    channel.sendMessage(
                            member.getAsMention()
                                    + " I didn't find anything when searching for **" + message + "**."
                    ).queue();
                    return;
                }
                result = searchResults.get(random.nextInt(searchResults.size()));
            }
            else {
                if(date.isBefore(START_DATE)) {
                    date = START_DATE;
                }
                if(date.isAfter(CURRENT_DATE)) {
                    date = CURRENT_DATE;
                }
                result = apodDateMap.get(date);
                if(result == null) {
                    channel.sendMessage(
                            member.getAsMention()
                                    + " They didn't take any pictures on **" + message + "** sorry bro."
                    ).queue();
                    return;
                }
            }
        }
        channel.sendMessage(buildApodEmbed(result)).queue();
    }

    /**
     * Search the titles and explanations of the APOD list to find
     * any that contain the search query.
     * Create a list containing the results and map it to the search query.
     *
     * @param query Search query
     * @return List of results
     */
    private ArrayList<APOD> searchApodList(String query) {
        String key = query.toLowerCase();
        ArrayList<APOD> results = apodSearchMap.get(key);
        if(results != null) {
            return results;
        }
        results = apodList
                .stream()
                .filter(apod -> apod.getSearchText().toLowerCase().contains(key))
                .collect(Collectors.toCollection(ArrayList::new));
        apodSearchMap.put(query, results);
        return results;
    }

    /**
     * Build a message embed detailing the given APOD
     *
     * @param apod Astronomy picture of the day
     * @return Message embed displaying APOD
     */
    private MessageEmbed buildApodEmbed(APOD apod) {
        String range = "Range: " + NZ_FORMAT.format(START_DATE) + " - " + NZ_FORMAT.format(CURRENT_DATE);
        return new EmbedBuilder()
                .setTitle(apod.getTitle() + " - " + NZ_FORMAT.format(apod.getDate()))
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
     * Attempt to parse the provided date String in to a LocalDate object
     * Return null if a parse exception is thrown
     *
     * @param date Date String to parse (NZ formatted)
     * @return LocalDate
     */
    private LocalDate parseNZDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("d/M/yyyy"));
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
    private APOD fetchApodFromNASA(LocalDate date) {
        String url = "https://api.nasa.gov/planetary/apod?api_key="
                + Secret.NASA_KEY
                + "&date=" + date.format(NASA_FORMAT) + "&thumbs=true";

        NetworkResponse response = new NetworkRequest(url, false).get();
        if(response.code == 404) {
            return null;
        }
        JSONObject data = new JSONObject(response.body);

        if(!data.has("media_type") || data.getString("media_type").equals("other")) {
            return null;
        }

        LocalDate parsedDate = LocalDate.parse(data.getString("date"), NASA_FORMAT);

        return new APOD.APODBuilder()
                .setDate(parsedDate)
                .setUrl(getApodPageURL(parsedDate))
                .setExplanation(data.getString("explanation"))
                .setImage(data.has("hdurl") ? data.getString("hdurl") : data.getString("thumbnail_url"))
                .setTitle(data.getString("title"))
                .build();
    }

    /**
     * Get the URL to the APOD page online for the given date
     *
     * @param date Date to get URL for
     * @return URL to APOD online
     */
    private String getApodPageURL(LocalDate date) {
        return "https://apod.nasa.gov/apod/" + DateTimeFormatter.ofPattern("'ap'yyMMdd").format(date) + ".html";
    }

    /**
     * Fetch all of the astronomy pictures of the day
     * from the database and store in an ordered list by date,
     * and a map of date String->APOD
     * <p>
     * Add the APOD to any relevant cached search results
     */
    private void fetchApodData() {
        JSONArray apodList = new JSONArray(new NetworkRequest("nasa", true).get().body);
        for(int i = 0; i < apodList.length(); i++) {
            JSONObject apodData = apodList.getJSONObject(i);
            APOD apod = new APOD.APODBuilder()
                    .setDate(LocalDate.parse(apodData.getString("date"), NZ_FORMAT))
                    .setImage(apodData.getString("image"))
                    .setUrl(apodData.getString("url"))
                    .setTitle(apodData.getString("title"))
                    .setExplanation(apodData.getString("explanation"))
                    .build();
            this.apodList.add(apod);
            this.apodDateMap.put(apod.getDate(), apod);
            Collections.sort(this.apodList);
        }
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("nasa");
    }
}
