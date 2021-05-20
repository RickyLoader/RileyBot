package Vape;

/**
 * Vapo products
 */
public class Vapo extends VapeStore {
    private static Vapo instance = null;

    /**
     * Initialise a vape store with Vapo values
     */
    private Vapo() {
        super(
                "Vapo",
                "https://www.vapo.co.nz/products",
                "https://i.imgur.com/Ex4yqV8.png"
        );
    }

    /**
     * Get an instance of the Vapo class
     *
     * @return Instance
     */
    public static Vapo getInstance() {
        if(instance == null) {
            instance = new Vapo();
        }
        return instance;
    }
}
