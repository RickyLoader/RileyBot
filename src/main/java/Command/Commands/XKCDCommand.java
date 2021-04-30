package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableTableEmbed;
import Network.NetworkRequest;
import Network.NetworkResponse;
import XKCD.Comic;
import XKCD.Comic.PublicationDetails;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * View XKCD comics
 */
public class XKCDCommand extends DiscordCommand {
    private final String BASE_URL = "https://xkcd.com/";
    private final Random random = new Random();
    private final String thumbnail = "https://i.imgur.com/gshCGA1.png", helpMessage = "Type: xkcd for help";
    private ArrayList<PublicationDetails> archive;
    private long archiveFetched;

    public XKCDCommand() {
        super("xkcd\nxkcd [random/issue #/latest/search term]", "Check out some XKCD comics!");
        updateArchive();
    }

    /**
     * Update the archive of comic publication details
     * if it has been an hour since the last update (or the archive is null)
     */
    private void updateArchive() {
        long currentTime = System.currentTimeMillis();
        if(archive != null && currentTime - archiveFetched < 3600000) {
            return;
        }
        this.archive = fetchComicArchive();
        this.archiveFetched = System.currentTimeMillis();
    }

    /**
     * Fetch the archive of XKCD comic publication details.
     *
     * @return List of XKCD comic publication details (in ascending order of issue #)
     */
    private ArrayList<PublicationDetails> fetchComicArchive() {
        ArrayList<PublicationDetails> archive = new ArrayList<>();
        Document archivePage = fetchComicArchivePage();
        if(archivePage == null) {
            return null;
        }
        Elements publications = archivePage.getElementById("middleContainer").getElementsByTag("a");
        for(Element publication : publications) {
            archive.add(
                    new PublicationDetails(
                            Integer.parseInt(publication.attr("href").replace("/", "")),
                            parseDate(publication.attr("title")),
                            publication.text()
                    )
            );
        }
        archive.sort(Comparator.comparingInt(PublicationDetails::getIssue));
        return archive;
    }

    /**
     * Get the HTML document of the XKCD comic archive page
     *
     * @return HTML document of XKCD comic archive page
     */
    private Document fetchComicArchivePage() {
        try {
            return Jsoup.connect(BASE_URL + "archive").get();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get an XKCD comic by the issue number.
     * Use the API to retrieve the description and image.
     * The archived publication details contain the remaining info required to build a comic.
     *
     * @param issue Comic issue to get
     * @return Comic
     */
    private Comic getComic(int issue) {
        NetworkResponse response = new NetworkRequest(BASE_URL + issue + "/info.0.json", false).get();
        if(response.code == 404) {
            return null;
        }
        int index = issue < 404 ? issue - 1 : issue - 2; // Issue #404 doesn't exist
        JSONObject comic = new JSONObject(response.body);
        return new Comic(
                archive.get(index),
                comic.getString("img"),
                comic.getString("alt")
        );
    }

    /**
     * Parse a date String into a Date object
     *
     * @param dateString Date String in format yyyy-MM-dd
     * @return Date object
     */
    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        String message = context
                .getLowerCaseMessage()
                .replace("xkcd", "")
                .trim();

        if(message.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        channel.sendTyping().queue();
        updateArchive();

        if(archive == null) {
            channel.sendMessage(
                    member.getAsMention() + " I wasn't able to fetch the XKCD archive bro, try again!"
            ).queue();
            return;
        }

        Comic comic;
        switch(message) {
            case "latest":
                comic = getComic(archive.size() + 1);
                break;
            case "random":
                comic = getComic(getRandomIssueNumber());
                break;
            default:
                int issueNumber = toInteger(message);
                if(issueNumber > 0) {
                    comic = getComic(issueNumber);
                }
                else {
                    PublicationDetails[] searchResults = searchComics(message);
                    if(searchResults.length == 1) {
                        comic = getComic(searchResults[0].getIssue());
                    }
                    else {
                        showSearchResults(message, searchResults, context);
                        return;
                    }
                }
        }

        if(comic == null) {
            channel.sendMessage(
                    member.getAsMention() + " Couldn't find that one bro, settle down"
            ).queue();
            return;
        }
        channel.sendMessage(buildComicEmbed(comic)).queue();
    }

    /**
     * Show the XKCD comic search results for the given query.
     * Display in a pageable embed message.
     *
     * @param query         Query used to find search results
     * @param searchResults Search results found with query
     * @param context       Command context for initialising pageable embed
     */
    private void showSearchResults(String query, PublicationDetails[] searchResults, CommandContext context) {
        new PageableTableEmbed(
                context,
                Arrays.asList(searchResults),
                thumbnail,
                "XKCD Comic Search",
                searchResults.length + " results found for: **" + query + "**",
                helpMessage,
                new String[]{
                        "Issue #",
                        "Title",
                        "Date"
                },
                5,
                searchResults.length == 0 ? EmbedHelper.RED : EmbedHelper.GREEN
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                PublicationDetails publicationDetails = (PublicationDetails) items.get(index);
                return new String[]{
                        "#" + publicationDetails.getIssue(),
                        publicationDetails.getTitle(),
                        publicationDetails.getFormattedDate()
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    int i1 = ((PublicationDetails) o1).getIssue();
                    int i2 = ((PublicationDetails) o2).getIssue();
                    if(defaultSort) {
                        return i1 - i2;
                    }
                    return i2 - i1;
                });
            }
        }.showMessage();
    }

    /**
     * Search the archived publication details for a title containing
     * the given query.
     *
     * @return Array of search results
     */
    private PublicationDetails[] searchComics(String query) {
        return archive
                .stream()
                .filter(publicationDetails -> publicationDetails.getTitle().toLowerCase().contains(query))
                .toArray(PublicationDetails[]::new);
    }

    /**
     * Build a message embed detailing the given comic
     *
     * @param comic Comic to build embed for
     * @return Message embed detailing comic
     */
    private MessageEmbed buildComicEmbed(Comic comic) {
        PublicationDetails details = comic.getPublicationDetails();
        return new EmbedBuilder()
                .setTitle("#" + details.getIssue() + " - " + details.getTitle())
                .setImage(comic.getImage())
                .setDescription(
                        comic.getDesc()
                                + "\n\n"
                                + EmbedHelper.embedURL("View", BASE_URL + "/" + details.getIssue())
                )
                .setThumbnail(thumbnail)
                .setFooter(
                        "Date: " + details.getFormattedDate() + " | " + helpMessage,
                        thumbnail
                )
                .setColor(EmbedHelper.GREEN)
                .build();
    }

    /**
     * Get a random comic issue number between 1 and the most recently released issue (inclusive).
     * Return 405 if the rolled issue is 404 - Issue #404 doesn't exist
     *
     * @return Random comic issue number
     */
    private int getRandomIssueNumber() {
        int issue = 1 + random.nextInt(archive.size());
        return issue == 404 ? 405 : issue;
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("xkcd");
    }
}
