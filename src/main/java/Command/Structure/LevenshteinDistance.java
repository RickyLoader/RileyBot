package Command.Structure;

import java.util.Comparator;

/**
 * Edit distance between two Strings comparator
 */
public abstract class LevenshteinDistance<T> implements Comparator<T> {
    private final boolean mostSimilarFirst;
    private final String query;

    /**
     * Initialise the comparator with a query to compare edit distance against and a sort direction.
     *
     * @param query            String to get edit distance to
     * @param mostSimilarFirst Sort by most similar (least edit distance) first
     */
    public LevenshteinDistance(String query, boolean mostSimilarFirst) {
        this.query = query;
        this.mostSimilarFirst = mostSimilarFirst;
    }

    /**
     * Take two objects and retrieve a String from each.
     * Compare the edit distance from each String to the query.
     *
     * @param o1 Object 1
     * @param o2 Object 2
     * @return Edit distance comparison
     */
    public int compare(T o1, T o2) {
        int distance1 = getDistance(getString(o1), query);
        int distance2 = getDistance(getString(o2), query);
        return mostSimilarFirst ? distance1 - distance2 : distance2 - distance1;
    }

    /**
     * Get the String to compare from the given object
     *
     * @param o Object to get String for
     * @return String from object
     */
    public abstract String getString(T o);

    /**
     * Get the levenshtein distance between the given Strings.
     * This is the edit distance (minimum number of operations required to transform one String to another)
     *
     * @param a String a
     * @param b String b
     * @return Edit distance between Strings a & b
     */
    public int getDistance(String a, String b) {
        int[][] distance = new int[a.length() + 1][b.length() + 1];
        for(int i = 0; i <= a.length(); i++) {
            for(int j = 0; j <= b.length(); j++) {
                if(i == 0) {
                    distance[i][j] = j;
                }
                else if(j == 0) {
                    distance[i][j] = i;
                }
                else {
                    int substitution = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    distance[i][j] = Math.min(
                            distance[i][j - 1] + 1,
                            Math.min(
                                    distance[i - 1][j - 1] + substitution,
                                    distance[i - 1][j] + 1
                            ));
                }
            }
        }
        return distance[a.length()][b.length()];
    }
}
