package arithmetic.shared;
/** This is an double wrapper, made because the JDK1.2.2 Double wrapper
 * doesn't allow changes to be made to the integer value.
 * @author James Louis.
 */
public class DoubleRef{
    
    /** The value of this wrapper.
     */
    public double value;
    
    /** Constructor. Sets value to 0 by default.
     */
    public DoubleRef(){ value = 0;}
    
    /** Constructor.
     * @param v The value to be stored in this wrapper.
     */
    public DoubleRef(double v){value = v;}
    
    /** Converts this wrapper to a String.
     * @return A String representation of the value stored in this wrapper.
     */
    public String toString(){return Double.toString(value);}
    
    /** Compares this DoubleRef object to the specified DoubleRef object.
     * @return True if the values are equal, false otherwise.
     * @param rhs The DoubleRef object this object is being compared to.
     */
    public boolean equals(Object rhs){return ((DoubleRef)rhs).value == value;}
    
}
