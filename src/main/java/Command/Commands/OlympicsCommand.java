package Command.Commands;

import Command.Structure.*;
import Olympics.*;
import Olympics.Athlete.*;
import Olympics.Country.Country;
import Olympics.Country.CountryBio;
import Olympics.Country.NationalAnthem;
import Olympics.Medal.Medal;
import Olympics.Medal.MedalCount;
import Olympics.Medal.MedalStanding;
import Olympics.Sport.Sport;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static Command.Structure.PageableTableEmbed.MAX_COLUMNS;

/**
 * 2020 Tokyo Olympics - view athletes, medals, etc
 */
public class OlympicsCommand extends OnReadyDiscordCommand {
    private final Olympics olympics;

    private String
            heightEmote,
            weightEmote,
            facebookEmote,
            instagramEmote,
            twitterEmote,
            websiteEmote,
            unknownEmote,
            bronzeEmote,
            silverEmote,
            goldEmote,
            totalEmote,
            medalRankEmote,
            blankEmote;

    private static final String
            TRIGGER = "tokyo",
            ALL = "all",
            ATHLETES_TITLE = "ATHLETES",
            COUNTRIES_TITLE = "COUNTRIES",
            ATHLETE_ARG = "-a",
            COUNTRY_ARG = "-c",
            MEDALS_ARG = "medals",
            ATHLETE_ARGS = TRIGGER + " " + ATHLETE_ARG,
            ATHLETE_FOOTER = "Type: " + ATHLETE_ARGS + " for help",
            ATHLETE_SEARCH_HELP = ATHLETE_ARGS + " [athlete name/id]",
            ATHLETES_BY_COUNTRY_ARGS = ATHLETE_ARGS + " " + COUNTRY_ARG,
            ATHLETES_BY_COUNTRY_HELP = ATHLETES_BY_COUNTRY_ARGS + " [country code]",
            ATHLETE_MEDALS_HELP = ATHLETE_ARGS + " " + MEDALS_ARG,
            COUNTRY_ARGS = TRIGGER + " " + COUNTRY_ARG,
            COUNTRY_FOOTER = "Type: " + COUNTRY_ARGS + " for help",
            COUNTRY_SEARCH_HELP = COUNTRY_ARGS + " [country name/code/" + ALL + "]",
            COUNTRY_MEDALS_HELP = COUNTRY_ARGS + " " + MEDALS_ARG;

    /**
     * Initialise Olympic data
     */
    public OlympicsCommand() {
        super(
                TRIGGER,
                "Cool Tokyo Olympics stuff",
                ATHLETE_ARGS + " (" + ATHLETES_TITLE + ")\n" + COUNTRY_ARGS + " (" + COUNTRIES_TITLE + ")"
        );
        this.olympics = new Olympics();
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();

        // No args given
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // -a, -c etc
        final String arg = query.split(" ")[0].trim();

        query = query.replaceFirst(arg, "").trim();

        switch(arg) {
            case COUNTRY_ARG:
                handleCountryQuery(context, query);
                return;
            case ATHLETE_ARG:
                handleAthleteQuery(context, query);
                return;
            default:
                channel.sendMessage(getHelpNameCoded()).queue();
        }
    }

