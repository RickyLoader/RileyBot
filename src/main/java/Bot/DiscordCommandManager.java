package Bot;

import Audio.DiscordAudioPlayer;
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
import Command.Structure.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hold a list of commands and handle incoming text input to check for command triggers
 */
public class DiscordCommandManager {
    private final ArrayList<DiscordCommand> commands, viewableCommands;
    private final ArrayList<OnReadyDiscordCommand> onReadyCommands;
    private final HashMap<Guild, DiscordAudioPlayer> audioPlayers = new HashMap<>();
    public EmoteHelper emoteHelper;

    /**
     * Add the commands to the list
     */
    public DiscordCommandManager() {
        FontManager.initialiseFonts();
        this.commands = new ArrayList<>();
        this.viewableCommands = new ArrayList<>();
        this.onReadyCommands = new ArrayList<>();
        addCommands();
    }

    /**
     * Create the emote helper and pass the JDA to all OnReadyDiscordCommands
     *
     * @param jda Discord API
     */
    public void onReady(JDA jda) {
        this.emoteHelper = new EmoteHelper(jda);
        for(OnReadyDiscordCommand command : onReadyCommands) {
            command.onReady(jda, emoteHelper);
        }
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
        if(c instanceof OnReadyDiscordCommand) {
            onReadyCommands.add((OnReadyDiscordCommand) c);
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
        command.execute(new CommandContext(event, viewableCommands, player, emoteHelper));
    }

    /**
     * Add all of the commands to the list
     */
    private void addCommands() {
        addRandomCommands();
        addLinkCommands();
        addCommand(new TrademeCommand());
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
        addCommand(new PollCommand());
        addCommand(new StatusCommand());
        addCommand(new GIFCommand());
        addCommand(new StocksCommand());
        addCommand(new MWDataCommand());
        addCommand(new TFTLookupCommand());
        addCommand(new TTVLookupCommand());
        addCommand(new PhasmophobiaCommand());
        addCommand(new MWRandomCommand());
        addCommand(new ValheimWikiCommand());
        addCommand(new GrandExchangeCommand());
        addCommand(new ValheimServerCommand());
        addCommand(new RedditCommand());
        addCommand(new SteamGameCommand());
        addCommand(new SteamCommand());
        addCommand(new HotTubCommand());
        addCommand(new RSPlayerCountCommand());
        addCommand(new DictionaryCommand());
        addCommand(new OSRSLendingCommand());
        addCommand(new VapouriumCommand());
        addCommand(new VapoCommand());
        addCommand(new AlphaVapeCommand());
        /* TODO
            addCommand(new FacebookCommand());
            addCommand(new TeamTreesGuessingCommand());
            addCommand(new NASACommand());
         */
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
        // addCommand(new VapeCommand());

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
