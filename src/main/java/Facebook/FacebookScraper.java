package Facebook;

import Network.Secret;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import static Command.Structure.EmbedHelper.downloadVideo;
import static Facebook.Attachments.*;

/**
 * Scrape Facebook posts
 */
public class FacebookScraper {
    public static final String BASE_URL = "https://www.facebook.com/", MBASIC_URL = "https://mbasic.facebook.com/";
    private Set<Cookie> cookies;

    public enum PAGE_TYPE {
        POST,
        VIDEO,
        IGNORE;

        /**
         * Attempt to discern the page type from a Facebook URL
         *
         * @param url    Facebook URL
         * @param domain URL domain
         * @return URL page type
         */
        public static PAGE_TYPE discernType(String url, String domain) {
            String[] args = url.replace(domain, "").split("/");
            String target = args.length == 2 ? args[0] : args[1];
            switch(target) {
                case "videos":
                case "watch":
                    return VIDEO;
                case "posts":
                    return POST;
                default:
                    return IGNORE;
            }
        }

        /**
         * Get the HTML id of the root element of a post with the given page type
         *
         * @return ID of post root element
         */
        public String getPostRootElementId() {
            switch(this) {
                case POST:
                    return "m_story_permalink_view";
                case VIDEO:
                    return "mobile_injected_video_feed_pagelet";
                default:
                    return null;
            }
        }
    }

    /**
     * Fetch the Facebook post from the given url
     *
     * @param url URL to facebook post
     * @return Facebook post
     */
    public FacebookPost fetchFacebookPost(String url) {
        ChromeDriver browser = new ChromeDriver(new ChromeOptions().setHeadless(true));
        try {
            loadCookies(browser);
            PAGE_TYPE type = navigateToPage(browser, url);
            if(type == PAGE_TYPE.IGNORE) {
                throw new Exception("Ignoring " + url);
            }
            Element postElement = getPostElement(browser, type);
            if(postElement == null) {
                throw new Exception("Unable to resolve post element for " + url);
            }
            Attachments attachments = parsePostAttachments(postElement, browser);
            endSession(browser);
            return new FacebookPost(
                    parseUserDetails(postElement),
                    parsePostDetails(postElement, url),
                    type == PAGE_TYPE.POST ? parseNormalPostSocial(postElement) : parseVideoPostSocial(postElement),
                    attachments
            );
        }
        catch(Exception e) {
            endSession(browser);
            return null;
        }
    }

