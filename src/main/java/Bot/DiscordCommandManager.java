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
        // gunfight
        commands.add(new GunfightCommand());
        commands.add(new GunfightHelpCommand());
        commands.add(new LeaderboardCommand());
        commands.add(new MeCommand());
        // OSRS
        commands.add(new OSRSLookupCommand());
        commands.add(new GrandExchangeLookupCommand());
        //commands.add(new WordCloudCommand());
        // audio
        commands.add(new GhostCommand());
        commands.add(new SawCommand());
        //commands.add(new LOLLookupCommand());
        //commands.add(new VoiceChannelCommand());
        commands.add(new TTSCommand());
        commands.add(new SurvivorCommand());
        commands.add(new PlayYoutubeCommand());
        commands.add(new MakeAChoiceCommand());
        commands.add(new InviteCommand());
        commands.add(new BrewTrackerCommand());
        commands.add(new ExecuteOrder66Command());
        commands.add(new KillListCommand());
        commands.add(new TobinCommand());

        commands.add(new MWLookupCommand());
        commands.add(new HelpCommand());
    }

    private void addRandomCommands() {
        commands.add(new MemeCommand());
        commands.add(new RileyCommand());
        commands.add(new BrockCommand());
        commands.add(new KimmyCommand());
        commands.add(new ElsaCommand());
        commands.add(new SydneyCommand());
        commands.add(new GenerateNameCommand());
    }

    private void addLinkCommands() {
        commands.add(new BrewsCommand());
        commands.add(new DobroCommand());
        commands.add(new ExodiaCommand());
        commands.add(new FriendlyCommand());
        commands.add(new GothCommand());
        commands.add(new HagridCommand());
        commands.add(new MandrillCommand());
        commands.add(new MegaCommand());
        commands.add(new PureCommand());
        commands.add(new RememberCommand());
        commands.add(new BrewTrackerCommand());
        commands.add(new ShrekCommand());
        commands.add(new SmirnyCommand());
        commands.add(new SpidermanCommand());
        commands.add(new TrulyCommand());
        commands.add(new UnderbellyCommand());
        commands.add(new VapeCommand());

        commands.add(new FishingCommand());
        commands.add(new NormanCommand());
        commands.add(new PhotoshopCommand());
        commands.add(new RaidsCommand());
        commands.add(new RestingCommand());
        commands.add(new RiggsCommand());
        commands.add(new SleepingCommand());
        commands.add(new TurboCommand());
        commands.add(new TwitchCommand());
        commands.add(new VegetableCommand());
        commands.add(new VodkaCommand());
    }
}
