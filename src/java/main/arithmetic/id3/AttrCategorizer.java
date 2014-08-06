package arithmetic.id3;
import java.io.BufferedWriter;
import java.io.IOException;

import arithmetic.shared.AttrInfo;
import arithmetic.shared.AttrValue;
import arithmetic.shared.AugCategory;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Error;
import arithmetic.shared.Instance;
import arithmetic.shared.MLJ;
import arithmetic.shared.NominalAttrInfo;
import arithmetic.shared.Schema;

/** The AttrCategorizer categorizes an Instance based on an attribute.
 * Assumes the attribute is of a type derived from Nominal.
 * @author James Louis	2/25/2001	Ported to Java.
 * @author Chia-Hsin Li	11/23/94	Add operator==
 * @author Ronny Kohavi	8/02/93	Initial revision
 */
public class AttrCategorizer extends NodeCategorizer {
    /** The number of the specific attribute in this AttrCategorizer. **/
    private int attrNum;
    /** The information on labels and attributes. **/
    private AttrInfo attrInfo;
    
    /** Constructor.
     * @param schma		The schema for this categorizer.
     * @param attributeNum	The number of the specific attribute in the schema
     * for the data.
     * @param dscr			The description of this categorizer.
     */
    public AttrCategorizer(Schema schma, int attributeNum, String dscr) {
        super(num_nominal_attr(schma, attributeNum), dscr, schma);
        try {
            attrInfo =(AttrInfo) schma.attr_info(attributeNum).clone();
        } catch(CloneNotSupportedException e) {
            System.out.println(e.toString());
        }
        attrNum = attributeNum;
    }
    
    /** Finds the number of attributes that are nominal in nature.
     * @return The number of nominal attributes.
     * @param schema	The schema containing attribute and label information.
     * @param attrNum	The number of the specific attribute in the schema for
     * the data.
     */
    public static int num_nominal_attr(Schema schema, int attrNum) {
        if (attrNum < 0)
            Error.fatalErr("AttrCategorizer::num_nominal_attr: Bad attribute number: " +
            attrNum);
        
        // find the matching schema
        NominalAttrInfo nai = schema.nominal_attr_info(attrNum);
        return nai.num_values();
    }
    
    /** Identifies the class.
     * @deprecated Java's instanceof operator should be used instead.
     * @return A value uniquely identifying this object as an AttrCategorizer.
     */
    public int class_id() {
        return CLASS_ATTR_CATEGORIZER;
    }
    
    /** Choses the child branch that the given instance would go down. Used
     * for scoring, categorization, and splitting of InstanceLists.
     * @return The AugCategory that correlates to the specified Instance.
     * @param inst	The specified Instance.
     */
    public AugCategory branch(Instance inst) {
        //      if (Globals.DBG) inst.attr_info(attrNum).compatible_with(attrInfo, true);
        AttrValue val = inst.values[attrNum];
        String dscr = attrInfo.attrValue_to_string(val);
        AugCategory ac = new AugCategory(attrInfo.get_nominal_val(val) , dscr);
        return ac;
    }
    
    /** Prints a readable representation of the Categorizer to the given stream.
     * @param stream	The output stream to be written to.
     * @param dp		The preference settings for the display.
     */
    public void display_struct(BufferedWriter stream,
    DisplayPref dp) {
        //   if (stream.output_type() == XStream)
        //      err << "AttrCategorizer::display_struct: Xstream is not a supported "
        //             "stream"  << fatal_error;
        
        //   if (dp.preference_type() != DisplayPref::ASCIIDisplay)
        //      err << "AttrCategorizer::display_struct: Only ASCIIDisplay is "
        //             "supported by AttrCategorizer"  << fatal_error;
        try {
            stream.write("Attribute Categorizer " +description()
            + " categorizing on attribute " +attrInfo.name() + '\n');
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /** Updates usedAttr to include the attributes used in this categorizer.
     * @param usedAttr A boolean array representing attributes. Each element set
     * to TRUE indicates the attribute is used. It is FALSE
     * otherwise. This method sets the appropriate attribute
     * element to true.
     */
    public void set_used_attr(boolean[] usedAttr) {
        MLJ.ASSERT((attrNum >= 0)&&(attrNum < usedAttr.length),
        "AttrCategorizer::set_used_attr: "
        + "attrNum is not between 0 and usedAttr.length");
        usedAttr[attrNum] = true;
    }
    
}