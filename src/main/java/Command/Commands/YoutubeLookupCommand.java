package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.PageableEmbed;
import Command.Structure.PageableEmbedCommand;
import Network.ApiRequest;
import net.dv8tion.jda.api.entities.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class YoutubeLookupCommand extends PageableEmbedCommand {
    public YoutubeLookupCommand() {
        super("ytlookup [channel name/me/@someone], ytlookup save [channel name]", "Look up your favourite youtube channel!");
    }

    @Override
    public void execute(CommandContext context) {
        super.execute(context);
    }

    private String getName(Message m, User u, MessageChannel channel) {
        String msg = m.getContentRaw().toLowerCase().replace("ytlookup", "").trim();
        String[] args = msg.split(" ");
        if(args[0].equals("save")) {
            String name = msg.replace("save", "").trim();
            if(!name.isEmpty()) {
                DiscordUser.saveYTName(msg.replace("save ", ""), channel, u);
                return null;
            }
        }

        if(args[0].equals("me")) {
            String name = DiscordUser.getYTName(u.getIdLong());
            if(name == null) {
                channel.sendMessage(u.getAsMention() + " I don't have a name saved for you, try: ```ytlookup save [channel name]```").queue();
            }
            return name;
        }
        List<User> mentioned = m.getMentionedUsers();
        if(!mentioned.isEmpty()) {
            User mention = mentioned.get(0);
            String name = DiscordUser.getYTName(mention.getIdLong());
            if(name == null) {
                channel.sendMessage(u.getAsMention() + " I don't have a name saved for " + mention.getAsMention() + " they will need to: ```ytlookup save [their channel name]```").queue();
            }
            return name;
        }
        if(msg.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return null;
        }
        return msg;
    }


    @Override
    public PageableEmbed getEmbed(CommandContext context) {
        String name = getName(context.getMessage(), context.getUser(), context.getMessageChannel());
        if(name == null){
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
                channel.getDesc(),
                new String[]{"Title", "URL", "Views"}
        );
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith("ytlookup");
    }

    private static class YoutubeChannel {
        private String id, name, thumbnail, desc, link;
        private final String key = "AIzaSyCoHcK0485x1Edq_4X1PNJNqaVuHZ11Evg";
        private ArrayList<Video> videos;

        public YoutubeChannel(String query) {
            if(getChannelInfo(query)) {
                videos = findVideos();
            }
        }

        private boolean getChannelInfo(String query) {
            String searchChannel = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=channel&maxResults=1&q=" + query + "&key=" + key;
            String json = ApiRequest.executeQuery(searchChannel, "GET", null, false);
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
            this.link = "https://www.youtube.com/user/jarvie911/videos";
            this.thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
            this.desc = snippet.getString("description");
            return true;
        }

        private ArrayList<Video> findVideos() {
            HashMap<String, Video> videos = new HashMap<>();
            String videoSearch = "https://www.googleapis.com/youtube/v3/search?channelId=" + id + "&part=snippet,id&order=date&maxResults=50&key=" + key;
            String json = ApiRequest.executeQuery(videoSearch, "GET", null, false);
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

                videos.put(id, new Video(
                                id,
                                snippet.getString("title"),
                                snippet.getString("description"),
                                snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url")
                        )
                );
            }
            ArrayList<String> videoIds = videos.values().stream().map(Video::getId).collect(Collectors.toCollection(ArrayList::new));
            String idList = videoIds.toString().replace("[", "").replace("]", "").replace(" ", "");
            String statSearch = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&id=" + idList + "&key=" + key;
            String statInfo = ApiRequest.executeQuery(statSearch, "GET", null, false);
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

        public String getThumbnail() {
            return thumbnail;
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

        private class Video {
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

    private class YoutubeChannelMessage extends PageableEmbed {

        public YoutubeChannelMessage(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
            super(channel, guild, items, thumb, title, desc, columns);
        }

        @Override
        public String[] getValues(int index, List<?> items, boolean defaultSort) {
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
