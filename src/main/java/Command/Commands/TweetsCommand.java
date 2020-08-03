package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.PageableEmbed;
import Command.Structure.PageableEmbedCommand;
import TwitterManager.TwitterManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

import java.util.Comparator;
import java.util.List;

public class TweetsCommand extends PageableEmbedCommand {
    private TwitterManager twitterManager;
    private ResponseList<Status> tweets;

    public TweetsCommand() {
        super("tweets", "View all of the tweets!");
    }

    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        return new TweetsMessage(
                context.getMessageChannel(),
                context.getGuild(),
                tweets,
                twitterManager.getThumbnail(),
                "My Tweets",
                "Here are my " + tweets.size() + " tweets",
                new String[]{"Date", "Content"}
        );
    }

    @Override
    public void execute(CommandContext context) {
        if(embed != null) {
            embed.delete();
        }
        addEmoteListener(context.getJDA());

        if(twitterManager == null) {
            twitterManager = new TwitterManager(new TwitterAdapter() {
                @Override
                public void gotUserTimeline(ResponseList<Status> statuses) {
                    tweets = statuses;
                    embed = getEmbed(context);
                    embed.showMessage();
                }
            });
        }
        twitterManager.getTweets();
    }

    private static class TweetsMessage extends PageableEmbed {

        public TweetsMessage(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
            super(channel, guild, items, thumb, title, desc, columns);
        }

        @Override
        public String[] getValues(int index, List<?> items, boolean defaultSort) {
            Status status = (Status) items.get(index);
            int number = defaultSort ? index + 1 : items.size() - index;
            return new String[]{status.getCreatedAt().toString(), status.getText()};
        }

        @Override
        public void sortItems(List<?> items, boolean defaultSort) {
            items.sort((Comparator<Object>) (o1, o2) -> {
                Status a = (Status) o1;
                Status b = (Status) o2;
                if(defaultSort) {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
        }
    }
}
