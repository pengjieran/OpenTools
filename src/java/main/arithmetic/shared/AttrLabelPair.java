package arithmetic.shared;

/** This class contains basic information on labels and attributes.
 * @author James Louis	Created	2/25/2001
 */
public class AttrLabelPair implements Sortable, Comparable {
    /** Value for real information.
     */
    public float value;
    /** Value for nominal information.
     */
    public int label;
    /** Weight value for this attribute.
     */
    public double weight;
    
    /** Constructor.
     */
    public AttrLabelPair()
    {}
    
    /** Compares this AttrLabelPair to another AttrLabelPair.
     * @return TRUE if this AttrLabelPair has a smaller value, smaller label and
     * equal value, or smaller weight and equal value and label.
     * @param otherSortable The instance implementing the Sortable interface to which this AttrLabelPair is
     * being compared to.
     */
    public boolean lessThan(Sortable otherSortable) {
        AttrLabelPair other =(AttrLabelPair) otherSortable;
        return((value < other.value) ||
        ((value == other.value) &&
        ((label < other.label) ||
        ((label == other.label) &&
        (weight < other.weight)))));
        
    }
    
    /** Compares this AttrLabelPair to another AttrLabelPair.
     * @return TRUE if both AttrLabelPairs contain equivalent information.
     * @param otherSortable	The specified AttrLabelPair. This object should be
     * an AttrLabelPair or a subclass of this class.
     */
    public boolean Equals(Sortable otherSortable) {
        AttrLabelPair other =(AttrLabelPair) otherSortable;
        return((value == other.value) &&
        (label == other.label) &&
        (weight == other.weight));
    }
    
    /** Converts this AttrLabelPair to a String.
     * @return A String with information on the value, label, and weight values.
     */
    public String toString() {
        return"(value = " +Float.toString(value)
        +", label = " +Integer.toString(label)
        +", weight = " +Double.toString(weight) +")";
    }
    
    /** Compares this AttrLabelPair to the specified object.
     * @return -1 if this AttrLabelPair is less than the specified object,
     * 0 if equal, or 1 otherwise.
     * @param compare The object to be tested.
     * @throws ClassCastException if the specified object's type prevents it from
     * being compared to this object.
     */
    public int compareTo(Object compare) throws ClassCastException {
        AttrLabelPair other =(AttrLabelPair) compare;
        if((value == other.value) && (label == other.label) &&
        (weight == other.weight)) return 0;
        if((value < other.value) || ((value == other.value) &&
        ((label < other.label) || ((label == other.label) &&
        (weight < other.weight))))) return -1;
        return 1;
    }
}