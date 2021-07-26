package Olympics;

import Network.NetworkRequest;
import Olympics.Athlete.*;
import Olympics.Country.Country;
import Olympics.Country.CountryBio;
import Olympics.Country.CountryMapping;
import Olympics.Country.NationalAnthem;
import Olympics.Medal.Medal;
import Olympics.Medal.MedalCount;
import Olympics.Medal.MedalStanding;
import Olympics.Sport.Event;
import Olympics.Sport.Sport;
import Olympics.Sport.SportMapping;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Olympics API functions
 */
public class Olympics {
    private final AthleteMapping athletes;
    private final CountryMapping countries;
    private final SportMapping sports;
    public static final String LOGO = "https://i.imgur.com/2LQRZKj.png";
    private static final String
            NOC_KEY = "noc",
            MEDAL_TYPE_KEY = "medal",
            BASE_API_URL = "https://olympics.com/tokyo-2020/";

    /**
     * Initialise the Olympic data
     */
    public Olympics() {
        this.countries = new CountryMapping(fetchCountries());
        this.sports = new SportMapping(fetchSports());
        this.athletes = new AthleteMapping(fetchAthletes());
    }

    /**
     * Create a map of all Olympic sports.
     * Map from sport code to sport - e.g "BOX" -> Boxing
     *
     * @return Sport mapping
     */
    private HashMap<String, Sport> fetchSports() {
        HashMap<String, Sport> sports = new HashMap<>();
        final String url = BASE_API_URL + "/RMA/olympic/data/CCO/CC_ENG.json", nameKey = "Sn";

        JSONObject data = new JSONObject(new NetworkRequest(url, false).get().body);
        JSONObject sportsList = data.getJSONObject("Discipline");

        for(String sportCode : sportsList.keySet()) {
            sports.put(
                    sportCode,
                    new Sport(
                            sportCode,
                            sportsList.getJSONObject(sportCode).getString(nameKey),
                            Olympics.BASE_API_URL
                                    + "olympic-games/resOG2020-/img/sports/" + sportCode + ".png"
                    )
            );
        }

        // Add events to the sports
        JSONObject eventList = data.getJSONObject("Event");

        for(String eventCode : eventList.keySet()) {
            JSONObject eventDetails = eventList.getJSONObject(eventCode);

            /*
             * Sport codes are always 3 characters and found at the beginning of an event code.
             * E.g Men's Team Sprint event = "CTRMSPRTEAM3----------------------" -> "CTR" -> "Cycling Track"
             */
            final String sportCode = eventCode.substring(0, 3);

            sports.get(sportCode).addEvent(
                    new Event(
                            eventCode,
                            eventDetails.getString(nameKey)
                    )
            );
        }
        return sports;
    }

    /**
     * Create a map of all Olympic athletes.
     * Map from athlete ID to athlete.
     *
     * @return Athlete mapping
     */
    private HashMap<Long, Athlete> fetchAthletes() {
        HashMap<Long, Athlete> athletes = new HashMap<>();
        final String url = BASE_API_URL + "RMA/olympic/data/CAT/athletes_search.json";

        JSONArray athleteList = new JSONObject(
                new NetworkRequest(url, false).get().body
        ).getJSONArray("list");

        for(int i = 0; i < athleteList.length(); i++) {
            JSONObject athleteData = athleteList.getJSONObject(i);

            final String id = athleteData.getString("id");
            final String name = athleteData.getString("name");

            JSONArray sportsList = athleteData.getJSONArray("disciplines");
            Sport[] athleteSports = new Sport[sportsList.length()];

            // Parse athlete sports
            for(int j = 0; j < sportsList.length(); j++) {
                athleteSports[j] = sports.getMapping().get(sportsList.getString(j));
            }

            athletes.put(
                    Long.parseLong(id),
                    new Athlete(
                            id,
                            formatAthleteName(name),
                            name,
                            Olympics.BASE_API_URL + "olympic-games/resOG2020-/img/bios/photos/" + id + ".jpg",
                            countries.getMapping().get(athleteData.getString(NOC_KEY)),
                            athleteSports
                    )
            );
        }
        return athletes;
    }

