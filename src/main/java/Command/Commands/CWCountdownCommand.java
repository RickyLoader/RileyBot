package Command.Commands;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Countdown.Countdown;
import Network.ImgurManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Show how long until Cold War releases
 */
public class CWCountdownCommand extends DiscordCommand {
    private long lastFetched, releaseDate;
    private final Font font;
    private final ResourceHandler handler;
    private String type;
    private final String[] bgImages;
    public static final String thumbnail = "https://i.imgur.com/R1YXMmB.png";

    /**
     * Initialise release date
     */
    public CWCountdownCommand() {
        super("cold war\ncold war beta\ncold war early beta", "How long until Cold War!");
        this.handler = new ResourceHandler();
        this.font = FontManager.COLD_WAR_FONT;
        this.bgImages = new String[]{
                "1.png",
                "2.png",
                "3.png",
                "4.png",
                "5.png",
                "6.png",
                "7.png",
                "8.png",
                "9.png",
                "10.png",
        };
    }

    /**
     * Calculate how long until the release date and build the countdown message to send
     *
     * @param context Command context
     */
    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");

        switch(context.getLowerCaseMessage()) {
            case "cold war beta":
                calendar.set(2020, Calendar.OCTOBER, 17, 10, 0, 0);
                type = "PC open beta";
                break;
            case "cold war early beta":
                calendar.set(2020, Calendar.OCTOBER, 15, 10, 0, 0);
                type = "PC early beta";
                break;
            case "cold war":
                calendar.set(2020, Calendar.NOVEMBER, 12, 21, 0, 0);
                type = "Cold War";
                break;
            default:
                channel.sendMessage(getHelpNameCoded()).queue();
                return;
        }
        calendar.setTimeZone(timeZone);
        releaseDate = calendar.getTimeInMillis();
        lastFetched = Calendar.getInstance().getTimeInMillis();
        Countdown countdown = Countdown.from(lastFetched, releaseDate);
        buildCountdownEmbed(channel, buildImage(countdown), lastFetched >= releaseDate);
    }

    /**
     * Build an image displaying the countdown time values
     *
     * @param countdown Countdown until release
     * @return Image displaying countdown in byte array
     */
    private byte[] buildImage(Countdown countdown) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BufferedImage countdownImage = handler.getImageResource(
                    "/COD/CW/Templates/Countdown/" + bgImages[new Random().nextInt(bgImages.length)]
            );
            Graphics g = countdownImage.getGraphics();
            g.setFont(font.deriveFont(80f));
            FontMetrics fm = g.getFontMetrics();
            int y = ((countdownImage.getHeight() / 4) + (countdownImage.getHeight() / 8)) + (fm.getHeight() / 2);
            int padding = 250;
            int x = 100;
            drawTimeUnit(countdown.getDays(), g, fm, x, y);
            x += padding;
            drawTimeUnit(countdown.getHours(), g, fm, x, y);
            x += padding;
            drawTimeUnit(countdown.getMinutes(), g, fm, x, y);
            x += padding;
            drawTimeUnit(countdown.getSeconds(), g, fm, x, y);
            g.dispose();
            ImageIO.write(ImgurManager.stripAlpha(countdownImage), "jpg", outputStream);
            outputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    /**
     * Draw the time unit on to the image with a drop shadow
     *
     * @param value Value to draw
     * @param g     Image graphics
     * @param fm    Font metrics
     * @param x     X coordinate
     * @param y     Y coordinate
     */
    private void drawTimeUnit(long value, Graphics g, FontMetrics fm, int x, int y) {
        String unit = new DecimalFormat("00").format(value);
        g.drawString(unit, x - (fm.stringWidth(unit) / 2), y);
    }

    /**
     * Build the message embed and send to the channel
     *
     * @param channel  Channel to send message
     * @param image    Image byte array displaying countdown
     * @param released Game has released
     */
    private void buildCountdownEmbed(MessageChannel channel, byte[] image, boolean released) {
        MessageEmbed embed = new EmbedBuilder()
                .setImage("attachment://countdown.jpg")
                .setThumbnail(thumbnail)
                .setDescription(released ? type + " has been out for:" : type + " release date: **" + new SimpleDateFormat("dd/MM/yyyy").format(new Date(releaseDate)) + "**")
                .setTitle((released ? "Time since" : "Cuntdown to") + " Black Ops: Cold War")
                .setColor(EmbedHelper.ORANGE)
                .setFooter("Try: " + getHelpName().replace("\n", " | ") + " | Last checked: " + new SimpleDateFormat("HH:mm:ss").format(lastFetched), "https://i.imgur.com/s6p534X.png")
                .build();
        channel.sendMessage(embed).addFile(image, "countdown.jpg").queue();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith("cold war");
    }
}
