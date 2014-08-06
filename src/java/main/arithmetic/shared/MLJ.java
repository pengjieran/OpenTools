package arithmetic.shared;
import java.util.ListIterator;
import java.util.Vector;

/** This class contains methods and data members that were external to classes.
 */
public class MLJ {
    
    /** The clamping epsilon used to determine if a real value is significantly
     * different from another value.
     */    
    static public final double realEpsilon = 2.22204e-16;
    
    /** The clamping epsilon used to determine if a real value is significantly
     * different from another value.
     */    
    static public final double storedRealEpsilon= 1.1920928955078e-7;
    
    
    
    /** The clamping epsilon used to determine if a real value is significantly
     * different from another value during clamping.
     */    
    static public final double clampingEpsilon = realEpsilon * 10;
    
    /** If the given source value is lower than the lower bound, the source value is
     * changed to the lower bound value.
     * @param source The value to be clamped.
     * @param lowBound The lower bound.
     * @param additionalErrMsg Message to be displayed if clamping occurs.
     */
    static public void clamp_above(DoubleRef source, double lowBound,
    
    String additionalErrMsg) {
        clamp_above(source, lowBound, additionalErrMsg, 1);
    }
    
    
    /** If the given source value is lower than the lower bound, the source value is
     * changed to the lower bound value.
     * @param source The value to be clamped.
     * @param lowBound The lower bound.
     * @param additionalErrMsg Message to be displayed if clamping occurs.
     * @param precMultiplier The precision multiplier for real values that are close to the lower bound.
     */
    static public void clamp_above(DoubleRef source, double lowBound, String additionalErrMsg, int precMultiplier) {
        if (precMultiplier < 0) {
            Error.err(additionalErrMsg + '\n'+ "clamp_above(Real): precision multiplier ("+
            precMultiplier + ") must be non-negative-->fatal_error");
        }
        if (source.value >= lowBound) return;
        if (source.value < lowBound - clampingEpsilon * precMultiplier) {
            Error.err(additionalErrMsg + '\n'+ "clamp_above(Real): minimum value allowed ("+
            lowBound + ") exceeds variable to be clamped ("+ source + ") by more than is allowed ("+
            clampingEpsilon + ")-->fatal_error");
        }
        source.value = lowBound;
    }
    
    /** Compares two double values for equality.
     * @param lhs The left value to be compared.
     * @param rhs The right value to be compared.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(double lhs, double rhs) {
        return approx_equal(lhs,rhs,1);
    }
    
    /** Compares two double values for equality, using the given
     * precisionMultiplier to drive approximate comparisons.
     * @param lhs The left value to be compared.
     * @param rhs The right value to be compared.
     * @param precMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(double lhs, double rhs, int precMultiplier) {
        if (Globals.DBG) MLJ.ASSERT(precMultiplier >= 0,"MLJ::approx_equal: precMultiplier < 0");
        return (Math.abs(lhs - rhs) <= clampingEpsilon * precMultiplier * Math.max(1, Math.min(Math.abs(lhs), Math.abs(rhs))));
    }
    
    
    /** Compares two arrays of double values for equality.
     * @param lhs The left array to be compared.
     * @param rhs The right array to be compared.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    public static boolean approx_equal(double[] lhs, double[] rhs) {
        return approx_equal(lhs,rhs,1);
    }
    
    /** Compares two arrays of doubles for equality, using the given
     * precisionMultiplier to drive approximate comparisons at each element.
     * @param lhs The left array to be compared.
     * @param rhs The right array to be compared.
     * @param precisionMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    public static boolean approx_equal(double[] lhs, double[] rhs,
    int precisionMultiplier) {
        // FALSE if bounds differ
        if(lhs.length != rhs.length)
            return false;
        for (int x = 0; x < lhs.length; x++)
            if (!approx_equal(lhs[x], rhs[x], precisionMultiplier))
                return false;
        
        return true;
    }
    
    /** Compares two matrices of double values for equality.
     * @param lhs The left matrix to be compared.
     * @param rhs The right matrix to be compared.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(double[][] lhs, double[][] rhs) {
        return approx_equal(lhs,rhs,1);
    }
    
    /** Compares two matrices of double values for equality, using the given
     * precisionMultiplier to drive approximate comparisons at each element.
     * @param lhs The left matrix to be compared.
     * @param rhs The right matrix to be compared.
     * @param precisionMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(double[][] lhs, double[][] rhs,
    int precisionMultiplier) {
        // FALSE if bounds differ
        if((lhs.length != rhs.length) || (lhs[0].length != rhs[0].length))
            return false;
        for (int x = 0; x < lhs.length; x++)
            for (int y = 0; y < lhs[0].length; y++)
                if (!approx_equal(lhs[x][y], rhs[x][y], precisionMultiplier))
                    return false;
        
        return true;
    }
    
    
    /** Checks if the double values are approximately equal and displays an error
     * message if they are not.
     * @param lhs The left value to be compared.
     * @param rhs The right value to be compared.
     * @param errMsg The error message to be displayed.
     */
    static public void verify_approx_equal(double lhs, double rhs, String errMsg ) {
        if (!approx_equal(lhs, rhs ))
            Error.err(errMsg + '\n'+ lhs + " versus "+ rhs + "-->fatal_error");
    }
    
