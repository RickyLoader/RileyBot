package TikTokAPI;

import Bot.ResourceHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Date;
import java.util.Random;

/**
 * Signing process for TikTok API.
 * API URLs must have signature parameters generated & attached to function
 */
public class Signer {
    private final String userAgent;
    private final ChromeDriver browser;
    public static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36";

    /**
     * Initialise a headless browser to the TikTok home page and load the signing script in to the page.
     * From here the signing function in their Javascript can be called.
     * A modified chrome driver executable is used to bypass Selenium detection.
     *
     * @param userAgent User agent to use when signing. Requests made with a
     *                  signed URL must be made with the same user agent.
     * @see <a href="https://stackoverflow.com/a/52108199/7466895">Modified chrome driver</a>
     * @see <a href="https://github.com/carcabot/tiktok-signature">Signing script</a>
     */
    public Signer(String userAgent) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        this.browser = new ChromeDriver(getBrowserOptions(userAgent));
        this.userAgent = userAgent;

        // Open TikTok home page
        browser.get(TikTok.BASE_WEB_URL);

        // Load signing script
        final String script = new ResourceHandler().getResourceFileAsString("/TikTok/signer.js");
        browser.executeScript(script);
    }

    /**
     * Get the browser options to use.
     *
     * @param userAgent User agent to use when signing. Requests made with a
     *                  signed URL must be made with the same user agent.
     * @return Browser options
     */
    private ChromeOptions getBrowserOptions(String userAgent) {
        return new ChromeOptions()
                .setHeadless(true)
                .addArguments(
                        "--disable-blink-features",
                        "--disable-blink-features=AutomationControlled",
                        "--disable-infobars",
                        "--window-size=1920,1080",
                        "--start-maximized",
                        "user-agent=" + userAgent
                );
    }

    /**
     * Get the user agent used to sign TikTok API URLs.
     * Requests made with a signed URL must use the same user agent (or the response will be empty).
     *
     * @return User agent used to sign URLs
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sign the given TikTok API URL.
     * For an API URL to function, there are two required parameters that must be generated.
     * These are "_signature" and "verifyFp".
     * As the signature is generated from the given URL and the user agent of the browser,
     * it cannot be used on a different URL or with a different user agent (no data will be returned).
     *
     * @param apiUrl Tiktok API URL e.g "https://m.tiktok.com/api/item/detail/?itemId=VIDEO_ID"
     * @return Signed URL - e.g "{API_URL}&verifyFp={VERIFY_FP}&_signature={SIGNATURE}" or null (if an error occurs generating the required parameters)
     */
    @Nullable
    public String signUrl(String apiUrl) {

        // Generate the verifyFp parameter and attach to URL
        final String verifyFp = generateVerifyFp();
        final String withVerifyFp = apiUrl + "&verifyFp=" + verifyFp;

        // Pass the URL to the function to generate the _signature parameter
        final String signature = generateSignature(withVerifyFp);

        // Couldn't execute signing Javascript
        if(signature == null) {
            return null;
        }

        // Attach signature parameter to URL
        return withVerifyFp + "&_signature=" + signature;
    }

    /**
     * Generate a verifyFp.
     * This is the value for the required "verifyFp" parameter.
     *
     * @return Value for verifyFp parameter e.g "verify_kqxw7mut_28vdpiPx_cNqD_4snT_AeT6_tn8GXsYQI2fy"
     */
    private String generateVerifyFp() {
        final String[] args = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");
        final String dateString = Long.toString(new Date().getTime(), 36);

        final String[] values = new String[36];
        values[8] = values[13] = values[18] = values[23] = "_";
        values[14] = "4";

        for(int i = 0; i < values.length; i++) {
            int value = (int) (new Random().nextDouble() * args.length);
            int index = i == 19 ? ((3 & value) | 8) : value;
            if(values[i] != null) {
                continue;
            }
            values[i] = args[index];
        }
        return "verify_" + dateString + "_" + StringUtils.join(values, "");
    }

    /**
     * Generate a signature for the given TikTok API URL.
     * This is the value for the required "_signature" parameter, and is generated by passing an API URL
     * to a function (window.byted_acrawler.sign) in the TikTok Javascript.
     * The function uses the given URL, the user agent of the browser, and a "verifyFp" parameter to create the signature.
     *
     * @param apiUrl Tiktok API URL e.g "https://m.tiktok.com/api/item/detail/?itemId=VIDEO_ID&verifyFp={VERIFY_FP}"
     * @return Signature for URL e.g "_02B4Z6wo00f01.toP-wAAIDAJwQgRNdTiY.7SDtAAJ4989" or null (if an error occurs)
     */
    @Nullable
    private String generateSignature(String apiUrl) {
        try {
            final String function = "window.byted_acrawler.sign";   // Javascript function responsible for signing URLs
            final String urlArg = "\"" + apiUrl + "\"";             // "URL_TO_SIGN"
            final String args = "({" + "url:" + urlArg + "})";      // ({url: "URL_TO_SIGN"})
            return (String) browser.executeScript("return " + function + args + ";");
        }
        catch(Exception e) {
            return null;
        }
    }
}