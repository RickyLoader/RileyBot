package Command.Commands;

import Command.Structure.*;
import Network.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

import java.util.Comparator;
import java.util.List;

public class TweetsCommand extends DiscordCommand {
    private TwitterManager twitterManager;
    private ResponseList<Status> tweets;

    public TweetsCommand() {
        super("tweets", "View all of the tweets!");
    }

    @Override
    public void execute(CommandContext context) {

        if(twitterManager == null) {
            twitterManager = new TwitterManager(new TwitterAdapter() {
                @Override
                public void gotUserTimeline(ResponseList<Status> statuses) {
                    tweets = statuses;
                    getTweetEmbed(context).showMessage();
                }
            });
        }
        twitterManager.getTweets();
    }

    /**
     * Get the table embed displaying tweet history
     *
     * @param context Command context
     * @return Tweet history pageable embed
     */
    public PageableEmbed getTweetEmbed(CommandContext context) {
        return new PageableTableEmbed(
                context.getJDA(),
                context.getMessageChannel(),
                context.getEmoteHelper(),
                tweets,
                twitterManager.getThumbnail(),
                "My Tweets",
                "Here are my latest " + tweets.size() + " tweets",
                new String[]{"Date", "Content"},
                5,
                EmbedHelper.getBlue()
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Status status = (Status) items.get(index);
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
        };
    }
}
