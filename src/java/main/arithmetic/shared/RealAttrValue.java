package arithmetic.shared;
/** RealAttrValue is a class which stores the values of attributes. The values can
 * only be accessed through AttrInfo functions.
 *
 */
public class RealAttrValue extends AttrValue {
    private RealAttrValue(NominalAttrValue r) {
        Error.err("RealAttrValue::RealAttrValue("
        +"(NominalAttrValue):cannot construct a RealAttrValue with a "
        +"NominalAttrValue -->fatal_error");
    }
    
    /** Constructor. Automatically sets value to 0.
     */
    public RealAttrValue() {
        realVal = 0; //(StoredReal)UNKNOWN_STORED_REAL_VAL;
        type = AttrInfo.real;
    }
    
    /** Copy constructor. Checks for compatibility between the given AttrValue and
     * RealAttrValue.
     * @param src The AttrValue to be copied.
     */
    public RealAttrValue(AttrValue src) {
        if(src.type != AttrInfo.real)
            Error.err("RealAttrValue::RealAttrValue(AttrValue)"
            +":cannot construct a RealAttrValue from a "
            +AttrInfo.attr_type_to_string(src.type)+" AttrValue-->fatal_error");
        type = AttrInfo.real;
        intVal = src.intVal;
        realVal = src.realVal;
    }
    
    /** Copy constructor.
     * @param src The RealAttrValue to be copied.
     */
    public RealAttrValue(RealAttrValue src) {
        //ASSERT(src.type = real);
        type = AttrInfo.real;
        intVal = src.intVal;
        realVal = src.realVal;
    }
    
    /** Assigns a NominalValue to this RealAttrValue. Displays an error message and does
     * nothing else.
     * @param r The NominalAttrInfo to be assigned.
     */
    public void gets(NominalAttrValue r) {
        Error.err("RealAttrValue::gets(NominalAttrValue):"
        +" cannot assign a NominalAttrValue to a RealAttrValue-->fatal_error");
    }
    
    /** Assigns a AttrValue to this RealAttrValue. If the given AttrValue is not
     * compatible an error message is displayed and nothing else is done.
     * @param src the AttrValue to be copied.
     */
    public void gets(AttrValue src) {
        if(this != src) {
            if(src.type != AttrInfo.real)
                Error.err("RealAttrValue::gets: cannot assign "
                +"a "+AttrInfo.attr_type_to_string(src.type) +" AttrValue to "
                +"a RealAttrValue-->fatal_error");
            type = AttrInfo.real;
            intVal = src.intVal;
            realVal = src.realVal;
        }
    }
    
    /** Checks if the given RealAttrValue is equivalent to this RealAttrValue.
     * @param source The RealAttrValue to be compared to.
     * @return TRUE if the given RealAttrValue is equivalent to this RealAttrValue, FALSE
     * otherwise.
     */
    public boolean equals(RealAttrValue source) {
        //ASSERT(source.type == real);
        //ASSERT(type == real);
        return realVal == source.realVal;
    }
    
    /** Checks if the given AttrValue is equivalent to this RealAttrValue.
     * @param a The AttrValue to be compared to.
     * @return TRUE if the given AttrValue is equivalent to this RealAttrValue, FALSE
     * otherwise.
     */
    public boolean equals(AttrValue a) {
        Error.err("ReallAttrValue::equals: cannot compare"
        + " against base class -->fatal_error ");
        return false;
    }
}