    /**
     * Create a map of all competing Olympic countries. Map from NOC (e.g NZL) -> details
     *
     * @return Country mapping
     */
    private HashMap<String, Country> fetchCountries() {
        HashMap<String, Country> countries = new HashMap<>();
        final String url = BASE_API_URL + "olympic-games/en/results/all-sports/zzjs002c.json";

        JSONArray countryList = new JSONObject(
                new NetworkRequest(url, false).get().body
        ).getJSONArray("nocs");

        for(int i = 0; i < countryList.length(); i++) {
            JSONObject countryData = countryList.getJSONObject(i);
            final String code = countryData.getString("code");

            countries.put(
                    code, // Uppercase
                    new Country(
                            code,
                            countryData.getString("name"),
                            BASE_API_URL + "olympic-games/resCOMMON/img/flags/" + code + ".png"
                    )
            );
        }
        return countries;
    }

    /**
     * Get the Olympic athlete mapping. Athletes are mapped from ID -> athlete.
     *
     * @return Olympic athlete mapping
     */
    public AthleteMapping getAthletes() {
        return athletes;
    }

    /**
     * Get the Olympic country mapping. Countries are mapped from code -> country.
     *
     * @return Olympic country mapping
     */
    public CountryMapping getCountries() {
        return countries;
    }

    /**
     * Get the Olympic sports mapping. Sports are mapped from code -> sport.
     *
     * @return Olympic sports mapping
     */
    public SportMapping getSports() {
        return sports;
    }

    /**
     * Fetch/update the given country's bio, this contains their medal standing, flag bearers, etc.
     * If the country has already had their bio fetched, only update their medals (as the other values don't change).
     *
     * @param country Country to fetch/update bio for
     */
    public void updateCountryBio(Country country) {
        final String url = BASE_API_URL + "RMA/olympic/data/CAT/BIO/NOC/" + country.getCode() + ".json";
        JSONObject data = new JSONObject(new NetworkRequest(url, false).get().body);

        MedalStanding medalStanding = parseCountryMedalStanding(data.getJSONObject("medalStandings"));

        // Only update medal standing
        if(country.hasBio()) {
            country.getBio().setMedalStanding(medalStanding);
        }

        // Parse bio
        else {
            JSONObject basicData = data.getJSONObject("icms");

            // Parse flag bearers, countries may have none, one, or two
            final String firstKey = "FLAGBEARER_ID", secondKey = "FLAGBEARER2_ID";
            Athlete[] flagBearers = null;

            if(data.has(firstKey)) {
                flagBearers = new Athlete[data.has(secondKey) ? 2 : 1];
                flagBearers[0] = athletes.getMapping().get(Long.parseLong(data.getString(firstKey)));

                // A country may have two flag bearers
                if(data.has(secondKey)) {
                    flagBearers[1] = athletes.getMapping().get(Long.parseLong(data.getString(secondKey)));
                }
            }

            CountryBio countryBio = new CountryBio(
                    new NationalAnthem(

                            // Strip alternative name
                            basicData.getString("04#TITLE").split("\\[")[0].trim(),
                            basicData.getString("05#COMPOSER")
                    ),
                    medalStanding,
                    flagBearers == null ? new Athlete[0] : flagBearers,
                    basicData.getString("07#OFFICIALNAME"),
                    Integer.parseInt(basicData.getString("09#FOUNDINGDATE")),
                    Integer.parseInt(basicData.getString("15#FIRSTOG")),
                    Integer.parseInt(basicData.getString("10#DATEIOC")),

                    // Often contains a qualifier e.g [Tokyo 2020 included], strip it
                    Integer.parseInt(basicData.getString("16#NUMBEROFOG").split("\\[")[0].trim())
            );
            country.setBio(countryBio);
        }
    }

    /**
     * Parse the given medal standing JSON data in to an object.
     *
     * @param medalStandingData Medal standing JSON data
     * @return Country medal standing or null
     */
    @Nullable
    private MedalStanding parseCountryMedalStanding(JSONObject medalStandingData) {

        // Country has no medals
        if(medalStandingData.isEmpty()) {
            return null;
        }

        final Country country = countries.getMapping().get(medalStandingData.getString(NOC_KEY));

        return new MedalStanding(
                country,
                new MedalCount(
                        medalStandingData.getInt("b"),
                        medalStandingData.getInt("s"),
                        medalStandingData.getInt("g")
                ),
                medalStandingData.getInt("rank")
        );
    }

