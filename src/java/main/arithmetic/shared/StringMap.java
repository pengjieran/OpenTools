package arithmetic.shared;


/** StringMap implements a general mapping/lookup class for strings. The strings
 * may be mapped to any data type Elem. The mapping is accomplished using both a
 * hash table and an array.  The hash table is provided for speedy searches, and
 * the array is provided for iteration.
 *
 * @author Dan Sommerfield 2/01/96 Initial revision (.h,.c)
 */
public class StringMap {
    /** Constructor. The default constructor allows an optional parameter for an
     * estimated size of the hash table.
     *
     * @param sizeHint The size of the hashtable.
     */
    public StringMap(int sizeHint){}
    
    /** Sets the mapping for an item, adding the mapping if the item is not currently in
     * the table.
     * @param name The name to be mapped.
     * @param value The value to be mapped to.
     */
    public void map(String name, OptionAccess value){}
    
    /** Retrieves a mapping given the name and a reference in which to store the value.
     * Returns TRUE if the mapping exists, false if it does not. No value is placed in
     * the value reference if the mapping does not exist.
     *
     * @return TRUE if the mapping exists, FALSE if it does not.
     * @param name Name being mapped.
     * @param value The value to be found.
     */
    public boolean get(String name, OptionAccess value){return true;}
    
    
}
