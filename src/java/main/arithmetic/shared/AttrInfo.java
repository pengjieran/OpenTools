package arithmetic.shared;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.io.Writer;
/** The AttrInfo class allows giving attribute names, types, and a position
 * in the instance array. This implementation assumes the value of
 * UNKNOWN_REAL_VAL is never used for a known real value. Given k discrete
 * categories, they will be mapped into integers in the range [0..k-1].
 * Globals.UNKNOWN_CATEGORY_VAL is used for unknown/undefined.
 *
 * @author James Louis 5/30/2001   Ported to Java.
 * @author Richard Long    5/17/94 Added AttrValue_, NominalAttrValue_, and RealAttrValue_ classes.
 * @author Brian Frasca    1/21/94 Added AttrInfo::display().
 * @author Svetlozar Nestorov  1/15/94 Added clone(int aNum) in AttrInfo class.
 * @author Brian Frasca    1/13/94 Added _equal_value(), is_unknown()
 * @author Svetlozar Nestorov  1/08/94 Added copy constructors and
 * method clone().
 * @author Richard Long    7/14/93 Initial revision (.c)
 * @author Ronny Kohavi    7/13/93 Initial revision (.h)
 */

abstract public class AttrInfo implements Cloneable {
    /** The name of this attribute.
     */
    public String attrName;
    /** The type of this attribute.
     */
    public byte attrType;
    /** TRUE if this attribute should be ignored during induction.
     */
    private boolean ignore;
    
    /** Value for Unknown attribute type.
     */
    public static final byte unknown = 0;
    /** Value for Real attribute type.
     */
    public static final byte real = 1;
    /** Value for BoundedReal attribute type.
     */
    public static final byte boundedReal = 2;
    /** Value for Nominal attribute type.
     */
    public static final byte nominal = 3;
    /** Value for LinearNominal attribute type.
     */
    public static final byte linearNominal = 4;
    /** Value for TreeStructured attribute type.
     */
    public static final byte treeStructured = 5;
    /** Value for InternalDisjunction attribute type.
     */
    public static final byte internalDisjunction = 6;
    /** Value for UserReal attribute type.
     */
    public static final byte userReal = 7;
    /** Value for UserNominal attribute type.
     */
    public static final byte userNominal = 8;
    /** Value for UserLinearNominal attribute type.
     */
    public static final byte userLinearNominal = 9;
     /** Value for UserTreeStructured attribute type.
      */
    public static final byte userTreeStructured = 10;
    /** Value for UserInternalDisjunction attribute type.
     */
    public static final byte userInternalDisjunction = 11;
    
    /** The String to be displayed for unknown values.
     */
    public static final String UNKNOWN_VAL_STR = "?";
    
    //Simple methods
    
    /** Checks if this AttrInfo object has linear values. Always returns FALSE.
     * @return Always FALSE.
     */
    public boolean is_linear() { return false; }    
    
    /** Constructor.
     * @param aName	The name of the attribute.
     * @param aType	The type of the attribute.
     */
    public AttrInfo(String aName, byte aType) {
        attrName = aName;
        attrType = aType;
    }
    
    /** Copy constructor.
     * @param source	The AttrInfo to be copied.
     */
    public AttrInfo(AttrInfo source) {
        attrName = source.attrName;
        attrType = source.attrType;
    }
    
    /** Checks if the specified AttrValue is an unknown value.
     * @returns TRUE if the value is unknown, FALSE otherwise.
     * @param v The specified AttrValue containing the value to be checked.
     * @return TRUE if this AttrValue instance is an unknown value, FALSE otherwise.
     */
    abstract public boolean is_unknown(AttrValue v);
    
    /** Checks if the specified AttrValue is with in the range indicated in
     * the Globals file.
     * @returns TRUE if the value is within the range, FALSE otherwise.
     * @param val The AttrValue being checked.
     */
    abstract public void check_in_range(AttrValue val);
    
    /** Returns a String value of the type of attribute the specified byte
     * represents.
     * @return A String with the name of the type of data represented by this byte
     * value.
     * @param t	The byte value to be checked.
     */
    public static String attr_type_to_string(byte t)
    {  String temp = "";
       switch(t){
           case 0 : temp = "unknown";
           break;
           case 1 : temp = "real";
           break;
           case 2 : temp = "boundedReal";
           break;
           case 3 : temp = "nominal";
           break;
           case 4 : temp = "linearNominal";
           break;
           case 5 : temp = "treeStructured";
           break;
           case 6 : temp = "internalDisjunction";
           break;
           case 7 : temp = "userReal";
           break;
           case 8 : temp = "userNominal";
           break;
           case 9 : temp = "userLinearNominal";
           break;
           case 10 : temp = "userTreeStructured";
           break;
           case 11 : temp = "userInternalDisjuntion";
           break;
       }
       return temp;
    }
    
