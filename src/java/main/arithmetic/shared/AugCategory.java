package arithmetic.shared;
import java.io.BufferedWriter;

/** The AugCategory class contains information on categories used in
 * labelling and provides support for augmented categories. An augmented
 * category has extra information besides the category number.  The minimal
 * augmentation implemented here also gives a string description. This class
 * could be subclassed to provide certainty levels and other augmentations
 * relevant for categories.
 * @author James Louis 2/25/2001   Ported to Java.
 * @author Chia-Hsin Li    9/26/94 Added AugCategory::operator==. Deleted
 *                                  AugCategoryAlloc.
 * @author Richard Long    9/28/93 Added AugCategoryAlloc.
 * @author Ronny Kohavi    9/13/93 Initial revision (.h,.c)
 */
public class AugCategory {
    /** Option to change display between MLC++ binary and MLC++ source
    specifications. Default is TRUE, setting to binary. FALSE sets to source.
     **/
    static public boolean MLCBinaryDisplay = true;
    /** The category number for this category. **/
    private int category;
    /** The description of this category. **/
    private String catDscr;
    
    /** Constructor.
     * @param aCat	The category this AugCategory represents.
     * @param dscr	The description of this AugCategory.
     */
    public AugCategory(int aCat, String dscr) {
        category = aCat;
        catDscr = dscr;
        if (category < Globals.UNKNOWN_CATEGORY_VAL
        || category > Globals.UNKNOWN_CATEGORY_VAL + Globals.MAX_NUM_CATEGORIES)
            System.err.print("AugCategory::AugCategory: category " +
            aCat + " out of range (legal values are "
            + Globals.UNKNOWN_CATEGORY_VAL +
            " to " + Globals.UNKNOWN_CATEGORY_VAL + Globals.MAX_NUM_CATEGORIES);
    }
    
    /** Copy constructor.
     * @param ac	The AugCategory to be copied.
     */
    public AugCategory(AugCategory ac) {
        //DBG(ASSERT(ac.catDscr != EMPTY_STRING));
        category = ac.category;
        catDscr = new String(ac.catDscr);
    }
    
    /** Returns the category number.
     * @return The category number.
     */
    public int num() {
        return category;
    }
    
    /** Returns the description as a string.
     * @return The description of this AugCategorizer.
     */
    public String description() {
        return catDscr;
    }
    
    /** Returns the category number.
     * @return The category number.
     */
    public int Category() {
        return category;
    }
    
    /** Prints the description and the category number.
     * @param ostream	The output stream to be written to.
     */
    public void display(BufferedWriter ostream) {
        String output = new String(catDscr + " (" + category + ")");
        try {
            ostream.write(output,0,output.length());
        }
        catch(Exception e) {
        }
    }
    
    /** Checks if this AugCategory is "equivalent" to the specified AugCategory.
     * The categories may match, but the strings may be different for continuous
     * splits, where the numbers are just consecutive.
     * @return TRUE if the categories match and the descriptions are equal, FALSE
     * otherwise.
     * @param rhs The AugCategory to which this instance is being compared.
     */
    public boolean equals(AugCategory rhs) {
        if (category != rhs.category)
            return false;
        else
            return(catDscr.equals(rhs.catDscr));
    }
    
    /** Checks if this AugCategory is not "equivalent" to the specified
     * AugCategory. The categories may match, but the strings may be different for
     * continuous splits, where the numbers are just consecutive.
     * @return TRUE if the categories do not match or the descriptions are not
     * equal, FALSE otherwise.
     * @param rhs The AugCategory to which this instance is being compared.
     */
    public boolean notequal(AugCategory rhs) {
        return !(equals(rhs));
    }
    
    
    /** Sets the category and description to the specified values.
     * @param aCat	The new category.
     * @param dscr	The new description.
     */
    public void update(int aCat, String dscr) {
        if (category < Globals.UNKNOWN_CATEGORY_VAL
        || category > Globals.UNKNOWN_CATEGORY_VAL + Globals.MAX_NUM_CATEGORIES)
            System.err.print("AugCategory::AugCategory: category " +
            aCat + " out of range (legal values are "
            + Globals.UNKNOWN_CATEGORY_VAL +
            " to " + Globals.UNKNOWN_CATEGORY_VAL + Globals.MAX_NUM_CATEGORIES);
        category = aCat;
        catDscr = dscr;
    }
    
    /** Creates a String value containing the description and the category number.
     * @return The description of the category stored in this AugCategory.
     */
    public String toString() {
        //Changed to allow switching between MLC++ binary display specification and
        //MLC++ source code display specification. -JL
        if(MLCBinaryDisplay) return description();
        else return description() +" (" +num() +")";
    }
    
}