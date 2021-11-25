package PCPartPicker;

import Steam.Price;
import org.jetbrains.annotations.Nullable;

/**
 * PC component
 */
public class Component {
    private final String name, imageUrl, url;
    private final PurchaseLocation purchaseLocation;
    private final CATEGORY category;

    public enum CATEGORY {
        CPU("CPU"),
        CPU_COOLER("CPU Cooler"),
        MOTHERBOARD("Motherboard"),
        MEMORY("Memory"),
        STORAGE("Storage"),
        VIDEO_CARD("Video Card"),
        CASE("Case"),
        POWER_SUPPLY("Power Supply"),
        OPERATING_SYSTEM("Operating System"),
        MONITOR("Monitor"),
        SOUND_CARD("Sound Card"),
        WIRED_NETWORK_ADAPTER("Wired Network Adapter"),
        WIRELESS_NETWORK_ADAPTER("Wireless Network Adapter"),
        HEADPHONES("Headphones"),
        KEYBOARD("Keyboard"),
        MOUSE("Mouse"),
        SPEAKERS("Speakers"),
        WEBCAM("Webcam"),
        CASE_ACCESSORY("Case Accessory"),
        CASE_FAN("Case Fan"),
        FAN_CONTROLLER("Fan Controller"),
        THERMAL_COMPOUND("Thermal Compound"),
        EXTERNAL_STORAGE("External Storage"),
        OPTICAL_DRIVE("Optical Drive"),
        UPS("UPS"),
        CUSTOM("Custom");

        private final String name;

        /**
         * Create a PC component category
         *
         * @param name Name of the category - e.g "Optical Drive"
         */
        CATEGORY(String name) {
            this.name = name;
        }

        /**
         * Get the name of the category
         *
         * @return Name of the category - e.g "Optical Drive"
         */
        public String getName() {
            return name;
        }

        /**
         * Get a category by its name
         *
         * @param categoryName Name of the category - e.g "Optical Drive"
         * @return Category - e.g OPTICAL_DRIVE
         */
        @Nullable
        public static CATEGORY byName(String categoryName) {
            try {
                return CATEGORY.valueOf(categoryName.toUpperCase().replaceAll(" ", "_"));
            }
            catch(IllegalArgumentException e) {
                for(CATEGORY category : CATEGORY.values()) {
                    if(!category.getName().equalsIgnoreCase(categoryName)) {
                        continue;
                    }
                    return category;
                }
                return null;
            }
        }
    }

    /**
     * Create a PC component
     *
     * @param category         Component category - e.g CPU
     * @param name             Name of the component - e.g "Intel Core 2 Duo"
     * @param url              URL to the component on pcpartpicker
     * @param imageUrl         URL to an image of the component
     * @param purchaseLocation Optional purchase location & price info of the component
     */
    public Component(CATEGORY category, String name, String url, String imageUrl, @Nullable PurchaseLocation purchaseLocation) {
        this.category = category;
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
        this.purchaseLocation = purchaseLocation;
    }

    /**
     * Get the URL to the component on pcpartpicker
     *
     * @return URL to component
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get a URL to an image of the component
     *
     * @return URL to component image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get the name of the component - e.g "Intel Core 2 Duo"
     *
     * @return Component name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the component category - e.g CPU
     *
     * @return Component category
     */
    public CATEGORY getCategory() {
        return category;
    }

    /**
     * Get the purchase location and price info of the component
     *
     * @return Purchase location
     */
    @Nullable
    public PurchaseLocation getPurchaseLocation() {
        return purchaseLocation;
    }

    /**
     * Store & price info for component
     */
    public static class PurchaseLocation {
        private final Price price;
        private final String storeUrl;

        /**
         * Create a purchase location for a component
         *
         * @param price    Price of the component
         * @param storeUrl Optional URL to the component in the store
         */
        public PurchaseLocation(Price price, @Nullable String storeUrl) {
            this.price = price;
            this.storeUrl = storeUrl;
        }

        /**
         * Create a purchase location for a component
         *
         * @param price Price of the component
         */
        public PurchaseLocation(Price price) {
            this(price, null);
        }

        /**
         * Get the price of the component
         *
         * @return Component price
         */
        public Price getPrice() {
            return price;
        }

        /**
         * Get the URL to the component in the store
         *
         * @return URL to component
         */
        @Nullable
        public String getStoreUrl() {
            return storeUrl;
        }
    }
}