    /**
     * Fetch/update the given athlete's bio, this contains their medals, social media, etc.
     * If the athlete has already had their bio fetched, only update their medals (as the other values don't change).
     *
     * @param athlete Athlete to fetch/update bio for
     */
    public void updateAthleteBio(Athlete athlete) {
        final String url = BASE_API_URL + "RMA/olympic/data/CAT/BIO/ATH/" + athlete.getCode() + ".json";
        JSONObject data = new JSONObject(new NetworkRequest(url, false).get().body);

        // Parse medals
        ArrayList<Medal> medals = new ArrayList<>();
        JSONArray medalList = data.getJSONArray("medals");

        for(int i = 0; i < medalList.length(); i++) {
            JSONObject medalData = medalList.getJSONObject(i);
            Sport sport = sports.getMapping().get(medalData.getString("sport"));
            medals.add(
                    new Medal(
                            sport,
                            sport.getEventByCode(medalData.getString("event")),
                            Medal.TYPE.valueOf(medalData.getString(MEDAL_TYPE_KEY).toUpperCase()) // "silver" -> SILVER
                    )
            );
        }

        // Only update medals
        if(athlete.hasBio()) {
            athlete.getBio().setMedals(medals);
        }

        // Parse bio
        else {
            final String socialMediaKey = "socialMedia";

            // Parse social media connections
            SocialMedia[] socialMedia = null;

            if(data.has(socialMediaKey)) {
                JSONArray socialMediaList = data.getJSONArray("socialMedia");
                socialMedia = new SocialMedia[socialMediaList.length()];

                for(int i = 0; i < socialMedia.length; i++) {
                    JSONObject socialMediaData = socialMediaList.getJSONObject(i);
                    socialMedia[i] = new SocialMedia(
                            SocialMedia.PLATFORM.fromName(socialMediaData.getString("code")), // Platform e.g TWITTER
                            socialMediaData.getString("link") // URL to the athlete's profile
                    );
                }
            }

            // Parse basic data
            JSONObject basicData = data.getJSONObject("basicInfo");

            final String weight = basicData.getString("weight");
            final String height = basicData.getString("height");

            final String philosophyKey = "99#PHILOSOPHY";
            final String philosophyQuote = data.has(philosophyKey)
                    ? data.getJSONObject("genInt")
                    .getString(philosophyKey)
                    .split("\\(")[0] // Remove source of quote e.g "quote (source 1), (source 2)..." -> "quote"
                    .trim()
                    : null;

            AthleteBio athleteBio = new AthleteBio(
                    new Biometrics(
                            Biometrics.GENDER_CODE.valueOf(basicData.getString("gender")),
                            parseAthleteBirthDate(basicData.getString("birthDate")),
                            height.isEmpty() || height.equals("-") ? null : Integer.parseInt(height),
                            weight.isEmpty() || weight.equals("-") ? null : Integer.parseInt(weight)
                    ),
                    philosophyQuote,
                    parseAthleteResidenceInfo("residence", basicData),
                    parseAthleteResidenceInfo("birth", basicData),
                    socialMedia == null ? new SocialMedia[0] : socialMedia,
                    medals
            );
            athlete.setBio(athleteBio);
        }
    }

    /**
     * Parse athlete residence info from the given JSON data.
     * This is a country with an optional location within the country.
     *
     * @param baseKey Base key used in the data - e.g "birth" -> "birthCountry" & "birthPlace"
     * @param data    JSON data
     * @return Residence info from data or null (if country unavailable)
     */
    @Nullable
    private ResidenceInfo parseAthleteResidenceInfo(String baseKey, JSONObject data) {
        final Country country = countries.getMapping().get(data.getString(baseKey + "Country"));

        // No country provided
        if(country == null) {
            return null;
        }

        final String location = StringUtils.capitalize(data.getString(baseKey + "Place").toLowerCase());
        return new ResidenceInfo(
                country,
                location.isEmpty() ? null : location
        );
    }

