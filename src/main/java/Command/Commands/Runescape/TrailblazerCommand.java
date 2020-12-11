package Command.Commands.Runescape;

import Bot.DiscordUser;
import Bot.ResourceHandler;
import Command.Structure.*;
import Network.NetworkRequest;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.RelicTier;
import Runescape.OSRS.Stats.OSRSHiscores;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.api.entities.Message.*;

/**
 * Store/Display exported OSRS Trailblazer League regions and relics
 * data from osleague.tools.
 * Also displays in osrslookup
 */
public class TrailblazerCommand extends DiscordCommand {
    private final ArrayList<RelicTier> relicTierData;
    private String missingEmote;

    public TrailblazerCommand() {
        super(
                "trailblazer view " + LookupCommand.RESOLVE_NAME_ARGS
                        + "\ntrailblazer store " + LookupCommand.RESOLVE_NAME_ARGS + "*"
                        + "\n\n*(Attach exported data from osleague.tools)",
                "Update/Display a player's OSRS League regions and relics"
        );
        this.relicTierData = getRelics();
    }

    /**
     * Get the relic data from the database
     *
     * @return Array of relic tiers
     */
    private ArrayList<RelicTier> getRelics() {
        return RelicTier.parseRelics(new JSONArray(
                new NetworkRequest("osrs/league/relics", true).get().body
        ));
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        String content = context.getLowerCaseMessage();
        Message message = context.getMessage();
        List<Member> mentioned = message.getMentionedMembers();

        if(missingEmote == null) {
            missingEmote = EmoteHelper.formatEmote(context.getEmoteHelper().getFail());
        }

        content = content.replace("trailblazer", "").trim();

        if(content.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        String arg = content.split(" ")[0];
        String name = content.replaceFirst(arg, "").trim();

        if(name.isEmpty()) {
            channel.sendMessage("I need a name!").queue();
            return;
        }

        boolean me = name.equals("me");
        if(me || !mentioned.isEmpty()) {
            name = DiscordUser.getSavedName(
                    me ? member.getIdLong() : mentioned.get(0).getIdLong(),
                    DiscordUser.OSRS
            );
            if(name == null) {
                channel.sendMessage(
                        (me ? "You" : "They") + " don't have a saved osrslookup name"
                ).queue();
                return;
            }
        }

        String finalName = name;
        new Thread(() -> {
            switch(arg) {
                case "view":
                    showPlayerData(finalName, member, channel);
                    return;
                case "store":
                    processAttachedFile(message, member, finalName, channel);
            }
        }).start();
    }

    /**
     * Display the stored Trailblazer league relics & regions
     * for the given player name
     *
     * @param name    Player name to get stored data for
     * @param member  Member to inform of error
     * @param channel Channel to send relic/region data to
     */
    private void showPlayerData(String name, Member member, MessageChannel channel) {
        JSONObject data = new JSONObject(
                DiscordUser.getOSRSLeagueData(name)
        );
        ArrayList<RelicTier> playerRelics = RelicTier.parseRelics(data.getJSONArray("relics"));
        ArrayList<Region> playerRegions = Region.parseRegions(data.getJSONArray("regions"));
        if(playerRegions.isEmpty() && playerRelics.isEmpty()) {
            channel.sendMessage(
                    member.getAsMention() + " I don't have any league data stored about **" + name + "**." +
                            "\nYou will need to use **trailblazer store " + name + "** to add data for that player."
            ).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(name.toUpperCase() + " League data")
                .setColor(EmbedHelper.RUNESCAPE_ORANGE)
                .setThumbnail(OSRSHiscores.leagueThumbnail)
                .setImage(EmbedHelper.SPACER_IMAGE)
                .setFooter("Type: trailblazer for help.", OSRSHiscores.leagueThumbnail);

        StringBuilder description = new StringBuilder();

        description.append("**Regions**\n\n");
        if(playerRegions.isEmpty()) {
            description.append("I don't have your region data!");
        }
        else {
            for(int i = 0; i < Region.MAX_REGIONS; i++) {
                description.append(i + 1)
                        .append(": ")
                        .append(i < playerRegions.size() ? playerRegions.get(i).getName() : missingEmote);
                if(i < Region.MAX_REGIONS - 1) {
                    description.append("\n");
                }
            }
        }

        description.append("\n\n**Relics**\n\n");
        if(playerRelics.isEmpty()) {
            description.append("I don't have any relic data for **").append(name).append("**!");
        }
        else {
            for(int i = 0; i < RelicTier.MAX_RELICS; i++) {
                String relicName = (i < playerRelics.size())
                        ? playerRelics.get(i).getRelicByIndex(0).getName() : missingEmote;
                description
                        .append(i + 1)
                        .append(": ")
                        .append(relicName)
                        .append("\n");
            }
        }
        channel.sendMessage(
                builder.setDescription(description).build()
        ).queue();
    }

    /**
     * Get a message explaining how to store/update the player data
     *
     * @return Help message
     */
    private String getHelpMessage() {
        String link = "https://www.osleague.tools/#/tracker";
        return "You need to export & attach a league data file from:\n" + link + "\n" +
                "Select relics/regions & go to **Manage Data** -> **Export to file**.";
    }

    /**
     * Get and read the attached file from osleague.tools, parse the data to be stored
     * in the database if successful
     *
     * @param message Message with attached file
     * @param member  Member to inform of status
     * @param name    Player name to store data to
     * @param channel Channel to send updates to
     */
    private void processAttachedFile(Message message, Member member, String name, MessageChannel channel) {
        List<Attachment> attachments = message.getAttachments();
        if(attachments.isEmpty()) {
            channel.sendMessage(getHelpMessage()).queue();
            return;
        }
        Attachment data = message.getAttachments().get(0);
        String ext = data.getFileExtension();

        if(ext == null || !ext.equals("txt")) {
            channel.sendMessage(member.getAsMention() + " What the fuck is that file?").queue();
            return;
        }

        data.retrieveInputStream().thenAccept(inputStream -> {
                    updateLeagueData(
                            new ResourceHandler().getStreamAsString(inputStream),
                            name,
                            member,
                            channel
                    );
                    message.delete().queue();
                }
        ).exceptionally(throwable -> {
            channel.sendMessage(
                    member.getAsMention() + "Something went wrong reading that file, try again."
            ).queue();
            return null;
        });
    }

    /**
     * Update the user data
     *
     * @param data    Exported data file from osleague.tools
     * @param name    Player name to store data to
     * @param member  Member to inform of status
     * @param channel Channel to send status update
     */
    private void updateLeagueData(String data, String name, Member member, MessageChannel channel) {
        try {
            String response = new NetworkRequest("osrs/league/player/update", true).post(
                    getUpdateJSON(new JSONObject(data), name)
            ).body;
            if(response.contains("updated")) {
                channel.sendMessage(
                        member.getAsMention()
                                + " The league info for **"
                                + name
                                + "** has been updated!"
                ).queue(message -> showPlayerData(name, member, channel));
            }
            else {
                channel.sendMessage(
                        member.getAsMention() + " Something went wrong saving that data, sorry bro"
                ).queue();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            channel.sendMessage(
                    member.getAsMention() + " What kind of file is that? Type: **trailblazer** for help."
            ).queue();
        }
    }

    /**
     * Reformat the JSON to be stored in the database
     *
     * @param playerData Player data from osleague.tools
     * @param name       Player name to store data to
     * @return Update JSON
     */
    private String getUpdateJSON(JSONObject playerData, String name) {
        JSONObject updateData = new JSONObject();
        JSONArray regions = new JSONArray();

        if(!playerData.has("unlockedRegions")) {
            regions
                    .put(new JSONObject().put("unlock_index", 1).put("name", "Misthalin"))
                    .put(new JSONObject().put("unlock_index", 2).put("name", "Karamja"));
        }
        else {
            JSONArray regionData = new JSONArray(playerData.getString("unlockedRegions"));
            for(int i = 0; i < 5; i++) {
                regions.put(
                        new JSONObject()
                                .put("unlock_index", i + 1)
                                .put("name", regionData.isNull(i) ? JSONObject.NULL : regionData.getString(i))
                );
            }
        }
        updateData.put("regions", regions);

        JSONObject relicData = new JSONObject(
                playerData.getString("unlockedRelics")
        );
        JSONArray updateRelics = new JSONArray();

        boolean ignore = false; // Ignore the rest of the relics if one is missing
        for(int i = 0; i < 6; i++) {
            JSONObject relic = new JSONObject().put("unlock_index", i + 1);
            String key = String.valueOf(i);
            if(!ignore && relicData.has(key) && relicData.getJSONObject(key).getInt("relic") != -1) {
                int relicIndex = relicData.getJSONObject(key).getInt("relic");
                relic.put("relic_id", relicTierData.get(i).getRelicByIndex(relicIndex).getId());
            }
            else {
                relic.put("relic_id", JSONObject.NULL);
                ignore = true;
            }
            updateRelics.put(relic);
        }
        return updateData
                .put("relics", updateRelics)
                .put("player_name", name)
                .toString();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("trailblazer");
    }
}