    /** If the given source value is lower than the lower bound, the source value is
     * changed to the lower bound value. If the source value is above the higher bound,
     * the higher bound is substituted.
     * @param source The value to be clamped.
     * @param lowBound The lower bound.
     * @param highBound The higher bound.
     * @param additionalErrMsg An additional error message to be displayed if clamping occurs.
     */
    static public void clamp_to_range(DoubleRef source, double lowBound, double highBound, String additionalErrMsg) {
        clamp_to_range(source, lowBound, highBound, additionalErrMsg, 1);
    }
    
    /** If the given source value is lower than the lower bound, the source value is
     * changed to the lower bound value. If the source value is above the higher bound,
     * the higher bound is substituted. The precision multiplier is to determine to
     * what precision checks for significant difference are conducted.
     * @param source The value to be clamped.
     * @param lowBound The lower bound.
     * @param highBound The higher bound.
     * @param additionalErrMsg An additional error message to be displayed if clamping occurs.
     * @param precMultiplier The precision multiplier for real values that are close to the a bound.
     */
    static public void clamp_to_range(DoubleRef source, double lowBound, double highBound, String additionalErrMsg, int precMultiplier) {
        if (lowBound > highBound) {
            Error.err(additionalErrMsg + '\n'+ "clamp_to_range(Real): Lower bound of allowed range ("+
            lowBound + ") can not be greater than the upper bound of the range ("+
            highBound + ")-->fatal_error");
        }
        if (precMultiplier < 0) {
            Error.err(additionalErrMsg + '\n'+ "clamp_to_range(Real): precision multiplier ("+
            precMultiplier + ") must be non-negative-->fatal_error");
        }
        clamp_above(source, lowBound, additionalErrMsg, precMultiplier);
        clamp_below(source, highBound, additionalErrMsg, precMultiplier);
    }
    
    /** If the given source value is higher than the higher bound, the source value is
     * changed to the higher bound value.
     * @param source The value to be clamped.
     * @param highBound The higher bound.
     * @param additionalErrMsg An additional error message to be displayed if clamping occurs.
     */
    static public void clamp_below(DoubleRef source, double highBound, String additionalErrMsg) {
        clamp_below(source, highBound, additionalErrMsg, 1);
    }
    
    /** If the given source value is higher than the higher bound, the source value is
     * changed to the higher bound value.
     * @param source The value to be clamped.
     * @param highBound The higher bound.
     * @param additionalErrMsg An additional error message to be displayed if clamping occurs.
     * @param precMultiplier The precision multiplier for real values that are close to the higher bound.
     */
    static public void clamp_below(DoubleRef source, double highBound, String additionalErrMsg, int precMultiplier) {
        if (precMultiplier < 0) {
            Error.err(additionalErrMsg + '\n'+ "clamp_below(double): precision multiplier ("+
            precMultiplier + ") must be non-negative-->fatal_error");
        }
        if (source.value <= highBound) return;
        if (source.value > highBound + clampingEpsilon * precMultiplier) {
            Error.err(additionalErrMsg + '\n'+ "clamp_below(double): variable to be clamped ("+
            source + ") exceeds maximum value allowed ("+ highBound + ") by more than allowed ("+
            clampingEpsilon + ")-->fatal_error");
        }
        source.value = highBound;
    }
    
