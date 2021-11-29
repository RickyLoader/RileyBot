package StarCitizen;

import Steam.Price;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Star Citizen ship data
 */
public class Ship {
    private final String name, description, storeUrl, dataUrl, mainImageUrl;
    private final ArrayList<String> images;
    private final STATUS productionStatus;
    private final TYPE type;
    private final Measurements measurements;
    private final StructuralDetails structuralDetails;
    private final Manufacturer manufacturer;
    private final int id;
    private Price price;

    public enum STATUS {
        FLIGHT_READY,
        IN_CONCEPT,
        IN_PRODUCTION
    }

    public enum TYPE {
        MULTI,
        EXPLORATION,
        TRANSPORT,
        COMBAT,
        COMPETITION,
        SUPPORT,
        INDUSTRIAL,
        GROUND
    }

    /**
     * Create a ship
     *
     * @param id                Unique ID of the ship - e.g 21
     * @param name              Name of the ship - e.g "890 Jump"
     * @param description       Optional Description of the ship
     * @param manufacturer      Ship manufacturer
     * @param measurements      Ship measurements (length, mass, etc)
     * @param structuralDetails Structural details of ship (crew details etc)
     * @param productionStatus  Production status of the ship - e.g FLIGHT_READY
     * @param type              Type class of the ship - e.g EXPLORATION or TRANSPORT
     * @param storeUrl          URL to the ship webpage
     * @param dataUrl           URL to the ship API page
     * @param mainImageUrl      Optional URL to the main ship image
     */
    public Ship(int id, String name, @Nullable String description, Manufacturer manufacturer, Measurements measurements, StructuralDetails structuralDetails, STATUS productionStatus, TYPE type, String storeUrl, String dataUrl, @Nullable String mainImageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.manufacturer = manufacturer;
        this.measurements = measurements;
        this.structuralDetails = structuralDetails;
        this.productionStatus = productionStatus;
        this.type = type;
        this.storeUrl = storeUrl;
        this.dataUrl = dataUrl;
        this.mainImageUrl = mainImageUrl;

        this.images = new ArrayList<>();

        // Add main image to list of images if present
        if(mainImageUrl != null) {
            images.add(mainImageUrl);
        }
    }

    /**
     * Get the URL to the main image of the ship
     *
     * @return Main image URL
     */
    @Nullable
    public String getMainImageUrl() {
        return mainImageUrl;
    }

    /**
     * Get the ship manufacturer
     *
     * @return Ship manufacturer
     */
    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    /**
     * Get the store price of the ship
     *
     * @return Store price
     */
    @Nullable
    public Price getPrice() {
        return price;
    }

    /**
     * Set the store price of the ship
     *
     * @param price Store price to set
     */
    public void setPrice(Price price) {
        this.price = price;
    }

    /**
     * Get the unique ID of the ship
     *
     * @return Ship ID - e.g 21
     */
    public int getId() {
        return id;
    }

    /**
     * Get the store description of the ship
     *
     * @return Ship description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Get the production status of the ship
     *
     * @return Production status of the ship - e.g FLIGHT_READY
     */
    public STATUS getProductionStatus() {
        return productionStatus;
    }

    /**
     * Get the type class of the ship
     *
     * @return Type class of the ship - e.g EXPLORATION or TRANSPORT
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the ship measurements.
     * This includes length, mass, etc.
     *
     * @return Ship measurements
     */
    public Measurements getMeasurements() {
        return measurements;
    }

    /**
     * Get the structural details of the ship.
     * This includes cargo capacity, crew details, etc.
     *
     * @return Structural details
     */
    public StructuralDetails getStructuralDetails() {
        return structuralDetails;
    }

    /**
     * Get the name of the ship
     *
     * @return Ship name  - e.g "890 Jump"
     */
    public String getName() {
        return name;
    }

    /**
     * Get a list of URLs to images of the ship
     *
     * @return List of URLs to images of the ship
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get the URL to the ship store webpage
     *
     * @return Webpage URL
     */
    public String getStoreUrl() {
        return storeUrl;
    }

    /**
     * Get the URL to retrieve ship data from the API
     *
     * @return Ship data URL - e.g "https://robertsspaceindustries.com/ship-matrix/index?chassis_id=21"
     */
    public String getDataUrl() {
        return dataUrl;
    }

    /**
     * Star Citizen ship structural details
     */
    public static class StructuralDetails {
        private final Integer cargoCapacity, minimumCrew, maximumCrew;