    /** Sets the value for the specified AttrValue to the unknown value for that
     * attribute type.
     * @param a	The value to be changed.
     */
    abstract public void set_unknown(AttrValue a);
    
    /** Returns a String representing the value for the specified AttrValue.
     * @return A String representing the value for the specified AttrValue.
     * @param a	The AttrValue containing the value.
     */
    abstract public String attrValue_to_string(AttrValue a);
    
    /** Always returns zero and displays an error message. AttrInfo does not have
     * methods specific to different data types except as an necessity. Not meant
     * for use with AttrInfo base class.
     * @return Always zero.
     * @param av The value of the attribute.
     */
    public double get_real_val(AttrValue av) {
        Error.err("AttrInfo::get_real_val: cannot be called for"
        +" a " + attr_type_to_string(type())+ " AttrInfo-->fatal_error");
        return 0;
    }
    
    /** Sets the real value for this AttrInfo. Does nothing for the AttrInfo
     * base class. Not meant for use with AttrInfo base class.
     * @param av	The AttrValue where the new value is to be stored.
     * @param v	The new value to be stored.
     */
    public void set_real_val(AttrValue av, double v) {
        Error.err("AttrInfo::set_real_val: cannot be called for"
        +" a " + attr_type_to_string(type())+ " AttrInfo-->fatal_error");
    }
    
    /** Sets the nominal name for this AttrInfo. Does nothing for the AttrInfo
     * base class.
     * @param a	The AttrValue where the new value is to be stored.
     * @param s	The new value to be stored.
     * @param b	A boolean value.
     */
    public void set_nominal_string(AttrValue a, String s, boolean b) {
        Error.fatalErr("AttrInfo::set_nominal_val:cannot be "
        +"called for a "+attr_type_to_string(type())+" AttrInfo");
    }
    
    /** Sets the nominal value for this AttrInfo. Does nothing for the AttrInfo
     * base class. Not meant for use with AttrInfo base class.
     * @param a	The AttrValue where the new value is to be stored.
     * @param v	The new value to be stored.
     */
    public void set_nominal_val(AttrValue a, int v) {
        Error.fatalErr("AttrInfo::set_nominal_val:cannot be "
        +"called for a "+attr_type_to_string(type())+" AttrInfo");
    }
    
    /** Checks if this attribute should be ignored during induction.
     * @return TRUE if this attribute should be ignored, FALSE otherwise.
     */
    public boolean get_ignore(){return ignore;}
    
    /** Reads the value for the attribute from the StreamTokenizer specified.
     * @return The attribute value read from the StreamTokenizer.
     * @param r	The specified StreamTokenizer to be read from.
     * @param i	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f	The FileSchema of the file being read.
     */
    abstract AttrValue read_attr_value(StreamTokenizer r, boolean i,
    FileSchema f);
    
    /** Reads the value for the attribute from the BufferedReader specified.
     * @return The attribute value read from the BufferedReader.
     * @param r	The specified BufferedReader to be read from.
     * @param i	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f	The FileSchema of the file being read.
     */
    public AttrValue read_attr_value(BufferedReader r, boolean i,
    FileSchema f) {
        Error.err("AttrInfo::read_attr_value: this method"
        +" not supported in base class -->fatal_error");
        return null;
    }
    
    /** Compares attrName, attrType, and attrNum of this AttrInfo to the specified
     * AttrInfo.
     * @return TRUE if attrName, attrType, and attrNum are equal, FALSE otherwise.
     * @param info			The AttrInfo to be compared to.
     * @param fatalOnFalse	TRUE if an Error message should be displayed if
     * these AttrInfos are not equal, FALSE
     * otherwise.
     */
    protected boolean equal_shallow(AttrInfo info, boolean fatalOnFalse) {
        if(attrName != info.name()) {
            if(fatalOnFalse==true)
                Error.fatalErr("AttrInfo:equal_shallow: names are"
                + " different: "+attrName+", "+info.name());
            return false;
        }
        if(attrType != info.type()) {
            if (fatalOnFalse==true)
                Error.fatalErr("AttrInfo::equal_shallow: types are"
                + " different: " +attrType + ", "
                + info.type());
            return false;
        }
        return true;
    }
    
