package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Weather.WeatherManager;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Get the weather for a NZ location
 */
public class WeatherCommand extends DiscordCommand {
    private WeatherManager weatherManager;

    public WeatherCommand() {
        super("weather\nweather [NZ location]\nweather [NZ location] -t", "Get the weather!");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();
        String help = getHelpName().replaceAll("\n", " | ");

        if(!message.equals("weather") && !message.startsWith("weather ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        if(weatherManager == null) {
            weatherManager = new WeatherManager(context.getEmoteHelper());
        }

        if(message.equals("weather")) {
            channel.sendMessage(weatherManager.getExtremes(help)).queue();
            return;
        }

        message = message.replace("weather ", "");
        boolean tomorrow = false;
        if(message.contains(" -t")) {
            tomorrow = true;
            message = message.replace(" -t", "");
        }

        channel.sendMessage(weatherManager.getForecast(message, tomorrow, help)).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("weather");
    }
}
