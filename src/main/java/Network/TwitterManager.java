package Network;

import net.dv8tion.jda.api.entities.Message;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class TwitterManager {
    private AsyncTwitter twitter;
    private final TwitterListener listener;

    public TwitterManager(TwitterListener listener) {
        this.listener = listener;
        startTwitter();
    }

    public boolean sendTweet(String tweet, List<Message.Attachment> attachments) {
        if(tweet.length() > 280 || tweet.isEmpty()) {
            return false;
        }
        StatusUpdate update = new StatusUpdate(tweet);
        // Max 4 files
        for(int i = 0; i < Math.min(4, attachments.size()); i++) {
            Message.Attachment a = attachments.get(i);
            update.media(getImage(a.getProxyUrl()));
        }
        twitter.updateStatus(update);
        return true;
    }

    public void getTweets() {
        twitter.getUserTimeline();
    }

    public boolean updateProfileImage(String url, List<Message.Attachment> attachments) {
        if(!attachments.isEmpty()) {
            url = attachments.get(0).getUrl();
        }
        File profileImage = getImage(url);

        if(profileImage == null) {
            return false;
        }
        twitter.updateProfileImage(profileImage);
        return true;
    }

    private File getImage(String message) {
        if(message.equalsIgnoreCase("default")) {
            message = "https://i.imgur.com/hANBiA3.png";
        }
        if(message.isEmpty() || !message.startsWith("http")) {
            return null;
        }
        String suffix = message.substring(message.length() - 3);
        try {
            URLConnection connection = new URL(message).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();
            BufferedImage image = ImageIO.read(connection.getInputStream());
            File out = File.createTempFile("twitter", suffix);
            ImageIO.write(image, suffix, out);
            return out;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getThumbnail() {
        return "https://1000logos.net/wp-content/uploads/2017/06/Twitter-Logo-500x444.png";
    }

    private void startTwitter() {
        ConfigurationBuilder builder = new ConfigurationBuilder()
                .setOAuthConsumerKey(Secret.getTwitterOAuthConsumerKey())
                .setOAuthConsumerSecret(Secret.getTwitterOAuthConsumerSecret())
                .setOAuthAccessToken(Secret.getTwitterOAuthAccessToken())
                .setOAuthAccessTokenSecret(Secret.getTwitterOAuthAccessTokenSecret());
        this.twitter = new AsyncTwitterFactory(builder.build()).getInstance();
        this.twitter.addListener(listener);
    }

    public String formatName(User user) {
        return user.getName() + " (@" + user.getScreenName() + ")";
    }

    public String getTweetURL(Status status) {
        return "https://twitter.com/" + status.getUser().getName() + "/status/" + status.getId();
    }

    public String getProfileURL(User user) {
        return "https://twitter.com/" + user.getScreenName();
    }
}
