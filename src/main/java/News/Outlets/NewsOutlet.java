package News.Outlets;

import News.Article;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * News website
 */
public abstract class NewsOutlet {
    private final String name, articleUrlRegex, logo;

    /**
     * Create a news outlet
     *
     * @param name            Name of the outlet - e.g "Stuff"
     * @param logo            URL to the outlet's logo image
     * @param articleUrlRegex Regular expression to match article URLs from this news outlet
     */
    public NewsOutlet(String name, String logo, String articleUrlRegex) {
        this.name = name;
        this.logo = logo;
        this.articleUrlRegex = articleUrlRegex;
    }

    /**
     * Get the URL to the outlet's logo image
     *
     * @return Logo image URL
     */
    public String getLogo() {
        return logo;
    }

    /**
     * Check if the given query is a URL to an article from this news outlet
     *
     * @param query Query to check
     * @return Query is an article URL
     */
    public boolean isNewsUrl(String query) {
        return query.matches(articleUrlRegex);
    }

    /**
     * Get the article info from the given URL.
     * This will be null if the URL is not a valid article URL.
     *
     * @param articleUrl URL to article
     * @return Article info or null (if any errors occur/URL is not a news article)
     */
    @Nullable
    public Article getArticleByUrl(String articleUrl) {
        if(!isNewsUrl(articleUrl)) {
            return null;
        }
        return parseArticleByUrl(articleUrl);
    }

    /**
     * Get the article info from the given URL.
     * The article URL is assumed valid.
     *
     * @param articleUrl URL to article
     * @return Article info or null (if any errors occur/URL is not a news article)
     */
    @Nullable
    protected abstract Article parseArticleByUrl(String articleUrl);

    /**
     * Get the name of the outlet - e.g "Stuff"
     *
     * @return News outlet name
     */
    public String getName() {
        return name;
    }

    /**
     * Parse the given article date String in to a Date.
     * The date String should be in ISO 8601 format.
     *
     * @param dateString Date String in ISO 8601 format - e.g "20200320T212450+1300"
     * @return Date from date String (or today's date if unable to parse)
     */
    public Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyyMMdd'T'HHmmssZZZZZ").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
