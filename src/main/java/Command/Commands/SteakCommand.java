package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;

/**
 * Put the user's image in a selfie with Steak!
 */
public class SteakCommand extends DiscordCommand {
    public SteakCommand() {
        super("steak", "Get a photo with steak!");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        try {
            Member biggestFan = context.getMember();
            BufferedImage fanImage = getUserAvatar(biggestFan.getUser());
            if(fanImage == null) {
                throw new Exception();
            }
            BufferedImage steak = ImageIO.read(new File("src/main/resources/LOL/steak.jpg"));
            Graphics g = steak.getGraphics();
            g.drawImage(fanImage, 180, 50, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(steak, "jpg", out);
            out.close();
            sendMessage(channel, out.toByteArray(), biggestFan.getEffectiveName());
        }
        catch(Exception e) {
            e.printStackTrace();
            channel.sendMessage("Steak was too busy to take a selfie with you, sorry!").queue();
        }
    }

    /**
     * Build the selfie message
     *
     * @param channel     Channel to send selfie to
     * @param steakSelfie Selfie with the greatest League of Legends player in history
     * @param name        Name of the lucky fan
     */
    private void sendMessage(MessageChannel channel, byte[] steakSelfie, String name) {
        String steak = "https://i.imgur.com/KXnIE3C.png";
        MessageEmbed message = new EmbedBuilder()
                .setColor(EmbedHelper.getPurple())
                .setThumbnail(steak)
                .setTitle(name + " - Photo with Steak")
                .setDescription("The **GREATEST** League of Legends player in LCS history!")
                .setFooter("I wanted a short name and steak is my favourite food - Steak", steak)
                .setImage("attachment://steak.jpg")
                .build();
        channel.sendMessage(message).addFile(steakSelfie, "steak.jpg").queue();
    }

    /**
     * Get and resize the user's avatar to the correct size for replacing
     * the person's head beside him.
     *
     * @param fan User who is a big fan of steak
     * @return User avatar
     */
    private BufferedImage getUserAvatar(User fan) {
        try {
            URLConnection connection = new URL(fan.getEffectiveAvatarUrl()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();
            BufferedImage smallAvatar = ImageIO.read(connection.getInputStream());

            BufferedImage avatar = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
            Graphics g = avatar.getGraphics();
            g.drawImage(smallAvatar, 0, 0, 240, 240, null);
            g.dispose();
            return avatar;
        }
        catch(Exception e) {
            return null;
        }
    }
}
