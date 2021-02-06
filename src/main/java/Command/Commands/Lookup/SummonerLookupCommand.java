package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.LookupCommand;
import LOL.SummonerOverview;
import LOL.SummonerOverview.Region;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

/**
 * Look up a League of Legends summoner
 */
public abstract class SummonerLookupCommand extends LookupCommand {
    private final HashMap<String, Region> regions;
    private final boolean TFT;
    private Region region;

    /**
     * Create a summoner lookup command
     *
     * @param trigger Command trigger
     * @param desc    Command description
     * @param TFT     Use TFT summoner stats
     */
    public SummonerLookupCommand(String trigger, String desc, boolean TFT) {
        super(
                trigger,
                desc,
                "[region] " + getDefaultLookupArgs(trigger),
                16
        );
        this.regions = getRegions();
        this.TFT = TFT;
    }

    /**
     * Get a map of region display name -> region
     *
     * @return Region map
     */
    private HashMap<String, Region> getRegions() {
        HashMap<String, Region> regions = new HashMap<>();
        regions.put("br", new Region("br", "br1"));
        regions.put("eune", new Region("eune", "eun1"));
        regions.put("euw", new Region("euw", "euw1"));
        regions.put("jp", new Region("jp", "jp1"));
        regions.put("kr", new Region("kr", "kr"));
        regions.put("lan", new Region("lan", "la1"));
        regions.put("las", new Region("las", "la2"));
        regions.put("na", new Region("na", "na1"));
        regions.put("ru", new Region("ru", "ru"));
        regions.put("tr", new Region("tr", "tr1"));
        regions.put("oce", new Region("oce", "oc1"));
        return regions;
    }

    @Override
    public String stripArguments(String query) {
        String region = query.split(" ")[0];
        if(region.equals(getTrigger())) {
            this.region = regions.get("oce");
            return query;
        }
        this.region = regions.get(region);
        return query.replaceFirst(region, "").trim();
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        SummonerOverview summonerOverview = new SummonerOverview(name, region, TFT);
        if(!summonerOverview.exists()) {
            channel.sendMessage(
                    context.getMember().getAsMention()
                            + " I didn't find any summoners named **"
                            + name + "** on the **" + region.getDisplayName().toUpperCase() + "** region!"
            ).queue();
            return;
        }
        onSummonerFound(summonerOverview, context);
    }

    /**
     * Get the summoner region
     *
     * @return Summoner region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * A summoner has been found by the given name
     *
     * @param summonerOverview Basic summoner overview
     * @param context          Command context
     */
    protected abstract void onSummonerFound(SummonerOverview summonerOverview, CommandContext context);

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.LOL);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.LOL, channel, user);
    }

    @Override
    public boolean matches(String query, Message message) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || regions.containsKey(args[0]) && args[1].matches(getTrigger());
    }
}
