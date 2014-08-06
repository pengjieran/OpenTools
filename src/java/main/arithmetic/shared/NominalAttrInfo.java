package arithmetic.shared;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.LinkedList;
import java.util.ListIterator;

/** The NominalAttrInfo class allows giving the different values that the attribute
 * may obtain.
 *
 */
public class NominalAttrInfo extends AttrInfo {
    /** Possible values for this Attribute. **/
    private String[] values;
    /** Indicator if this attribute is fixed and can not be altered. **/
    private boolean fixedValueSet;
    
    /** Cloning function for this NominalAttrInfo class.
     * @return The clone of this object.
     * @throws CloneNotSupportedException if the cloning process for NominalAttrInfo data members encounters an Exception.
     */
    public Object clone() throws CloneNotSupportedException {
        NominalAttrInfo naiClone = new NominalAttrInfo(attrName, 0);
        naiClone.values = new String[values.length];
        for(int i=0;i<values.length;i++)
            naiClone.values[i] = get_value(i + Globals.FIRST_NOMINAL_VAL);
        return naiClone;
    }
    
    /** Constructor.
     * @param aName	The name for this attribute.
     * @param attrVals	The possible values for this attribute.
     */
    public NominalAttrInfo(String aName, LinkedList attrVals) {
        super(aName,AttrInfo.nominal);
        values = new String[attrVals.size()];
        ListIterator pix = attrVals.listIterator(0);
        for(int i=0;i<attrVals.size();i++) {
            values[i] = (String)pix.next();//attrVals.get(i);
        }
        attrVals = null;
        fixedValueSet = true;
    }
    
    /** Constructor.
     * @param aName The name of this attribute.
     * @param sizeHint Currently ignored.
     */
    public NominalAttrInfo(String aName, int sizeHint) {
        super(aName,AttrInfo.nominal);
        fixedValueSet = false;
        values = new String[0];
    }
    
    /** Fixes the values for this attribute so new values will be added
     * automatically. Alternately call with FALSE to unfix the values for this
     * attribute.
     * @param shouldFix TRUE if the values should be fixed, FALSE if the values should be
     * unfixed.
     */
    public void fix_values(boolean shouldFix) {
        fixedValueSet = shouldFix;
    }
    
    /** Displays the attribute values in names file format.
     */
    public void display_attr_values() {
        //if the attribute is marked "ignore" say so here
        if(get_ignore())
            System.out.println("ignore_attribute.");
        
        //if the attribute is non-fixed, display "discrete" here
        else if(!is_fixed())
            System.out.println("discrete.");
        else {
            int numValues = num_values();
            for(int i=Globals.FIRST_NOMINAL_VAL;i<=numValues;i++) {
                System.out.print(get_value(i));
                //            if(i!=numValues-1)
                if(i!=numValues)
                    System.out.print(", ");
                else
                    System.out.println(".");
            }
        }
    }
    
    /** Checks if the specified value is within the range for this attribute.
     * Causes fatal_error if the given value is not a valid nominal value for
     * this NominalAttrInfo. Otherwise does nothing for NominalAttrInfo.
     * Assumes that value stored in the specified AttrValue is an integer, since
     * this NominalAttrInfo uses integers to represent values.
     * @param val The value to be checked.
     */
    public void check_in_range(AttrValue val) {
        check_valid_attr_value_type(val);
        int intVal = val.intVal; //net_to_host(val.value.inVal);
        if((intVal < Globals.UNKNOWN_NOMINAL_VAL) // + NOMINAL_OFFSET
        || ( intVal > Globals.UNKNOWN_NOMINAL_VAL + num_values() )) // + NOMINAL_OFFSET
            Error.err("NominalAttrInfo::check_in_range : "
            + intVal /*-NOMINAL_OFFSET*/ + " must be in range "
            + Globals.UNKNOWN_NOMINAL_VAL + " to "
            + Globals.UNKNOWN_NOMINAL_VAL + num_values()
            + " for " + name() + " -->fatal_error");
    }
    
    /***************************************************************************
  Checks if the specified value is a nominal value. Causes fatal error
message if the given AttrValue is not nominal.
@param val	The value to be checked.
     ***************************************************************************/
    private void check_valid_attr_value_type(AttrValue val) {
        if(val.type != AttrInfo.nominal)
            Error.err("NominalAttrInfo::check_valid_attr_value_type: "
            + " Non-nominal AttrValue type "
            + attr_type_to_string(val.type)
            + " -->fatal_error ");
    }
    
    /** Converts the given AttrValue to the corresponding String representation
     * of the nominal attribute value. "?" will be returned for UNKNOWN_NOMINAL_VAL.
     * Assumes that value stored in val is an integer, since this
     * NominalAttrInfo class uses integers to represent nominal values.
     * @param val The value to convert to a string.
     * @return The String representation of the given value.
     */
    public String attrValue_to_string(AttrValue val) {
        //if(val == null)System.out.println("attrValue_to_string:AV is NULL");
        return get_value(get_nominal_val(val));
    }
    
