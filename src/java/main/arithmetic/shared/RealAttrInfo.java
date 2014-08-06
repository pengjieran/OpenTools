package arithmetic.shared;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.Writer;

/** The RealAttrInfo class allows AttrValue of type Real. The standard definitions
 * of the comparison operators is implemented.
 *
 */
public class RealAttrInfo extends AttrInfo {
    /** The minimum value for this attribute. **/
    private double min;
    /** The maximum value for this attribute. **/
    private double max;
    
    /** Constructor.
     * @param s The name for this attribute.
     */
    public RealAttrInfo(String s) {
        super(s,AttrInfo.real);
    }
    
    /** Checks if this AttrInfo subclass can be cast as a RealAttrInfo class.
     * @return Always returns TRUE for the RealAttrInfo class.
     */
    public boolean can_cast_to_real(){return true;}
    
    /** Casts this AttrInfo subclass to a RealAttrInfo class.
     * @return A reference to this RealAttrInfo object.
     */
    public RealAttrInfo cast_to_real(){return this;}
    
    /** Reads the value for the attribute from the BufferedReader specified.
     * @return The attribute read from the BufferedReader.
     * @param stream	The specified BufferedReader to be read from.
     * @param isTest	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f		The FileSchema for the file being read.
     */
    public AttrValue read_attr_value(BufferedReader stream, boolean isTest,
    FileSchema f ) {
        // behave the same whether or not we're reading test data
        //      (void)isTest;
        
        RealAttrValue attrValue = new RealAttrValue();
        if (!f.skip_white_comments_same_line(stream))
            Error.parse_error(stream, "Attribute value expected");
        
        try {
            stream.mark(1);
            char ch = (char)stream.read();
            stream.reset();
            
            if (ch != '?') {
                boolean sameLine = false;
                boolean[] boolArray = new boolean[1];//ADDED BY JL
                boolArray[0] = true;//ADDED BY JL
                String realStr = f.read_word(stream, sameLine, boolArray);//(stream, false, sameLine, true);
                double val = 0;
                try {
                    val = Double.parseDouble(realStr);
                    if(val == Globals.UNKNOWN_STORED_REAL_VAL) {
                        System.err.print("Warning: real value "+val+" is the same as "+
                        "the bit pattern for an unknown real value.  Changing the "+
                        "value to ");
                        val /= 2;
                        System.err.println(val+".");
                    }
                }
                catch(NumberFormatException e) {
                    Error.parse_error(stream,
                    "The value \"" + realStr+
                    "\" is not a legitimate "+
                    "Real value for attribute "+ name());
                }
                attrValue.realVal = (float)val;
            }
            else {
                String str = f.read_word_on_same_line(stream, true, false);//(stream, true);
                //            (void)str;  // to avoid compiler warning in FAST
                //            DBGSLOW(ASSERT(str[0] == '?'));
                attrValue.realVal = (float)Globals.UNKNOWN_STORED_REAL_VAL;
            }
        }
        catch(Exception e){e.printStackTrace();}
        return attrValue;
    }
    
    /** Reads the value for the attribute from the StreamTokenizer specified.
     * @return The attribute read from the StreamTokenizer.
     * @param stream	The specified StreamTokenizer to be read from.
     * @param isTest	TRUE if this value is for a test Instance, FALSE otherwise.
     * @param f		The FileSchema for the file being read.
     */
    public AttrValue read_attr_value(StreamTokenizer stream, boolean isTest,
    FileSchema f ) {
        // behave the same whether or not we're reading test data
        //      (void)isTest;
        
        RealAttrValue attrValue = new RealAttrValue();
        //      if (!f.skip_white_comments_same_line(stream))
        if(stream.ttype == (int)',')
            Error.parse_error(stream, "Attribute value expected");
        
        try{
            //       stream.mark(1);
            //       char ch = (char)stream.read();
            //       stream.reset();
            
            if (stream.ttype != '?') {
                boolean sameLine = false;
                boolean[] boolArray = new boolean[1];//ADDED BY JL
                boolArray[0] = true;//ADDED BY JL
                //            String realStr = f.read_word(stream, sameLine, boolArray);//(stream, false, sameLine, true);
                double val = 0;
                if (stream.ttype == StreamTokenizer.TT_NUMBER) {
                    val = stream.nval;
                    //               val = Double.parseDouble(realStr);
                    if(val == Globals.UNKNOWN_STORED_REAL_VAL) {
                        System.err.print("Warning: real value "+val+" is the same as "+
                        "the bit pattern for an unknown real value.  Changing the "+
                        "value to ");
                        val /= 2;
                        System.err.println(val+".");
                    }
                }
                else Error.parse_error(stream,
                "The value \"" + stream.toString()+ //realStr+
                "\" is not a legitimate "+
                "Real value for attribute "+ name());
                attrValue.realVal = val;
            }else{
                //            String str = f.read_word_on_same_line(stream, true, false);//(stream, true);
                //            (void)str;  // to avoid compiler warning in FAST
                //            DBGSLOW(ASSERT(str[0] == '?'));
                attrValue.realVal = (float)Globals.UNKNOWN_STORED_REAL_VAL;
            }
        }catch(Exception e){e.printStackTrace();}
        return attrValue;
    }
    
    
    /** Checks if the specified AttrValue is unknown.
     * @return TRUE if the specified AttrValue is unknown.
     * @param realValue	The specified AttrValue.
     */
    public boolean is_unknown(AttrValue realValue) {
        return (realValue.realVal == Globals.UNKNOWN_STORED_REAL_VAL);
    }
    
    
    /** Returns the real value of this AttrValue.
     * @return The real value of this AttrValue.
     * @param av realValue The specified av.
     */
    public double get_real_val(AttrValue av) {
        if(av.type != AttrValue.real)
            Error.err("RealAttrInfo:get_real_val: cannot get "
            + " a real value from a "+attr_type_to_string(av.type)
            + " AttrValue --> fatal_error");
        if(is_unknown(av))
            Error.err("RealAttrInfo:get_real_val: trying to get "
            + " UNKNOWN value --> fatal_error");
        return av.realVal;
    }
    
