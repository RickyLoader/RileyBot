package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

public class MeCommand extends DiscordCommand {

    public MeCommand() {
        super("me", "Look at your saved names!");
    }

    @Override
    public void execute(CommandContext context) {
        User user = context.getUser();

        String json = DiscordUser.getUserData(user.getIdLong());
        if(json.isEmpty()) {
            context.getMessageChannel().sendMessage("I don't have any data stored about you cunt").queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle(user.getName());
        builder.setThumbnail(user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        builder.setDescription("Here's all the data I've stolen from you:");
        builder.setFooter("Privacy policy: none", "https://icon-library.com/images/shield-icon-png/shield-icon-png-6.jpg");
        boolean first = true;
        JSONObject data = new JSONObject(json);
        for(String command : data.keySet()) {
            if(data.isNull(command)){
                continue;
            }
            String value = data.getString(command);
            if(first) {
                builder.addField("**Command**", command, true);
                builder.addBlankField(true);
                builder.addField("**Data**", value, true);
                first = false;
                continue;
            }
            builder.addField("\u200e", command, true);
            builder.addBlankField(true);
            builder.addField("\u200e", value, true);
        }


        context.getMessageChannel().sendMessage(builder.build()).queue();
    }
}
