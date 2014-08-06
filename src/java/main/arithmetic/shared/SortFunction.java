package arithmetic.shared;

/** Sort method interface.
 */
public interface SortFunction{
    //added by JL
    //based on LEDA
    
    /** The comparison method used for sorting functions.
     * @param num1 The first object compared.
     * @param num2 The second object compared.
     * @return TRUE if num1 "is less than" num2 in priority.
     */
    public boolean is_less_than(Object num1, Object num2);
    
}