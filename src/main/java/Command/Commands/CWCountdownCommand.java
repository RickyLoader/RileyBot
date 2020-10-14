package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.ImgurManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static Command.Structure.ImageBuilder.registerFont;

/**
 * Show how long until Cold War releases
 */
public class CWCountdownCommand extends DiscordCommand {
    private long lastFetched, releaseDate;
    private final Font font;
    private final String res = "/COD/CW/";
    private final ResourceHandler handler;
    private String type;
    private final String[] bgImages;

    /**
     * Initialise release date
     */
    public CWCountdownCommand() {
        super("cold war\ncold war beta\ncold war early beta", "How long until Cold War!");
        this.handler = new ResourceHandler();
        this.font = registerFont(res + "ColdWar.ttf", handler);
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
        switch(context.getLowerCaseMessage()) {
            case "cold war beta":
                calendar.set(2020, Calendar.OCTOBER, 17, 0, 0, 0);
                type = "PC open beta";
                break;
            case "cold war early beta":
                calendar.set(2020, Calendar.OCTOBER, 15, 0, 0, 0);
                type = "PC early beta";
                break;
            case "cold war":
                calendar.set(2020, Calendar.NOVEMBER, 13, 0, 0, 0);
                type = "Cold War";
                break;
            default:
                channel.sendMessage(getHelpNameCoded()).queue();
                return;
        }
        releaseDate = calendar.getTimeInMillis();
        lastFetched = Calendar.getInstance().getTimeInMillis();
        Countdown countdown = getCountdown(lastFetched);
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
                    res + "Templates/Countdown/" + bgImages[new Random().nextInt(bgImages.length)]
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
     * Create a Countdown object to hold the remaining time
     * until the release date
     *
     * @param currentTime Current time in ms
     * @return Countdown object
     */
    private Countdown getCountdown(long currentTime) {
        long period = Math.abs(releaseDate - currentTime);
        long seconds = period / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return new Countdown(
                days,
                hours % 24,
                minutes % 60,
                seconds % 60
        );
    }

    /**
     * Build the message embed and send to the channel
     *
     * @param channel  Channel to send message
     * @param image    Image byte array displaying countdown
     * @param released Game has released
     */
    private void buildCountdownEmbed(MessageChannel channel, byte[] image, boolean released) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setImage("attachment://countdown.jpg");
        builder.setThumbnail("https://i.imgur.com/R1YXMmB.png");
        builder.setDescription(released ? type + " has been out for:" : type + " release date: **" + new SimpleDateFormat("dd/MM/yyyy").format(new Date(releaseDate)) + "**");
        builder.setTitle((released ? "Time since" : "Cuntdown to") + " Black Ops: Cold War");
        builder.setColor(EmbedHelper.getOrange());
        builder.setFooter("Try: " + getHelpName().replaceAll("\n", " | ") + " | Last checked: " + new SimpleDateFormat("HH:mm:ss").format(lastFetched), "https://i.imgur.com/s6p534X.png");
        channel.sendMessage(builder.build()).addFile(image, "countdown.jpg").queue();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("cold war");
    }

    /**
     * Hold a period of time
     */
    private static class Countdown {
        private final long days, hours, minutes, seconds;

        /**
         * Create a countdown
         *
         * @param days    Days until release
         * @param hours   Hours until release
         * @param minutes Minutes until release
         * @param seconds Seconds until release
         */
        public Countdown(long days, long hours, long minutes, long seconds) {
            this.days = days;
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        /**
         * Get remaining days
         *
         * @return Days
         */
        public long getDays() {
            return days;
        }

        /**
         * Get remaining hours
         *
         * @return Hours
         */
        public long getHours() {
            return hours;
        }

        /**
         * Get remaining minutes
         *
         * @return Minutes
         */
        public long getMinutes() {
            return minutes;
        }

        /**
         * Get remaining seconds
         *
         * @return Seconds
         */
        public long getSeconds() {
            return seconds;
        }
    }
}
