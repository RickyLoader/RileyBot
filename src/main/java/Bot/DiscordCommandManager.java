package Bot;

import Command.Commands.*;
import Command.Commands.Audio.*;
import Command.Commands.ExecuteOrder.ExecuteOrder66Command;
import Command.Commands.ExecuteOrder.KillListCommand;
import Command.Commands.Link.*;
import Command.Commands.Passive.BrewTrackerCommand;
import Command.Commands.Passive.GunfightCommand;
import Command.Commands.Passive.GunfightHelpCommand;
import Command.Commands.Passive.LeaderboardCommand;
import Command.Commands.Random.*;
import Command.Commands.Variable.*;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;

public class DiscordCommandManager {
    private final ArrayList<DiscordCommand> commands = new ArrayList<>();

    public DiscordCommandManager() {
        addCommands();
    }

    private DiscordCommand getCommand(String query) {
        query = query.toLowerCase();
        for(DiscordCommand c : commands) {
            if(c.matches(query)) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<DiscordCommand> getCommands() {
        return commands;
    }

    private void addCommand(DiscordCommand c) {
        if(commands.contains(c)) {
            System.out.println("Duplicate command: " + c.getHelpName());
            return;
        }
        commands.add(c);
    }

    public void handleCommand(GuildMessageReceivedEvent event) {
        DiscordCommand command = getCommand(event.getMessage().getContentRaw());
        if(command == null) {
            return;
        }
        command.execute(new CommandContext(event, commands));
    }

    private void addCommands() {
        addRandomCommands();
        addLinkCommands();
        addCommand((new GunfightCommand()));
        addCommand((new GunfightHelpCommand()));
        addCommand((new LeaderboardCommand()));
        addCommand((new MeCommand()));
        addCommand((new OSRSLookupCommand()));
        addCommand((new GrandExchangeLookupCommand()));
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
        //addCommand((new VoiceChannelCommand());
    }

    private void addRandomCommands() {
        addCommand((new MemeCommand()));
        addCommand((new RileyCommand()));
        addCommand((new BrockCommand()));
        addCommand((new KimmyCommand()));
        addCommand((new ElsaCommand()));
        addCommand((new SydneyCommand()));
        addCommand((new GenerateNameCommand()));
    }

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
