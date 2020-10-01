package Bot;

import Audio.DiscordAudioPlayer;
import Command.Commands.*;
import Command.Commands.Audio.*;
import Command.Commands.ExecuteOrder.ExecuteOrder66Command;
import Command.Commands.ExecuteOrder.KillListCommand;
import Command.Commands.Link.*;
import Command.Commands.Lookup.CWCountdownCommand;
import Command.Commands.Lookup.LOLLookupCommand;
import Command.Commands.Lookup.MWLookupCommand;
import Command.Commands.Lookup.YoutubeLookupCommand;
import Command.Commands.Passive.*;
import Command.Commands.JSON.*;
import Command.Commands.Runescape.OSRSLookupCommand;
import Command.Commands.Runescape.RS3LookupCommand;
import Command.Commands.Variable.*;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmoteHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Hold a list of commands and handle incoming text input to check for command triggers
 */
public class DiscordCommandManager {
    private final ArrayList<DiscordCommand> commands = new ArrayList<>();
    private final HashMap<Guild, DiscordAudioPlayer> audioPlayers = new HashMap<>();
    private final EmoteHelper emoteHelper = new EmoteHelper();

    /**
     * Add the commands to the list
     */
    public DiscordCommandManager() {
        addCommands();
    }

    /**
     * Check a given query against the matches method of each command
     *
     * @param query String query from chat
     * @return Command if found or null
     */
    private DiscordCommand getCommand(String query) {
        query = query.toLowerCase();
        for(DiscordCommand c : commands) {
            if(c.matches(query)) {
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
            DiscordCommand j = this.commands.get(rand.nextInt(this.commands.size()));
            while(seen.contains(j)) {
                j = this.commands.get(rand.nextInt(this.commands.size()));
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
    }

    /**
     * Get the command from a message event and execute it
     *
     * @param event MessageReceivedEvent
     */
    public void handleCommand(GuildMessageReceivedEvent event) {
        DiscordCommand command = getCommand(event.getMessage().getContentRaw());
        if(command == null) {
            return;
        }
        DiscordAudioPlayer player = audioPlayers.get(event.getGuild());
        if(player == null) {
            player = new DiscordAudioPlayer();
            audioPlayers.put(event.getGuild(), player);
        }
        if(!emoteHelper.hasGuild()) {
            emoteHelper.setGuild(event.getJDA().getGuildById("421443474391564299"));
        }
        command.execute(new CommandContext(event, commands, player, emoteHelper));
    }

    /**
     * Add all of the commands to the list
     */
    private void addCommands() {
        addRandomCommands();
        addLinkCommands();
        addCommand((new GunfightCommand()));
        addCommand((new GunfightHelpCommand()));
        addCommand((new LeaderboardCommand()));
        addCommand((new MeCommand()));
        addCommand((new OSRSLookupCommand()));
        addCommand((new GhostCommand()));
        addCommand((new SawCommand()));
        addCommand((new TTSCommand()));
        addCommand((new SurvivorCommand()));
        addCommand((new PlayYoutubeCommand()));
        addCommand((new MakeAChoiceCommand()));
        addCommand((new InviteCommand()));
        addCommand((new ExecuteOrder66Command()));
        addCommand((new KillListCommand()));
        addCommand((new TobinCommand()));
        addCommand((new MWLookupCommand()));
        addCommand((new HelpCommand()));
        addCommand((new LOLLookupCommand()));
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
    }

    /**
     * Commands which return a random element from a list
     */
    private void addRandomCommands() {
        addCommand((new MemeCommand()));
        addCommand(new GIFCommand());
        addCommand((new GenerateNameCommand()));
    }

    /**
     * Commands which return a single image
     */
    private void addLinkCommands() {
        addCommand((new BrewsCommand()));
        addCommand((new DobroCommand()));
        addCommand((new ExodiaCommand()));
        addCommand((new FriendlyCommand()));
        addCommand((new GothCommand()));
        addCommand((new HagridCommand()));
        addCommand((new MandrillCommand()));
        addCommand((new MegaCommand()));
        addCommand((new PureCommand()));
        addCommand((new RememberCommand()));
        addCommand((new BrewTrackerCommand()));
        addCommand((new ShrekCommand()));
        addCommand((new SmirnyCommand()));
        addCommand((new SpidermanCommand()));
        addCommand((new TrulyCommand()));
        addCommand((new UnderbellyCommand()));
        addCommand((new VapeCommand()));

        addCommand((new FishingCommand()));
        addCommand((new NormanCommand()));
        addCommand((new PhotoshopCommand()));
        addCommand((new RaidsCommand()));
        addCommand((new RestingCommand()));
        addCommand((new RiggsCommand()));
        addCommand((new SleepingCommand()));
        addCommand((new TurboCommand()));
        addCommand((new TwitchCommand()));
        addCommand((new VegetableCommand()));
        addCommand((new VodkaCommand()));
    }
}