    /** Returns the type of this attribute.
     * @return A byte representing the type of this attribute.
     */
    public byte type(){return attrType;}
    
    /** Returns the name of this attribute.
     * @return A String object with the name of this attribute.
     */
    public String name(){return attrName;}
    
    /** Sets wether this attribute should be ignored.
     * @param ig	TRUE if this attribute should be ignored.
     */
    public void set_ignore(boolean ig){ignore = ig;}
    
    /** Checks if this attribute can be considered a real value attribute.
     * @return Always FALSE for this class.
     */
    public boolean can_cast_to_real(){ return false;}
    
    /** Checks if this attribute can be considered a nominal value attribute.
     * @return Always FALSE for this class.
     */
    public boolean can_cast_to_nominal(){return false;}
    
     /** Clones this AttrInfo.
      * @return The new AttrInfo cloned.
      * @throws CloneNotSupportedException Thrown if cloning is not possible.
      */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    /** Determines if this AttrInfo can be considered a RealAttrInfo and creates
     * a new one if possible. For AttrInfo, this is not possible.
     * @return The RealAttrInfo containing the information for this AttrInfo.
     */
    public RealAttrInfo cast_to_real() {
        Error.err("AttrInfo::cast_to_real: Type "
        + attr_type_to_string(type()) +" is not derived from"
        +" RealAttrInfo --> fatal_error");
        return null;
    }
    
    /** Determines if this AttrInfo can be considered a Nominla AttrInfo and
     * creates a new one if possible. For AttrInfo, this is not possible.
     * @return The NominalAttrInfo containing the information for this AttrInfo.
     */
    public NominalAttrInfo cast_to_nominal() {
        Error.err("AttrInfo::cast_to_nominal is not "
        + "derived from NominalAttrInfo --> fatal_error");
        return null;
    }
    
    /** Always returns zero and displays an error message. AttrInfo does not have
     * methods specific to different data types except as an necessity. Not meant
     * for use with AttrInfo base class.
     * @return Always zero.
     * @param AV The value of the attribute.
     */
    public int get_nominal_val(AttrValue AV) {
        Error.fatalErr("AttrInfo::get_nominal_val: Cannot be called for a "
        +attr_type_to_string(type())+" AttrInfo");
        return 0;  // To avoid compiler warning
    }
    
    /** Determines if the specified AttrValues are equal.
     * @return TRUE if the specified AttrValues are equal, FALSE otherwise.
     * @param i_attrvalue1	An AttrValue to be compared.
     * @param i_attrvalue2	An AttrValue to be compared.
     */
    abstract public boolean equal_value(AttrValue i_attrvalue1,
    AttrValue i_attrvalue2);
    
    /** Displays the values for the attribute represented by this object to the
     * specified Writer.
     * @param stream		The Writer to which the values will be displayed.
     * @param protectChars	TRUE if the '/' and '.' characters should be
     * preceeded by a '/' character, FALSE otherwise.
     */
    abstract public void display_attr_values(Writer stream, boolean protectChars);
    
    /** Displays the values for the attribute represented by this object to the
     * specified Writer. Writes to Globals.Mcout and does not protect characters by
     * default.
     */
    public void display_attr_values(){display_attr_values(Globals.Mcout,false);}
    
    /** Displays the values for the attribute represented by this object to the
     * specified Writer. Does not protect characters by default.
     * @param stream		The Writer to which the values will be displayed.
     */
    public void display_attr_values(Writer stream){display_attr_values(stream,false);}    
    
    /** Protect special characters by prefixing them with \. We protect periods,
     * commas, and backslashes themselves.
     * @param str The String to be searched for protected characters.
     * @return The input String with protected characters marked.
     */
    static public String protect_chars(String str) {
        String retrn = new String(str);
        int index = 0;
        char[] prot_chars = {'/','.'};
        for(int i = 0; i < prot_chars.length; i++) {
            while((index = retrn.indexOf(prot_chars[i],index)) != -1){
                retrn = retrn.substring(0,index) + "\\" + retrn.substring(index);
                index++;
            }
        }
        return retrn;
        //obs      return str.protect("\\.,");
    }
    
}
