package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.ApiRequest;
import OSRS.Stats.Hiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.util.ArrayList;

public class OSRSLookupCommand extends DiscordCommand {
    private final ArrayList<String> currentLookups = new ArrayList<>();

    public OSRSLookupCommand() {
        super("osrslookup [player/me], osrslookup save [player]", "Lookup a player on the OSRS Hiscores!");
    }

    @Override
    public void execute(CommandContext context) {
        String msg = context.getLowerCaseMessage().trim();
        String[] args = msg.split(" ");
        MessageChannel channel = context.getMessageChannel();
        User user = context.getUser();

        if(args.length < 2) {
            channel.sendMessage(getTrigger()).queue();
            return;
        }
        if(args[1].equals("save")) {
            String name = msg.replace("osrslookup save ", "");
            savePlayer(name, channel, user);
            return;
        }

        String name = msg.replace("osrslookup ", "");

        if(name.equals("me")) {
            name = getPlayerName(user.getIdLong());
            if(name == null) {
                channel.sendMessage(user.getAsMention() + " I don't have a name saved for you, try osrslookup save [player]").queue();
                return;
            }
        }

        Hiscores hiscores = new Hiscores(channel);
        if(currentLookups.contains(name)) {
            channel.sendMessage("Oi I told you their website is slow, patience is a virtue cunt").queue();
            return;
        }
        currentLookups.add(name);

        String finalName = name;
        new Thread(() -> {
            boolean success = hiscores.lookupPlayer(finalName);
            if(!success) {
                currentLookups.remove(finalName);
                return;
            }
            currentLookups.remove(finalName);
        }).start();
    }

    private void savePlayer(String name, MessageChannel channel, User user) {
        long id = user.getIdLong();
        JSONObject body = new JSONObject().put("discord_id", id).put("table", "OSRS").put("name", name);
        ApiRequest.executeQuery("users/submit", "ADD", body.toString(), true);
        channel.sendMessage(user.getAsMention() + " Your osrslookup name is now " + name).queue();
    }

    private String getPlayerName(long id) {
        String json = ApiRequest.executeQuery("users/osrs/" + id, "GET", null, true);
        if(json == null || json.isEmpty()) {
            return null;
        }
        return new JSONObject(json).getString("name");
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrslookup");
    }
}