        /**
         * Create the structural details of a ship
         *
         * @param cargoCapacity Cargo capacity (in units) - e.g 460
         * @param minimumCrew   Minimum crew members required to operate the ship
         * @param maximumCrew   Maximum crew members required to operate the ship
         */
        public StructuralDetails(@Nullable Integer cargoCapacity, @Nullable Integer minimumCrew, @Nullable Integer maximumCrew) {
            this.cargoCapacity = cargoCapacity != null && cargoCapacity == 0 ? null : cargoCapacity;
            this.minimumCrew = minimumCrew;
            this.maximumCrew = maximumCrew;
        }

        /**
         * Get the minimum number of crew members required to operate the ship
         *
         * @return Minimum crew members
         */
        @Nullable
        public Integer getMinimumCrew() {
            return minimumCrew;
        }

        /**
         * Get the maximum number of crew members required to operate the ship
         *
         * @return Maximum crew members
         */
        @Nullable
        public Integer getMaximumCrew() {
            return maximumCrew;
        }

        /**
         * Get the cargo capacity of the ship (in units)
         *
         * @return Cargo capacity - e.g 460
         */
        @Nullable
        public Integer getCargoCapacity() {
            return cargoCapacity;
        }

        /**
         * Get the crew summary String for the ship.
         * This is in the format "[MINIMUM_CREW] - [MAXIMUM_CREW]" and will be null if either value is missing.
         * If both values are the same, only one value will be returned, e.g "1".
         *
         * @return Crew summary String - e.g "2 - 4" or null
         */
        @Nullable
        public String getCrewSummary() {
            if(minimumCrew == null || maximumCrew == null) {
                return null;
            }

            // Min/max values are identical (e.g "1")
            if(minimumCrew.equals(maximumCrew)) {
                return String.valueOf(minimumCrew);
            }

            return minimumCrew + " - " + maximumCrew;
        }
    }

    /**
     * Star Citizen ship measurements
     */
    public static class Measurements {
        private final double length, beam, height;
        private final Integer mass;
        private final SHIP_SIZE size;

        // Ordered smallest to largest for ordinal (VEHICLE is misc)
        public enum SHIP_SIZE {
            MISC,
            VEHICLE,
            SNUB,
            SMALL,
            MEDIUM,
            LARGE,
            CAPITAL
        }

        /**
         * Create measurements for a ship
         *
         * @param length Length of the ship (in metres)
         * @param beam   Width of the ship (in metres)
         * @param height Height of the ship (in metres)
         * @param mass   Optional mass of the ship (in kilograms)
         * @param size   Size class of the ship - e.g CAPITAL
         */
        public Measurements(double length, double beam, double height, @Nullable Integer mass, SHIP_SIZE size) {
            this.length = length;
            this.beam = beam;
            this.height = height;
            this.mass = mass;
            this.size = size;
        }

        /**
         * Get the size class of the ship
         *
         * @return Ship size class -  e.g CAPITAL
         */
        public SHIP_SIZE getSize() {
            return size;
        }

        /**
         * Get the length of the ship (in metres)
         *
         * @return Ship length
         */
        public double getLength() {
            return length;
        }

        /**
         * Get the height of the ship (in metres)
         *
         * @return Ship height
         */
        public double getHeight() {
            return height;
        }

        /**
         * Get the width of the ship (in metres)
         *
         * @return Ship width
         */
        public double getBeam() {
            return beam;
        }

        /**
         * Get the mass of the ship (in kilograms)
         *
         * @return Ship mass
         */
        @Nullable
        public Integer getMass() {
            return mass;
        }
    }

    /**
     * Star Citizen ship manufacturer
     */
    public static class Manufacturer {
        private final String name, imageUrl;

        /**
         * Create a ship manufacturer
         *
         * @param name     Manufacturer name - e.g "Origin Jumpworks"
         * @param imageUrl URL to the manufacturer logo/image
         */
        public Manufacturer(String name, @Nullable String imageUrl) {
            this.name = name;
            this.imageUrl = imageUrl;
        }

        /**
         * Get the manufacturer name
         *
         * @return Manufacturer name - e.g "Origin Jumpworks"
         */
        public String getName() {
            return name;
        }

        /**
         * Get the URL to an image of the manufacturer logo
         *
         * @return URL to the manufacturer logo/image
         */
        @Nullable
        public String getImageUrl() {
            return imageUrl;
        }
    }
}