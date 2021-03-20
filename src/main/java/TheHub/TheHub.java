package TheHub;

import COD.Assets.Ratio;
import TheHub.HubVideo.Channel;
import TheHub.HubVideo.VideoInfo;
import TheHub.Performer.PROFILE_TYPE;
import TheHub.Performer.PerformerBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TheHub {
    public static final int FIRST_PAGE = 54, OTHER_PAGE = 57;
    private static final String BASE_URL = "https://www.pornhub.com/";
    private final String listUrl = BASE_URL + "pornstars";
    private final HashMap<String, Performer> performersByName = new HashMap<>(), performersByUrl = new HashMap<>();
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
        performersByUrl.clear();
        lastReset = System.currentTimeMillis();
    }

    /**
     * Add the provided performer to the maps
     * Map from name to instance, rank to instance, and url to instance
     *
     * @param performer Performer to map
     */
    private void mapPerformer(Performer performer) {
        performersByName.put(performer.getName().toLowerCase(), performer);
        performersByUrl.put(performer.getURL(), performer);

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
                : completeModelProfile(doc, builder);
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
    private Performer completeModelProfile(Document doc, PerformerBuilder builder) {
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
            return doc.baseUri().equals(listUrl) ? null : doc; // Redirected
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Retrieve a performer by their url
     * If the given url is not located in the map, attempt to parse their page
     *
     * @param url URL to performer
     * @return Performer of the given URL or null
     */
    public Performer getPerformerByUrl(String url) {
        if(!isProfileUrl(url)) {
            return null;
        }
        Performer performer = performersByUrl.get(url);
        if(performer != null) {
            return performer;
        }
        resetData();
        String type = url.replace(BASE_URL, "").split("/")[0];
        performer = parseHomePage(url, PROFILE_TYPE.valueOf(type.toUpperCase()));
        mapPerformer(performer);
        return performer;
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

        Document doc = fetchPage(listUrl + "?performerType=pornstar&t=a&page=" + page);
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
     * Retrieve a list of performers by their name.
     * If the given name is not located in the map, perform a search for each PROFILE_TYPE
     * and append the results to a list.
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param name Performer name
     * @return List of performer results
     */
    public ArrayList<Performer> getPerformersByName(String name) {
        resetData();
        HashSet<Performer> performerSet = new HashSet<>();
        Performer performer = performersByName.get(name.toLowerCase());

        if(performer != null) {
            return new ArrayList<>(Collections.singletonList(performer));
        }

        performerSet.addAll(getStarsByName(name));
        performerSet.addAll(getModelsByName(name));
        performerSet.addAll(getCamModelsByName(name));
        performerSet.addAll(getChannelsByName(name));

        ArrayList<Performer> searchResults = new ArrayList<>(performerSet);

        if(searchResults.size() == 1) {
            return completeSearchResult(searchResults.get(0));
        }

        ArrayList<Performer> exactMatches = searchResults
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .collect(Collectors.toCollection(ArrayList::new));

        if(exactMatches.size() == 1) {
            return completeSearchResult(exactMatches.get(0));
        }

        return searchResults;
    }

    /**
     * Parse and map the full performer details of an incomplete performer built from a search result.
     *
     * @param incomplete Incomplete performer built from a search result (containing only name, url, and type)
     * @return List containing completed performer
     */
    private ArrayList<Performer> completeSearchResult(Performer incomplete) {
        Performer match = parseHomePage(incomplete.getURL(), incomplete.getType());
        mapPerformer(match);
        return new ArrayList<>(Collections.singletonList(match));
    }

    /**
     * Get a list of models by the given name.
     * Invoke a search with the given name and return the results.
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param name Name to search
     * @return List of model search results
     */
    private ArrayList<Performer> getModelsByName(String name) {
        return parseModelSearchResults(getUserSearchUrl(name) + "&isPornhubModel=1");
    }

    /**
     * Get a list of cam models by the given name.
     * Invoke a search with the given name and return the results.
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param name Name to search
     * @return List of cam model search results
     */
    private ArrayList<Performer> getCamModelsByName(String name) {
        return parseModelSearchResults(getUserSearchUrl(name) + "&hasCamShow=1");
    }

    /**
     * Get the base URL to perform a user search for the given name
     *
     * @param name Name to search
     * @return URL to search for users of the given name
     */
    private String getUserSearchUrl(String name) {
        return BASE_URL + "user/search/?username=" + name;
    }

    /**
     * Get a list of channels by the given name.
     * Invoke a search with the given name and return the results.
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param name Name to search
     * @return List of channel search results
     */
    private ArrayList<Performer> getChannelsByName(String name) {
        ArrayList<Performer> channels = new ArrayList<>();
        Document searchPage = fetchPage(BASE_URL + "channels/search?channelSearch=" + name);
        if(searchPage == null || searchPage.select("#searchChannelsSection").isEmpty()) {
            return channels;
        }
        searchPage.getElementById("searchChannelsSection").children().forEach(element -> {
            Element info = element.selectFirst(".descriptionContainer").selectFirst(".usernameLink");
            channels.add(
                    new PerformerBuilder()
                            .setURL(info.absUrl("href"))
                            .setName(info.text())
                            .setType(PROFILE_TYPE.CHANNELS)
                            .build()
            );
        });
        return channels;
    }

    /**
     * Parse the model search results from the given url
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param url URL to parse
     * @return List of model search results
     */
    private ArrayList<Performer> parseModelSearchResults(String url) {
        ArrayList<Performer> members = new ArrayList<>();
        Document searchPage = fetchPage(url);
        if(searchPage == null || searchPage.getElementsByClass("search-results").isEmpty()) {
            return members;
        }
        searchPage.selectFirst(".search-results").children().forEach(element -> members.add(
                new PerformerBuilder()
                        .setURL(element.selectFirst(".userLink").absUrl("href"))
                        .setName(element.selectFirst(".usernameLink").text())
                        .setType(PROFILE_TYPE.MODEL)
                        .build()
        ));
        return members;
    }

    /**
     * Get a list of stars by the given name.
     * Invoke a search with the given name and return the results
     * The resulting performers contain only the data available from a search request - name, url, and type
     *
     * @param name Name to search
     * @return List of member search results
     */
    private ArrayList<Performer> getStarsByName(String name) {
        ArrayList<Performer> stars = new ArrayList<>();
        PROFILE_TYPE type = PROFILE_TYPE.PORNSTAR;
        Document searchPage = fetchPage(BASE_URL + type.name().toLowerCase() + "/search?search=" + name);
        if(searchPage == null || !searchPage.getElementsByClass("noResultsWrapper").isEmpty()) {
            return stars;
        }
        searchPage.getElementById("pornstarsSearchResult").select("li:not(:first-child)").forEach(element -> {
            Element url = element.selectFirst(".thumbnail-info-wrapper").selectFirst("a");
            stars.add(
                    new PerformerBuilder()
                            .setURL(url.absUrl("href"))
                            .setName(url.text())
                            .setType(type)
                            .build()
            );
        });
        return stars;
    }

    /**
     * Get details on a video from the given URL
     *
     * @param url Video URL
     * @return Video details or null
     */
    public HubVideo getVideo(String url) {
        if(!isVideoUrl(url)) {
            return null;
        }
        Document videoPage = fetchPage(url);
        if(videoPage == null || videoPage.title().equalsIgnoreCase("page not found")) {
            return null;
        }

        Ratio likeDislikeRatio = new Ratio(
                Integer.parseInt(videoPage.selectFirst(".votesUp").attr("data-rating")),
                Integer.parseInt(videoPage.selectFirst(".votesDown").attr("data-rating"))
        );

        Element channelInfo = videoPage.selectFirst(".userRow");
        Element channelName = channelInfo.selectFirst(".userInfo").selectFirst("a");
        Channel channel = new Channel(
                channelName.text(),
                channelName.absUrl("href"),
                channelInfo.selectFirst(".userAvatar").selectFirst("img").attr("data-src")
        );
        Element video = videoPage.getElementById("player");
        VideoInfo videoInfo = new VideoInfo(
                videoPage.selectFirst(".title-container").selectFirst(".title").selectFirst(".inlineFree").text(),
                videoPage.baseUri(),
                video.selectFirst(".videoElementPoster").attr("src")
        );

        Element ratingInfo = videoPage.selectFirst(".ratingInfo");
        String views = ratingInfo.selectFirst(".views").selectFirst("span").text().replace(",", "");

        return new HubVideo(
                videoInfo,
                ratingInfo.selectFirst(".videoInfo").text(),
                Long.parseLong(views),
                likeDislikeRatio,
                channel,
                getVideoInfoRow(videoPage, ".pornstarsWrapper", ".pstar-list-btn"),
                getVideoInfoRow(videoPage, ".categoriesWrapper", ".item")
        );
    }

    /**
     * Get an array of items from a video info row.
     * Video info rows are rows of information displayed under a video, this includes categories, stars, etc.
     *
     * @param video        HTML document of video
     * @param rowSelector  Outermost CSS selector of the desired row
     * @param itemSelector CSS selector for the items within the row
     * @return Array of items in the row (may be empty)
     */
    private String[] getVideoInfoRow(Document video, String rowSelector, String itemSelector) {
        Elements rows = video.select(rowSelector);
        if(rows.isEmpty()) {
            return new String[]{};
        }
        Elements items = rows.select(itemSelector);
        return items
                .stream()
                .map(Element::text)
                .toArray(String[]::new);
    }

    /**
     * Check if the given URL is a video URL
     *
     * @param url URL to check
     * @return URL is a video URL
     */
    public static boolean isVideoUrl(String url) {
        Pattern pattern = Pattern.compile(Pattern.quote(BASE_URL + "view_video.php?viewkey=ph") + "[\\w]+");
        return pattern.matcher(url).matches();
    }

    /**
     * Check if the given URL is a profile URL
     *
     * @param url URL to check
     * @return URL is a profile url
     */
    public static boolean isProfileUrl(String url) {
        Pattern pattern = Pattern.compile(Pattern.quote(BASE_URL) + "\\b(?:channels|model|pornstar)\\b/[\\w-]+");
        return pattern.matcher(url).matches();
    }
}
