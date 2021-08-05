package Command.Commands;

import Command.Structure.*;
import News.Article;
import News.Author;
import News.Image;
import News.Outlets.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * Take news URLs and embed details about the article
 */
public class NewsCommand extends OnReadyDiscordCommand {
    private final String[] prefixes;
    private final Random random;
    private final ArrayList<NewsOutlet> newsOutlets;
    private Emote parseFailEmote;

    /**
     * Set to secret to prevent showing in help command
     * Generate an array of news related prefixes to use when referring to a member & a list of news outlets
     * to monitor URLs from.
     */
    public NewsCommand() {
        super("[News URL]", "View some articles!");
        this.random = new Random();
        this.prefixes = getPrefixes();
        this.newsOutlets = getNewsOutlets();
        setSecret(true);
        setBotInput(true);
    }

    /**
     * Get a list of news outlets to monitor for article URLs
     *
     * @return List of news outlets
     */
    private ArrayList<NewsOutlet> getNewsOutlets() {
        ArrayList<NewsOutlet> newsOutlets = new ArrayList<>();
        newsOutlets.add(new Guardian());
        newsOutlets.add(new LADbible());
        newsOutlets.add(new Newshub());
        newsOutlets.add(new NZHerald());
        newsOutlets.add(new OneNews());
        newsOutlets.add(new StuffNews());
        newsOutlets.add(new HollywoodReporter());
        newsOutlets.add(new RadioNewZealand());
        return newsOutlets;
    }

    /**
     * Get an array of prefix possibilities to use when referring to a member who posted a news article URL.
     *
     * @return Array of member prefixes
     */
    private String[] getPrefixes() {
        return new String[]{
                "Correspondent",
                "Man on the ground",
                "News anchor",
                "Journalist",
                "Reporter",
                "Producer",
                "Produced by",
                "Director",
                "Writer",
                "Editor",
                "Camera operator",
                "Camera man",
                "Broadcast technician",
                "Photographer",
                "Eye witness",
                "Ear witness"
        };
    }

    @Override
    public void execute(CommandContext context) {
        Message message = context.getMessage();
        new Thread(() -> {
            final String url = context.getMessageContent();
            NewsOutlet newsOutlet = getNewsOutletFromUrl(url);

            // URL doesn't match an outlet
            if(newsOutlet == null) {
                return;
            }

            Article article = newsOutlet.getArticleByUrl(context.getMessageContent());

            // Not an article/error fetching
            if(article == null) {
                message.addReaction(parseFailEmote).queue();
                return;
            }

            message.delete().queue(deleted -> displayArticle(context, article, newsOutlet));
        }).start();
    }

    /**
     * Display the given article in a pageable message (to page through images)
     *
     * @param context    Command context
     * @param article    Article to display
     * @param newsOutlet News outlet where article is from
     */
    private void displayArticle(CommandContext context, Article article, NewsOutlet newsOutlet) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String memberPrefix = getMemberPrefix();

        new CyclicalPageableEmbed<Image>(
                context,
                article.getImages(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(article.getTitle(), article.getBrowserUrl())
                        .setThumbnail(newsOutlet.getLogo())
                        .setColor(newsOutlet.getColour())
                        .setDescription(buildDescription())
                        .setFooter(pageDetails + " | Published: " + dateFormat.format(article.getDate()));

                if(article.hasAuthors()) {
                    ArrayList<Author> authors = article.getAuthors();
                    Author primaryAuthor = authors.get(0);

                    String name = primaryAuthor.getName();

                    // "Dave Dobbyn" -> "Dave Dobbyn & 5 more"
                    if(authors.size() > 1) {
                        name += " & " + (authors.size() - 1) + " more";
                    }

                    builder.setAuthor(
                            name,
                            primaryAuthor.hasProfileUrl() ? primaryAuthor.getProfileUrl() : null,
                            primaryAuthor.getImageUrl());
                }
                return builder;
            }

            /**
             * Build the description to use in the pageable article message embed.
             * This is in format [Optional intro text] member prefix: member name
             *
             * @return Embed description
             */
            private String buildDescription() {
                String description = "";
                if(article.hasIntro()) {
                    description += article.getIntro() + "\n\n";
                }
                return description + "**" + memberPrefix + "**: " + context.getMember().getAsMention();
            }

            @Override
            public String getPageDetails() {
                return "Image: " + getPage() + "/" + getPages();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Image image) {
                builder.setImage(image.getUrl());
                if(image.hasCaption()) {
                    builder.getDescriptionBuilder().append("\n\n**Caption**: ").append(image.getCaption());
                }
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No images to display!").build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Get a random prefix to use in the article message embeds when referring to the member who posted
     * the article URL. e.g "correspondent".
     *
     * @return Prefix to refer to member
     */
    private String getMemberPrefix() {
        return prefixes[random.nextInt(prefixes.length)];
    }

    /**
     * Get a news outlet by a URL to an article from the outlet.
     * Return the matching outlet or null (if no match is found).
     *
     * @param url URL to an article
     * @return News outlet where article is from
     */
    @Nullable
    private NewsOutlet getNewsOutletFromUrl(String url) {
        for(NewsOutlet outlet : newsOutlets) {
            if(outlet.isNewsUrl(url)) {
                return outlet;
            }
        }
        return null;
    }

    @Override
    public boolean matches(String query, Message message) {
        return getNewsOutletFromUrl(message.getContentDisplay()) != null;
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.parseFailEmote = emoteHelper.getFail();
    }
}