    /** Checks if the left value is significantly less than the right value. If not an
     * error message is displayed.
     * @param lhs The left value.
     * @param rhs The right value.
     * @param additionalErrMsg An additional error message for display.
     */
    static public void verify_strictly_greater(double lhs, double rhs, String additionalErrMsg) {
        if (lhs <= rhs + realEpsilon) {
            Error.err(additionalErrMsg + '\n'+ "verify_strictly_greater(Real): variable ("+
            lhs + ") is not at least "+ realEpsilon + " greater than its lower bound ("+ rhs + ")-->fatal_error");
        }
    }
    
    /** Compares two float values for equality.
     * @param lhs The left value to be compared.
     * @param rhs The right value to be compared.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(float lhs, float rhs) {
        return approx_equal(lhs,rhs,1);
    }
    
    /** Compares two float values for equality. Uses the given precision multiplier to
     * determine if there is significant difference.
     * @param lhs The left value compared.
     * @param rhs The right value compared.
     * @param precMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if approximately equal, FALSE if significantly different.
     */
    static public boolean approx_equal(float lhs, float rhs, int precMultiplier) {
        if (Globals.DBG) MLJ.ASSERT(precMultiplier >= 0,"MLJ::approx_equal: precMultiplier < 0");
        return (Math.abs(lhs - rhs) <= clampingEpsilon * precMultiplier * Math.max(1, Math.min(Math.abs(lhs), Math.abs(rhs))));
    }
    
    /** Returns the binary log of the given value.
     * @param number The value for which a log is requested.
     * @return The binary log value.
     */
    static public double log_bin(double number) {
        return (Math.log(number) / Math.log(2.0));
    }
    
    /** Displays an error message stating an unexpected condition was reached.
     */    
    static public void Abort() {
        Error.err( "MLC++ internal error: unexpected condition in file ");
    }
    
    /** Checks if the left value is greater than the right value.
     * @param lhs The left value compared.
     * @param rhs The right value compared.
     * @return TRUE if the left value is greater than the right value, FALSE otherwise.
     */
    static public boolean approx_greater(double lhs, double rhs) {
        return approx_greater(lhs,rhs,1);
    }
    
    /** Checks if the left value is greater than the right value. Uses a precision
     * multiplier for determining significant difference.
     * @param lhs The left value compared.
     * @param rhs The right value compared.
     * @param precMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if the left value is greater than the right value, FALSE otherwise.
     */
    static public boolean approx_greater(double lhs, double rhs, int precMultiplier) {
        return (approx_equal(lhs, rhs, precMultiplier) ? false: (lhs > rhs));
    }
    
    /** Checks if the left value is less than the right value.
     * @param lhs The left value compared.
     * @param rhs The right value compared.
     * @return TRUE if the left value is less than the right value, FALSE otherwise.
     */
    static public boolean approx_less(double lhs, double rhs) {
        return (approx_less(lhs, rhs, 1));
    }
    
    /** Checks if the left value is less than the right value. Uses a precision
     * multiplier for determining significant difference.
     * @param lhs The left value compared.
     * @param rhs The right value compared.
     * @param precMultiplier The precision multiplier for determining if a value is signifacntly different.
     * @return TRUE if the left value is less than the right value, FALSE otherwise.
     */
    static public boolean approx_less(double lhs,double rhs, int precMultiplier) {
        return (approx_equal(lhs, rhs, precMultiplier) ? false : (lhs < rhs));
    }
    
    /** If the boolean value given is FALSE, the statement given is displayed as part of
     * an assertion error message.
     * @param theItem FALSE if message displayed, TRUE otherwise.
     * @param statement The statement to be displayed.
     */
    static public void ASSERT(boolean theItem, String statement) {
        if (!(theItem))
            Error.err( "ASSERT failed on "+statement);
    }
    
