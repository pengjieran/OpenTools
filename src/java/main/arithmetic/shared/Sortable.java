package arithmetic.shared;

/** Interface for classes that need to be sorted in a specific manner.
 */
public interface Sortable{
    boolean lessThan(Sortable other);
    boolean Equals(Sortable other);
}