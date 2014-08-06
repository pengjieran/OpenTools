package arithmetic.id3;
import arithmetic.shared.Schema;

/** DTCategorizer performs the same way as a RDGCategorizer, but for the
 * DTCategorizer the associated graph must be a tree.  See "RDGCat.c" for
 * details. Assume graph exists.
 * @author James Louis 5/30/2001 Ported to Java.
 * @author Richard Long 9/05/93 Initial revision
 */
public class DTCategorizer extends RDGCategorizer {
    /** Constructor.
     * @param dt The DecisionTree on which this categorizer will be based.
     * @param descr A description of this categorizer.
     * @param numCat The number of categories possible.
     * @param sch The Schema of data this categorizer will be used on.
     */
    public DTCategorizer(DecisionTree dt, String descr, int numCat,
    Schema sch) {
        super(constructor_cast(dt), descr, numCat, sch);
    }
    
    /** Casts this object to a RootedCatGraph object.
     * @return This object as a RootedCatGraph object.
     * @param dt	The DecisionTree to be stored in this DTCategorizer.
     */
    private static RootedCatGraph constructor_cast(DecisionTree dt) {
        RootedCatGraph rcg;
        
        rcg = dt; //cast
        dt = null;
        return rcg;
    }
    
    /***************************************************************************
     * Displays this DTCategorizer object.
     * @param stream	The BufferedWriter to which information will be displayed.
     * @param dp		The preferences for display.
     ***************************************************************************
     * public void display_struct(BufferedWriter stream, DisplayPref dp)
     * {
     * try{
     * stream.write("DTCategorizer::display_struct: not implemented yet."+'\n');
     * }catch(IOException e){e.printStackTrace();}
     * }
     */
}


