package Command.Commands;

import Command.Structure.*;
import Weather.Forecast;
import Weather.Location;
import Weather.WeatherManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Get the weather for a NZ location
 */
public class WeatherCommand extends OnReadyDiscordCommand {
    private static final String
            LOCATION = "[NZ location]",
            TRIGGER = "weather",
            EXTREMES = "extremes",
            TOMORROW = "-t";
    private final String helpText;
    private WeatherManager weatherManager;

    public WeatherCommand() {
        super(
                TRIGGER,
                "Get the weather!",
                TRIGGER + " " + LOCATION + "\n"
                        + TRIGGER + " " + LOCATION + " " + TOMORROW + "\n"
                        + TRIGGER + " " + EXTREMES
        );
        this.helpText = "Type: " + getTrigger() + " for help";
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.weatherManager = new WeatherManager(emoteHelper, helpText);
    }

    @Override
    public void execute(CommandContext context) {
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();
        MessageChannel channel = context.getMessageChannel();

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        channel.sendTyping().queue();

        if(query.equals(EXTREMES)) {
            channel.sendMessage(weatherManager.getExtremeWeatherEmbed()).queue();
            return;
        }

        boolean tomorrow = false;
        if(query.contains(TOMORROW)) {
            tomorrow = true;
            query = query.replaceFirst(TOMORROW, "").trim();
        }

        ArrayList<Location> locations = weatherManager.searchLocations(query);

        if(locations.isEmpty()) {
            channel.sendMessage(weatherManager.getFailedLookupEmbed(query)).queue();
            return;
        }

        String finalQuery = query;

        // Filter by exact name
        ArrayList<Location> filtered = locations
                .stream()
                .filter(location -> location.getName().equalsIgnoreCase(finalQuery))
                .collect(Collectors.toCollection(ArrayList::new));

        Location toDisplay = filtered.isEmpty() ? locations.get(0) : filtered.get(0);
        Forecast forecast = weatherManager.getForecast(toDisplay, tomorrow);

        if(forecast == null) {
            channel.sendMessage(weatherManager.getNoForecastEmbed(toDisplay)).queue();
            return;
        }

        channel.sendMessage(weatherManager.buildForecastEmbed(toDisplay, forecast, tomorrow)).queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
