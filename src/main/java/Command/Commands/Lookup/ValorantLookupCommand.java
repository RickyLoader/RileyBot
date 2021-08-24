package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.ImageLoadingMessage;
import Command.Structure.LookupCommand;
import TrackerGG.ValorantTrackerAPI;
import TrackerGG.ValorantTrackerAPI.PLAYLIST;
import TrackerGG.ValorantTrackerAPI.ValorantTrackerException;
import Valorant.Stats.PlayerStats;
import Valorant.ValorantImageBuilder;
import Valorant.ValorantStatsManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/**
 * View Valorant stats
 */
public class ValorantLookupCommand extends LookupCommand {
    private static final String
            TRIGGER = "vallookup",
            NAME_URL = "https://account.riotgames.com/",
            NAME_HELP = "```Name must be in the format: name#tagline\n\n"
                    + "If they have not set a tagline, it will be their region code (e.g OCE).\n\n"
                    + "Examples:\n\thavockii#1111\n\tthe bread guy#oce\n\n"
                    + "You can view yours/set one at:\n\n" + NAME_URL + "```";

    private static final PLAYLIST DEFAULT_PLAYLIST = PLAYLIST.COMPETITIVE;
    private final ValorantStatsManager statsManager = ValorantStatsManager.getInstance();
    private final ValorantImageBuilder imageBuilder = new ValorantImageBuilder();
    private PLAYLIST playlist;

    public ValorantLookupCommand() {
        super(
                TRIGGER,
                "View some Valorant Stats!",
                "[playlist] " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS
                        + "\n\nIf no playlist is provided, " + DEFAULT_PLAYLIST.getActualName()
                        + " stats will be retrieved."
                        + "\n\nPlaylists:\n" + PLAYLIST.getHelpText(),


                // name#tagline - 16 for name, 5 for tagline, 1 for #
                22
        );
    }

    @Override
    public String stripArguments(String query) {

        // "name of playlist vallookup name of player" -> [name of playlist , name of player]
        final String playlistName = query.split(getTrigger())[0].trim();

        playlist = PLAYLIST.byActualName(playlistName);

        // Not a playlist
        if(playlist == null) {
            playlist = DEFAULT_PLAYLIST;
            return query;
        }

        // "competitive vallookup me" -> "vallookup me"
        return query.replaceFirst(playlist.getActualName().toLowerCase(), "").trim();
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        // Invalid name
        if(!ValorantTrackerAPI.isValidName(name)) {
            channel.sendMessage(NAME_HELP).queue();
            return;
        }

        channel.sendTyping().queue();

        try {
            PlayerStats playerStats = statsManager.fetchStats(name, playlist);
            System.out.println("got stats");
            BufferedImage image = imageBuilder.buildImage(playerStats);
            channel.sendFile(ImageLoadingMessage.imageToByteArray(image), "stats.png").queue();
        }

        // Failed to fetch stats
        catch(ValorantTrackerException e) {
            channel.sendMessage("```" + e.getMessage() + "```").queue();
        }
    }

    @Override
    public @Nullable String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.VALORANT);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        if(!ValorantTrackerAPI.isValidName(name)) {
            channel.sendMessage(NAME_HELP).queue();
            return;
        }
        DiscordUser.saveName(name, DiscordUser.VALORANT, channel, user);
    }

    @Override
    public boolean matches(String query, Message message) {
        final String[] args = query.split(getTrigger());
        return super.matches(query, message) || query.contains(getTrigger()) && PLAYLIST.isPlaylist(args[0].trim());
    }
}
