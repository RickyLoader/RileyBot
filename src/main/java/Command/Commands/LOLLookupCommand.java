package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import LOL.Summoner;
import LOL.SummonerImage;
import net.dv8tion.jda.api.entities.MessageChannel;

public class LOLLookupCommand extends DiscordCommand {

    public LOLLookupCommand() {
        super("lollookup [summoner]", "Look up a summoner!");
    }

    @Override
    public void execute(CommandContext context) {
        String name = context.getLowerCaseMessage().replace("lollookup ", "");
        MessageChannel channel = context.getMessageChannel();
        Summoner summoner = new Summoner(name);
        if(!summoner.exists()) {
            channel.sendMessage(name + " doesn't exist on the OCE server cunt").queue();
            return;
        }
        //channel.sendFile(new SummonerImage(summoner).buildImage()).queue();
        channel.sendMessage("\uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7 UNDER CONSTRUCTION BEEP BEEP \uD83D\uDEA7\uD83D\uDEA7\uD83D\uDEA7").queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("lollookup");
    }
}
