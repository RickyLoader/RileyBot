package Steam;

import Network.NetworkRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Steam application search
 */
public class SteamStore {
    public final static String
            STEAM_LOGO = "https://i.imgur.com/6gE0Aog.png",
            STEAM_STORE_WEB_BASE_URL = "https://steamcommunity.com/app/",
            STEAM_STORE_WEB_URL = STEAM_STORE_WEB_BASE_URL + "\\d+/?",
            STEAM_STORE_DESKTOP_URL = "https://store.steampowered.com/app/\\d+/.+/?",
            STEAM_API_BASE_URL = "https://api.steampowered.com/";

    private final HashMap<Integer, AppInfo> appIdentifiers;
    private final String steamSpyBaseUrl = "https://steamspy.com/api.php?request=";
    private long lastFetched;
    private static SteamStore instance = null;

    /**
     * Initialise the steam store data
     */
    private SteamStore() {
        this.appIdentifiers = new HashMap<>();
        updateStoreData();
    }

    /**
     * Get an instance of the SteamStore class
     *
     * @return Instance
     */
    public static SteamStore getInstance() {
        if(instance == null) {
            instance = new SteamStore();
        }
        return instance;
    }

    /**
     * Update the store data if it has been an hour since the last update
     * Map unseen applications from unique id -> name
     */
    public void updateStoreData() {
        long now = System.currentTimeMillis();
        if(now - lastFetched < 3600000) {
            return;
        }
        fetchAppIdentifiers();
        lastFetched = now;
    }

    /**
     * Get the top Steam applications (by concurrent players) for the last 2 weeks.
     *
     * @return List of top Steam applications of the last 2 weeks
     */
    public ArrayList<Application> fetchTopSteamApplications() {
        String json = new NetworkRequest(steamSpyBaseUrl + "top100in2weeks", false).get().body;
        JSONObject applicationData = new JSONObject(json);
        ArrayList<Application> topApplications = new ArrayList<>();

        for(String id : applicationData.keySet()) {
            JSONObject application = applicationData.getJSONObject(id);
            AppInfo appInfo = appIdentifiers.get(Integer.valueOf(id));
            if(appInfo == null) {
                continue;
            }
            double price = application.getDouble("price");
            topApplications.add(
                    new Application(
                            appInfo,
                            price == 0 ? null : new Price(price / 100, "USD"),
                            application.getLong("ccu")
                    )
            );
        }
        topApplications.sort(Comparator.comparingLong(Application::getConcurrentPlayers));
        return topApplications;
    }

    /**
     * Fetch the list of all apps on the Steam store. The information provided for each
     * app is the unique application id and name. Add any unseen applications to the map of id -> name
     */
    private void fetchAppIdentifiers() {
        String json = new NetworkRequest(
                STEAM_API_BASE_URL + "ISteamApps/GetAppList/v0002/?format=json", false
        ).get().body;

        JSONArray appList = new JSONObject(json).getJSONObject("applist").getJSONArray("apps");
        for(int i = 0; i < appList.length(); i++) {
            JSONObject application = appList.getJSONObject(i);
            int id = application.getInt("appid");
            if(appIdentifiers.containsKey(id)) {
                continue;
            }
            appIdentifiers.put(id, new AppInfo(id, application.getString("name")));
        }
    }

    /**
     * Fetch the application details from the given application info
     *
     * @param appInfo Application info
     * @return Application
     */
    public Application fetchApplicationDetails(AppInfo appInfo) {
        try {
            String detailsJSON = new NetworkRequest(
                    "https://store.steampowered.com/api/appdetails?appids="
                            + appInfo.getId() + "&currency=NZD",
                    false
            ).get().body;

            String metricsJSON = new NetworkRequest(
                    steamSpyBaseUrl + "appdetails&appid=" + appInfo.getId(),
                    false
            ).get().body;

            JSONObject details = new JSONObject(detailsJSON)
                    .getJSONObject(String.valueOf(appInfo.getId()))
                    .getJSONObject("data");

            JSONObject price = details.has("price_overview")
                    ? details.getJSONObject("price_overview")
                    : null;
            JSONObject metrics = new JSONObject(metricsJSON);
            return new Application(
                    appInfo,
                    details.getString("type").toUpperCase(),
                    details.getString("short_description"),
                    price == null
                            ? null
                            : new Price(
                            price.getDouble("final") / 100,
                            price.getString("currency")
                    ),
                    metrics.getLong("ccu")
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get an array of applications with the given name. Search for an exact match before
     * falling back to a fuzzy search.
     *
     * @param query Name to search
     * @return Applications matching/containing name
     */
    public ArrayList<AppInfo> getApplicationsByName(String query) {
        ArrayList<AppInfo> matching = appIdentifiers.values().stream()
                .filter(appInfo -> appInfo.getName().equalsIgnoreCase(query))
                .collect(Collectors.toCollection(ArrayList::new));

        if(matching.size() == 1) {
            return matching;
        }
        return appIdentifiers.values().stream()
                .filter(appInfo -> appInfo.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get the info for an application from a unique application id
     *
     * @param appId Unique application id
     * @return App info
     */
    public AppInfo getApplicationInfo(int appId) {
        return appIdentifiers.get(appId);
    }

    /**
     * Check if the given query matches a Steam store URL (Desktop or Web)
     *
     * @param query Query to check
     * @return Query is a Steam store URL
     */
    public static boolean isSteamUrl(String query) {
        return query.matches(STEAM_STORE_WEB_URL) || query.matches(STEAM_STORE_DESKTOP_URL);
    }
}
