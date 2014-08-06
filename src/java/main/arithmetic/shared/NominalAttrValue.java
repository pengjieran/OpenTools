package arithmetic.shared;
/** NominalAttrValue is a class which stores the vlaues of nominal attributes. The
 * values can only be accessed through AttrInfo functions.
 */
public class NominalAttrValue extends AttrValue {
    /** Creates a deep copy clone of this object.
     * @throws CloneNotSupportedException if the AttrValue cloning process encounters an Exception.
     * @return A deep copy of this NominalAttrValue.
     */
    public Object clone() throws CloneNotSupportedException {
        Object navClone = super.clone();
        return navClone;
    }
    
    /**
     * @param r  */
    private NominalAttrValue(RealAttrValue r) {
        Error.err("NominalAttrValue::NominalAttrValue"
        +"(RealAttrValue):Cannot construct Nominal from Real"
        + "-->fatal_error ");
    }
    
    /** Constructor. The value will be set to unknown.
     */    
    public NominalAttrValue() {
        intVal = Globals.UNKNOWN_NOMINAL_VAL; //host_to_net(UNKNOWN_NOMINAL_VAL+NOMINAL_OFFSET);
        type = AttrInfo.nominal;
    }
    
    /** Copy constructor. The AttrValue passed in is checked for compatibility with this
     * NominalAttrValue.
     * @param src The AttrValue to be copied.
     */
    public NominalAttrValue(AttrValue src) {
        if( src.type != AttrInfo.nominal)
            Error.err("NominalAttrValue::NominalAttrValue"
            +"(AttrValue): cannot construct a nominal from a "
            + AttrInfo.attr_type_to_string(src.type) + "--> fatal_error");
        type = AttrInfo.nominal;
        intVal = src.intVal;
        realVal = src.realVal;
    }
    
    /** Copy constructor.
     * @param src The NominalAttrValue to be copied.
     */
    public NominalAttrValue(NominalAttrValue src) {
        //ASSERT(src.type == nominal);
        gets(src);
    }
    
    /** Assigns a RealAttrValue to this NominalAttrValue. Displays an error message and
     * does nothing.
     * @param r The RealAttrValue to be assigned to this NominalAttrValue.
     */
    public void gets(RealAttrValue r) {
        Error.err("Nominal::gets(RealAttrValue): cannot "
        +"assign a Real to a Nominal -->fatal_error");
    }
    
    /** Assigns a AttrValue to this NominalAttrValue. If the AttrValue is not compatible
     * with this NominalAttrValue then an error message is displayed and nothing else
     * is done.
     * @param src The AttrValue to be assigned to this NominalAttrValue.
     */
    public void gets(AttrValue src) {
        if(src != this) {
            if(src.type != AttrInfo.nominal)
                Error.err("NominalAttrValue::gets: cannot "
                +"assign a "+AttrInfo.attr_type_to_string(src.type)
                +"(AttrInfo attrType) to a "
                +"NominalAttrValue -->fatal_error");
            gets(src);
        }
        
    }
    
    /** Checks if this NominalAttrValue is equivalent to the given NominalAttrValue.
     * @param source The NominalAttrValue this NominalAttrValue is compared to.
     * @return TRUE if the values are the same, FALSE otherwise.
     */
    public boolean equals(NominalAttrValue source) {
        //ASSERT(source.type == nominal);
        //ASSERT(type===nominal);
        return intVal == source.intVal;
    }
    
    /** Checks if this NominalAttrValue is equivalent to the given AttrValue.
     * @param a The AttrValue this NominalAttrValue is compared to.
     * @return TRUE if the values are the same, FALSE otherwise.
     */
    public boolean equals(AttrValue a) {
        Error.err("NominalAttrValue::equals: cannot compare"
        + " against base class -->fatal_error ");
        return false;
    }
}
