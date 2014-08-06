package arithmetic.shared;

/** The AttrValue class contains the possible values for attributes.
 * @author James Louis 2/25/2001   Ported to Java.
 * @author Richard Long    5/17/94 Added AttrValue_, NominalAttrValue_,
 * and RealAttrValue_ classes.
 */
public class AttrValue implements Cloneable {
    
    //VALUE UNION
    /** Value for nominal and other discrete values.*/
    public int intVal;
    /** Value for real and other continuous values.*/
    public double realVal;
    
    //ENUMS
     /** Value for Unknown attribute type.*/
    public static final byte unknown = 0;
    /** Value for Real attribute type.*/
    public static final byte real = 1;
    /** Value for BoundedReal attribute type.*/
    public static final byte boundedReal = 2;
    /** Value for Nominal attribute type.*/
    public static final byte nominal = 3;
    /** Value for LinearNominal attribute type.*/
    public static final byte linearNominal = 4;
    /** Value for TreeStructured attribute type.*/
    public static final byte treeStructured = 5;
    /** Value for InternalDisjunction attribute type.*/
    public static final byte internalDisjunction = 6;
    /** Value for UserReal attribute type.*/
    public static final byte userReal = 7;
    /** Value for UserNominal attribute type.*/
    public static final byte userNominal = 8;
    /** Value for UserLinearNominal attribute type.*/
    public static final byte userLinearNominal = 9;
    /** Value for UserTreeStructure attribute type.*/
    public static final byte userTreeStructured = 10;
    /** Value for UserInternalDisjunction attribute type.*/
    public static final byte userInternalDisjunction = 11;
    //END ENUM
    
    /** The type of attribute this AttrValue is.*/
    public byte type;
    
    /** Clones this AttrValue.
     * @return The Clone of this AttrValue.
     * @throws CloneNotSupportedException This exception is never thrown by this
     * class and is only present because Java's Object class requires
     * it.
     */
    public Object clone() throws CloneNotSupportedException {
        AttrValue avClone = new AttrValue();
        avClone.intVal = intVal;
        avClone.realVal = realVal;
        avClone.type = type;
        
        return avClone;
    }
    
    /** Constructor.
     */
    public AttrValue() {
        type = AttrInfo.unknown;
    }
    
    /** Copy constructor.
     * @param src	The AttrValue to be copied.
     */
    public AttrValue(AttrValue src) {
        intVal = src.intVal;
        realVal = src.realVal;
        type = src.type;
    }
    
    /** Constructor.
     * @param aType	The type of AttrValue this object will be.
     */
    public AttrValue(byte aType) {
        type = aType;
    }
    
    /** Sets this AttrValue to the same values as the specified AttrValue.
     * @param src The AttrValue containing values to be copied.
     */
    public void gets(AttrValue src) {
        if (src != this) {
            type = src.type;
            intVal = src.intVal;
            realVal = src.realVal;
        }
    }
    
    /** Compares this AttrValue to the specified AttrValue. Base class AttrValues
     * cannot be compared. Displays an error message if attempted.
     * @return TRUE if equal, FALSE otherwise. Always FALSE for the AttrValue base
     * class.
     * @param a	The AttrValue to be compared to.
     */
    public boolean equals(AttrValue a) {
        Error.err("AttrValue::equals: can not compare base"
        + " class -->fatal_error");
        return false;
    }
    
    /** Converts the specified AttrValue type to a String.
     * @return The String representing this AttrValue.
     * @param attrType The specific value to be converted to a String.
     */
    public String attr_type_to_string(byte attrType) {
        switch (attrType) {
            case  unknown : return"unknown";
            case  real: return"real";
            case  nominal: return"nominal";
            case  linearNominal: return"linearNominal";
            case  treeStructured: return"treeStructured";
            case  internalDisjunction: return"internalDisjunction";
            case  userReal: return"userReal";
            case  userNominal: return"userNominal";
            case  userLinearNominal: return"userLinearNominal";
            case  userTreeStructured: return"userTreeStructured";
            case  userInternalDisjunction: return"userInternalDisjunction";
            default:
                Error.fatalErr("Attribute.c::attr_type_to_string: Invalid AttrType value " +
                (int) attrType);
                return"Attribute.c:attr_type bug";
        }
    }
    
    /** Converts the specified AttrValue to a String.
     * @return A String representing the value stored in this AttrValue.
     */
    public String toString() {
        if (type == nominal) return "[intVal = "+ intVal +"]";
        else return "[realVal = "+ realVal +"]";
    }
    
    /** Copies information from the supplied AttrValue to this AttrValue.
     * @param other The AttrValue to be copied.
     */    
    public void copy(AttrValue other) {
        intVal = other.intVal;
        realVal = other.realVal;
        type = other.type;
    }
    
}
