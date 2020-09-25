package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Weather.WeatherManager;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Get the weather for a NZ location
 */
public class WeatherCommand extends DiscordCommand {
    WeatherManager weatherManager;

    public WeatherCommand() {
        super("weather [NZ location]\nweather [NZ location] -t", "Get the weather!");
    }

    @Override
    public void execute(CommandContext context) {
        String message = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();
        if(!message.startsWith("weather ")) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        message = message.replace("weather ", "");
        boolean tomorrow = false;
        if(message.contains(" -t")) {
            tomorrow = true;
            message = message.replace(" -t", "");
        }
        if(weatherManager == null) {
            weatherManager = new WeatherManager(context.getEmoteHelper());
        }
        channel.sendMessage(weatherManager.getForecast(message, tomorrow, getHelpName().replaceAll("\n", " | "))).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("weather");
    }
}
