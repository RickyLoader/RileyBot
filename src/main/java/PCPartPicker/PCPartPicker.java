package PCPartPicker;

import Command.Structure.HTMLUtils;
import PCPartPicker.Component.CATEGORY;
import PCPartPicker.Component.PurchaseLocation;
import Steam.Price;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Scraping functionality for https://nz.pcpartpicker.com
 */
public class PCPartPicker {
    private static final String BASE_URL_REGEX = "https://(.+\\.)?pcpartpicker.com/";
    public static final String LOGO = "https://i.imgur.com/6liMKqF.png";

    // Component lists come in various formats
    private enum LIST_TYPE {

        // https://nz.pcpartpicker.com/list/CdCdrV
        LIST(BASE_URL_REGEX + "list/.+"),

        // https://nz.pcpartpicker.com/guide/MWv6Mp/great-amd-gaming-build
        GUIDE(BASE_URL_REGEX + "guide/.+/.+"),

        // https://nz.pcpartpicker.com/b/cR27YJ
        BUILD(BASE_URL_REGEX + "b/.+");

        private final String regex;

        /**
         * Create a component list type
         *
         * @param regex Regular expression used to match a URL for a component list of this type
         */
        LIST_TYPE(String regex) {
            this.regex = regex;
        }

        /**
         * Get the list type from a URL to a component list
         *
         * @param url URL to check e.g "https://nz.pcpartpicker.com/list/CdCdrV"
         * @return List type or null
         */
        @Nullable
        public static LIST_TYPE getListTypeByUrl(String url) {
            for(LIST_TYPE type : LIST_TYPE.values()) {
                if(!url.matches(type.getRegex())) {
                    continue;
                }
                return type;
            }
            return null;
        }

        /**
         * Get the regular expression used to match a URL for a component list of this type.
         *
         * @return URL regex
         */
        public String getRegex() {
            return regex;
        }
    }

    /**
     * Check if the given URL is a URL to a component list.
     *
     * @param url URL to check e.g "https://nz.pcpartpicker.com/list/CdCdrV"
     * @return URL is a URL to a component list
     */
    public static boolean isListUrl(String url) {
        return LIST_TYPE.getListTypeByUrl(url) != null;
    }

    /**
     * Attempt to scrape a PC component list from the given URL to the list.
     *
     * @param listUrl URL to a component list e.g "https://nz.pcpartpicker.com/list/CdCdrV"
     * @return Component list or null
     */
    @Nullable
    public static ComponentList fetchComponentListByUrl(String listUrl) {
        try {
            LIST_TYPE type = LIST_TYPE.getListTypeByUrl(listUrl);
            if(type == null) {
                throw new Exception("Not a list URL: " + listUrl);
            }

            Document listPage = Jsoup.connect(listUrl).get();

            // "New Zealand"
            final String countryName = listPage
                    .selectFirst(".pp-country-select")
                    .getElementsByAttribute("selected")
                    .first()
                    .text();

            String name = listPage.selectFirst(".pageTitle").text(), imageUrl = null;

            if(type == LIST_TYPE.BUILD) {
                Element imageElement = HTMLUtils.getMetaTag(listPage, "property", "og:image");
                if(imageElement != null) {
                    imageUrl = imageElement.absUrl("content");
                }

                // A build page displays components differently, navigate to its component list page before scraping
                listPage = Jsoup.connect(
                        listPage
                                .selectFirst(".header-actions")
                                .child(0)
                                .absUrl("href")
                ).get();
            }

            ArrayList<Component> components = parseComponents(listPage, countryName);
            if(components == null) {
                throw new Exception("Failed to parse components: " + listUrl);
            }

            // Total price summary of components
            Element total = listPage.selectFirst(".tr__total .td__price");

            ComponentList componentList = new ComponentList(
                    name,
                    listUrl,
                    imageUrl,
                    total == null ? null : new Price(parsePrice(total.text()), countryName)
            );

            for(Component component : components) {
                componentList.addComponent(component);
            }

            return componentList;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Attempt to scrape a list of components from the given HTML document of a component list page.
     *
     * @param listPage    Component list HTML document
     * @param countryName Country of origin for component list - e.g "New Zealand"
     * @return List of components on page
     */
    @Nullable
    private static ArrayList<Component> parseComponents(Document listPage, String countryName) {
        try {
            ArrayList<Component> componentList = new ArrayList<>();

            // List of components in the build
            Elements componentListElements = listPage
                    .selectFirst(".partlist tbody")
                    .getElementsByClass("tr__product");

            for(Element componentElement : componentListElements) {
                Element priceElement = componentElement.selectFirst(".td__price");
                PurchaseLocation purchaseLocation = null;

                // No price has a span element saying "No Prices Available"
                if(priceElement.selectFirst("span") == null) {

                    // Hardcoded price - no store
                    if(priceElement.childrenSize() == 1) {
                        purchaseLocation = new PurchaseLocation(
                                new Price(

                                        // "$100 CURRENCY_CODE" -> "$100"
                                        parsePrice(priceElement.ownText().split(" ")[0]),
                                        countryName
                                )
                        );
                    }

                    // Price with link to store
                    else {
                        priceElement = priceElement.selectFirst("a");
                        purchaseLocation = new PurchaseLocation(
                                new Price(
                                        parsePrice(priceElement.text()),
                                        countryName
                                ),
                                priceElement.absUrl("href")
                        );
                    }
                }

                Element nameElement = componentElement.selectFirst(".td__name a");

                componentList.add(
                        new Component(
                                CATEGORY.byName(componentElement.selectFirst(".td__component").text()),
                                nameElement.text(),
                                nameElement.absUrl("href"),
                                componentElement.selectFirst(".td__image img").absUrl("src"),
                                purchaseLocation
                        )
                );
            }

            return componentList;
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Parse the given price String (in the format $100.00) to a double
     *
     * @param priceString Price String
     * @return Price as double
     */
    private static double parsePrice(String priceString) {
        return Double.parseDouble(priceString.replace("$", ""));
    }
}