    static Object read_rep(Object rep) {
        if (rep == null)
            Error.fatalErr( "RefCount.h:HANDLE_CLASS"+ ":read_rep: Attempt to dereference NULL Pointer ");
        return rep;
    }
    
    /** Converts a numerical value to String form.
     * @param value The value to be converted.
     * @param total_length The total length of the resulting String.
     * @param mantissa The length of the mantissa.
     * @return A String representation of this numerical value with the required mantissa and
     * total length.
     */
    static public String numberToString(double value, int total_length, int mantissa) {
        String returned_value = Double.toString(value);
        int returned_length = returned_value.length();
        int period_position = returned_value.indexOf( '.');
        if (returned_length - period_position > mantissa+1)
            returned_value = returned_value.substring(0,period_position + 2);
        else if (returned_length - period_position == mantissa)
            returned_value = returned_value + "0";
        else if (returned_length - period_position == mantissa-1)
            returned_value = returned_value + "00";
        for(; returned_value.length() < total_length;)
            returned_value = " "+ returned_value;
        return returned_value;
    }
    
     /** Converts a numerical value to String form.
      * @param value The value to be converted.
      * @param mantissa The length of the mantissa.
      * @return A String representation of this numerical value with the required mantissa and
      * total length.
      */
    static public String numberToString(double value, int mantissa) {
        String returned_value = Double.toString(value);
        int returned_length = returned_value.length();
        int period_position = returned_value.indexOf( '.');
        if (returned_length - period_position > mantissa+1)
            returned_value = returned_value.substring(0,period_position + 2);
        else if (returned_length - period_position == mantissa)
            returned_value = returned_value + "0";
        else if (returned_length - period_position == mantissa-1)
            returned_value = returned_value + "00";
        return returned_value;
    }
    
    
    /** Substitutes characters with their protected versions.
     * @param stringToProtect The String containing characters to be protected.
     * @param protChars The characters to be protected.
     * @return The String with characteres protected by '\\' characters.
     */
    static public String protect(String stringToProtect, String protChars) {
        // illegal to NOT include a backslash in the list of protected
        // characters.
        if(protChars.indexOf( "\\") == -1)
            Error.fatalErr( "MLJ::protect: set of protected characters ("
            + protChars + ") must include a backslash");
        
        // illegal to include the same character twice in the list
        for(int i=0; i<protChars.length()-1; i++) {
            if(protChars.indexOf(protChars.substring(i, i+1), i+1) > -1)
                Error.fatalErr( "MLJ::protect: the set of protected characters ("
                + protChars + ") contains duplicate characters");
        }
        
        char[] c = new char[1];
        String retval = new String();
        for(int i=0; i<stringToProtect.length(); i++) {
            c[0] = stringToProtect.charAt(i);
            for(int j=0; j<protChars.length(); j++) {
                if(c[0] == protChars.charAt(j))
                    retval.concat( "\\");
            }
            retval.concat(new String(c));
        }
        return retval;
    }
    
    /** Squares the given real number.
     * @param x The number to be squared.
     * @return The square of the given number.
     */
    public static double square_real(double x) {return x*x;}
    
    /** Checks if two Vectors of DoubleRefs are approximately equivalent in values.
     * @param a The first Vector compared.
     * @param b The second Vector compared.
     * @return TRUE if the vectors are approximately equal, FALSE if not.
     */
    public static boolean approx_equal(Vector a, Vector b){
        if (a.size() != b.size()) return false;
        
        ListIterator via = a.listIterator();
        ListIterator vib = b.listIterator();
        
        while(via.hasNext()){
            if(approx_equal(((DoubleRef) via.next()).value, ((DoubleRef) vib.next()).value)) return false;
        }
        return true;
    }
    
    /** Rounds the given value by the number of digits.
     * @param x The value to be rounded.
     * @param digits The number of digits to be rounded off.
     * @return The rounded value.
     */
    public static double Mround(double x, int digits) {
        double scale = Math.pow(10, digits);
        int  sign  = (x >= 0)?1: -1;
        return Math.floor(Math.abs(x) * scale + 0.5) / scale * sign;
    }
}