    /**
     * Handle a country query. Display either a country found for the given query or a pageable message with the
     * search results.
     *
     * @param context Command context
     * @param query   Country name/code query
     */
    private void handleCountryQuery(CommandContext context, String query) {
        MessageChannel channel = context.getMessageChannel();
        String helpMessage = "```\n"
                + "COUNTRIES\n\n"
                + "Search for a country:\n\n"
                + COUNTRY_SEARCH_HELP
                + "\n\n\tExamples:\n"
                + "\t\tSearch:\n"
                + "\t\t\t" + COUNTRY_ARGS + " new zealand"
                + "\n\t\t\t" + COUNTRY_ARGS + " nzl"
                + "\n\n\t\tView all countries:\n"
                + "\t\t\t" + COUNTRY_ARGS + " " + ALL
                + "\n\nView medals by country:\n\n"
                + COUNTRY_MEDALS_HELP
                + "```";

        // Send country help message
        if(query.isEmpty()) {
            channel.sendMessage(helpMessage).queue();
            return;
        }


        // View all countries
        if(query.equals(ALL)) {
            showCountries(context, olympics.getCountries().getAllValues(), "All Countries");
            return;
        }

        // View country medal standings
        if(query.equals(MEDALS_ARG)) {
            channel.sendTyping().queue();
            showMedalStandings(context, olympics.fetchMedalStandings());
        }

        // Search countries by name/code
        else {
            Country country = olympics.getCountries().getMapping().get(query.toUpperCase());

            // Not a country code
            if(country == null) {
                ArrayList<Country> countries = olympics.getCountries().getValuesByName(query);

                // Single result
                if(countries.size() == 1) {
                    country = countries.get(0);
                }

                // Show search results
                else {
                    showCountries(context, countries, "Country Search: " + query, query);
                    return;
                }
            }

            channel.sendTyping().queue();

            // Make a request to get/update the country bio (includes medal standing)
            olympics.updateCountryBio(country);

            channel.sendMessage(buildCountryEmbed(country)).queue();
        }
    }

    /**
     * Handle an athlete query. Display either an athlete found for the given query or a pageable message with the
     * search results.
     *
     * @param context Command context
     * @param query   Athlete name/id query
     */
    private void handleAthleteQuery(CommandContext context, String query) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        String helpMessage = "```\n"
                + "ATHLETES\n\n"
                + "Search for an athlete:\n\n"
                + ATHLETE_SEARCH_HELP
                + "\n\n\tExamples:\n"
                + "\t\t" + ATHLETE_ARGS + " valerie adams"
                + "\n\t\t" + ATHLETE_ARGS + " 1465747"
                + "\n\n"
                + "View ALL athletes for a country code:\n\n"
                + ATHLETES_BY_COUNTRY_HELP
                + "\n\n\tExamples:\n"
                + "\t\t" + ATHLETES_BY_COUNTRY_ARGS + " NZL"
                + "\n\nView medalists:\n\n"
                + ATHLETE_MEDALS_HELP
                + "```";

        // Send athlete help message
        if(query.isEmpty()) {
            channel.sendMessage(helpMessage).queue();
            return;
        }

        // View all athletes for a country
        if(query.startsWith(COUNTRY_ARG)) {
            query = query.replaceFirst(COUNTRY_ARG, "").trim().toUpperCase(); // e.g NZL

            // No country code provided
            if(query.isEmpty()) {
                channel.sendMessage(helpMessage).queue();
                return;
            }

            Country country = olympics.getCountries().getMapping().get(query);

            // Not a country code
            if(country == null) {
                channel.sendMessage(
                        member.getAsMention()
                                + " There are no countries with that code!\n\n"
                                + "Make sure you're using a country code (`NZL`), **not** a name (`New Zealand`)."
                ).queue();
                return;
            }

            ArrayList<Athlete> athletes = olympics.getAthletes().getByCountry(country);
            showAthletes(context, athletes, country.getSummary() + " Athletes");
        }

        // View medalists
        else if(query.equals(MEDALS_ARG)) {
            channel.sendTyping().queue();
            showMedalists(context, olympics.fetchAthleteMedalists());
        }