    /**
     * Get the post HTML container element based on the page type
     *
     * @param browser Browser to use
     * @param type    Page type
     * @return Post HTML container element
     */
    private Element getPostElement(ChromeDriver browser, PAGE_TYPE type) {
        try {
            return Jsoup.parse(browser.getPageSource()).getElementById(type.getPostRootElementId());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Navigate to the given Facebook URL and log in as necessary
     * Return the type of page found - e.g a Facebook post URL may actually redirect to a video
     *
     * @param browser Browser to use
     * @param url     URL to navigate to
     * @return Page type
     */
    private PAGE_TYPE navigateToPage(ChromeDriver browser, String url) {
        String loginUrl = getLoginURL(url.replace("www", "mbasic"));
        browser.get(loginUrl);
        List<WebElement> emailInput = browser.findElementsById("m_login_email");
        if(!emailInput.isEmpty()) {
            emailInput.get(0).sendKeys(Secret.FACEBOOK_EMAIL);
            browser.findElementByName("pass").sendKeys(Secret.FACEBOOK_PASSWORD);
            browser.findElementByName("login").click();
        }
        if(browser.getTitle().equals("Content not found")) {
            return PAGE_TYPE.IGNORE;
        }
        return PAGE_TYPE.discernType(browser.getCurrentUrl(), MBASIC_URL);
    }

    /**
     * Save the browser cookies and close
     *
     * @param browser Browser to use
     */
    private void endSession(ChromeDriver browser) {
        saveCookies(browser);
        browser.close();
    }

    /**
     * Parse the post attachments from an HTML element representing a Facebook post
     *
     * @param post    HTML element representing a Facebook post
     * @param browser Browser to use
     * @return Post attachments
     */
    private Attachments parsePostAttachments(Element post, ChromeDriver browser) {
        Elements media = post.child(0).child(0).child(0).child(2).select("a");
        Attachments attachments = new Attachments();

        for(Element attachment : media) {
            String thumbnail = attachment.selectFirst("img").attr("src");
            String href = attachment.attr("href");
            if(href.contains("video")) {
                attachments.addVideo(
                        new VideoAttachment(
                                downloadVideo(decodeVideo(href)),
                                thumbnail
                        )
                );
            }
            else {
                String image = getFullSizedImage(
                        MBASIC_URL + attachment.attr("href").replaceFirst("/", ""),
                        browser
                );
                attachments.addImage(image == null ? thumbnail : image);
            }
        }
        return attachments;
    }

    /**
     * Get the full sized image URL for a link to an image given in a Facebook post.
     * mbasic.facebook displays a thumbnail sized image with a link to where the full sized image can be found,
     * navigate to this link and obtain the full sized image URL.
     *
     * @param imageURL URL to where full sized image URL can be found
     * @param browser  Browser to use
     * @return Full sized image URL
     */
    private String getFullSizedImage(String imageURL, ChromeDriver browser) {
        try {
            browser.get(imageURL);
            Document document = Jsoup.parse(browser.getPageSource());
            return document
                    .getElementById("MPhotoContent")
                    .selectFirst(".attachment")
                    .getElementsByClass("sec")
                    .get(0)
                    .attr("href");
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Decode the given video URL
     *
     * @param encodedUrl Encoded video URL
     * @return Decoded video URL
     */
    private String decodeVideo(String encodedUrl) {
        try {
            String destination = encodedUrl.split("\\?src=")[1];
            return URLDecoder.decode(destination, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            return encodedUrl;
        }
    }

    /**
     * Parse the social response from an HTML element representing a standard Facebook post.
     *
     * @param post HTML element representing a standard Facebook post
     * @return Social response to post
     */
    private SocialResponse parseNormalPostSocial(Element post) {
        Elements footer = post.child(1).child(0).children();
        Element commentBlock = footer.get(4).childrenSize() == 0 ? footer.get(3) : footer.get(4);
        return new SocialResponse(
                SocialResponse.parseSocialResponse(footer.get(2).text()),
                commentBlock.childrenSize()
        );
    }

    /**
     * Parse the social response from an HTML element representing a Facebook video post
     *
     * @param post HTML element representing a Facebook video post
     * @return Social response to post
     */
    private SocialResponse parseVideoPostSocial(Element post) {
        Elements footer = post.selectFirst("._5sq4").child(1).select("a");
        return new SocialResponse(
                SocialResponse.parseSocialResponse(footer.get(0).text()),
                SocialResponse.parseSocialResponse(footer.get(3).text())
        );
    }

    /**
     * Load any stored cookies in to the browser instance.
     * This helps maintain a Facebook login session and also reduce the chance of getting slammed.
     *
     * @param browser Browser Browser to load cookies into
     */
    private void loadCookies(ChromeDriver browser) {
        if(cookies == null) {
            return;
        }
        browser.get(MBASIC_URL);
        for(Cookie cookie : cookies) {
            browser.manage().addCookie(cookie);
        }
    }

    /**
     * Save the cookies from the given browser instance.
     *
     * @param browser Browser to save cookies from
     */
    private void saveCookies(ChromeDriver browser) {
        this.cookies = browser.manage().getCookies();
    }

    /**
     * Get a URL to go to the Facebook login page and redirect once logged in.
     * Will also redirect if already logged in
     *
     * @param url Destination URL
     * @return Login url
     */
    private String getLoginURL(String url) {
        String loginUrl = MBASIC_URL + "login.php?next=";
        try {
            return loginUrl + URLEncoder.encode(url, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            return loginUrl + url;
        }
    }

    /**
     * Parse the user page details from the given HTML element representing a Facebook post.
     * User thumbnail, name, and URL
     *
     * @param post HTML element representing Facebook post
     * @return User page details
     */
    private UserDetails parseUserDetails(Element post) {
        Elements userInfo = post.selectFirst("tbody").selectFirst("tr").children();
        Elements thumbnail = userInfo.get(0).select("img");
        Element pageName = userInfo.get(1).selectFirst("span").selectFirst("a");
        return new UserDetails(
                pageName.text(),
                BASE_URL + pageName.attr("href").split("\\?")[0].replaceFirst("/", ""),
                thumbnail.isEmpty()
                        ? "https://i.stack.imgur.com/l60Hf.png"
                        : thumbnail.get(0).attr("src")
        );
    }

    /**
     * Parse the post details from the given HTML element representing a Facebook post.
     * Post text, date published, and URL
     *
     * @param post HTML element representing Facebook post
     * @param url  URL to the Facebook post
     * @return Post details
     */
    private PostDetails parsePostDetails(Element post, String url) {
        return new PostDetails(
                post.selectFirst("p").parent().text(),
                post.selectFirst("abbr").text(),
                url
        );
    }
}
