package Command.Commands;

import Command.Structure.*;
import Hangman.DictWord;
import Hangman.Dictionary;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Get the definitions of words!
 */
public class DictionaryCommand extends DiscordCommand {
    private final String footer, thumbnail;

    public DictionaryCommand() {
        super("define", "Get the dictionary definition of a word!", "define [word]");
        this.footer = "Try: " + getHelpName();
        this.thumbnail = "https://i.imgur.com/pRu1hDN.png";
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        String word = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();

        if(word.isEmpty()) {
            channel.sendMessage(member.getAsMention() + " You forgot the word bro").queue();
            return;
        }

        DictWord dictWord = Dictionary.getInstance().getWord(word);
        if(dictWord == null) {
            ArrayList<DictWord> similar = Dictionary.getInstance().searchWords(word);
            showSimilarWords(context, similar, word);
            return;
        }
        channel.sendTyping().queue();
        channel.sendMessage(buildDictionaryWordEmbed(dictWord)).queue();
    }

    /**
     * Display a list of words and truncated descriptions in a pageable embed.
     * The list of words are the result of searching the dictionary for the given query.
     *
     * @param context Command context
     * @param words   List of words found from the given query
     * @param query   Query used to find words
     */
    private void showSimilarWords(CommandContext context, ArrayList<DictWord> words, String query) {
        new PageableTableEmbed(
                context,
                words,
                thumbnail,
                "Dictionary Search",
                (words.isEmpty() ? "No" : words.size()) + " words found for: **" + query + "**",
                footer,
                new String[]{
                        "Word",
                        "Definition"
                },
                5,
                words.isEmpty() ? EmbedHelper.RED : EmbedHelper.ORANGE
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                DictWord dictWord = (DictWord) items.get(index);
                return new String[]{
                        dictWord.getWord(),
                        dictWord.getTruncatedDescription(50)
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance(query, defaultSort) {
                    @Override
                    public String getString(Object o) {
                        return ((DictWord) o).getWord();
                    }
                });

            }
        }.showMessage();
    }

    /**
     * Build a message embed detailing the given dictionary word.
     * Attempt to retrieve an image based on the word to use in the embed,
     * if unsuccessful the embed will not contain an image.
     *
     * @param dictWord Dictionary word to display in the embed
     * @return Message embed detailing dictionary word
     */
    private MessageEmbed buildDictionaryWordEmbed(DictWord dictWord) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(EmbedHelper.ORANGE)
                .setTitle("Dictionary | " + dictWord.getWord())
                .setDescription(dictWord.getTruncatedDescription(200))
                .setThumbnail(thumbnail)
                .setFooter(footer);

        String imageUrl = getImageForWord(dictWord.getWord());
        if(imageUrl != null) {
            builder.setImage(imageUrl);
        }
        return builder.build();
    }

    /**
     * Get a random image for the given word
     *
     * @param word Word to search
     * @return Random image or null
     */
    private String getImageForWord(String word) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Client-ID " + Secret.UNSPLASH_CLIENT_ID);
        String url = "https://api.unsplash.com/search/photos?page=1&query=" + EmbedHelper.urlEncode(word);
        NetworkResponse response = new NetworkRequest(url, false).get(headers);
        JSONObject data = new JSONObject(response.body);
        if(response.code != 200 || data.getLong("total") == 0) {
            return null;
        }
        JSONArray results = data.getJSONArray("results");
        JSONObject randomResult = results.getJSONObject(new Random().nextInt(results.length()));
        return randomResult.getJSONObject("urls").getString("full");
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
