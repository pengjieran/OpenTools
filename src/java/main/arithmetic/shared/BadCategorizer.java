package arithmetic.shared;
import java.io.BufferedWriter;
import java.util.LinkedList;

/** The BadCategorizer class is used for repeated references to categorizers
 * that either don't exist or are not specified. All methods other than
 * constructor and destructor dispay fatal_error messages.
 * This allows faster comparison using is_bad_categorizer() and prevents some
 * unnecessary waste of memory.
 *
 * @author James Louis 2/25/2001   Ported to Java.
 * @author Richard Long    8/25/93 Changes so that only 1 BadCategorizer
 * can be instantiated.
 * @author Richard Long    8/04/93 Initial revision (.c)
 * @author Richard Long    8/03/93 Initial revision (.h)
 */
public class BadCategorizer extends Categorizer {
    /** TRUE if the BadCategorizer has been initialized, FALSE otherwise.
     */
    static boolean instantiated;
    /** Description of the BadCategorizer.
     */
    private static String BadCatDscr ="Bad Categorizer";
    
    /** Checks if BadCategorizer has not been instantiated and creates one if it
     * has. Updates "instantiated".
     */
    public BadCategorizer() {
        super(1, BadCatDscr, create_dummy_schema());
        if (instantiated)
            Error.fatalErr("BadCategorizer::BadCategorizer:" +
            " There is already an instance " +
            "of BadCategorizer");
        instantiated = true;
        //      DBG(OK());
    }
    
    /** Identifies this objects class as a BadCategorizer.
     * @deprecated Java's instanceof operator should be used.
     * @return The number identifing this class. In this case -1.
     */
    public int class_id() {
        return -1;
    }
    
    /** Creates an unused Schema for this Categorizer.
     * @return A dummy Schema instance for this BadCategorizer.
     */
    static Schema create_dummy_schema() {
        LinkedList attrList = new LinkedList();
        return new Schema(attrList);
    }
    
    /** Clones this BadCategorizer. Displays an error message.
     * @return The clone of this BadCategorizer.
     */
    public Object clone() {
        Error.fatalErr("BadCategorizer::clone: Bad Categorizer");
        return null;
    }
    
    /** Displays the structure for this categorizer. Displays an error message.
     * @param stream	The output stream to be written to.
     * @param dp		The preferences for displaying.
     */
    public void display_struct(BufferedWriter stream,
    DisplayPref dp) {
        Error.fatalErr("BadCategorizer::display_struct: Bad categorizer");
    }
    
    /** Updates usedAttr to include the attributes used in this categorizer.
     * Displays an error message.
     * @param usedAttr A boolean array representing attributes. Each element set
     * to TRUE indicates the attribute is used. It is FALSE
     * otherwise. This method sets the appropriate attribute
     * element to true.
     */
    public void set_used_attr(boolean[] usedAttr) {
        Error.fatalErr("BadCategorizer::set_used_attr is not defined");
    }
    
    /** Returns the number of categories in this categorizer. Displays an error
     * message.
     * @return The number of categories.
     */
    public int num_categories() {
        Error.fatalErr("BadCategorizer::num_categories: Bad categorizer");
        return 0;
    }
    
    /** Categorizes an Instance. Displays an error message.
     * @return The category the specified Instance is labelled.
     * @param i	The specified Instance.
     */
    public AugCategory categorize(Instance i) {
        Error.fatalErr("BadCategorizer::categorize: Bad categorizer");
        return null;
    }
}