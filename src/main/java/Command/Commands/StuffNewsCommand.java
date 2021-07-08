package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.CyclicalPageableEmbed;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import News.Article;
import News.StuffNews;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Take Stuff news URLs and embed details about the article
 */
public class StuffNewsCommand extends DiscordCommand {
    private final String[] prefixes;
    private final Random random;

    /**
     * Set to secret to prevent showing in help command
     * Generate an array of news related prefixes to use when referring to a member.
     */
    public StuffNewsCommand() {
        super("[Stuff NZ URL]", "View some Stuff articles!");
        this.random = new Random();
        this.prefixes = getPrefixes();
        setSecret(true);
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
        Article article = StuffNews.getArticleByUrl(context.getLowerCaseMessage());

        // Not an article/error fetching
        if(article == null) {
            return;
        }

        context.getMessage().delete().queue(deleted -> displayArticle(context, article));
    }

    /**
     * Display the given article in a pageable message (to page through images)
     *
     * @param context Command context
     * @param article Article to display
     */
    private void displayArticle(CommandContext context, Article article) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String memberPrefix = getMemberPrefix();

        new CyclicalPageableEmbed<String>(
                context,
                article.getImages(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setTitle(article.getTitle(), article.getBrowserUrl())
                        .setThumbnail(StuffNews.LOGO)
                        .setColor(EmbedHelper.BLUE)
                        .setDescription(
                                article.getIntro()
                                        + "\n\n**" + memberPrefix + "**: " + context.getMember().getAsMention()
                        )
                        .setFooter(buildFooter(pageDetails));
            }

            /**
             * Build the footer to use in the pageable article message embed.
             * This is in the format "Image: n/n | [optional author - ] article publish date"
             * e.g "Image: 1/2 | Dave Wobbly - 26/03/2020" or "Image: 1/2 | 26/03/2020"
             *
             * @param pageDetails Details about the current page e.g "Image: 1/2"
             * @return Embed footer
             */
            private String buildFooter(String pageDetails) {
                String footer = pageDetails + " | ";
                if(article.hasAuthor()) {
                    footer += article.getAuthor() + " - ";
                }
                return footer + dateFormat.format(article.getDate());
            }

            @Override
            public String getPageDetails() {
                return "Image: " + getPage() + "/" + getPages();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String imageUrl) {
                builder.setImage(imageUrl);
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

    @Override
    public boolean matches(String query, Message message) {
        return StuffNews.isNewsUrl(query);
    }
}
