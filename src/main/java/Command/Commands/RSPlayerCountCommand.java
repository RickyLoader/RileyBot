package Command.Commands;

import Bot.FontManager;
import Command.Structure.*;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * View the online player count of Runescape & OSRS
 */
public class RSPlayerCountCommand extends DiscordCommand {
    public RSPlayerCountCommand() {
        super("runescape", "View the Runescape player counts!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        int totalPlayerCount = getTotalPlayerCount();
        int osrsPlayerCount = getOSRSPlayerCount();
        int rs3PlayerCount = totalPlayerCount - osrsPlayerCount;

        PieChart.Section[] sections = new PieChart.Section[]{
                new PieChart.Section("OSRS", osrsPlayerCount, new Color(EmbedHelper.BLUE)),
                new PieChart.Section("RS3", rs3PlayerCount, new Color(EmbedHelper.RUNESCAPE_ORANGE)),
        };
        PieChart pieChart = new PieChart(sections, FontManager.MODERN_WARFARE_FONT, false);

        String imageFileName = "player_count.png";
        byte[] image = ImageLoadingMessage.imageToByteArray(pieChart.getFullImage(true));
        int colour = osrsPlayerCount > rs3PlayerCount ? EmbedHelper.BLUE : EmbedHelper.RUNESCAPE_ORANGE;
        MessageEmbed playerCountEmbed = buildPlayerCountEmbed(imageFileName, totalPlayerCount, colour);
        channel.sendMessage(playerCountEmbed).addFile(image, imageFileName).queue();
    }

    /**
     * Build a message embed displaying the Runescape player counts
     *
     * @param imageFileName    File name of image to use
     * @param totalPlayerCount Total player count across RS3 & OSRS
     * @param colour           Colour to use
     * @return Message embed
     */
    private MessageEmbed buildPlayerCountEmbed(String imageFileName, int totalPlayerCount, int colour) {
        DecimalFormat df = new DecimalFormat("#,###");
        return new EmbedBuilder()
                .setFooter("Try: " + getTrigger())
                .setThumbnail("https://i.imgur.com/c9FYAaY.png")
                .setImage("attachment://" + imageFileName)
                .setTitle("Runescape Online Players - " + df.format(totalPlayerCount))
                .setColor(colour)
                .build();
    }

    /**
     * Get the total online player count across Runescape & Old School Runescape
     *
     * @return Total player count
     */
    private int getTotalPlayerCount() {
        String baseUrl = "https://www.runescape.com/player_count.js?varname=iPlayerCount";
        String url = baseUrl + "&callback=jQuery33102792551319766081_1618634108386&_=" + System.currentTimeMillis();
        try {
            String response = new NetworkRequest(url, false).get().body;
            Matcher matcher = Pattern.compile("\\(\\d+\\)").matcher(response);
            if(!matcher.find()) {
                throw new Exception();
            }
            return Integer.parseInt(response.substring(matcher.start() + 1, matcher.end() - 1));
        }
        catch(Exception e) {
            return 0;
        }
    }

    /**
     * Get the Old School Runescape online player count
     *
     * @return OSRS player count
     */
    private int getOSRSPlayerCount() {
        try {
            Document document = Jsoup.connect("https://oldschool.runescape.com").get();
            String playerCountText = document.getElementsByClass("player-count").get(0).text();
            Matcher matcher = Pattern.compile("[\\d,?]+").matcher(playerCountText);
            if(!matcher.find()) {
                throw new Exception();
            }
            playerCountText = playerCountText.substring(matcher.start(), matcher.end());
            return Integer.parseInt(playerCountText.replace(",", ""));
        }
        catch(Exception e) {
            return 0;
        }
    }
}
