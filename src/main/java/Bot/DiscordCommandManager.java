package Bot;

import Audio.DiscordAudioPlayer;
import COD.CWManager;
import COD.MWManager;
import Command.Commands.*;
import Command.Commands.Audio.*;
import Command.Commands.COD.CWCountdownCommand;
import Command.Commands.COD.MWDataCommand;
import Command.Commands.COD.MWRandomCommand;
import Command.Commands.ExecuteOrder.ExecuteOrder66Command;
import Command.Commands.ExecuteOrder.KillListCommand;
import Command.Commands.Link.*;
import Command.Commands.Lookup.*;
import Command.Commands.Passive.*;
import Command.Commands.JSON.*;
import Command.Commands.Runescape.OSRSPollCommand;
import Command.Commands.Runescape.TrailblazerCommand;
import Command.Commands.Variable.*;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmoteHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Hold a list of commands and handle incoming text input to check for command triggers
 */
public class DiscordCommandManager {
    private final ArrayList<DiscordCommand> commands, viewableCommands;
    private final HashMap<Guild, DiscordAudioPlayer> audioPlayers = new HashMap<>();
    public static final MWManager mwAssetManager = new MWManager();
    public static final CWManager cwManager = new CWManager();
    private EmoteHelper emoteHelper;

    /**
     * Add the commands to the list
     */
    public DiscordCommandManager() {
        FontManager.initialiseFonts();
        this.commands = new ArrayList<>();
        this.viewableCommands = new ArrayList<>();
        addCommands();
    }