    /**
     * Parse the given date String representing the birth date of an athlete.
     *
     * @param dateString Athlete birth date String
     * @return Local date from date String or today's date (if unable to parse)
     */
    private LocalDate parseAthleteBirthDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay().toLocalDate();
        }
        catch(DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    /**
     * Change the athlete name given by the API to first name last name format and correct the capitalisation
     * The API gives names in the format LASTNAME firstname.
     * E.g:
     * Dave Dobbyn = DOBBYN Dave
     * Rien van der Schaft = van der SCHAFT Rien
     *
     * @param name API formatted name e.g "DOBBYN Dave"
     * @return Name formatted to firstname lastname e.g "Dave Dobbyn"
     */
    public String formatAthleteName(String name) {

        /*
         * ".+[A-Z.']" matches all up until the final capital letter (optionally followed by a period/apostrophe).
         *  E.g
         * "DOBBYN Dave" -> "[DOBBYN D]ave"
         * "BERRE' Enrico" -> [BERRE'] Enrico
         * "(?= )" is a positive lookahead which matches on a space character without including it in the result.
         * The combined regex matches anything before the final occurrence of a space character which follows a capital.
         * E.g:
         * "DOBBYN Dave" -> "[DOBBYN] Dave"
         * "van der SCHAFT Rien" -> "[van der SCHAFT] Rien"
         * "KHAMIS AL-NASAWI Saeeda" -> "[KHAMIS AL-NASAWI] Saeeda"
         */
        final String regex = ".+[A-Z.'](?= )";

        Matcher matcher = Pattern.compile(regex).matcher(name);

        if(matcher.find()) {
            final int lastNamesEnd = matcher.end();
            String lastNames = name.substring(matcher.start(), lastNamesEnd);
            String firstNames = name.substring(lastNamesEnd + 1); // Exclude space before first names
            name = firstNames + " " + lastNames;
        }

        // Single name - e.g "SUMIT" or two names with non capitalised last name e.g "los Urszula" = "Urszula Los"
        else {
            String[] args = name.split(" ");
            if(args.length == 2) {
                name = args[1] + " " + args[0];
            }
        }
        return fixCapitalisation(name);
    }

    /**
     * Fix the capitalisation of the given name.
     *
     * @param name Name - e.g Dave DOBBYN
     * @return Name with fixed capitalisation e.g Dave Dobbyn
     */
    private String fixCapitalisation(String name) {
        String[] args = name.split(" ");

        for(int i = 0; i < args.length; i++) {
            args[i] = StringUtils.capitalize(args[i].toLowerCase());
        }

        return StringUtils.join(args, " ");
    }

    /**
     * Fetch the current medal standings. This is a list of medal standings, each containing a country and
     * their medals, as well as their current rank amongst all countries.
     *
     * @return List of medal Standings
     */
    public ArrayList<MedalStanding> fetchMedalStandings() {
        ArrayList<MedalStanding> medalStandings = new ArrayList<>();

        final String url = BASE_API_URL + "RMA/OG20D/data/CAT/medalStandings.json";
        JSONArray medalStandingList = new JSONObject(
                new NetworkRequest(url, false).get().body
        ).getJSONArray("total");

        for(int i = 0; i < medalStandingList.length(); i++) {
            medalStandings.add(parseCountryMedalStanding(medalStandingList.getJSONObject(i)));
        }

        return medalStandings;
    }

    /**
     * Fetch a list of athletes which have been awarded medals, and the total count of these medals.
     * Athletes with no medals are not included.
     *
     * @return List of medalists
     */
    public ArrayList<AthleteMedalCount> fetchAthleteMedalists() {
        HashMap<Long, AthleteMedalCount> medalsByAthleteId = new HashMap<>();

        final String url = BASE_API_URL + "RMA/olympic/data/CAT/medalists.json";

        // List of medals awarded, contains the athlete ID and medal type
        JSONArray medalistList = new JSONObject(
                new NetworkRequest(url, false).get().body
        ).getJSONArray("list");

        for(int i = 0; i < medalistList.length(); i++) {
            JSONObject medalistData = medalistList.getJSONObject(i);
            final long athleteId = Long.parseLong(medalistData.getString("id"));

            AthleteMedalCount athleteMedalCount = medalsByAthleteId.get(athleteId);

            // Initialise an empty medal count when encountering an athlete for the first time
            if(athleteMedalCount == null) {
                athleteMedalCount = new AthleteMedalCount(
                        athletes.getMapping().get(athleteId),
                        new MedalCount()
                );
                medalsByAthleteId.put(athleteId, athleteMedalCount);
            }

            // Medal type - e.g "silver" -> SILVER
            final Medal.TYPE type = Medal.TYPE.valueOf(medalistData.getString(MEDAL_TYPE_KEY).toUpperCase());

            // Increment the athlete's count of this type of medal
            athleteMedalCount.getMedalCount().addMedalType(type);
        }
        return new ArrayList<>(medalsByAthleteId.values());
    }
}
