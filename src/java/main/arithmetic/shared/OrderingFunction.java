package arithmetic.shared;

/** The interface for classes using an ordering function for sorting various lists
 * in the Graph class.
 */
public interface OrderingFunction{
    /** The order function should return an index value for the object it recieves as a
     * parameter. This index value is then used to determine its correct position in
     * the list when the list is bucket sorted.
     *
     * @param item The item to be sorted into place.
     * @return The index value of the item sorted.
     */
    public int order(Object item);
};