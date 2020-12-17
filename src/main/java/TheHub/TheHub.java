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
    private final HashMap<String, Performer> performersByName = new HashMap<>();
    private final HashMap<Integer, Performer> performersByRank = new HashMap<>();
    public static final int FIRST_PAGE = 54, OTHER_PAGE = 57;
    public final String BASE_URL = "https://www.pornhub.com/";
    public long lastReset;

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

        // Models & stars are ranked differently, don't cache models to prevent overwriting stars
        if(performer.getType() == PROFILE_TYPE.PORNSTAR) {
            performersByRank.put(performer.getRank(), performer);
        }
    }

    /**
     * Parse a performer's page into an object
     *
     * @param url  URL to parse
     * @param type Profile type
     * @return Performer
     */
    private Performer parsePerformerPage(String url, PROFILE_TYPE type) {
        Document doc = fetchPage(url);

        if(doc == null || !doc.baseUri().equals(url)) { // Redirected because performer does not exist
            return null;
        }

        PerformerBuilder builder = new PerformerBuilder();

        Element thumbnail = doc.selectFirst(".thumbImage img");
        if(thumbnail == null) {
            thumbnail = doc.selectFirst(".previewAvatarPicture img");
        }

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
        Performer performer = builder
                .setDesc(desc == null ? "No bio provided" : desc
                        .text()
                        .replace("Bio ", "")
                        .trim()
                        .substring(0, 150) + "..."
                )
                .setImage(thumbnail.absUrl("src"))
                .setSubscribers(doc.select(".infoBox span").last().text())
                .setViews(doc.selectFirst(".videoViews span").text())
                .setURL(url)
                .setName(doc.selectFirst(".name").text())
                .setRank(Integer.parseInt(doc.selectFirst("span.big").text()))
                .setType(type)
                .setAge(age)
                .setGender(gender)
                .build();

        mapPerformer(performer);
        return performer;
    }

    /**
     * Fetch the HTML document of the given page
     *
     * @param url URL to fetch
     * @return HTML document
     */
    private Document fetchPage(String url) {
        try {
            return Jsoup.connect(url).get();
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

        performer = parsePerformerPage(url, PROFILE_TYPE.PORNSTAR);
        mapPerformer(performer);
        return performer;
    }

    /**
     * Attempt to locate the URL to a performer profile by the given rank.
     * Calculate which page of the list to query and the position on the list to find the performer.
     *
     * @param rank Rank to locate
     * @return URL to performer page of given rank or null
     */
    private String locateProfileByRank(int rank) {
        int page = 1;
        int indexOnPage;
        if(rank <= FIRST_PAGE) {
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

        Document doc = fetchPage(BASE_URL + "pornstars?performerType=pornstar&page=" + page);
        String listID = "#popularPornstars";
        if(doc == null || doc.selectFirst(listID) == null) {
            return null;
        }
        return doc
                .selectFirst(listID)
                .child(indexOnPage)
                .selectFirst(".title")
                .absUrl("href");
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
        PROFILE_TYPE[] types = new PROFILE_TYPE[]{PROFILE_TYPE.PORNSTAR, PROFILE_TYPE.MODEL};

        for(PROFILE_TYPE type : types) {
            performer = parsePerformerPage(BASE_URL + type.name().toLowerCase() + "/" + name, type);
            if(performer == null) {
                continue;
            }
            mapPerformer(performer);
            return performer;
        }
        return null;
    }
}
