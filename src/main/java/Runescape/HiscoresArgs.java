package Runescape;

/**
 * Arguments for searching Runescape hiscores/building hiscores image
 */
public class HiscoresArgs {
    private final boolean virtual;

    /**
     * Create the hiscores arguments
     *
     * @param virtual Display virtual levels
     */
    public HiscoresArgs(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * Check whether to display the player's virtual levels
     *
     * @return Display virtual levels
     */
    public boolean displayVirtualLevels() {
        return virtual;
    }
}
