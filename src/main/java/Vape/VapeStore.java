package Vape;

import Network.NetworkRequest;
import Network.NetworkResponse;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Vape store products
 */
public class VapeStore {
    public final String name, productsUrl, urlRegex, logo;
    private HashMap<Long, Product> products;
    private long lastUpdate;

    /**
     * Initialise the vape store
     *
     * @param name        Store name
     * @param productsUrl Base URL to the store products - e.g https://vapourium.nz/products
     * @param logo        URL to the store logo - e.g https://i.imgur.com/cAZkQRq.png
     */
    public VapeStore(String name, String productsUrl, String logo) {
        this.name = name;
        this.productsUrl = productsUrl;
        this.urlRegex = productsUrl + "/(.+)";
        this.logo = logo;
        refreshProductMap();
    }

    /**
     * Get a product by the unique id.
     *
     * @param id Product id
     * @return Product with the given id or null
     */
    public Product getProductById(long id) {
        refreshProductMap();
        return products.get(id);
    }

    /**
     * Get a list of products matching/containing the given name.
     * If a singular match is found, the returned list will contain only the match.
     *
     * @param name Product name/query
     * @return List of product search results
     */
    public ArrayList<Product> getProductsByName(String name) {
        refreshProductMap();
        ArrayList<Product> results = getProductList()
                .stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Product> matching = results
                .stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .collect(Collectors.toCollection(ArrayList::new));
        return matching.size() == 1 ? matching : results;
    }

    /**
     * Recursively fetch and add the vape store products to the given map.
     * When a page returns an empty list, there are no more products to retrieve.
     *
     * @param products Map of unique product id -> product
     * @param page     Page to fetch
     * @return Map of vape store products
     */
    private HashMap<Long, Product> addProducts(HashMap<Long, Product> products, int page) {
        String url = productsUrl + ".json?limit=250&page=" + page;
        System.out.println("Fetching " + name + " products: Page " + page);
        JSONObject response = new JSONObject(
                new NetworkRequest(url, false).get().body
        );

        JSONArray productArray = response.getJSONArray("products");
        if(productArray.isEmpty()) {
            return products;
        }

        for(int i = 0; i < productArray.length(); i++) {
            Product product = parseProduct(productArray.getJSONObject(i));
            if(product == null) {
                continue;
            }
            products.put(product.getId(), product);
        }
        return addProducts(products, page + 1);
    }

    /**
     * Parse a product from the given product JSON
     *
     * @param product Product JSON to parse
     * @return Product from JSON or null
     */
    private Product parseProduct(JSONObject product) {
        long id = product.getLong("id");
        String type = product.getString("product_type");
        if(type.equalsIgnoreCase("options_hidden_product")) {
            return null;
        }
        JSONArray optionsList = product.getJSONArray("options");
        ArrayList<Variant> variants = parseProductVariants(
                product.getJSONArray("variants"),
                optionsList.length()
        );
        String description = parseDescription(product.getString("body_html"));
        return new Product(
                id,
                product.getString("title"),
                parsePriceRange(variants),
                productsUrl + "/" + product.getString("handle"),
                description.isEmpty() ? "-" : description,
                product.getString("product_type"),
                parseDate(product.getString("created_at")),
                parseDate(product.getString("updated_at")),
                parseImages(product.getJSONArray("images")),
                parseOptions(optionsList, variants)
        );
    }

    /**
     * Parse the price range of the product variants
     *
     * @param variants List of product variants, each with a price
     * @return Price range of product variants - lowest & highest price
     */
    private Pair<Double, Double> parsePriceRange(ArrayList<Variant> variants) {
        // Sort by price low -> high
        variants.sort(Comparator.comparingDouble(Variant::getPrice));
        return new Pair<>(variants.get(0).getPrice(), variants.get(variants.size() - 1).getPrice());
    }

    /**
     * Parse the product description from the given HTML body of a product.
     *
     * @param bodyHtml HTML body of product, description is contained within.
     * @return Product description
     */
    private String parseDescription(String bodyHtml) {
        try {
            Document document = Jsoup.parse(bodyHtml);
            Element span = document.selectFirst("span");
            return span == null ? document.selectFirst("p").text() : span.text();
        }
        catch(Exception e) {
            return bodyHtml
                    .replaceAll("<.*?>", "")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
    }

    /**
     * Parse the list of product variants from the given JSON array.
     * Product variants are unique combinations of product option values.
     * E.g if the product has a "volume" option with values 60ml & 120ml, and a "nicotine" option with values 1mg & 6mg,
     * the resulting variants will be "60ml / 1mg", "60ml / 6mg", "120ml / 1mg", and "120ml / 6mg".
     *
     * @param variantList Product variants JSON
     * @param numOptions  Number of product options
     * @return List of product variants
     */
    private ArrayList<Variant> parseProductVariants(JSONArray variantList, int numOptions) {
        ArrayList<Variant> variants = new ArrayList<>();
        String availableKey = "available";
        for(int i = 0; i < variantList.length(); i++) {
            JSONObject variantData = variantList.getJSONObject(i);
            Variant variant = new Variant(
                    variantData.getDouble("price"),
                    variantData.has(availableKey)
                            ? variantData.getBoolean(availableKey)
                            : variantData.getInt("inventory_quantity") > 0
            );

            /*
             * If a product has 2 options, every variant will have a value for both options,
             * stored as "option1" and "option2"
             */
            for(int j = 1; j <= numOptions; j++) {
                variant.addOptionValue(variantData.getString("option" + j));
            }
            variants.add(variant);
        }
        return variants;
    }

    /**
     * Create a map where the keys are every unique product option value, and the values
     * are whether that option value is in stock in any variants.
     *
     * @param variants List of product variants
     * @return Map of option value -> in stock
     */
    private HashMap<String, Boolean> getOptionAvailability(ArrayList<Variant> variants) {
        HashMap<String, Boolean> optionAvailability = new HashMap<>();

        for(Variant variant : variants) {
            ArrayList<String> optionValues = variant.getOptionValues();

            for(String optionValue : optionValues) {
                if(optionAvailability.containsKey(optionValue)) {

                    // Only overwrite false -> true
                    if(optionAvailability.get(optionValue) || !variant.inStock()) {
                        continue;
                    }
                }
                optionAvailability.put(optionValue, variant.inStock());
            }
        }
        return optionAvailability;
    }

    /**
     * Parse the given date String in to a Date
     *
     * @param dateString Date String to parse
     * @return Date from date String or current Date if unable to parse
     */
    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }

