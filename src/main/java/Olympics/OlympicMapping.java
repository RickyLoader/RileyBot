package Olympics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Mapping functions for Olympic data
 */
public abstract class OlympicMapping<V, T extends OlympicData> {
    protected final HashMap<V, T> mapping;

    /**
     * Create the Olympic mapping
     */
    public OlympicMapping(HashMap<V, T> mapping) {
        this.mapping = mapping;
    }

    /**
     * Get a list of Olympic values by name.
     * If a singular matching name is found, the list will contain only the matching value,
     * otherwise the list will contain all values found for the name query.
     *
     * @param nameQuery Name to search
     * @return List of values for given name query
     */
    public ArrayList<T> getValuesByName(String nameQuery) {

        // Names containing the query
        ArrayList<T> values = this.mapping
                .values()
                .stream()
                .filter(v -> v.getName().toLowerCase().contains(nameQuery.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Exact matches
        ArrayList<T> matching = values
                .stream()
                .filter(a -> a.getName().equalsIgnoreCase(nameQuery))
                .collect(Collectors.toCollection(ArrayList::new));

        // Return single match or any names containing the query
        return matching.size() == 1 ? matching : values;
    }

    /**
     * Get all of the available values in the mapping
     *
     * @return All values
     */
    public ArrayList<T> getAllValues() {
        return new ArrayList<>(mapping.values());
    }

    /**
     * Get the mapping of the Olympic data
     *
     * @return Olympic data mapping
     */
    public HashMap<V, T> getMapping() {
        return mapping;
    }
}
