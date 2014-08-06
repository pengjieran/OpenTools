package arithmetic.shared;

/** FSLossEntry class contains information about loss specification entries in the FileSchema
 * class.
 */
public class FSLossEntry {
    /** Predicted value of the entry.
     */
    public String predName;
    /** Actual value of the entry.
     */
    public String actName;
    /** Loss value.
     */
    public double loss;
    
    /** Copy constructor.
     * @param source The FSLossEntry to be copied.
     */
    public FSLossEntry(FSLossEntry[] source){}
}
