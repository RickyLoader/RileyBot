package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.*;
import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class YoutubeLookupCommand extends LookupCommand {
    private CommandContext context;

    public YoutubeLookupCommand() {
        super("ytlookup", "Look up your favourite youtube channel!", 20);
    }

    @Override
    public void execute(CommandContext context) {
        super.execute(context);
        this.context = context;
    }

    @Override
    public void processName(String name, MessageChannel channel, Guild guild) {
        YoutubeBrowser youtubeBrowser = new YoutubeBrowser(getTrigger(), getDesc(), name);
        youtubeBrowser.execute(context);
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getYTName(id);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveYTName(name, channel, user);
    }

    private static class YoutubeBrowser extends PageableEmbedCommand {
        private final String name;

        public YoutubeBrowser(String trigger, String desc, String name) {
            super(trigger, desc);
            this.name = name;
        }

        @Override
        public PageableEmbed getEmbed(CommandContext context) {
            if(name == null) {
                return null;
            }
            YoutubeChannel channel = new YoutubeChannel(name);
            if(!channel.exists()) {
                context.getMessageChannel().sendMessage("I couldn't find " + name + " on youtube cunt.").queue();
            }
            return new YoutubeChannelMessage(
                    context.getMessageChannel(),
                    context.getGuild(),
                    channel.getVideos(),
                    channel.getThumbnail(),
                    channel.getName(),
                    channel.getEmbeddedDescription(),
                    new String[]{"Title", "URL", "Views"}
            );
        }

        private static class YoutubeChannel {
            private String id, name, thumbnail, desc, url;
            private final String key = "AIzaSyCoHcK0485x1Edq_4X1PNJNqaVuHZ11Evg";
            private ArrayList<Video> videos;

            public YoutubeChannel(String query) {
                if(getChannelInfo(query)) {
                    videos = findVideos();
                }
            }

            private boolean getChannelInfo(String query) {
                String searchChannel = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=channel&maxResults=1&q=" + query + "&key=" + key;
                String json = new NetworkRequest(searchChannel, false).get();
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

            private ArrayList<Video> findVideos() {
                HashMap<String, Video> videos = new HashMap<>();
                String videoSearch = "https://www.googleapis.com/youtube/v3/search?channelId=" + id + "&part=snippet,id&order=date&maxResults=50&key=" + key;
                String json = new NetworkRequest(videoSearch, false).get();
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
                ArrayList<String> videoIds = videos.values().stream().map(Video::getId).collect(Collectors.toCollection(ArrayList::new));
                String idList = videoIds.toString().replace("[", "").replace("]", "").replace(" ", "");
                String statSearch = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + idList + "&key=" + key;
                String statInfo = new NetworkRequest(statSearch, false).get();
                if(statInfo == null) {
                    return null;
                }
                JSONArray videoStats = new JSONObject(statInfo).getJSONArray("items");
                for(int i = 0; i < videoStats.length(); i++) {
                    JSONObject video = videoStats.getJSONObject(i);
                    videos.get(video.getString("id")).setViews(Integer.parseInt(video.getJSONObject("statistics").getString("viewCount")));
                }
                return new ArrayList<>(videos.values());
            }

            public boolean exists() {
                return id != null;
            }

            public String getUrl() {
                return url;
            }

            public String getThumbnail() {
                return thumbnail;
            }

            public String getEmbeddedDescription() {
                return "[" + desc + "](" + url + ")";
            }

            public String getName() {
                return name;
            }

            public String getDesc() {
                return desc.isEmpty() ? "No description provided" : desc;
            }

            public ArrayList<Video> getVideos() {
                return videos;
            }

            private static class Video {
                private final String url, title, desc, thumbnail, id;
                private long views;

                public Video(String id, String title, String desc, String thumbnail) {
                    this.id = id;
                    this.url = "https://www.youtube.com/watch?v=" + id;
                    this.title = title;
                    this.desc = desc;
                    this.thumbnail = thumbnail;
                }

                public String getId() {
                    return id;
                }

                public String getTitle() {
                    return title;
                }

                public String getThumbnail() {
                    return thumbnail;
                }

                public String getDesc() {
                    return desc.isEmpty() ? "No description provided" : desc;
                }

                public String getUrl() {
                    return url;
                }

                public long getViews() {
                    return views;
                }

                public void setViews(long views) {
                    this.views = views;
                }
            }
        }

        private static class YoutubeChannelMessage extends PageableTableEmbed {

            public YoutubeChannelMessage(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
                super(channel, guild, items, thumb, title, desc, columns);
            }

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
        }
    }
}
