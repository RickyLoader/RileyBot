package Command.Commands;

import Bot.DiscordUser;
import COD.CombatRecord;
import COD.Player;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.ApiRequest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MWLookupCommand extends DiscordCommand {
    ArrayList<String> platforms = new ArrayList<>();

    public MWLookupCommand() {
        super("mwlookup [player#1234/me/@someone], mwlookup save [player#1234]", "Have a gander at a player's stats!");
        platforms.add("bnet");
        platforms.add("psn");
        platforms.add("xbox");
        platforms.add("acti");
    }


    @Override
    public void execute(CommandContext context) {
        String[] args = context.getMessageContent().trim().split(" ");
        MessageChannel channel = context.getMessageChannel();

        if(args.length == 3 && args[1].equals("save")) {
            DiscordUser.saveMWName(args[2], channel, context.getUser());
        }
        else if(args.length == 2 || args.length >= 2 && !context.getMessage().getMentionedMembers().isEmpty()) {
            lookupPlayer(args, channel, context.getUser(), context.getMessage());
        }
        else {
            channel.sendMessage(getHelpNameCoded()).queue();
        }
    }

    private void lookupPlayer(String[] args, MessageChannel channel, User user, Message m) {
        long id = user.getIdLong();

        String name = args[1];

        if(name.equals("me")) {
            name = DiscordUser.getMWName(id);
            if(name == null) {
                channel.sendMessage(user.getAsMention() + " I don't have a name saved for you, try: ```mwlookup save [player#1234]```").queue();
                return;
            }
        }

        List<User> mentioned = m.getMentionedUsers();
        if(!mentioned.isEmpty()) {
            User u = mentioned.get(0);
            name = DiscordUser.getMWName(u.getIdLong());
            if(name == null) {
                channel.sendMessage(user.getAsMention() + " I don't have a name saved for " + u.getAsMention() + " they will need to: ```mwlookup save [their name]```").queue();
                return;
            }
        }
        if(name.length() > 17) {
            channel.sendMessage("Maximum username length is 18 characters cunt").queue();
            return;
        }
        Player player = new Player(name, "bnet");
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
