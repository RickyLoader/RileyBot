package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.ImgurManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static Command.Structure.ImageBuilder.registerFont;

/**
 * Show how long until Cold War releases
 */
public class CWCountdownCommand extends DiscordCommand {
    private MessageEmbed countdownMessage = null;
    private long lastFetched;
    private final long releaseDate;
    private final Font font;
    private final String res = "src/main/resources/COD/CW/";

    /**
     * Initialise release date
     */
    public CWCountdownCommand() {
        super("cold war", "How long until Cold War!");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.NOVEMBER, 13, 0, 0, 0);
        releaseDate = calendar.getTimeInMillis();
        font = registerFont(res + "ColdWar.ttf");
    }

    /**
     * Calculate how long until the release date and build the countdown message to send
     *
     * @param context Command context
     */
    @Override
    public void execute(CommandContext context) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(countdownMessage == null || currentTime - lastFetched > 600000) { // 10 minutes
            lastFetched = currentTime;
            Countdown countdown = getCountdown(currentTime);
            String image = buildImage(countdown);
            countdownMessage = buildCountdownEmbed(image, currentTime >= releaseDate);
        }
        context.getMessageChannel().sendMessage(countdownMessage).queue();
    }

    /**
     * Get a random background image for the countdown
     *
     * @return Background image
     */
    private File getBackgroundImage() {
        File[] dir = new File(res + "Templates/Countdown").listFiles();
        return dir[new Random().nextInt(dir.length)];
    }

    /**
     * Build an image displaying the countdown time values
     *
     * @param countdown Countdown until release
     * @return Image displaying countdown
     */
    private String buildImage(Countdown countdown) {
        String url = null;
        try {
            BufferedImage countdownImage = ImageIO.read(getBackgroundImage());
            Graphics g = countdownImage.getGraphics();
            g.setFont(font.deriveFont(80f));
            FontMetrics fm = g.getFontMetrics();
            int y = ((countdownImage.getHeight() / 4) + (countdownImage.getHeight() / 8)) + (fm.getHeight() / 2);
            int padding = 250;
            int x = 100;
            drawTimeUnit(countdown.getDays(), g, x, y);
            x += padding;
            drawTimeUnit(countdown.getHours(), g, x, y);
            x += padding;
            drawTimeUnit(countdown.getMinutes(), g, x, y);
            x += padding;
            drawTimeUnit(countdown.getSeconds(), g, x, y);
            g.dispose();
            url = ImgurManager.uploadImage(countdownImage);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Draw the time unit on to the image with a drop shadow
     *
     * @param value Value to draw
     * @param g     Image graphics
     * @param x     X coordinate
     * @param y     Y coordinate
     */
    private void drawTimeUnit(long value, Graphics g, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        String unit = new DecimalFormat("00").format(value);
        g.setColor(Color.WHITE);
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
     * Build the message to display
     *
     * @param image    Image displaying countdown
     * @param released Game has released
     * @return MessageEmbed summarising countdown
     */
    private MessageEmbed buildCountdownEmbed(String image, boolean released) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setImage(image);
        builder.setThumbnail("https://i.imgur.com/R1YXMmB.png");
        builder.setDescription(released ? "Cold War has been out for:" : "Cold War release date: **13/11/2020**");
        builder.setTitle((released ? "Time since" : "Cuntdown to") + " Black Ops: Cold War");
        builder.setColor(EmbedHelper.getOrange());
        builder.setFooter("Type: " + getHelpName() + " | Limit once every 10 minutes | Last checked: " + new SimpleDateFormat("HH:mm:ss").format(lastFetched), "https://i.imgur.com/DOATel5.png");
        return builder.build();
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
