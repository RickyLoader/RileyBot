package Vape;

/**
 * Vapourium products
 */
public class Vapourium extends VapeStore {
    private static Vapourium instance = null;

    /**
     * Initialise a vape store with Vapourium values
     */
    private Vapourium() {
        super(
                "Vapourium",
                "https://vapourium.nz",
                "https://i.imgur.com/cAZkQRq.png"
        );
    }

    /**
     * Get an instance of the Vapourium class
     *
     * @return Instance
     */
    public static Vapourium getInstance() {
        if(instance == null) {
            instance = new Vapourium();
        }
        return instance;
    }
}