    /** Returns the nominal value stored in the specified AttrValue. Returns a
     * fatal error message if the value is not a nominal value.
     * @return The value stored in the AttrValue specified.
     * @param av	The AttrValue containing the value required.
     */
    public int get_nominal_val(AttrValue av) {
        if(av == null)System.out.println("get_nominal_val:"
        +"AV is NULL");
        if(av.type != AttrInfo.nominal)
            Error.err("NominalAttrInfo::get_nominal_val:"
            +" Cannot get a nominal value from a "
            +attr_type_to_string(av.type)
            +" AttrValue. -->fatal_error");
        return av.intVal; //net_to_host(av.intVal) - NOMINAL_OFFSET
    }
    
    /** Sets the given AttrValue specified to be unknown.
     * @param val The value to be set to unknown.
     */
    public void set_unknown(AttrValue val) {
        val.type = AttrInfo.unknown;
        set_nominal_val(val, Globals.UNKNOWN_NOMINAL_VAL);
    }
    
    /** Returns TRUE if given AttrValue is unknown. Assumes that given AttrValue
     * is of the type described by the instance of NominalAttrInfo calling this
     * function.
     * @return TRUE if the value is unknown, FALSE otherwise.
     * @param nominalValue The AttrValue being checked for unknown value.
     */
    public boolean is_unknown(AttrValue nominalValue) {
        return nominalValue.intVal == Globals.UNKNOWN_NOMINAL_VAL;
    }
    
    /** Sets the representation of the given AttrValue using a String as input.
     * If the attribute does not have a fised value set(fixedValueSet = TRUE), new
     * values may be added.
     * @param av		The AttrValue where the new value will be stored.
     * @param str		The String representing the new value.
     * @param suppress	If the suppress parameter is TRUE, any unrecognized values
     * will become unknowns if fixedValueSet is set FALSE.
     */
    public void set_nominal_string(AttrValue av, String str, boolean suppress) {
        int val;
        //similar to nominal_to_int, but check for errors
        //      if(str==AttrInfo.UNKNOWN_VAL_STR)
        //      if(str .compareTo(AttrInfo.UNKNOWN_VAL_STR) == 0)
        if(str == AttrInfo.UNKNOWN_VAL_STR) {
            val = Globals.UNKNOWN_NOMINAL_VAL;
        }
        else if(get_ignore()) {
            val = Globals.UNKNOWN_NOMINAL_VAL;
        }
        else {
            for (val = Globals.FIRST_NOMINAL_VAL;
            val<=values.length && !get_value(val).equals(str);
            val++)//Locates the correct nominal value.
                ;//null
            //USING NEW GLOBAL VALUE -JL
            if(val > values.length ) { //When the value can't be found in the possible nominal values.
                if(fixedValueSet) {
                    Error.err("NominalAttrInfo::set_nominal_string:"
                    + str +" is not a valid attribute value for a fixed set"
                    +" "+name()+".\nPossible values are: ");
                    display_attr_values();
                    Error.err("fatal_error");
                }
                else if(suppress) {
                    //make the value unknown
                    val = Globals.UNKNOWN_NOMINAL_VAL;
                }
                else {
                    //add a new value to the array
                    String[] newValues = new String[values.length+1];
                    for(int i=0;i<values.length;i++)newValues[i]=values[i];
                    values = null;
                    values = newValues;
                    newValues = null;
                    values[values.length-1]=str;
                    System.out.print("Values : "+values[values.length-1]+" ");
                }
            }
        }
        set_nominal_val(av,val);
    }
    
    /** Sets the integer representation of the given AttrValue.
     * @param av	The AttrValue storing the new value.
     * @param val	The new value to be stored.
     */
    public void set_nominal_val(AttrValue av, int val) {
        if(av.type == AttrInfo.unknown)
            av.type = AttrInfo.nominal;
        else if(av.type != nominal)
            Error.err("NominalAttrInfo::set_nominal_val: "
            +"cannot assign a nominal value to a "
            +attr_type_to_string(av.type)+" AttrValue-->fatal_error");
        av.intVal = val; //host_to_net(val + NOMINAL_OFFSET);
    }
    
    /** Checks if this attribute's possible value set is fixed.
     * @return TRUE if the value set is fixed, FALSE otherwise.
     */
    public boolean is_fixed() {
        return fixedValueSet;
    }
    
    /** Returns number of possible values that the attribute has.
     * @return The number of possible values.
     */
    public int num_values() {
        return values.length;// + 1;
    }
    
