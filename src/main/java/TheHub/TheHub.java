package TheHub;

import TheHub.Performer.PROFILE_TYPE;
import TheHub.Performer.PerformerBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class TheHub {
    public static final int FIRST_PAGE = 54, OTHER_PAGE = 57;
    public final String BASE_URL = "https://www.pornhub.com/", LIST_URL = BASE_URL + "pornstars";
    private final HashMap<String, Performer> performersByName = new HashMap<>();
    private final HashMap<Integer, Performer> performersByRank = new HashMap<>();
    public long lastReset = System.currentTimeMillis();

    /**
     * Reset the cached info if it has been more than an hour since the last reset
     */
    private void resetData() {
        if(System.currentTimeMillis() - lastReset < 3600000) {
            return;
        }
        performersByRank.clear();
        performersByName.clear();
        lastReset = System.currentTimeMillis();
    }

    /**
     * Add the provided performer to the maps
     * Map from name to instance & rank to instance
     *
     * @param performer Performer to map
     */
    private void mapPerformer(Performer performer) {
        performersByName.put(performer.getName().toLowerCase(), performer);

        // Models & channels are ranked differently, don't cache them to prevent overwriting stars
        if(performer.getType() == PROFILE_TYPE.PORNSTAR) {
            performersByRank.put(performer.getRank(), performer);
        }
    }

    /**
     * Parse the home page of a performer/channel/model into an object
     * Determine which info to scrape based on page type
     *
     * @param url  URL to parse
     * @param type Profile type
     * @return Performer
     */
    private Performer parseHomePage(String url, PROFILE_TYPE type) {
        Document doc = fetchPage(url);

        if(doc == null) {
            return null;
        }

        Element thumbnail = doc.selectFirst(".thumbImage img");
        if(thumbnail == null) {
            thumbnail = doc.selectFirst(".previewAvatarPicture img");
        }
        PerformerBuilder builder = new PerformerBuilder()
                .setImage(thumbnail.absUrl("src"))
                .setURL(url)
                .setType(type);

        return (type == PROFILE_TYPE.CHANNELS)
                ? completeChannelProfile(doc, builder)
                : completePersonProfile(doc, builder);
    }

    /**
     * Retrieve the required data from a person's page document to complete a
     * PerformerBuilder.
     * Name, rank, views, bio, subscribers, gender, and age
     *
     * @param doc     HTML document of person's profile
     * @param builder Incomplete performer builder
     * @return Completed Performer object
     */
    private Performer completePersonProfile(Document doc, PerformerBuilder builder) {
        String gender = null;
        int age = 0;
        Elements details = doc.select(".infoPiece");

        for(Element detail : details) {
            Elements keyValue = detail.children();
            if(keyValue.size() == 1) {
                continue;
            }
            String key = keyValue.get(0).text();
            String value = keyValue.get(1).text();
            if(key.equals("Gender:")) {
                gender = value;
            }
            else if(key.equals("Age:")) {
                age = Integer.parseInt(value);
            }
        }

        Element desc = doc.selectFirst(".bio");

        String rank = doc.selectFirst(".rankingInfo").child(4).selectFirst(".big").text();
        if(!rank.equals("N/A")) {
            builder.setRank(Integer.parseInt(rank));
        }

        return builder
                .setViews(doc.selectFirst(".videoViews span").text())
                .setSubscribers(doc.select(".infoBox span").last().text())
                .setName(doc.selectFirst(".name").text())
                .setAge(age)
                .setGender(gender)
                .setDesc(desc == null ? "No bio provided" : desc
                        .text()
                        .replace("Bio ", "")
                        .trim()
                        .substring(0, 150) + "..."
                )
                .build();
    }

    /**
     * Retrieve the required data from a channel's page document to complete a
     * PerformerBuilder.
     * Name, rank, views, bio, and subscribers
     *
     * @param doc     HTML document of channel's profile
     * @param builder Incomplete performer builder
     * @return Completed Performer object
     */
    private Performer completeChannelProfile(Document doc, PerformerBuilder builder) {
        Elements stats = doc.selectFirst("#stats").children();

        String rank = doc.selectFirst(".ranktext span").text();
        if(!rank.equals("N/A")) {
            builder.setRank(Integer.parseInt(rank));
        }

        return builder
                .setDesc(doc.selectFirst("p.joined").text().substring(0, 150) + "...")
                .setViews(stats.get(0).text().replace(" VIDEO VIEWS", ""))
                .setSubscribers(stats.get(1).text().replace(" SUBSCRIBERS", ""))
                .setName(doc.selectFirst(".title h1").text())
                .build();
    }

    /**
     * Fetch the HTML document of the given page
     *
     * @param url URL to fetch
     * @return HTML document
     */
    private Document fetchPage(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.baseUri().equals(LIST_URL) ? null : doc; // Redirected
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Retrieve a performer by their rank.
     * If the given rank is not located in the map, attempt to locate and parse
     * their page
     *
     * @param rank Performer rank
     * @return Performer of the given rank or null
     */
    public Performer getPerformerByRank(int rank) {
        resetData();
        Performer performer = performersByRank.get(rank);

        if(performer != null) {
            return performer;
        }

        String url = locateProfileByRank(rank);

        if(url == null) {
            return null;
        }

        performer = parseHomePage(url, PROFILE_TYPE.PORNSTAR);

        if(performer == null) {
            return null;
        }

        if(!performer.hasRank()) {
            performer.setRank(rank);
        }
        mapPerformer(performer);
        return performer;
    }

    /**
     * Attempt to locate the URL to a performer profile by the given rank.
     * Calculate which page of the list to query and the position on the list to find the performer.
     * If the rank at that position does not match, loop through to find it
     *
     * @param rank Rank to locate
     * @return URL to performer page of given rank or null
     */
    private String locateProfileByRank(int rank) {
        int page = 1;
        int indexOnPage;
        boolean firstPage = rank <= FIRST_PAGE;

        if(firstPage) {
            indexOnPage = rank - 1;
        }
        else {
            int offset = rank - FIRST_PAGE;
            page += (int) Math.ceil(((double) offset) / OTHER_PAGE);

            // Don't -1 from index as after the first page there is always an advert in the first list position
            indexOnPage = offset % OTHER_PAGE;

            // Correct for multiples of OTHER_PAGE (last index on a page)
            if(indexOnPage == 0) {
                indexOnPage = OTHER_PAGE;
            }
        }

        Document doc = fetchPage(LIST_URL + "?performerType=pornstar&t=a&page=" + page);
        String listID = "#popularPornstars";

        if(doc == null || doc.selectFirst(listID) == null) {
            return null;
        }

        Elements list = doc.selectFirst(listID).children();

        if(indexOnPage >= list.size()) {
            return null;
        }

        Element target = list.get(indexOnPage);

        if(parseRank(target) == rank) {
            return parseURL(target);
        }

        for(int i = firstPage ? 0 : 1; i < list.size(); i++) {
            Element performer = list.get(i);
            if(parseRank(performer) == rank) {
                return parseURL(performer);
            }
        }
        return null;
    }

    /**
     * Parse the displayed rank from a performer's list item
     *
     * @param performer Performer list element
     * @return Rank
     */
    public int parseRank(Element performer) {
        return Integer.parseInt(performer.selectFirst(".rank_number").text());
    }

    /**
     * Parse the URL to a performer's page from their list item
     *
     * @param performer Performer list element
     * @return URL
     */
    public String parseURL(Element performer) {
        return performer.selectFirst(".title").absUrl("href");
    }

    /**
     * Retrieve a performer by their name.
     * If the given name is not located in the map, attempt to navigate
     * to the performer's page using their name and parse their profile
     *
     * @param name Performer name
     * @return Performer of the given name or null
     */
    public Performer getPerformerByName(String name) {
        resetData();
        Performer performer = performersByName.get(name.toLowerCase());

        if(performer != null) {
            return performer;
        }

        name = name.replace(" ", "-");

        for(PROFILE_TYPE type : PROFILE_TYPE.values()) {
            performer = parseHomePage(BASE_URL + type.name().toLowerCase() + "/" + name, type);
            if(performer == null) {
                continue;
            }
            mapPerformer(performer);
            return performer;
        }
        return null;
    }
}
