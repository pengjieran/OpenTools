package arithmetic.shared;
import java.io.BufferedWriter;
import java.io.IOException;

/** ConstCategorizer always gives the same category. Categories must be
 * greater than UNKNOWN_CATEGORY_VAL. For safety reasons, an upper bound of
 * MAX_NUM_CATEGORIES is exists.
 * @author James Louis	8/21/2001	Ported to Java.
 * @author Chia-Hsin Li	11/24/94	Add operator==
 * @author Ronny Kohavi	7/17/93	Initial revision
 */

public class ConstCategorizer extends Categorizer {
    
    /** The distribution of predicted categories.
     */
    private CatDist predDist;
    
    /** This constructor builds the "all the eggs in one basket" type of const
     * categorizer.  It will always predict the given category, but its
     * prediction distribution will give 100% of the weight to this choice.
     * @param dscr		Description of this ConstCategorizer.
     * @param augCat	Category that all Instances will be categorized as.
     * @param sch		The Schema of attributes and labels for this Categorizer.
     */
    public ConstCategorizer(String dscr, AugCategory augCat,
    Schema sch) {
        super(1, dscr, sch);
        predDist = new CatDist(sch, augCat);
    }
    
    /** This constructor allows full specification of the prediction distribution
     * for this categorizer.  The schema in the prediction distribution MUST match
     * the schema being used to build the categorizer.
     * @param dscr		Description of this ConstCategorizer.
     * @param pDist	The distribution of predicted categories specified.
     * @param sch		The Schema of attributes and labels for this Categorizer.
     */
    public ConstCategorizer(String dscr, CatDist pDist,
    Schema sch) {
        super(1, dscr, sch);
        predDist = pDist;
        if (pDist == null)
            Error.fatalErr("ConstCategorizer::ConstCategorizer: given a null CatDist");
        if(sch != pDist.get_schema())
            Error.fatalErr("ConstCategorizer::ConstCategorizer: the prediction "
            +"distribution passed in does not match the given schema");
        pDist = null;
    }
    
    /** Categorizes the given Instance.
     * @return The category this Instance is labelled as.
     * @param i	The Instance to be categorized.
     */
    public AugCategory categorize(Instance i) {
        //        DBG(OK(1));
        return predDist.best_category();
    }
    
    /** Returns the CatDist containing the weighted distribution score for the given Instance. Displays an
     * error message for the Categorizer Class.
     * @return The CatDist containing the weighted distribution.
     * @param i The Instance to be scored.
     */
    public CatDist score(Instance i) {
        //        DBG(OK(1));
        return new CatDist(predDist);
    }
    
    /** Returns the class id of this of this Categorizer.
     * @deprecated This method should be replaced with Java's instanceof operator.
     * @return Integer assigned to this Categorizer.
     */
    public int class_id() {return 2;}
    
    /** Returns the category that all Instances will be classified as.
     * @return The category that all Instances will be classified as.
     */
    public AugCategory get_category() {
        //      DBG(OK(1));
        return predDist.best_category(); }
    
    /** Returns the distribution of predicted categories.
     * @return The distribution of predicted categories.
     */
    public CatDist get_cat_dist() {
        //      DBG(OK(1));
        return predDist;
    }
    
    /** Clones this ConstCategorizer.
     * @return The clone of this Categorizer.
     */
    public Object clone() {
        //   if (class_id() != CLASS_CONST_CATEGORIZER)
        //      err << "ConstCategorizer::clone: invoked for improper class of id "
        //	  << class_id() << fatal_error;
        //   return new ConstCategorizer(*this, ctorDummy);
        return super.clone();
    }
    
/*Categorizer* ConstCategorizer::clone() const
{
   if (class_id() != CLASS_CONST_CATEGORIZER)
      err << "ConstCategorizer::clone: invoked for improper class of id "
          << class_id() << fatal_error;
   return new ConstCategorizer(*this, ctorDummy);
}
 */
    
    /** Prints a readable representation of the ConstCategorizer to the given
     * stream.
     * @param stream	The Buffered Writer to which information will be
     * displayed.
     * @param dp		The preferences for Display.
     */
    public void display_struct(BufferedWriter stream, DisplayPref dp) {
        //   if (stream.output_type() == XStream)
        //      Error.fatalErr("ConstCategorizer::display_struct: Xstream is not a valid "
        //          +"stream for this display_struct");
        
        //   if (dp.preference_type() != DisplayPref.ASCIIDisplay)
        //      Error.fatalErr("ConstCategorizer::display_struct: Only ASCIIDisplay is "
        //          +"valid for this display_struct");
        
        try{
            stream.write("Constant Categorizer "+description()
            +" scoring as "+predDist+'\n');
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Sets which attributes have been used in this ConstCategorizer.
     * @param used	The attributes used. TRUE if the corresponding attribute has
     * been used.
     */
    public void set_used_attr(boolean[] used){}
    
    
}