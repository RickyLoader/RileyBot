package WordCloud;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordCloudBuilder {
    private final MessageChannel channel;
    private ArrayList<String> wordList = new ArrayList<>();
    private long id;

    public WordCloudBuilder(MessageChannel channel) {
        this.channel = channel;
        sendStatusMessage();
    }

    private String getUpdateMessage(int count) {
        String finding = "Finding words...";
        if(count > 0) {
            finding += " " + count + "/5000";
        }
        return finding;
    }

    private void sendStatusMessage() {
        channel.sendMessage(getUpdateMessage(0)).queue(message -> {
            id = message.getIdLong();
            getLastMessages(channel.getLatestMessageIdLong());
        });
    }

    private void getLastMessages(long last) {
        if(wordList.size() == 5000) {
            for(String word : wordList) {
                System.out.println(word);
            }
            channel.sendFile(buildCloud()).queue();
            return;
        }
        channel.getHistoryBefore(last, Math.min(100, 5000 - wordList.size())).queue(messageHistory -> {
            System.out.println("hello");
            if(wordList.size() == 5000) {
                return;
            }
            List<Message> messages = messageHistory.getRetrievedHistory();
            for(Message m : messages) {
                String content = m.getContentRaw()
                        .toLowerCase()
                        .replace(",", "")
                        .replace(".", "");
                if(m.getAuthor().isBot() || content.contains("http") || !m.getMentionedMembers().isEmpty()) {
                    continue;
                }
                String[] words = content.split(" ");
                wordList.addAll(Arrays.asList(words));
                channel.retrieveMessageById(id).queue(message -> message.editMessage(getUpdateMessage(wordList.size())).queue());
                getLastMessages(messages.get(messages.size() - 1).getIdLong());
            }
        });

    }

    public File buildCloud() {
        String location = "src/main/resources/WordCloud/cloud.png";
        try {
            WordCloud wordCloud = new WordCloud(new Dimension(600, 600), CollisionMode.PIXEL_PERFECT);
            wordCloud.setPadding(2);
            wordCloud.setBackground(new CircleBackground(300));
            wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
            wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
            wordCloud.build(new FrequencyAnalyzer().load(wordList));
            wordCloud.writeToFile(location);
        }
        catch(Exception e) {
            return null;
        }
        return new File(location);
    }
}
