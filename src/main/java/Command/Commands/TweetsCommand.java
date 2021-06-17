package Command.Commands;

import Command.Structure.*;
import Network.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;

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
    public PageableEmbed<Status> getTweetEmbed(CommandContext context) {
        return new PageableTableEmbed<Status>(
                context,
                tweets,
                twitterManager.getThumbnail(),
                "My Tweets",
                "Here are my latest " + tweets.size() + " tweets:",
                "Try: " + getTrigger(),
                new String[]{"Date", "Content"},
                5,
                EmbedHelper.BLUE
        ) {
            @Override
            public String[] getRowValues(int index, Status status, boolean defaultSort) {
                return new String[]{status.getCreatedAt().toString(), status.getText()};
            }

            @Override
            public void sortItems(List<Status> items, boolean defaultSort) {
                items.sort((o1, o2) -> defaultSort
                        ? o2.getCreatedAt().compareTo(o1.getCreatedAt())
                        : o1.getCreatedAt().compareTo(o2.getCreatedAt()));
            }
        };
    }
}
