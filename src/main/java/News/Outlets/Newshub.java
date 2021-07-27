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

import java.util.ArrayList;

import static Command.Structure.HTMLUtils.getMetaTagValue;

/**
 * Newshub news parsing via scraping their website
 */
public class Newshub extends NewsOutlet {
    private static final String
            SOURCE_KEY = "srcset",
            BASE_URL = "https://www.newshub.co.nz/";

    /**
     * Initialise Newshub values
     */
    public Newshub() {
        super(
                "Newshub",
                "https://i.imgur.com/GajefpZ.png",
                BASE_URL + "home/(.+).html(/)?(.+)?"
        );
    }

    @Override
    protected @Nullable Article parseArticleByUrl(String articleUrl) {
        articleUrl = articleUrl.split("\\?")[0]; // Strip parameters
        Document articleDocument = getArticleDocument(articleUrl);

        // Not an article
        if(articleDocument == null) {
            return null;
        }
        return parseArticleDocument(articleDocument, articleUrl);
    }

    /**
     * Parse an article from the given HTML document.
     *
     * @param articleDocument HTML document of the article
     * @param articleUrl      URL to the article
     * @return Article or null (if an error occurs parsing the document)
     */
    @Nullable
    private Article parseArticleDocument(Document articleDocument, String articleUrl) {
        try {
            return new Article(
                    articleUrl,
                    articleUrl, // Data URL is the same (as the data is scraped)
                    parseAuthors(articleDocument),
                    articleDocument.getElementsByClass("c-ArticleHeading-title").get(0).text(),

                    // Open graph 'description' tag holds the brief article summary - may not always be present
                    HTMLUtils.getMetaTagValue(articleDocument, "property", "og:description"),

                    parseDate(getMetaTagValue(articleDocument, "property", "article:published_time")),
                    parseImages(articleDocument)
            );
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse a list of authors from the given HTML document of an article
     *
     * @param articleDocument HTML document of article to get authors from
     * @return List of the article authors (may be empty)
     */
    private ArrayList<Author> parseAuthors(Document articleDocument) {
        final String authorPrefix = "c-ArticleHeading-";
        ArrayList<Author> authors = new ArrayList<>();

        Elements authorData = articleDocument.getElementsByClass(authorPrefix + "authorProfile");

        for(Element author : authorData) {
            Elements images = author.getElementsByTag("img");
            Elements profileUrls = author.getElementsByTag("a");

            authors.add(
                    new Author(
                            author.getElementsByClass(authorPrefix + "authorName").get(0).text(),
                            images.isEmpty() ? null : images.get(0).attr(SOURCE_KEY),
                            profileUrls.isEmpty() ? null : profileUrls.get(0).absUrl("href")
                    )
            );
        }

        return authors;
    }

    /**
     * Parse a list of images from the given HTML document of an article
     *
     * @param articleDocument HTML document of article to get images from
     * @return List of images in article document (may be empty)
     */
    private ArrayList<Image> parseImages(Document articleDocument) {
        ArrayList<Image> imageList = new ArrayList<>();

        /*
         * Attempt to retrieve the open graph image tag to use as the first image.
         * This is the image that appears when platforms embed the URL e.g Discord.
         * It may be a thumbnail of a video or an image from the article.
         */
        String thumbnail = getMetaTagValue(articleDocument, "property", "og:image");
        String thumbnailName = null;

        if(thumbnail != null) {
            imageList.add(new Image(thumbnail, null));
            thumbnailName = getImageFileName(thumbnail);
        }

        // Article 'figures' - not sure if they're always images
        Elements figures = articleDocument.getElementsByClass("c-ArticleFigure");
        for(Element figure : figures) {

            // The 'picture' tag holds various sizes of the same image
            Elements images = figure.getElementsByTag("picture");
            if(images.isEmpty()) {
                continue;
            }

            // The 'img' tag inside 'picture' holds the full sized image
            Elements sources = images.get(0).getElementsByTag("img");
            if(sources.isEmpty()) {
                continue;
            }

            String imageUrl = sources.get(0).attr(SOURCE_KEY);

            // The thumbnail image may be an image selected from within the article, skip if it is seen again
            if(thumbnailName != null) {

                // The URL may differ (different size etc) but the image name should be constant
                if(getImageFileName(imageUrl).equals(thumbnailName)) {
                    continue;
                }
            }
            Elements captions = figure.getElementsByClass("c-ArticleFigure-caption");
            imageList.add(
                    new Image(
                            imageUrl,
                            captions.isEmpty() ? null : captions.get(0).text()
                    )
            );
        }

        return imageList;
    }

    /**
     * Get the file name of an image URL.
     * This is the last argument in an image URL and remains constant despite the non constant size parameters that
     * may precede it.
     *
     * @param imageUrl URL to a Newshub image e.g "https://URL-TO-IMAGE/SIZE/VERSION/IMAGE_NAME.jpg"
     * @return Image file name e.g "IMAGE_NAME.jpg"
     */
    private String getImageFileName(String imageUrl) {
        String[] urlArgs = imageUrl.split("/");
        return urlArgs[urlArgs.length - 1];
    }

    /**
     * Get the HTML document of the given article.
     * This will be null if the article doesn't exist, or the HTML is not a valid article.
     *
     * @param articleUrl URL to Newshub article
     * @return HTML document of article or null
     */
    @Nullable
    private Document getArticleDocument(String articleUrl) {
        try {
            Document document = Jsoup.connect(articleUrl).get();
            String pageType = getMetaTagValue(document, "property", "og:type");

            // Not an article
            if(pageType == null || !pageType.equals("article")) {
                throw new Exception("Not an article!");
            }
            return document;
        }
        catch(Exception e) {
            return null;
        }
    }
}
