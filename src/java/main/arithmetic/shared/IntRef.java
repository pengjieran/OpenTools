package arithmetic.shared;
/** This is an integer wrapper, made because the JDK1.2.2 Integer wrapper
 * doesn't allow changes to be made to the integer value.
 * @author James Louis.
 */
public class IntRef{

    /** The value of this wrapper.
     */
	public int value;

        /** Constructor. Sets value to 0 by default.
         */
	public IntRef(){ value = 0;}

        /** Constructor.
         * @param v The value to be stored in this wrapper.
         */
	public IntRef(int v){value = v;}

        /** Converts this wrapper to a String.
         * @return A String representation of the value stored in this wrapper.
         */
	public String toString(){return Integer.toString(value);}

        /** Compares this IntRef object to the specified IntRef object.
         * @return True if the values are equal, false otherwise.
         * @param rhs The Object to be compared to.
         */
	public boolean equals(Object rhs){return ((IntRef)rhs).value == value;}

}