    /** Sets the specified AttrValue to the specified value.
     * @param av	The specified AttrValue.
     * @param val	The specified new value.
     */
    public void set_real_val(AttrValue av, double val) {
        
        if(GlobalOptions.debugLevel >= 1){
            if (av.type == AttrValue.unknown)
                av.type = AttrValue.real;
            else if (av.type != AttrValue.real)
                Error.fatalErr("RealAttrInfo:set_real_val: Cannot assign a real value to a"+
                attr_type_to_string(av.type)+"AttrValue_");
        }
        if (val == Globals.UNKNOWN_STORED_REAL_VAL)
            Error.fatalErr("RealAttrInfo::set_real_val: attempt to store value="+
            "UNKNOWN_STORED_REAL_VAL="+Globals.UNKNOWN_STORED_REAL_VAL);
        if (val > Globals.STORED_REAL_MAX)
            Error.fatalErr("RealAttrInfo::set_real_val: value "+
            val+" too large.  Maximum is "+Globals.STORED_REAL_MAX);
        if (val < -Globals.STORED_REAL_MAX)
            Error.fatalErr("RealAttrInfo::set_real_val: value "+
            val+" too small.  Minimum valus is "+ -Globals.STORED_REAL_MAX);
        av.realVal = (float)val;
    }
    
    /** Normalize the specified AttrValue according to the min and max values for
     * this AttrValue.
     * @return The normalized value.
     * @param val	The AttrValue to be normalized.
     */
    public double normalized_value(AttrValue val) {
        if(is_unknown(val))
            Error.err("RealAttrInfo::normalized_value: Unknown"
            +" attribute value passed to RealAttrinfo "+name()+" fatal_error");
        if(min==max)
            return .5;
        else
            return (val.realVal - min) / (max-min);
    }
    
    /** Returns a String value of the value in the specified AttrInfo.
     * @return A String value of the value in this AttrInfo.
     * @param val	The specified AttrInfo.
     */
    public String attrValue_to_string(AttrValue val) {
        if (is_unknown(val))
            return UNKNOWN_VAL_STR;
        String ret = Double.toString(val.realVal);
        return ret.substring(0,ret.indexOf((int)'.')+ 2);
    }
    
    /** Returns TRUE if given AttrValue are equivalent, FALSE otherwise. Assumes
     * that given AttrValue are both of the type described by the instance of
     * RealAttrInfo calling this function.
     * @param realValue1 The first AttrValue to be compared.
     * @param realValue2 The second AttrValue to be compared.
     * @return TRUE if the give AttrValue are equivalent, FALSE otherwise.
     */
    // @@ why does this exist if we have equal() ?
    public boolean equal_value(AttrValue realValue1, AttrValue realValue2) {
        return MLJ.approx_equal((float)(realValue1.realVal),
        (float)(realValue2.realVal), 10);
    }
    
    /** At the moment this does nothing. Checks if the given AttrValue is in the
     * range for this AttrValue.
     * @param val	The specified AttrValue.
     */
    public void check_in_range(AttrValue val){}
    
    /** Sets the given AttrValue to be unknown.
     * @param val The AttrValue to be changed.
     */
    public void set_unknown(AttrValue val) {
/*   ATTRDBG(if (val.type == unknown)
          val.type = real;
       else if (val.type != real)
          err << "RealAttrInfo:set_real_val: Cannot assign a real value to a"
              << attr_type_to_string(val.type) << "AttrValue_"
              << fatal_error);
 */
        val.realVal = Globals.UNKNOWN_STORED_REAL_VAL;
    }
    
    /** Displays the attribute values in names file format.
     * @param stream	Writer to which the display will be written.
     */
    public void display_attr_values(Writer stream)
    { display_attr_values(stream,false);}
    
    /** Displays the attribute values in names file format.
     * @param stream	Writer to which the display will be written.
     * @param protectchars True if "\" and "." are to be protected by "\"
     * characters, false otherwise.
     */
    public void display_attr_values(Writer stream, boolean protectchars) {
        try{
            stream.write("continuous");
            if (min != Globals.UNKNOWN_STORED_REAL_VAL)
                stream.write(" min="+min);
            if (max != Globals.UNKNOWN_STORED_REAL_VAL)
                stream.write(" max="+max);
            stream.write(".\n");
        }catch(IOException e){e.printStackTrace();System.exit(1);}
    }
}
