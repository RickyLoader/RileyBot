package PCPartPicker;

import PCPartPicker.Component.CATEGORY;
import Steam.Price;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List of components for a PC build
 */
public class ComponentList {
    private final Price totalPrice;
    private final String name, url, imageUrl;
    private final HashMap<CATEGORY, ArrayList<Component>> componentMap;

    /**
     * Create a PC build component list
     *
     * @param name       Name of the component list - e.g "My Roblox build"
     * @param url        URL to the component list - e.g "https://nz.pcpartpicker.com/list/CdCdrV"
     * @param imageUrl   Optional URL to an image of the component
     * @param totalPrice Optional total price of the component list
     */
    public ComponentList(String name, String url, @Nullable String imageUrl, @Nullable Price totalPrice) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
        this.totalPrice = totalPrice;

        // Initialise component map
        this.componentMap = new HashMap<>();
    }

    /**
     * Add a component to the list.
     *
     * @param component Component to add
     */
    public void addComponent(Component component) {
        final CATEGORY category = component.getCategory();

        // Map components from category -> list of components with the category
        ArrayList<Component> categoryComponents = componentMap.computeIfAbsent(category, k -> new ArrayList<>());
        categoryComponents.add(component);
    }

    /**
     * Get a list of components in the component list for the given category
     *
     * @param category Component category - e.g CPU
     * @return List of components in the list with the given category
     */
    public ArrayList<Component> getComponentsByCategory(CATEGORY category) {
        return componentMap.containsKey(category) ? componentMap.get(category) : new ArrayList<>();
    }

    /**
     * Get a list of components in order of category.
     *
     * @return List of components
     */
    public ArrayList<Component> getComponents() {
        return componentMap
                .values()
                .stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparingInt(c -> c.getCategory().ordinal()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get a URL to an image of the component
     *
     * @return URL to component image
     */
    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get the name of the component list
     *
     * @return Name of component list - e.g "My Roblox build"
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the component list
     *
     * @return URL to component list - e.g "https://nz.pcpartpicker.com/list/CdCdrV"
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the total price of the component list
     *
     * @return Total price
     */
    @Nullable
    public Price getTotalPrice() {
        return totalPrice;
    }
}
