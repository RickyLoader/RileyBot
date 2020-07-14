package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.ApiRequest;
import OSRS.Stats.Hiscores;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OSRSLookupCommand extends DiscordCommand {
    private final ArrayList<String> currentLookups = new ArrayList<>();

    public OSRSLookupCommand() {
        super("osrslookup [player/me/@someone], osrslookup save [player]", "Lookup a player on the OSRS Hiscores!");
    }

    @Override
    public void execute(CommandContext context) {
        String msg = context.getLowerCaseMessage().trim();
        String[] args = msg.split(" ");
        MessageChannel channel = context.getMessageChannel();
        User user = context.getUser();

        if(args.length < 2) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        if(args[1].equals("save")) {
            String name = msg.replace("osrslookup save ", "");
            DiscordUser.saveOSRSName(name, channel, user);
            return;
        }

        String name = msg.replace("osrslookup ", "");

        if(name.equals("me")) {
            name = DiscordUser.getOSRSName(user.getIdLong());
            if(name == null) {
                channel.sendMessage(user.getAsMention() + " I don't have a name saved for you, try: ```osrslookup save [player]```").queue();
                return;
            }
        }

        List<User> mentioned = context.getMessage().getMentionedUsers();
        if(!mentioned.isEmpty()) {
            User u = mentioned.get(0);
            name = DiscordUser.getOSRSName(u.getIdLong());
            if(name == null) {
                channel.sendMessage(user.getAsMention() + " I don't have a name saved for " + u.getAsMention() + " they will need to: ```osrslookup save [their name]```").queue();
                return;
            }
        }

        if(name.length()>12){
            channel.sendMessage("Maximum username length is 12 characters cunt").queue();
            return;
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

    @Override
    public boolean matches(String query) {
        return query.startsWith("osrslookup");
    }
}