    /**
     * Get a list of products in descending order of creation date.
     *
     * @param numProducts Number of products to return (out of bounds will return all products)
     * @return Latest products
     */
    public ArrayList<Product> getLatestProducts(int numProducts) {
        ArrayList<Product> products = getProductList();
        products.sort((o1, o2) -> dateSort(o1.getCreated(), o2.getCreated(), true));
        if(numProducts <= 0 || numProducts > products.size()) {
            return products;
        }
        return new ArrayList<>(products.subList(0, numProducts));
    }

    /**
     * Get a comparator for sorting products by their creation date
     *
     * @param descending Sort in descending order of creation date
     * @return Comparator for sorting products by their creation date
     */
    public static int dateSort(Date d1, Date d2, boolean descending) {
        return descending ? d2.compareTo(d1) : d1.compareTo(d2);
    }

    /**
     * Get the products as a list
     *
     * @return Products as list
     */
    private ArrayList<Product> getProductList() {
        return new ArrayList<>(products.values());
    }

    /**
     * Parse a list of options from the given options JSON
     *
     * @param optionList Product options JSON
     * @param variants   List of product variants
     * @return List of options from the given JSON
     */
    private ArrayList<Option> parseOptions(JSONArray optionList, ArrayList<Variant> variants) {
        ArrayList<Option> options = new ArrayList<>();

        // Map of option value -> in stock (in at least one product variant)
        HashMap<String, Boolean> optionAvailability = getOptionAvailability(variants);

        for(int i = 0; i < optionList.length(); i++) {
            JSONObject optionData = optionList.getJSONObject(i);
            String name = optionData.getString("name");
            if(name.equalsIgnoreCase("title")) {
                continue;
            }
            JSONArray values = optionData.getJSONArray("values");
            Option option = new Option(name);

            for(int j = 0; j < values.length(); j++) {
                String value = values.getString(j);
                option.addValue(
                        new Option.Value(
                                value,
                                optionAvailability.get(value.toLowerCase())
                        )
                );
            }
            options.add(option);
        }
        return options;
    }

    /**
     * Parse a list of image URLs from the given images JSON
     *
     * @param imageList Product images JSON
     * @return List of image URLs from the given JSON
     */
    private ArrayList<String> parseImages(JSONArray imageList) {
        ArrayList<String> images = new ArrayList<>();

        for(int i = 0; i < imageList.length(); i++) {
            JSONObject imageData = imageList.getJSONObject(i);
            images.add(imageData.getString("src").split("\\?")[0]);
        }
        return images;
    }

    /**
     * Fetch the list of products from the vape store
     * Map from the unique product id -> product
     *
     * @return Map of products
     */
    private HashMap<Long, Product> fetchProducts() {
        return addProducts(new HashMap<>(), 1);
    }

    /**
     * Check if an update of the products is due to occur
     *
     * @return Update is due to occur
     */
    public boolean updateDue() {
        return System.currentTimeMillis() - lastUpdate >= 3600000;
    }

    /**
     * Refresh the product map if an hour or more has passed
     */
    private void refreshProductMap() {
        if(!updateDue()) {
            return;
        }
        this.products = fetchProducts();
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Check if the given URL is a vape store product URL
     *
     * @param url URL to check
     * @return URL is a vape store product URL
     */
    public boolean isStoreUrl(String url) {
        return url.matches(urlRegex);
    }

    /**
     * Get a product from a vape store URL
     *
     * @param url URL to get product from
     * @return Product from URL or null
     */
    public Product getProductByUrl(String url) {
        if(!isStoreUrl(url)) {
            return null;
        }
        url = url.split("\\?")[0];
        if(url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        NetworkResponse response = new NetworkRequest(url + ".json", false).get();
        if(response.code != 200) {
            return null;
        }
        return parseProduct(new JSONObject(response.body).getJSONObject("product"));
    }

    /**
     * Get the URL to the store logo
     *
     * @return URL to store logo
     */
    public String getLogo() {
        return logo;
    }

    /**
     * Get the store name
     *
     * @return Store name
     */
    public String getName() {
        return name;
    }
}
