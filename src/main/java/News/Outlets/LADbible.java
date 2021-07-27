package News.Outlets;

import Command.Structure.HTMLUtils;
import News.Article;
import News.Author;
import News.Image;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LADbible news parsing via scraping their website
 */
public class LADbible extends NewsOutlet {
    private static final String BASE_URL = "https://www.ladbible.com/";

    /**
     * Initialise LADbible values
     */
    public LADbible() {
        super(
                "LADbible",
                "https://i.imgur.com/FCfoRUl.png",
                BASE_URL + "news/[a-z-\\d]+(-\\d+)(/)?(.+)?"
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        articleUrl = articleUrl.split("\\?")[0]; // Strip parameters
        try {
            Document articleDocument = getArticleHtmlDocument(articleUrl);

            // Error fetching article page
            if(articleDocument == null) {
                throw new Exception("Unable to retrieve HTML for article: " + articleUrl);
            }

            return new Article(
                    articleUrl,
                    articleUrl, // Data URL is the same (as the data is scraped)
                    parseAuthors(articleDocument),
                    articleDocument.getElementsByClass("css-1h8vhnh").first().text(),

                    // Open graph 'description' tag holds the brief article summary - may not always be present
                    HTMLUtils.getMetaTagValue(articleDocument, "property", "og:description"),
                    parseDate(articleDocument.getElementsByTag("time").get(0).attr("datetime")),
                    parseImages(articleDocument)
            );
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse a list of authors from the given article HTML document.
     *
     * @param articleDocument Article HTML document
     * @return List of the article authors (may be empty)
     */
    private ArrayList<Author> parseAuthors(Document articleDocument) {
        ArrayList<Author> authors = new ArrayList<>();

        final Pattern imageRegex = Pattern.compile("(?!\")(" + BASE_URL + "cdn-cgi/image/.+)(?=\")");

        Elements authorElements = articleDocument.getElementsByClass("css-1wkce1i-Byline");

        for(Element authorElement : authorElements) {

            // Access author info via index as the classes are dynamic
            Elements authorParts = authorElement.children(); // [0] Avatar style [1] Avatar [2] Name style [3] Name

            /*
             * Author image URL is applied as a "background-image" to the author avatar HTML element via CSS.
             * Attempt to pull the URL from the CSS.
             */
            String avatarElementStyle = authorParts
                    .get(0)
                    .html();

            String imageUrl = null;
            Matcher matcher = imageRegex.matcher(avatarElementStyle);

            // Matched author image URL
            if(matcher.find()) {
                imageUrl = avatarElementStyle.substring(matcher.start(), matcher.end());
            }

            authors.add(
                    new Author(
                            authorParts.get(3).text(),
                            imageUrl,
                            null
                    )
            );
        }
        return authors;
    }

    /**
     * Parse a list of images from the given article HTML document.
     *
     * @param articleDocument Article HTML document
     * @return List of images in the article (may be empty)
     */
    private ArrayList<Image> parseImages(Document articleDocument) {
        ArrayList<Image> images = new ArrayList<>();

        // Find article pictures via index as the classes are dynamic
        Elements pictures = articleDocument

                // Article container children: [0] Nav bar style [1] Nav bar [2] Main article style [3] Main article
                .getElementsByClass("css-1davbuc-ArticleContainer")
                .first()
                .children()

                // [3] Main article
                .get(3)

                // Picture elements hold various sizes of an image
                .getElementsByTag("picture");

        for(Element picture : pictures) {
            Element parent = picture.parent();
            String caption = null;


            // Picture elements may be a child of a 'figure' element, which may have a caption.
            if(parent.tagName().equals("figure")) {
                Elements captions = parent.getElementsByTag("figcaption");

                // Image has caption
                if(!captions.isEmpty()) {
                    caption = captions.first().text();
                }
            }

            images.add(
                    new Image(
                            picture.child(0).attr("srcset"), // Take the first image size
                            caption
                    )
            );
        }

        return images;
    }

    /**
     * Get the HTML document for the given article URL
     *
     * @param articleUrl Article URL to fetch HTML document for
     * @return HTML document for article or null (if unable to retrieve)
     */
    @Nullable
    private Document getArticleHtmlDocument(String articleUrl) {
        try {
            return Jsoup.connect(articleUrl).get();
        }
        catch(IOException e) {
            return null;
        }
    }

    @Override
    public Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