    /** Returns the ith attribute value stored in this NominalAttrInfo's
     * possible value set.
     * @return A String value representing the value stored in the specified
     * position of the possible values set.
     * @param i	The index value of the value queried.
     */
    public String get_value(int i) {
        //  if (i == Globals.UNKNOWN_NOMINAL_VAL)
        //     return Globals.UNKNOWN_VAL_STR;
        
        
        if (i == 0) return "";					//ADDED TO COMPENSTATE FOR UNKNOWN VALUE -JL
        return values[i - Globals.FIRST_NOMINAL_VAL];	//USING NEW GLOBAL VALUE -JL
        
    }
    
    /** Checks if this AttrInfo subclass can be cast to a NominalAttrInfo class.
     * @return Always returns TRUE for the NominalAttrInfo class.
     */
    public boolean can_cast_to_nominal() {
        return true;
    }
    
    /** Casts this AttrInfo subclass to a NominalAttrInfo class.
     * @return A reference to this NominalAttrInfo object.
     */
    public NominalAttrInfo cast_to_nominal() {
        return this;
    }
    
    /** Reads in an attribute value from the specified BufferedReader. Attribute
     * value to be read assumed to match AttrInfo. Although this function may
     * potentially modify this NominalAttrInfo because it may add a new nominal
     * value if fixedValueSet is FALSE. If the ignore flag is set, we read
     * specially so that real values can be properly ignored.  Then we always set
     * an unknown value.
     * @return The attribute value read.
     * @param stream	The BufferedReader containing the file to be read.
     * @param isTest	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f		The FileSchema for the file being read from.
     */
    public AttrValue read_attr_value(BufferedReader stream, boolean isTest,
    FileSchema f) {
        AttrValue av = new AttrValue();
        set_unknown(av);
        if(get_ignore()) {
            String dummy = f.read_word_on_same_line(stream, true, true);
            //set_unknown(av);
        }
        else {
            String str = f.read_word_on_same_line(stream, true,true);
            set_nominal_string(av, str, isTest);
        }
        return av;
    }
    
    /** Reads in an attribute value from the specified StreamTokenizer. Attribute
     * value to be read assumed to match AttrInfo. Although this function may
     * potentially modify this NominalAttrInfo because it may add a new nominal
     * value if fixedValueSet is FALSE. If the ignore flag is set, we read
     * specially so that real values can be properly ignored.  Then we always set
     * an unknown value.
     * @return The attribute value read.
     * @param stream	The StreamTokenizer containing the file to be read.
     * @param isTest	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f		The FileSchema for the file being read from.
     */
    public AttrValue read_attr_value(StreamTokenizer stream, boolean isTest, FileSchema f) {
        AttrValue av = new AttrValue();
        //set_unknown(av);
        if(get_ignore()) {
            //         String dummy = f.read_word_on_same_line(stream, true, true);
            set_unknown(av);
        }
        else {
            //String str = stream.sval; //CHANGED FOR ZOO DATASET -JL
            String str = null;
            if (stream.ttype == StreamTokenizer.TT_WORD) str = stream.sval;
            else if (stream.ttype == StreamTokenizer.TT_NUMBER)
                str = Double.toString(stream.nval);
            else if (stream.ttype == (int)'?')
                str = "?";	//FIX ERROR WITH READING '?' -JL
            if (str.indexOf((int)'.') > -1)
                str = str.substring(0,str.indexOf((int)'.'));
            set_nominal_string(av, str, isTest);
        }
        return av;
    }
    
    /** Returns TRUE if given AttrValues are equivalent. Assumes that given
     * AttrValues are both of the type described by the NominalAttrInfo object
     * calling this function.
     * @return TRUE if the AttrValues are equal, FALSE otherwise.
     * @param val1	An AttrValue to be compared.
     * @param val2	An AttrValue to be compared.
     */
    public boolean equal_value(AttrValue val1, AttrValue val2) {
        //   ATTRDBG(check_valid_attr_value_type(val1); check_valid_attr_value_type(val2));
        return (val1.intVal == val2.intVal);
    }
    
    /** Displays the attribute values in names file format. ProtectChars forces
     * quoting of special characters (now only periods).
     * @param stream		Writer to which the display will be written.
     * @param protectChars	True if "\" and "." are to be protected by "\"
     * characters, false otherwise.
     */
    public void display_attr_values(Writer stream,
    boolean protectChars) {
        try{
            // if the attribute is marked "ignore" say so here
            if(get_ignore())
                stream.write("ignore_attribute.\n");
            
            // if the attribute is non-fixed, display "discrete" here
            else if(!is_fixed())
                stream.write("discrete.\n");
            
            else {
                
                int numValues = num_values();
                for (int i = 0; i < numValues; i++) {
                    if (protectChars)
                        stream.write(protect_chars(get_value(Globals.FIRST_CATEGORY_VAL+i)));
                    else
                        stream.write(get_value(Globals.FIRST_CATEGORY_VAL+i));
                    if (i != numValues - 1)
                        stream.write(", ");
                    else
                        stream.write(".\n");
                }
                
            }
        }catch(IOException e){e.printStackTrace();}
        
    }
}