    /**
     * Check a given query against the matches method of each command
     *
     * @param message Message from chat
     * @return Command if found or null
     */
    private DiscordCommand getCommand(Message message) {
        String query = message.getContentRaw().toLowerCase();
        for(DiscordCommand c : commands) {
            if(c.matches(query, message)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Randomly select the given number of commands
     *
     * @param bound How many commands to return
     * @return An array of random commands
     */
    public DiscordCommand[] pickRandomCommands(int bound) {
        DiscordCommand[] commands = new DiscordCommand[bound];
        ArrayList<DiscordCommand> seen = new ArrayList<>();
        Random rand = new Random();
        for(int i = 0; i < bound; i++) {
            DiscordCommand j = this.viewableCommands.get(rand.nextInt(this.viewableCommands.size()));
            while(seen.contains(j)) {
                j = this.viewableCommands.get(rand.nextInt(this.viewableCommands.size()));
            }
            commands[i] = j;
            seen.add(j);
        }
        return commands;
    }

    /**
     * Get the list of commands
     *
     * @return List of commands
     */
    public ArrayList<DiscordCommand> getCommands() {
        return commands;
    }

    /**
     * Add a command to the list and alert if the command is a duplicate
     *
     * @param c Command to be added
     */
    private void addCommand(DiscordCommand c) {
        if(commands.contains(c)) {
            System.out.println("Duplicate command: " + c.getHelpName());
            return;
        }
        commands.add(c);
        if(!c.isSecret()) {
            viewableCommands.add(c);
        }
    }

    /**
     * Get the command from a message event and execute it
     *
     * @param event MessageReceivedEvent
     */
    public void handleCommand(GuildMessageReceivedEvent event) {
        DiscordCommand command = getCommand(event.getMessage());
        if(command == null || event.getAuthor().isBot() && !command.acceptsBotInput()) {
            return;
        }
        DiscordAudioPlayer player = audioPlayers.get(event.getGuild());
        if(player == null) {
            player = new DiscordAudioPlayer();
            audioPlayers.put(event.getGuild(), player);
        }
        if(emoteHelper == null) {
            emoteHelper = new EmoteHelper(event.getJDA());
        }
        command.execute(new CommandContext(event, viewableCommands, player, emoteHelper));
    }

    /**
     * Add all of the commands to the list
     */
    private void addCommands() {
        addRandomCommands();
        addLinkCommands();
        addCommand(new GunfightCommand());
        addCommand(new GunfightHelpCommand());
        addCommand(new LeaderboardCommand());
        addCommand(new MeCommand());
        addCommand(new OSRSLookupCommand());
        addCommand(new GhostCommand());
        addCommand(new SawCommand());
        addCommand(new TTSCommand());
        addCommand(new SurvivorCommand());
        addCommand(new PlayYoutubeCommand());
        addCommand(new MakeAChoiceCommand());
        addCommand(new InviteCommand());
        addCommand(new ExecuteOrder66Command());
        addCommand(new KillListCommand());
        addCommand(new TobinCommand());
        addCommand(new MWStatsCommand());
        addCommand(new HelpCommand());
        addCommand(new LOLLookupCommand());
        addCommand(new YoutubeLookupCommand());
        addCommand(new ImpersonateCommand());
        addCommand(new TweetCommand());
        addCommand(new TweetsCommand());
        addCommand(new PlexCommand());
        addCommand(new VoiceChannelCommand());
        addCommand(new OSRSPollCommand());
        addCommand(new CWCountdownCommand());
        addCommand(new KnockCommand());
        addCommand(new RS3LookupCommand());
        addCommand(new WeatherCommand());
        addCommand(new SteakCommand());
        addCommand(new UrbanDictionaryCommand());
        addCommand(new StoryTimeCommand());
        addCommand(new BlitzCommand());
        addCommand(new HangmanCommand());
        addCommand(new FBICommand());
        addCommand(new MillionaireCommand());
        addCommand(new TrailblazerCommand());
        addCommand(new MWHistoryCommand());
        addCommand(new CWHistoryCommand());
        addCommand(new DealOrNoDealCommand());
        addCommand(new JokeCommand());
        addCommand(new HubCommand());
        addCommand(new XKCDCommand());
        addCommand(new RiddleCommand());
        addCommand(new YuGiOhCommand());
        addCommand(new NASACommand());
        addCommand(new PollCommand());
        addCommand(new StatusCommand());
        addCommand(new GIFCommand());
        addCommand(new TeamTreesGuessingCommand());
        addCommand(new StocksCommand());
        addCommand(new MWDataCommand());
        addCommand(new TFTLookupCommand());
        addCommand(new TTVLookupCommand());
        addCommand(new PhasmophobiaCommand());
        addCommand(new MWRandomCommand());
    }

    /**
     * Commands which return a random element from a list
     */
    private void addRandomCommands() {
        addCommand(new MemeCommand());
        addCommand(new GenerateNameCommand());
        addCommand(new LeoCommand());
        addCommand(new PhotographyCommand());
    }

    /**
     * Commands which return a single image
     */
    private void addLinkCommands() {
        addCommand(new BrewsCommand());
        addCommand(new DobroCommand());
        addCommand(new ExodiaCommand());
        addCommand(new FriendlyCommand());
        addCommand(new GothCommand());
        addCommand(new HagridCommand());
        addCommand(new MandrillCommand());
        addCommand(new MegaCommand());
        addCommand(new PureCommand());
        addCommand(new RememberCommand());
        addCommand(new BrewTrackerCommand());
        addCommand(new ShrekCommand());
        addCommand(new SmirnyCommand());
        addCommand(new SpidermanCommand());
        addCommand(new TrulyCommand());
        addCommand(new UnderbellyCommand());
        addCommand(new VapeCommand());

        addCommand(new FishingCommand());
        addCommand(new NormanCommand());
        addCommand(new PhotoshopCommand());
        addCommand(new RaidsCommand());
        addCommand(new RestingCommand());
        addCommand(new RiggsCommand());
        addCommand(new SleepingCommand());
        addCommand(new TurboCommand());
        addCommand(new TwitchCommand());
        addCommand(new VegetableCommand());
        addCommand(new VodkaCommand());
    }
}
