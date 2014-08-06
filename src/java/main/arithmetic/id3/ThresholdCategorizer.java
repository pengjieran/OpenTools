package arithmetic.id3;
import java.io.BufferedWriter;
import java.io.IOException;

import arithmetic.shared.AttrInfo;
import arithmetic.shared.AugCategory;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Globals;
import arithmetic.shared.Instance;
import arithmetic.shared.Schema;

/** Categorize an instance by comparing the value of a single attribute to a
 * threshold value. All instances for which the attribute is less than or equal
 * to the threshold value are put into one category and those for which the
 * attribute is greater than the threshold value are put into a second category.
 * The attribute must be Real.
 *
 * @author James Louis Java Implementation.
 * @author Chia-Hsin Li 11/23/94 Add operator==
 * @author Brian Frasca 4/12/94 Initial revision
 */
public class ThresholdCategorizer extends NodeCategorizer {
    private AttrInfo attrInfo;
    /**
     */
    private int attrNum;
    private double thresholdVal;
    /** "Less than or equal" to description.
     */
    private String LTEDscr;  // "less than or equal to" description
    /** "Greater than" description.
     */
    private String  GTDscr;  // "greater than" description
    
    static private String UNKNOWN_VAL_STRRC ="?";
    
    /** Construct a ThresholdCategorizer.
     * @param sch The schema for information this categorizer categorizes.
     * @param attributeNum The attribute number for this threshold.
     * @param threshold The threshold value.
     * @param dscr Description of this categorizer.
     */
    public ThresholdCategorizer(Schema sch,
    int attributeNum,
    double threshold,
    String dscr) {
        super(2, dscr, sch);
        attrInfo = sch.attr_info(attributeNum);
        attrNum = attributeNum;
        
        change_threshold(threshold);
    }
    
    /** Changes the threshold value.
     * @param threshold The new threshold value.
     */
    public void change_threshold(double threshold) {
        thresholdVal = threshold;
        // Added to match MLC mantissa length. -JL
        String output = Double.toString(thresholdVal);
        if(output.indexOf('.') != -1 && output.substring(output.indexOf('.'),output.length()).length() > 12)
            output = output.substring(0,output.indexOf('.') + 12);
        LTEDscr = "<= "+output;
        GTDscr =  "> "+output;
    }
    
    
    /** Returns the id number for this class.
     * @return The class id number.
     * @deprecated Use java's instanceOf function.
     */
    public int class_id(){ return CLASS_THRESHOLD_CATEGORIZER; }
    
    /** Builds an array of 3 strings for the labels (?, <= x, and > x) of the edges
     * depending from the node for which this is the categorizer.
     *
     * @return An array of the strings built.
     */
    public String[] real_edge_strings() {
        String LESS_THAN_OR_EQUAL = "<= ";
        String GREATER_THAN = "> ";
        String[] catNames = new String[3]; //(Globals.UNKNOWN_CATEGORY_VAL, 3);
        // CatNames is an array of 3 MStrings:
        //   unknown, left node (less than or equal to thresholdVal), or
        //   right node (greater than thresholdVal).
        catNames[Globals.UNKNOWN_CATEGORY_VAL] = Globals.UNKNOWN_VAL_STR;
        catNames[Globals.UNKNOWN_CATEGORY_VAL+1] = LESS_THAN_OR_EQUAL
        + thresholdVal;
        catNames[Globals.UNKNOWN_CATEGORY_VAL+2] = GREATER_THAN
        + thresholdVal;
        
        return catNames;
    }
    
    /** Returns the edge label leading to the node that the given instance should use
     * to continue scoring or categorization.
     *
     * @param inst The instance being scored.
     * @return Returns the category found after traversing the categorizer.
     */
    public AugCategory branch(Instance inst) {
        //   DBG(inst.attr_info(attrNum).compatible_with(attrInfo, TRUE));
        if (attrInfo.is_unknown(inst.values[attrNum]))
            return new AugCategory(Globals.UNKNOWN_CATEGORY_VAL, UNKNOWN_VAL_STRRC);
        else if (attrInfo.get_real_val(inst.values[attrNum]) <= thresholdVal)
            return new AugCategory(Globals.FIRST_CATEGORY_VAL, LTEDscr);
        else
            return new AugCategory(Globals.FIRST_CATEGORY_VAL+1, GTDscr);
    }
    
    /** Prints a readable representation of the Categorizer to the given stream.
     *
     * @param stream The BufferedWriter to be printed to.
     * @param dp The display preferences.
     */
    public void display_struct(BufferedWriter stream,
    DisplayPref dp) {
        //   if (stream.output_type() == XStream)
        //      err << "ThresholdCategorizer::display_struct: Xstream is not a "
        //          << "supported stream."  << fatal_error;
        
        //   if (dp.preference_type() != DisplayPref::ASCIIDisplay)
        //      err << "ThresholdCategorizer::display_struct: Only ASCIIDisplay is "
        //          << "supported by ThresholdCategorizer."  << fatal_error;
        try{
            stream.write("Threshold Categorizer "+description()
            +" categorizing on attribute "+attrInfo.name()+'\n');
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Updates usedAttr to include the attributes used in this categorizer.
     *
     * @param usedAttr An array of used attributes. TRUE values indicate the attribute matched to that
     * index number is used, FALSE indicates it is not.
     */
    public void set_used_attr(boolean[] usedAttr) {
        //   ASSERT (attrNum >= 0 && attrNum < usedAttr.size());
        usedAttr[attrNum] = true;
    }
}