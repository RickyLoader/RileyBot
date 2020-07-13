package Bot;

import Command.Commands.*;
import Command.Commands.Audio.*;
import Command.Commands.ExecuteOrder.ExecuteOrder66Command;
import Command.Commands.ExecuteOrder.KillListCommand;
import Command.Commands.Link.*;
import Command.Commands.Passive.GunfightCommand;
import Command.Commands.Passive.GunfightHelpCommand;
import Command.Commands.Passive.LeaderboardCommand;
import Command.Commands.Random.*;
import Command.Commands.Variable.*;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.LinkCommand;
import Command.Structure.RandomCommand;
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

        // OSRS
        commands.add(new OSRSLookupCommand());
        commands.add(new GrandExchangeLookupCommand());

        // audio
        commands.add(new GhostCommand());
        commands.add(new SawCommand());
        //commands.add(new VoiceChannelCommand());
        commands.add(new TTSCommand());
        commands.add(new SurvivorCommand());
        commands.add(new PlayYoutubeCommand());

        commands.add(new MakeAChoiceCommand());
        commands.add(new InviteCommand());
        commands.add(new BrewCountCommand());
        commands.add(new ExecuteOrder66Command());
        commands.add(new KillListCommand());
        commands.add(new TobinCommand());

        commands.add(new MWLookupCommand());
        commands.add(new HelpCommand());
    }

    private void addRandomCommands() {
        String random = RandomCommand.fetchPossibilities();
        commands.add(new MemeCommand(random));
        commands.add(new RileyCommand(random));
        commands.add(new BrockCommand(random));
        commands.add(new KimmyCommand(random));
        commands.add(new ElsaCommand(random));
        commands.add(new SydneyCommand(random));
        commands.add(new GenerateNameCommand(random));


    }

    private void addLinkCommands() {
        String links = LinkCommand.fetchLinks();
        commands.add(new BrewsCommand(links));
        commands.add(new DobroCommand(links));
        commands.add(new ExodiaCommand(links));
        commands.add(new FriendlyCommand(links));
        commands.add(new GothCommand(links));
        commands.add(new HagridCommand(links));
        commands.add(new MandrillCommand(links));
        commands.add(new MegaCommand(links));
        commands.add(new PureCommand(links));
        commands.add(new RememberCommand(links));
        commands.add(new ShrekCommand(links));
        commands.add(new SmirnyCommand(links));
        commands.add(new SpidermanCommand(links));
        commands.add(new TrulyCommand(links));
        commands.add(new UnderbellyCommand(links));
        commands.add(new VapeCommand(links));

        commands.add(new FishingCommand(links));
        commands.add(new NormanCommand(links));
        commands.add(new PhotoshopCommand(links));
        commands.add(new RaidsCommand(links));
        commands.add(new RestingCommand(links));
        commands.add(new RiggsCommand(links));
        commands.add(new SleepingCommand(links));
        commands.add(new TurboCommand(links));
        commands.add(new TwitchCommand(links));
        commands.add(new VegetableCommand(links));
        commands.add(new VodkaCommand(links));
    }
}
