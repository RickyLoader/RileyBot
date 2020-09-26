package COD;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Hold weapon information
 */
public abstract class Weapon implements Comparable<Weapon> {
    private final String name, iwName, imageTitle;
    private final File image;
    private final TYPE type;

    public enum TYPE {
        PRIMARY,
        SECONDARY,
        LETHAL,
        TACTICAL
    }

    /**
     * Create a weapon
     *
     * @param iwName   Infinity Ward name of weapon e.g "iw8_me_akimboblunt"
     * @param name     Real name of weapon e.g "Kali Sticks"
     * @param category Infinity Ward name of weapon category e.g "weapon_melee"
     * @param type     Type of weapon
     * @param res      Resource location
     */
    public Weapon(String iwName, String name, String category, TYPE type, String res) {
        this.iwName = iwName;
        this.name = name;
        this.type = type;
        this.image = new File(res + "Weapons/" + category + "/" + iwName + ".png");
        this.imageTitle = setImageTitle(type);
    }

    /**
     * Set title used above weapon in combat record image
     *
     * @param type Enum type of weapon
     * @return String title
     */
    private String setImageTitle(TYPE type) {
        String imageTitle = "";
        switch(type) {
            case PRIMARY:
                imageTitle = "Primary Weapon of Choice";
                break;
            case SECONDARY:
                imageTitle = "Secondary Weapon of Choice";
                break;
            case LETHAL:
                imageTitle = "Lethal Equipment of Choice";
                break;
            case TACTICAL:
                imageTitle = "Tactical Equipment of Choice";
                break;
        }
        return imageTitle;
    }

    /**
     * Get the Infinity Ward name of the weapon
     *
     * @return Infinity Ward name of weapon
     */
    public String getIwName() {
        return iwName;
    }

    /**
     * Get the image title for use in combat record
     *
     * @return Image title
     */
    public String getImageTitle() {
        return imageTitle;
    }

    /**
     * Get the name of the weapon
     *
     * @return Weapon name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the weapon type
     *
     * @return Weapon type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the image of the weapon
     *
     * @return Weapon image
     */
    public File getImage() {
        return image;
    }

    /**
     * Get the value used in sorting
     * Tactical - uses
     * Standard, Lethal - kills
     *
     * @return Sort value
     */
    public abstract int getSortValue();

    /**
     * Get the weapon categories associated with the given weapon type
     *
     * @param type (PRIMARY, SECONDARY...)
     * @return Weapon categories
     */
    public static String[] getCategories(TYPE type) {
        String[] categories = new String[]{};
        switch(type) {
            case PRIMARY:
                categories = new String[]{"weapon_sniper", "weapon_lmg", "weapon_assault_rifle", "weapon_other", "weapon_shotgun", "weapon_smg", "weapon_marksman"};
                break;
            case SECONDARY:
                categories = new String[]{"weapon_launcher", "weapon_pistol", "weapon_melee"};
                break;
            case LETHAL:
                categories = new String[]{"lethals"};
                break;
            case TACTICAL:
                categories = new String[]{"tacticals"};
                break;
        }
        return categories;
    }

    /**
     * Descending order of sort value
     *
     * @param o Weapon
     * @return Comparison of sort value
     */
    @Override
    public int compareTo(@NotNull Weapon o) {
        return o.getSortValue() - getSortValue();
    }
}
