package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class YoutubeLookupCommand extends LookupCommand {

    public YoutubeLookupCommand() {
        super("ytlookup", "Look up your favourite youtube channel!", 20);
    }

    @Override
    public void processName(String name, CommandContext context) {
        YoutubeChannel channel = new YoutubeChannel(name);
        if(!channel.exists()) {
            context.getMessageChannel().sendMessage("I couldn't find " + name + " on youtube cunt.").queue();
            return;
        }
        PageableTableEmbed youtubeEmbed = getYoutubeEmbed(context, channel);
        youtubeEmbed.showMessage();
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.YT);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.YT, channel, user);
    }

    /**
     * Get the youtube channel table embed
     *
     * @param context Command context
     * @param channel Youtube channel
     * @return Youtube channel table embed
     */
    private PageableTableEmbed getYoutubeEmbed(CommandContext context, YoutubeChannel channel) {
        return new PageableTableEmbed(
                context,
                channel.getVideos(),
                channel.getThumbnail(),
                channel.getName(),
                EmbedHelper.embedURL(channel.getDesc(), channel.getUrl()),
                new String[]{"Title", "URL", "Views"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                YoutubeChannel.Video v = (YoutubeChannel.Video) items.get(index);
                return new String[]{v.getTitle(), v.getUrl(), String.valueOf(v.getViews())};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    YoutubeChannel.Video v1 = (YoutubeChannel.Video) o1;
                    YoutubeChannel.Video v2 = (YoutubeChannel.Video) o2;

                    if(defaultSort) {
                        return (int) (v2.getViews() - v1.getViews());
                    }
                    return (int) (v1.getViews() - v2.getViews());
                });
            }
        };
    }

    /**
     * Hold data on a youtube channel
     */
    private static class YoutubeChannel {
        private String id, name, thumbnail, desc, url;
        private ArrayList<Video> videos;

        /**
         * Create a youtube channel
         *
         * @param channelName Channel name
         */
        public YoutubeChannel(String channelName) {
            if(getChannelInfo(channelName)) {
                videos = findVideos();
            }
        }

        /**
         * Get the channel name, id, url, thumbnail, and description.
         * Return the status of this operation
         *
         * @param channelName Channel name
         * @return Channel exists
         */
        private boolean getChannelInfo(String channelName) {
            String searchChannel = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=channel&maxResults=1&q="
                    + channelName
                    + Secret.getYoutubeKey();
            String json = new NetworkRequest(searchChannel, false).get().body;
            if(json == null) {
                return false;
            }
            JSONArray channels = new JSONObject(json).getJSONArray("items");
            if(channels.isEmpty()) {
                return false;
            }

            JSONObject channel = channels.getJSONObject(0);
            JSONObject snippet = channel.getJSONObject("snippet");
            this.name = snippet.getString("channelTitle");
            this.id = channel.getJSONObject("id").getString("channelId");
            this.url = "https://www.youtube.com/channel/" + id + "/videos";
            this.thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
            this.desc = snippet.getString("description");
            return true;
        }

        /**
         * Create a list of videos for the current Youtube channel
         *
         * @return List of channel videos
         */
        private ArrayList<Video> findVideos() {
            HashMap<String, Video> videos = new HashMap<>();
            String videoSearch = "https://www.googleapis.com/youtube/v3/search?channelId="
                    + id
                    + "&part=snippet,id&order=date&maxResults=50" + Secret.getYoutubeKey();
            String json = new NetworkRequest(videoSearch, false).get().body;
            if(json == null) {
                return null;
            }
            JSONArray videoJSON = new JSONObject(json).getJSONArray("items");
            for(int i = 0; i < videoJSON.length(); i++) {
                JSONObject video = videoJSON.getJSONObject(i);
                JSONObject idSummary = video.getJSONObject("id");
                if(!idSummary.getString("kind").equals("youtube#video")) {
                    continue;
                }
                JSONObject snippet = video.getJSONObject("snippet");
                String id = idSummary.getString("videoId");
                String title = StringEscapeUtils.unescapeHtml4(snippet.getString("title"));

                videos.put(id, new Video(
                                id,
                                title,
                                snippet.getString("description"),
                                snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url")
                        )
                );
            }
            ArrayList<String> videoIds = videos
                    .values()
                    .stream()
                    .map(Video::getId)
                    .collect(Collectors.toCollection(ArrayList::new));

            String idList = videoIds
                    .toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", "");

            String statSearch = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id="
                    + idList
                    + Secret.getYoutubeKey();

            String statInfo = new NetworkRequest(statSearch, false).get().body;
            if(statInfo == null) {
                return null;
            }
            JSONArray videoStats = new JSONObject(statInfo).getJSONArray("items");
            for(int i = 0; i < videoStats.length(); i++) {
                JSONObject video = videoStats.getJSONObject(i);
                videos.get(video.getString("id"))
                        .setViews(
                                Integer.parseInt(
                                        video.getJSONObject("statistics").getString("viewCount")
                                )
                        );
            }
            return new ArrayList<>(videos.values());
        }

        /**
         * Check if the Youtube channel exists
         *
         * @return Channel exists
         */
        public boolean exists() {
            return id != null;
        }

        /**
         * Get the channel URL
         *
         * @return Channel URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the channel thumbnail image
         *
         * @return Channel thumbnail image
         */
        public String getThumbnail() {
            return thumbnail;
        }

        /**
         * Get the channel name
         *
         * @return Channel name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the channel description
         *
         * @return Channel description
         */
        public String getDesc() {
            return desc.isEmpty() ? "No description provided" : desc;
        }

        /**
         * Get the list of channel videos
         *
         * @return Channel videos
         */
        public ArrayList<Video> getVideos() {
            return videos;
        }

        /**
         * Hold data on a Youtube video
         */
        private static class Video {
            private final String url, title, desc, thumbnail, id;
            private long views;

            /**
             * Create a Youtube video
             *
             * @param id        Video id
             * @param title     Video title
             * @param desc      Video description
             * @param thumbnail Video thumbnail image URL
             */
            public Video(String id, String title, String desc, String thumbnail) {
                this.id = id;
                this.url = "https://www.youtube.com/watch?v=" + id;
                this.title = title;
                this.desc = desc;
                this.thumbnail = thumbnail;
            }

            /**
             * Get the video id
             *
             * @return Video id
             */
            public String getId() {
                return id;
            }

            /**
             * Get the video title
             *
             * @return Video title
             */
            public String getTitle() {
                return title;
            }

            /**
             * Get the video thumbnail
             *
             * @return Video thumbnail
             */
            public String getThumbnail() {
                return thumbnail;
            }

            /**
             * Get the video description
             *
             * @return Video description
             */
            public String getDesc() {
                return desc.isEmpty() ? "No description provided" : desc;
            }

            /**
             * Get the URL to the video
             *
             * @return URL to video
             */
            public String getUrl() {
                return url;
            }

            /**
             * Get the number of views on the video
             *
             * @return Video view count
             */
            public long getViews() {
                return views;
            }

            /**
             * Set the video view count
             *
             * @param views Views to set
             */
            public void setViews(long views) {
                this.views = views;
            }
        }
    }
}
