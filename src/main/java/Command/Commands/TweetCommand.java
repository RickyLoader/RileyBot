package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.TwitterManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import twitter4j.*;


public class TweetCommand extends DiscordCommand {
    private TwitterManager twitterManager;
    private MessageChannel channel;

    public TweetCommand() {
        super("tweet [text & attached images], tweet update [profile image URL/attached image]", "Send a tweet!");
    }

    @Override
    public void execute(CommandContext context) {
        if(twitterManager == null) {
            twitterManager = new TwitterManager(new TwitterAdapter() {
                @Override
                public void updatedStatus(Status status) {
                    context.getMessage().delete().queue();
                    channel.sendMessage(getTweetEmbed(status)).queue();
                }

                @Override
                public void updatedProfileImage(User user) {
                    context.getMessage().delete().queue();
                    channel.sendMessage("Profile image updated!").queue();
                }

                @Override
                public void onException(TwitterException te, TwitterMethod method) {
                    te.printStackTrace();
                    channel.sendMessage("Something went wrong and I couldn't bring myself to do that").queue();
                }
            });
        }
        this.channel = context.getMessageChannel();
        String msg = context.getLowerCaseMessage();
        String raw = context.getMessage().getContentRaw();
        String[] args = raw.split(" ");
        if(msg.startsWith("tweet update")) {
            String url = args.length == 3 ? args[2] : null;
            if(!twitterManager.updateProfileImage(url, context.getMessage().getAttachments())) {
                channel.sendMessage(context.getUser().getAsMention() + " that isn't an image!").queue();
            }
        }
        else {
            String content = raw.replaceFirst(raw.split(" ")[0], "");
            if(!twitterManager.sendTweet(content, context.getMessage().getAttachments())) {
                channel.sendMessage(context.getUser().getAsMention() + " that is too long! What are you thinking? A tweet has to be short!").queue();
            }
        }
    }

    private MessageEmbed getTweetEmbed(Status status) {
        User user = status.getUser();
        MediaEntity[] mediaEntities = status.getMediaEntities();
        String tweetText = status.getText();
        String image = "https://i.imgur.com/24Xf03H.png"; // Blank
        if(mediaEntities.length > 0) {
            image = mediaEntities[0].getMediaURL();
            tweetText = tweetText.replace(mediaEntities[0].getURL(),"");
        }
        return new EmbedBuilder()
                .setAuthor(twitterManager.formatName(user), twitterManager.getProfileURL(user), user.getProfileImageURL())
                .setTitle("Tweet #" + user.getStatusesCount(), twitterManager.getTweetURL(status))
                .setColor(1942002)
                .setThumbnail(user.getProfileImageURL())
                .setFooter("Try: " + getHelpName(), twitterManager.getThumbnail())
                .setImage(image)
                .setDescription(tweetText)
                .build();
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("tweet ");
    }
}
