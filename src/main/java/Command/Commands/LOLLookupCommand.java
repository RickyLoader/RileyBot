package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageBuilderCommand;
import LOL.SummonerImage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class LOLLookupCommand extends ImageBuilderCommand {
    private final HashMap<String, String> regions;
    private String displayRegion, apiRegion;

    public LOLLookupCommand() {
        super("lollookup", "Look up a summoner", "region", 16);
        this.regions = getRegions();
    }

    private HashMap<String, String> getRegions() {
        HashMap<String, String> regions = new HashMap<>();
        regions.put("br", "br1");
        regions.put("eune", "eun1");
        regions.put("euw", "euw1");
        regions.put("jp", "jp1");
        regions.put("kr", "kr");
        regions.put("lan", "la1");
        regions.put("las", "la2");
        regions.put("na", "na1");
        regions.put("ru", "ru");
        regions.put("tr", "tr1");
        return regions;
    }

    @Override
    public ImageBuilder getImageBuilder(MessageChannel channel, Guild guild) {
        return new SummonerImage(channel, guild, "src/main/resources/LOL/", "font.otf");
    }

    @Override
    public String stripArguments(String query) {
        String region = query.split(" ")[0];
        if(region.equals(getTrigger())) {
            this.displayRegion = "oce";
            this.apiRegion = "oc1";
            return query;
        }
        this.displayRegion = region;
        this.apiRegion = regions.get(region);
        return query.replaceFirst(region, "").trim();
    }

    @Override
    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name, getHelpName(), this.displayRegion, this.apiRegion);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getLOLName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveLOLName(name, channel, user);
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || regions.containsKey(args[0]) && args[1].matches(getTrigger());
    }
}
