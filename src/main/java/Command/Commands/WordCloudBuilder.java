package Command.Commands;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
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
    private final ArrayList<String> history;

    public WordCloudBuilder(MessageChannel channel) {
        this.channel = channel;
        this.history = getLastMessages();
    }

    private ArrayList<String> getLastMessages() {
        ArrayList<String> wordList = new ArrayList<>();
        long latest = channel.getLatestMessageIdLong();
        while(wordList.size()<5000) {
            List<Message> messages = channel.getHistoryBefore(latest, 100).complete().getRetrievedHistory();
            for(Message m : messages) {
                String content = m.getContentRaw()
                        .toLowerCase()
                        .replace(",", "")
                        .replace(".", "");
                if(m.getAuthor().isBot() || content.contains("http")) {
                    continue;
                }
                String[] words = content.split(" ");
                wordList.addAll(Arrays.asList(words));
            }
            System.out.println(wordList.size());
            latest = messages.get(messages.size() - 1).getIdLong();
        }
        return wordList;
    }

    public File buildCloud() {
        String location = "src/main/resources/WordCloud/cloud.png";
        try {
            WordCloud wordCloud = new WordCloud(new Dimension(600, 600), CollisionMode.PIXEL_PERFECT);
            wordCloud.setPadding(2);
            wordCloud.setBackground(new CircleBackground(300));
            wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
            wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
            wordCloud.build(new FrequencyAnalyzer().load(history));
            wordCloud.writeToFile(location);
        }
        catch(Exception e) {
            return null;
        }
        return new File(location);
    }
}