        // Search athletes by name/ID
        else {

            long id = toLong(query);
            Athlete athlete;

            // Not an ID
            if(id == 0) {
                ArrayList<Athlete> athletes = olympics.getAthletes().getValuesByName(query);
                if(athletes.size() == 1) {
                    athlete = athletes.get(0);
                }

                // Display search results
                else {
                    showAthletes(context, athletes, "Athlete Search: " + query, query);
                    return;
                }
            }
            else {
                athlete = olympics.getAthletes().getMapping().get(id);
                if(athlete == null) {
                    channel.sendMessage(
                            member.getAsMention() + " There are no athletes with that ID!"
                    ).queue();
                    return;
                }
            }

            channel.sendTyping().queue();

            // Make a request to get/update the athlete's bio
            olympics.updateAthleteBio(athlete);

            channel.sendMessage(buildAthleteEmbed(athlete)).queue();
        }
    }

    /**
     * Build a message embed detailing the given Olympic country.
     * Display name, id, flag, medals, etc.
     *
     * @param country Olympic country to display in message embed
     * @return Message embed displaying country
     */
    private MessageEmbed buildCountryEmbed(Country country) {
        CountryBio bio = country.getBio();
        NationalAnthem nationalAnthem = bio.getNationalAnthem();

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedHelper.ORANGE)
                .setTitle(country.getSummary())
                .setThumbnail(Olympics.LOGO)
                .setFooter(COUNTRY_FOOTER)
                .setImage(country.getFlagImageUrl())
                .setDescription(
                        "**Committee**: " + bio.getCommitteeName() + " (" + bio.getCommitteeFoundedYear() + ")"
                )
                .addField(
                        "Joined IOC",
                        String.valueOf(bio.getJoinedIOCYear()),
                        true
                )
                .addField("First Games", String.valueOf(bio.getFirstGamesYear()), true)
                .addField("Total Games (Inclusive)", String.valueOf(bio.getTotalGames()), true)
                .addField(
                        "National Anthem",
                        nationalAnthem.getTitle(),
                        true
                )
                .addBlankField(true)
                .addBlankField(true); // Round out to 3 columns before adding optional fields

        // Add flag bearers
        if(bio.hasFlagBearers()) {
            Athlete[] flagBearers = bio.getFlagBearers();
            StringBuilder flagBearerDisplay = new StringBuilder();

            for(int i = 0; i < flagBearers.length; i++) {
                flagBearerDisplay
                        .append(i + 1)
                        .append(". ")
                        .append(flagBearers[i].getSummary());

                // Add new line on all but last
                if(i < flagBearers.length - 1) {
                    flagBearerDisplay.append("\n");
                }
            }
            builder.addField("Flag Bearer(s)", flagBearerDisplay.toString(), true);
        }

        MedalStanding medalStanding = bio.getMedalStanding();

        // Add country medal standing
        if(bio.hasMedalStanding()) {
            MedalCount medalCount = medalStanding.getMedalCount();
            String medalStandingSummary =
                    bronzeEmote + " " + medalCount.getBronzeMedals()
                            + blankEmote + medalRankEmote + " " + medalStanding.getRank()
                            + "\n" + silverEmote + " " + medalCount.getSilverMedals()
                            + "\n" + goldEmote + " " + medalCount.getGoldMedals()
                            + "\n" + totalEmote + " " + medalCount.getTotalMedals();
            builder.addField("Medal Standing", medalStandingSummary, true);
        }
        return builder.build();
    }

    /**
     * Build a message embed detailing the given athlete.
     * Display sports, medals, country of origin etc.
     *
     * @param athlete Athlete to display in message embed
     * @return Message embed displaying athlete
     */
    private MessageEmbed buildAthleteEmbed(Athlete athlete) {
        AthleteBio bio = athlete.getBio();
        Biometrics biometrics = bio.getBiometrics();
        Country country = athlete.getCountry();
        Sport[] sports = athlete.getSports();

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(biometrics.getGenderCode() == Biometrics.GENDER_CODE.M ? EmbedHelper.BLUE : EmbedHelper.PURPLE)
                .setTitle(athlete.getSummary())
                .setFooter(
                        ATHLETE_FOOTER,
                        sports[0].getIconImageUrl() // Show icon of first sport
                )
                .setThumbnail(Olympics.LOGO)
                .setAuthor(
                        country.getName() + " (" + country.getCode() + ")",
                        null, // No relevant URL to display for a country
                        country.getFlagImageUrl()
                )
                .setImage(athlete.getImageUrl())
                .addField(
                        "Birth Date",
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                .format(biometrics.getBirthDate()) + " (" + biometrics.getAge() + ")",
                        true
                )
                .addField("Gender", biometrics.getGenderCode().getName(), true)
                .addBlankField(true); // Round out to 3 columns before adding optional fields

        // Optional fields

        // Quote about the athlete's philosophy
        if(bio.hasPhilosophyQuote()) {
            builder.setDescription("**Philosophy**: " + bio.getPhilosophyQuote().replaceAll("\"", "*\""));
        }

        // Athlete's birth residence
        if(bio.hasBirthResidence()) {
            builder.addField(getResidenceField("Birth Residence", bio.getBirthResidence()));
        }

        // Athlete's current residence
        if(bio.hasCurrentResidence()) {
            builder.addField(getResidenceField("Current Residence", bio.getCurrentResidence()));
        }

        // Athlete's social media platforms
        if(bio.hasSocialMedia()) {
            SocialMedia[] socialMediaList = bio.getSocialMedia();
            StringBuilder socialMediaDisplay = new StringBuilder();

            for(int i = 0; i < socialMediaList.length; i++) {
                SocialMedia socialMedia = socialMediaList[i];
                socialMediaDisplay
                        .append(getSocialMediaEmote(socialMedia.getPlatform()))
                        .append(" ")
                        .append(EmbedHelper.embedURL("View", socialMedia.getUrl()));

                // Add new line on all but last
                if(i < socialMediaList.length - 1) {
                    socialMediaDisplay.append("\n");
                }
            }
            builder.addField("Social Media", socialMediaDisplay.toString(), true);
        }

        // Athlete height/weight measurements
        if(biometrics.hasMeasurements()) {
            String measurements = "";

            if(biometrics.hasHeight()) {
                measurements += heightEmote + " " + biometrics.getHeight() + "cm";
            }

            if(biometrics.hasWeight()) {
                measurements += "\n" + weightEmote + " " + biometrics.getWeight() + "kg";
            }

            builder.addField("Measurements", measurements, true);
        }

        // Pad the fields out for each row to be equal before adding medals/sports
        final int paddingRequired = MAX_COLUMNS - (builder.getFields().size() % MAX_COLUMNS);
        if(paddingRequired != MAX_COLUMNS) {
            for(int i = 0; i < paddingRequired; i++) {
                builder.addBlankField(true);
            }
        }

        // Add athlete sports
        StringBuilder sportsDisplay = new StringBuilder();
        for(int i = 0; i < sports.length; i++) {
            sportsDisplay.append(sports[i].getName());

            // Add new line on all but last
            if(i < sports.length - 1) {
                sportsDisplay.append("\n");
            }
        }
        builder.addField("Sports", sportsDisplay.toString(), true);

        // Athlete medals
        if(bio.hasMedals()) {
            ArrayList<Medal> medals = bio.getMedals();
            StringBuilder medalDisplay = new StringBuilder();

            for(int i = 0; i < medals.size(); i++) {
                Medal medal = medals.get(i);
                medalDisplay
                        .append(getMedalEmote(medal.getType()))
                        .append(" ")
                        .append(medal.getEvent().getName());

                // Add new line on all but last
                if(i < medals.size() - 1) {
                    medalDisplay.append("\n");
                }
            }
            builder.addField("Medals", medalDisplay.toString(), true);
        }
        return builder.build();
    }

    /**
     * Create a field for the given residence info. Display as an inline field with value "location,\ncountry name".
     *
     * @param title         Title to use for field - e.g "Birth residence"
     * @param residenceInfo Residence info to display
     * @return Field displaying residence info
     */
    private MessageEmbed.Field getResidenceField(String title, ResidenceInfo residenceInfo) {
        String value = residenceInfo.getCountry().getSummary();

        // Prepend country with location within country
        if(residenceInfo.hasLocation()) {
            value = residenceInfo.getLocation() + ",\n" + value;
        }

        return new MessageEmbed.Field(
                title,
                value,
                true
        );
    }

    /**
     * Display the given list of countries in a pageable message embed.
     * Show name and code, sort by either most similar to the given query (if provided) or alphabetically.
     *
     * @param context   Command context
     * @param countries List of countries
     * @param title     Title to use for the embed
     * @param query     Optional query used to find countries
     */
    private void showCountries(CommandContext context, ArrayList<Country> countries, String title, String... query) {
        new PageableTableEmbed<Country>(
                context,
                countries,
                Olympics.LOGO,
                title,
                null,
                COUNTRY_FOOTER,
                new String[]{
                        "Code",
                        "Name"
                },
                5
        ) {
            @Override
            public String getNoItemsDescription() {
                return "Nothing found, " + COUNTRY_FOOTER;
            }

            @Override
            public String[] getRowValues(int index, Country country, boolean defaultSort) {
                return new String[]{country.getCode(), country.getName()};
            }

            @Override
            public void sortItems(List<Country> items, boolean defaultSort) {
                sortOlympicItems(items, defaultSort, query);
            }
        }.showMessage();
    }

    /**
     * Sort a list of Olympic data items by either similarity of the name to an optional search query,
     * or alphabetically by name if not provided.
     *
     * @param items       Items to sort
     * @param defaultSort Sort in ascending order
     * @param query       Optional search query
     * @param <T>         Type of Olympic data to sort
     */
    private <T extends OlympicData> void sortOlympicItems(List<T> items, boolean defaultSort, String... query) {

        // Sort alphabetically
        if(query.length == 0) {
            items.sort((o1, o2) -> {
                String n1 = o1.getName();
                String n2 = o2.getName();
                return defaultSort ? n1.compareTo(n2) : n2.compareTo(n1);
            });
        }

        // Sort by similarity to search query
        else {
            final String searchQuery = query[0];
            items.sort(new LevenshteinDistance<T>(searchQuery, defaultSort) {
                @Override
                public String getString(T data) {
                    return data.getName();
                }
            });
        }
    }

    /**
     * Display the given list of athletes in a pageable message embed.
     * Show name, id, and country. Sort by either most similar to the given query (if provided) or alphabetically.
     *
     * @param context  Command context
     * @param title    Title to use for the embed
     * @param athletes List of athletes
     * @param query    Optional query used to find athletes
     */
    private void showAthletes(CommandContext context, ArrayList<Athlete> athletes, String title, String... query) {
        new PageableTableEmbed<Athlete>(
                context,
                athletes,
                Olympics.LOGO,
                title,
                null,
                ATHLETE_FOOTER,
                new String[]{
                        "ID",
                        "Name",
                        "Country"
                },
                5
        ) {
            @Override
            public String getNoItemsDescription() {
                return "Nothing found, " + ATHLETE_FOOTER;
            }

            @Override
            public String[] getRowValues(int index, Athlete athlete, boolean defaultSort) {
                return new String[]{
                        athlete.getCode(),
                        athlete.getName(),
                        athlete.getCountry().getSummary()
                };
            }

            @Override
            public void sortItems(List<Athlete> items, boolean defaultSort) {
                sortOlympicItems(items, defaultSort, query);
            }
        }.showMessage();
    }

    /**
     * Display a list of medalists. These are athletes who have been awarded medals, and the count of each type of
     * medal that the have been awarded. Sort in descending order of total medals e.g display the most medals first.
     *
     * @param context          Command context
     * @param athleteMedalists List of athletes who have been awarded medals, and the count of each type of medal
     */
    private void showMedalists(CommandContext context, ArrayList<AthleteMedalCount> athleteMedalists) {
        new PageableTableEmbed<AthleteMedalCount>(
                context,
                athleteMedalists,
                Olympics.LOGO,
                "Medalists",
                null,
                ATHLETE_FOOTER,
                new String[]{
                        "Athlete",
                        "Medals",
                        "Country"
                },
                5,
                EmbedHelper.PURPLE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "There are no medalists to display!";
            }

            @Override
            public String[] getRowValues(int index, AthleteMedalCount medalist, boolean defaultSort) {
                Athlete athlete = medalist.getAthlete();
                return new String[]{
                        athlete.getSummary(),
                        buildMedalCountString(medalist.getMedalCount()),
                        athlete.getCountry().getCode()
                };
            }

            @Override
            public void sortItems(List<AthleteMedalCount> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    int t1 = o1.getMedalCount().getTotalMedals();
                    int t2 = o2.getMedalCount().getTotalMedals();
                    return defaultSort ? t2 - t1 : t1 - t2;
                });
            }
        }.showMessage();
    }

    /**
     * Display the current medal standings of countries in a pageable message.
     * Each medal standing contains a country and their medals, as well as their current rank amongst all countries.
     * Sort by ascending rank e.g display rank 1 first.
     *
     * @param context        Command context
     * @param medalStandings List of country medal standings
     */
    private void showMedalStandings(CommandContext context, ArrayList<MedalStanding> medalStandings) {
        new PageableTableEmbed<MedalStanding>(
                context,
                medalStandings,
                Olympics.LOGO,
                "Medal Standings",
                null,
                COUNTRY_FOOTER,
                new String[]{
                        "Country",
                        "Medals",
                        "Rank"
                },
                5,
                EmbedHelper.PURPLE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "Nobody has any medals!";
            }

            @Override
            public String[] getRowValues(int index, MedalStanding medalStanding, boolean defaultSort) {
                return new String[]{
                        medalStanding.getCountry().getSummary(),
                        buildMedalCountString(medalStanding.getMedalCount()),
                        medalRankEmote + " " + medalStanding.getRank()
                };
            }

            @Override
            public void sortItems(List<MedalStanding> standings, boolean defaultSort) {
                standings.sort((o1, o2) -> {
                    int m1 = o1.getRank();
                    int m2 = o2.getRank();
                    return defaultSort ? m1 - m2 : m2 - m1;
                });
            }
        }.showMessage();
    }

    /**
     * Build a String displaying the given medal count for a country/athlete.
     * Display each type of medal (including total) with an appropriate emote beside each horizontally.
     * E.g gold medal emote -> gold medal count -> silver medal emote...
     *
     * @param medalCount Medal count for a country/athlete
     * @return Medal count String
     */
    private String buildMedalCountString(MedalCount medalCount) {
        return goldEmote + " " + medalCount.getGoldMedals()
                + " " + silverEmote + " " + medalCount.getSilverMedals()
                + " " + bronzeEmote + " " + medalCount.getBronzeMedals()
                + " " + totalEmote + " " + medalCount.getTotalMedals();
    }

    /**
     * Get the emote mention String to use for the given medal type
     *
     * @param type Medal type - e.g BRONZE
     * @return Medal emote mention String
     */
    private String getMedalEmote(Medal.TYPE type) {
        switch(type) {
            case GOLD:
                return goldEmote;
            case SILVER:
                return silverEmote;
            // BRONZE
            default:
                return bronzeEmote;
        }
    }

    /**
     * Get the emote mention String to use for the given social media platform
     *
     * @param platform Social media platform
     * @return Platform emote mention String
     */
    private String getSocialMediaEmote(SocialMedia.PLATFORM platform) {
        switch(platform) {
            case TWITTER:
                return twitterEmote;
            case FACEBOOK:
                return facebookEmote;
            case INSTAGRAM:
                return instagramEmote;
            case WEBSITE:
                return websiteEmote;

            // UNKNOWN
            default:
                return unknownEmote;
        }
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {

        // Biometrics
        this.heightEmote = emoteHelper.getHeight().getAsMention();
        this.weightEmote = emoteHelper.getWeight().getAsMention();

        // Social media
        this.facebookEmote = emoteHelper.getFacebookCircle().getAsMention();
        this.instagramEmote = emoteHelper.getInstagram().getAsMention();
        this.twitterEmote = emoteHelper.getTwitter().getAsMention();
        this.websiteEmote = emoteHelper.getWebsite().getAsMention();
        this.unknownEmote = emoteHelper.getUnknown().getAsMention();

        // Medals
        this.bronzeEmote = emoteHelper.getBronze().getAsMention();
        this.silverEmote = emoteHelper.getSilver().getAsMention();
        this.goldEmote = emoteHelper.getGold().getAsMention();
        this.totalEmote = emoteHelper.getTotalMedals().getAsMention();
        this.medalRankEmote = emoteHelper.getMedalRank().getAsMention();
        this.blankEmote = emoteHelper.getBlankGap().getAsMention();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
