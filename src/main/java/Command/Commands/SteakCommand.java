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
import java.util.Random;

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
        new Thread(() -> {
            try {
                Member biggestFan = context.getMember();
                BufferedImage fanImage = getUserAvatar(biggestFan.getUser());
                if(fanImage == null) {
                    throw new Exception();
                }
                BufferedImage steak = ImageIO.read(new File("src/main/resources/LOL/steak.png"));
                Graphics g = steak.getGraphics();
                g.drawImage(fanImage, 180, 50, null);
                g.dispose();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(steak, "png", out);
                out.close();
                sendMessage(channel, out.toByteArray(), biggestFan.getEffectiveName());
            }
            catch(Exception e) {
                e.printStackTrace();
                channel.sendMessage("Steak was too busy to take a selfie with you, sorry!").queue();
            }
        }).start();
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
                .setFooter(getQuote(), steak)
                .setImage("attachment://steak.jpg")
                .build();
        channel.sendMessage(message).addFile(steakSelfie, "steak.jpg").queue();
    }

    /**
     * Get a famous Steak quote
     *
     * @return Steak quote
     */
    private String getQuote() {
        String[] quotes = new String[]{
                "I wanted a short name and steak is my favourite food - Steak",
                "Uh it's pretty easy to go in the game knowing that i'm gonna be the only one to troll our team - Steak",
                "Yeah so like Maple's gonna carry me no matter what - Steak",
                "It's pretty easy yeah - Steak",
                "I think we're gonna make it through like, I don't know, I think - Steak",
                "They just kinda leave Steak to do whatever, most of the time he's just dead - Doublelift",
                "We have pretty much confident in this tournament - Steak",
                "Yeah but, hmm actually I think.. It's possible.. well the.. second to six team - Steak",
                "Everybody has like the same skill set yeah, except for SKT1, yeah they're the best - Steak",
                "If you count the whole region I think we might not even be better than wildcard - Steak",
                "Uh we prepared a lot, I would say enough, yeah but um our gameplay was pretty bad - Steak",
                "Like our communication and our team play was really really bad, so that's why we lost - Steak",
                "I'd say we're really really good, and I hope we can show you that in a few days, yeah - Steak",
                "I think strategicca..strateg..whatever. Strategy yeah, yeah strategy isn't that much from the coach - Steak",
                "Um thank you for your support and I hope we do good in the next few games, yeah - Steak",
                "Aphromoo is kinda famous for his hair, yeah but I think my hair crushes his - Steak",
                "Best hair award, there we go, for NA, that was me. I don't know what Steak's won.. but - Aphromoo"
        };
        return quotes[new Random().nextInt(quotes.length)];
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
            return resizeAvatar(ImageIO.read(connection.getInputStream()));
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Resize the user avatar image
     *
     * @param avatar User Discord avatar
     * @return Resized image
     */
    private BufferedImage resizeAvatar(BufferedImage avatar) {
        BufferedImage resized = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics g = avatar.getGraphics();
        g.drawImage(avatar, 0, 0, 240, 240, null);
        g.dispose();
        return resized;
    }
}
