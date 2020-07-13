package Command.Commands;

import COD.CombatRecord;
import COD.Player;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.ApiRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.util.ArrayList;

public class MWLookupCommand extends DiscordCommand {
    ArrayList<String> platforms = new ArrayList<>();

    public MWLookupCommand() {
        super("mwlookup [bnet/psn/xbox/acti] [player#1234/me], mwlookup save [player#1234]", "Have a gander at a player's stats!");
        platforms.add("bnet");
        platforms.add("psn");
        platforms.add("xbox");
        platforms.add("acti");
    }


    @Override
    public void execute(CommandContext context) {
        String[] args = context.getMessageContent().trim().split(" ");
        MessageChannel channel = context.getMessageChannel();

        if(args.length < 3) {
            channel.sendMessage(getTrigger()).queue();
            return;
        }
        if(args[1].equals("save")) {
            savePlayer(args, channel, context.getUser());
        }
        else {
            lookupPlayer(args, channel, context.getUser());
        }
    }

    private void savePlayer(String[] args, MessageChannel channel, User user) {
        String name = args[2];
        long id = user.getIdLong();
        JSONObject body = new JSONObject().put("discord_id", id).put("table", "MW").put("name", name);
        ApiRequest.executeQuery("users/submit", "ADD", body.toString(), true);
        channel.sendMessage(user.getAsMention() + " Your mwlookup name is now " + name).queue();
    }

    private String getPlayerName(long id) {
        String json = ApiRequest.executeQuery("users/mw/" + id, "GET", null, true);
        if(json == null || json.isEmpty()) {
            return null;
        }
        return new JSONObject(json).getString("name");
    }

    private void lookupPlayer(String[] args, MessageChannel channel, User user) {
        String platform = args[1];
        long id = user.getIdLong();
        if(!platforms.contains(platform)) {
            channel.sendMessage(getTrigger()).queue();
        }

        String name = args[2];

        if(name.equals("me")) {
            name = getPlayerName(id);
            if(name == null) {
                channel.sendMessage(user.getAsMention()+" I don't have a name saved for you, try mwlookup save [player#1234]").queue();
                return;
            }
        }

        Player player = new Player(name, platform);
        if(player.getData() == null) {
            channel.sendMessage("I couldn't find " + player.getName() + " on " + player.getPlatform()).queue();
            return;
        }
        channel.sendFile(new CombatRecord(player).buildImage()).queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("mwlookup");
    }
}
